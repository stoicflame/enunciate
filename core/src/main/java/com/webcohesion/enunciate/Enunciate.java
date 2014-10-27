package com.webcohesion.enunciate;

import com.webcohesion.enunciate.io.EnunciateModuleOperator;
import com.webcohesion.enunciate.io.EnunciateModuleZipper;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.DependingModuleAware;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.apache.commons.configuration.ConfigurationException;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author Ryan Heaton
 */
public class Enunciate implements Runnable {

  private Set<File> sourceFiles = Collections.emptySet();
  private List<EnunciateModule> modules;
  private final Set<String> includeClasses = new TreeSet<String>();
  private final Set<String> excludeClasses = new TreeSet<String>();
  private Collection<URL> buildClasspath = new ArrayList<URL>();
  private Collection<URL> apiClasspath = null;
  private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private EnunciateLogger logger = new EnunciateConsoleLogger();
  private final EnunciateConfiguration configuration = new EnunciateConfiguration();

  public Enunciate() {
  }

  public List<EnunciateModule> getModules() {
    return modules;
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

  public Set<String> getIncludeClasses() {
    return includeClasses;
  }

  public Enunciate addInclude(String include) {
    this.includeClasses.add(include);
    return this;
  }

  public Set<String> getExcludeClasses() {
    return excludeClasses;
  }

  public Enunciate addExclude(String exclude) {
    this.excludeClasses.add(exclude);
    return this;
  }

  public Collection<URL> getBuildClasspath() {
    return buildClasspath;
  }

  public Enunciate setBuildClasspath(Collection<URL> buildClasspath) {
    this.buildClasspath = buildClasspath;
    return this;
  }

  public Enunciate addBuildClasspathEntry(URL entry) {
    this.buildClasspath.add(entry);
    return this;
  }

  public Collection<URL> getApiClasspath() {
    return apiClasspath;
  }

  public Enunciate setApiClasspath(Collection<URL> apiClasspath) {
    this.apiClasspath = apiClasspath;
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

  public EnunciateConfiguration getConfiguration() {
    return configuration;
  }

  public Enunciate loadConfiguration(InputStream xml) {
    try {
      this.configuration.source.load(xml, "utf-8");
    }
    catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }

    //todo: apply any of the config into this class?

    return this;
  }

  public Enunciate loadConfiguration(URL url) {
    try {
      return loadConfiguration(url.openStream());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Enunciate loadConfiguration(File xml) {
    try {
      this.configuration.setBase(xml.getParentFile());
      loadConfiguration(xml.toURI().toURL());
      return this;
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run() {
    if (this.modules != null && !this.modules.isEmpty()) {
      //scan for any included types.
      Collection<URL> apiClasspath = this.apiClasspath == null ? this.buildClasspath : this.apiClasspath;
      Reflections reflections = loadApiReflections(apiClasspath);
      Set<String> scannedEntries = reflections.getStore().get(EnunciateReflectionsScanner.class.getSimpleName()).keySet();
      Set<String> includedTypes = new HashSet<String>();
      Set<String> scannedSourceFiles = new HashSet<String>();
      for (String entry : scannedEntries) {
        if (entry.endsWith(".java")) {
          scannedSourceFiles.add(entry);
        }
        else {
          includedTypes.add(entry);
        }
      }

      //gather all the java source files.
      List<URL> sourceFiles = getSourceFileURLs();
      URLClassLoader apiClassLoader = new URLClassLoader(apiClasspath.toArray(new URL[apiClasspath.size()]));
      for (String javaFile : scannedSourceFiles) {
        URL resource = apiClassLoader.findResource(javaFile);
        if (resource == null) {
          throw new IllegalStateException(String.format("Unable to load java source file %s.", javaFile));
        }
        sourceFiles.add(resource);
      }

      //invoke the processor.
      //todo: don't compile the classes; only run the annotation processing engine.
      List<String> options = Arrays.asList( "-cp", writeClasspath(this.buildClasspath));
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      List<JavaFileObject> sources = new ArrayList<JavaFileObject>(sourceFiles.size());
      for (URL sourceFile : sourceFiles) {
        sources.add(new URLFileObject(sourceFile));
      }
      JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, options, null, sources);
      task.setProcessors(Arrays.asList(new EnunciateAnnotationProcessor(this, includedTypes)));
      if (!task.call()) {
        throw new RuntimeException("Enunciate processor failed.");
      }
    }
    else {
      this.logger.warn("No Enunciate modules have been loaded. No work was done.");
    }
  }

  protected String writeClasspath(Collection<URL> cp) {
    StringBuilder builder = new StringBuilder();
    Iterator<URL> it = cp.iterator();
    while (it.hasNext()) {
      URL next = it.next();
      builder.append(next.toString());
      if (it.hasNext()) {
        builder.append(File.pathSeparatorChar);
      }
    }
    return builder.toString();
  }

  protected List<URL> getSourceFileURLs() {
    List<URL> sourceFiles = new ArrayList<URL>(this.sourceFiles.size());
    for (File sourceFile : this.sourceFiles  ) {
      try {
        sourceFiles.add(sourceFile.toURI().toURL());
      }
      catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return sourceFiles;
  }

  protected Reflections loadApiReflections(Collection<URL> classpath) {
    ConfigurationBuilder reflectionSpec = new ConfigurationBuilder()
      .setUrls(classpath)
      .setScanners(new EnunciateReflectionsScanner(this.includeClasses, this.excludeClasses));

    if (this.executorService != null) {
      reflectionSpec = reflectionSpec.setExecutorService(this.executorService);
    }

    return new Reflections(reflectionSpec);
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
      List<DependencySpec> dependencies = module.getDependencies();
      if (dependencies != null && !dependencies.isEmpty()) {
        for (DependencySpec dependency : dependencies) {
          for (EnunciateModule other : modules.values()) {
            if (dependency.accept(other)) {
              graph.addEdge(other.getName(), module.getName());
            }
          }

          if (!dependency.isFulfilled()) {
            throw new IllegalStateException(String.format("Unfulfilled dependency %s of module %s.", dependency.toString(), module.getName()));
          }
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

  public static class URLFileObject extends SimpleJavaFileObject {

    private final URL source;

    public URLFileObject(URL source) {
      super(toURI(source), Kind.SOURCE);
      this.source = source;
    }

    static URI toURI(URL source) {
      try {
        return source.toURI();
      }
      catch (URISyntaxException e) {
        throw new RuntimeException();
      }
    }

    @Override
    public InputStream openInputStream() throws IOException {
      return this.source.openStream();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
      StringBuilder content = new StringBuilder();
      InputStream in = openInputStream();
      byte[] bytes = new byte[2 * 1024];
      int len;
      while ((len = in.read(bytes)) >= 0) {
        content.append(new String(bytes, 0, len, "utf-8"));
      }
      return content;
    }
  }

}
