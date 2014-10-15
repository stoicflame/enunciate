package com.webcohesion.enunciate;

import com.webcohesion.enunciate.io.EnunciateModuleOperator;
import com.webcohesion.enunciate.io.EnunciateModuleZipper;
import com.webcohesion.enunciate.module.DependingModuleAware;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author Ryan Heaton
 */
public class Enunciate implements Runnable {

  private Set<File> sourceFiles = Collections.emptySet();
  private List<EnunciateModule> modules;
  private final Set<String> includes = new TreeSet<String>();
  private final Set<String> excludes = new TreeSet<String>();
  private Collection<URL> classpath = new ArrayList<URL>();
  private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private EnunciateLogger logger = new EnunciateConsoleLogger();

  public Enunciate() {
  }

  public Enunciate setModules(List<EnunciateModule> modules) {
    this.modules = modules;
    return this;
  }

  public Enunciate addModule(EnunciateModule module) {
    if (this.modules == null) {
      this.modules = new ArrayList<EnunciateModule>();
    }

    this.modules.add(module);
    return this;
  }

  public Enunciate loadDiscoveredModules() {
    ServiceLoader<EnunciateModule> moduleLoader = ServiceLoader.load(EnunciateModule.class);
    for (EnunciateModule module : moduleLoader) {
      addModule(module);
    }
    return this;
  }

  public Enunciate setSourceFiles(Set<File> sourceFiles) {
    this.sourceFiles = sourceFiles;
    return this;
  }

  public Enunciate addSourceFile(File source) {
    if (this.sourceFiles == null) {
      this.sourceFiles = new HashSet<File>();
    }
    this.sourceFiles.add(source);
    return this;
  }

  public Enunciate addSourceDir(File dir) {
    visitFiles(dir, JAVA_FILTER, new FileVisitor() {
      @Override
      public void visit(File file) {
        addSourceFile(file);
      }
    });

    return this;
  }

