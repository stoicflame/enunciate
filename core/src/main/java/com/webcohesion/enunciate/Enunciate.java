package com.webcohesion.enunciate;

import com.webcohesion.enunciate.io.EmptyEnunciateOutputSource;
import com.webcohesion.enunciate.io.EnunciateModuleOperator;
import com.webcohesion.enunciate.io.EnunciateModuleZipper;
import com.webcohesion.enunciate.module.DependingModuleAware;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.*;


/**
 * @author Ryan Heaton
 */
public class Enunciate {

  protected Map<String, ? extends EnunciateModule> getActiveModules() {
    return new TreeMap<String, EnunciateModule>();
  }

  protected DirectedGraph<String, DefaultEdge> loadModuleGraph() {
    Map<String, ? extends EnunciateModule> modules = getActiveModules();
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

  protected Observable<EnunciateOutput> composeEngine() {
    Observable<EnunciateOutput> source = createEmptyOutput().subscribeOn(Schedulers.io());

    Map<String, ? extends EnunciateModule> modules = getActiveModules();
    Map<String, Observable<EnunciateOutput>> moduleObservables = new TreeMap<String, Observable<EnunciateOutput>>();
    DirectedGraph<String, DefaultEdge> graph = loadModuleGraph();
    TopologicalOrderIterator<String, DefaultEdge> graphIt = new TopologicalOrderIterator<String, DefaultEdge>(graph);
    List<Observable<EnunciateOutput>> leafModules = new ArrayList<Observable<EnunciateOutput>>();
    while (graphIt.hasNext()) {
      String module = graphIt.next();
      Observable<EnunciateOutput> moduleObservable;

      Set<DefaultEdge> dependencies = graph.incomingEdgesOf(module);
      if (dependencies == null || dependencies.isEmpty()) {
        //no dependencies; plug in directly to the source.
        moduleObservable = source.lift(new EnunciateModuleOperator(modules.get(module)));
      }
      else {
        List<Observable<EnunciateOutput>> observableDependencies = new ArrayList<Observable<EnunciateOutput>>(dependencies.size());
        for (DefaultEdge dependency : dependencies) {
          EnunciateModule dep = modules.get(graph.getEdgeSource(dependency));
          Observable<EnunciateOutput> observableDependency = moduleObservables.get(dep.getName());
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

  protected Observable<EnunciateOutput> createEmptyOutput() {
    return Observable.create(new EmptyEnunciateOutputSource());
  }

}
