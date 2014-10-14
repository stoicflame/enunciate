package com.webcohesion.enunciate;

import com.webcohesion.enunciate.module.DependingModuleAware;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ryan Heaton
 */
public class EnunciateTest {

  @Test
  public void testLoadModuleGraph() throws Exception {
    final Map<String, TestModule> modules = new HashMap<String, TestModule>();
    modules.put("a", new TestModule("a"));
    modules.put("b", new TestModule("b"));
    modules.put("c", new TestModule("c"));
    modules.put("d", new TestModule("d", "a"));
    modules.put("e", new TestModule("e", "b", "c"));
    modules.put("f", new TestModule("f", "d", "e"));

    Enunciate enunciate = new Enunciate() {
      @Override
      protected Map<String, ? extends EnunciateModule> getActiveModules() {
        return modules;
      }
    };

    DirectedGraph<String, DefaultEdge> graph = enunciate.loadModuleGraph();
    assertEquals(1, modules.get("a").dependingModules.size());
    assertEquals("d", modules.get("a").dependingModules.iterator().next());
    assertEquals(1, modules.get("b").dependingModules.size());
    assertEquals("e", modules.get("b").dependingModules.iterator().next());
    assertEquals(1, modules.get("c").dependingModules.size());
    assertEquals("e", modules.get("c").dependingModules.iterator().next());
    assertEquals(1, modules.get("d").dependingModules.size());
    assertEquals("f", modules.get("d").dependingModules.iterator().next());
    assertEquals(1, modules.get("e").dependingModules.size());
    assertEquals("f", modules.get("e").dependingModules.iterator().next());

    modules.put("a", new TestModule("a", "f")); //replace 'a' with a circular dependency.
    try {
      enunciate.loadModuleGraph();
      fail();
    }
    catch (IllegalStateException e) {
      //fall through...
    }
  }

  private class TestModule implements EnunciateModule, DependingModuleAware {

    private final String name;
    private final Set<String> moduleDependencies;
    private Set<String> dependingModules;

    private TestModule(String name, String... moduleDependencies) {
      this.name = name;
      this.moduleDependencies = new TreeSet<String>(Arrays.asList(moduleDependencies));
    }

    @Override
    public void acknowledgeDependingModules(Set<String> dependingModules) {
      this.dependingModules = dependingModules;
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public Set<String> getModuleDependencies() {
      return this.moduleDependencies;
    }
  }
}