  public Enunciate setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }

  public Set<String> getIncludes() {
    return includes;
  }

  public Enunciate addInclude(String include) {
    this.includes.add(include);
    return this;
  }

  public Set<String> getExcludes() {
    return excludes;
  }

  public Enunciate addExclude(String exclude) {
    this.excludes.add(exclude);
    return this;
  }

  public Collection<URL> getClasspath() {
    return classpath;
  }

  public Enunciate setClasspath(Collection<URL> classpath) {
    this.classpath = classpath;
    return this;
  }

  public Enunciate addClasspathEntry(URL entry) {
    this.classpath.add(entry);
    return this;
  }

  public Enunciate setExtraThreadCount(int extraThreadCount) {
    if (extraThreadCount < 1) {
      this.executorService = null;
    }
    else {
      this.executorService = Executors.newFixedThreadPool(extraThreadCount);
    }

    return this;
  }

  public EnunciateLogger getLogger() {
    return logger;
  }

  public Enunciate setLogger(EnunciateLogger logger) {
    this.logger = logger;
    return this;
  }

  @Override
  public void run() {
    if (this.modules != null && !this.modules.isEmpty()) {
      //scan for any included types.
      Set<String> includedTypes = findIncludedTypes();

      //construct a context.
      EnunciateContext context = new EnunciateContext(this.logger, Collections.unmodifiableSet(this.sourceFiles), Collections.unmodifiableSet(includedTypes));

      //initialize the modules.
      for (EnunciateModule module : this.modules) {
        module.init(context);
      }

      //compose the engine.
      Map<String, ? extends EnunciateModule> enabledModules = getEnabledModules();
      DirectedGraph<String, DefaultEdge> graph = buildModuleGraph(enabledModules);
      Observable<EnunciateContext> engine = composeEngine(context, enabledModules, graph);

      //invoke the engine.
      context = engine.toBlocking().single();

      //process the results...?
    }
    else {
      this.logger.warn("No Enunciate modules have been loaded. No work was done.");
    }
  }

  protected Set<String> findIncludedTypes() {
    ConfigurationBuilder reflectionSpec = new ConfigurationBuilder()
      .setUrls(this.classpath)
      .setScanners(new SubTypesScanner(false));

    for (String include : this.includes) {
      //todo: what if it's not a package?
      reflectionSpec = reflectionSpec.filterInputsBy(new FilterBuilder().includePackage(include));
    }

    for (String exclude : this.excludes) {
      //todo: what if it's not a package?
      reflectionSpec = reflectionSpec.filterInputsBy(new FilterBuilder().excludePackage(exclude));
    }

    if (this.executorService != null) {
      reflectionSpec = reflectionSpec.setExecutorService(this.executorService);
    }

    return new Reflections(reflectionSpec).getAllTypes();
  }

  protected void visitFiles(File dir, FileFilter filter, FileVisitor visitor) {
    File[] files = dir.listFiles(filter);
    if (files != null) {
      for (File file : files) {
        visitor.visit(file);
      }
    }

    File[] dirs = dir.listFiles(DIR_FILTER);
    if (dirs != null) {
      for (File subdir : dirs) {
        visitFiles(subdir, filter, visitor);
      }
    }
  }

  protected Map<String, ? extends EnunciateModule> getEnabledModules() {
    TreeMap<String, EnunciateModule> enabledModules = new TreeMap<String, EnunciateModule>();
    for (EnunciateModule module : this.modules) {
      if (module.isEnabled()) {
        enabledModules.put(module.getName(), module);
      }
    }
    return enabledModules;
  }

  protected DirectedGraph<String, DefaultEdge> buildModuleGraph(Map<String, ? extends EnunciateModule> modules) {
    DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    for (String moduleName : modules.keySet()) {
      graph.addVertex(moduleName);
    }

    for (EnunciateModule module : modules.values()) {
      Set<String> dependencies = module.getModuleDependencies();
      if (dependencies != null && !dependencies.isEmpty()) {
        for (String dependency : dependencies) {
          if (!modules.containsKey(dependency)) {
            throw new IllegalStateException(String.format("Module %s depends on a module (%s) that is disabled or not found.", module.getName(), dependency));
          }
          graph.addEdge(dependency, module.getName());
        }
      }
    }

    for (EnunciateModule module : modules.values()) {
      if (module instanceof DependingModuleAware) {
        Set<DefaultEdge> edges = graph.outgoingEdgesOf(module.getName());
        Set<String> dependingModules = new TreeSet<String>();
        for (DefaultEdge edge : edges) {
          dependingModules.add(graph.getEdgeTarget(edge));
        }
        ((DependingModuleAware)module).acknowledgeDependingModules(dependingModules);
      }
    }

    CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<String, DefaultEdge>(graph);
    Set<String> modulesInACycle = cycleDetector.findCycles();
    if (!modulesInACycle.isEmpty()) {
      StringBuilder errorMessage = new StringBuilder("Module cycle detected: ");
      java.util.Iterator<String> subcycle = cycleDetector.findCyclesContainingVertex(modulesInACycle.iterator().next()).iterator();
      while (subcycle.hasNext()) {
        String next = subcycle.next();
        errorMessage.append(next);
        if (subcycle.hasNext()) {
          errorMessage.append(" --> ");
        }
      }

      throw new IllegalStateException(errorMessage.toString());
    }

    return graph;
  }

  protected Observable<EnunciateContext> composeEngine(EnunciateContext context, Map<String, ? extends EnunciateModule> modules, DirectedGraph<String, DefaultEdge> graph) {
    Scheduler scheduler = this.executorService == null ? Schedulers.immediate() : Schedulers.from(this.executorService);
    Observable<EnunciateContext> source = Observable.just(context).subscribeOn(scheduler);

    Map<String, Observable<EnunciateContext>> moduleObservables = new TreeMap<String, Observable<EnunciateContext>>();
    TopologicalOrderIterator<String, DefaultEdge> graphIt = new TopologicalOrderIterator<String, DefaultEdge>(graph);
    List<Observable<EnunciateContext>> leafModules = new ArrayList<Observable<EnunciateContext>>();
    while (graphIt.hasNext()) {
      String module = graphIt.next();
      Observable<EnunciateContext> moduleObservable;

      Set<DefaultEdge> dependencies = graph.incomingEdgesOf(module);
      if (dependencies == null || dependencies.isEmpty()) {
        //no dependencies; plug in directly to the source.
        moduleObservable = source.lift(new EnunciateModuleOperator(modules.get(module)));
      }
      else {
        List<Observable<EnunciateContext>> observableDependencies = new ArrayList<Observable<EnunciateContext>>(dependencies.size());
        for (DefaultEdge dependency : dependencies) {
          EnunciateModule dep = modules.get(graph.getEdgeSource(dependency));
          Observable<EnunciateContext> observableDependency = moduleObservables.get(dep.getName());
          if (observableDependency == null) {
            throw new IllegalStateException(String.format("Observable for module %s depended on by %s hasn't been established.", dep.getName(), module));
          }
          observableDependencies.add(observableDependency);
        }

        //zip up all the dependencies.
        moduleObservable = Observable.zip(observableDependencies, new EnunciateModuleZipper(modules.get(module)));
      }

      moduleObservables.put(module, moduleObservable);

      if (graph.outgoingEdgesOf(module).isEmpty()) {
        //no dependencies on this module; we'll add it to the list of leaf modules.
        leafModules.add(moduleObservable);
      }
    }

    //zip up all the leaves and return that.
    return Observable.zip(leafModules, new EnunciateModuleZipper(null));
  }

  /**
   * A file filter for java files.
   */
  private static FileFilter JAVA_FILTER = new FileFilter() {
    public boolean accept(File file) {
      return file.getName().endsWith(".java");
    }
  };

  /**
   * A file filter for directories.
   */
  private static FileFilter DIR_FILTER = new FileFilter() {
    public boolean accept(File file) {
      return file.isDirectory();
    }
  };

  /**
   * File visitor interface used to visit files.
   */
  public static interface FileVisitor {

    void visit(File file);
  }

}
