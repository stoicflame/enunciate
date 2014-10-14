package com.webcohesion.enunciate;

import com.webcohesion.enunciate.module.DependingModuleAware;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

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
      Iterator<String> subcycle = cycleDetector.findCyclesContainingVertex(modulesInACycle.iterator().next()).iterator();
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

}
