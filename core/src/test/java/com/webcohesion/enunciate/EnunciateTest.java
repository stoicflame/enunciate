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
    List<String> moduleCallOrder = new ArrayList<String>();
    modules.put("a", new TestModule("a", moduleCallOrder));
    modules.put("b", new TestModule("b", moduleCallOrder));
    modules.put("c", new TestModule("c", moduleCallOrder));
    modules.put("d", new TestModule("d", moduleCallOrder, "a"));
    modules.put("e", new TestModule("e", moduleCallOrder, "b", "c"));
    modules.put("f", new TestModule("f", moduleCallOrder, "d", "e"));

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

    modules.put("a", new TestModule("a", moduleCallOrder, "f")); //replace 'a' with a circular dependency.
    try {
      enunciate.loadModuleGraph();
      fail();
    }
    catch (IllegalStateException e) {
      //fall through...
    }
  }

  @Test
  public void testCallOrder() throws Exception {
    final Map<String, TestModule> modules = new HashMap<String, TestModule>();
    List<String> moduleCallOrder = Collections.synchronizedList(new ArrayList<String>());
    modules.put("a", new TestModule("a", moduleCallOrder));
    modules.put("b", new TestModule("b", moduleCallOrder));
    modules.put("c", new TestModule("c", moduleCallOrder));
    modules.put("d", new TestModule("d", moduleCallOrder, "a"));
    modules.put("e", new TestModule("e", moduleCallOrder, "b", "c"));
    modules.put("f", new TestModule("f", moduleCallOrder, "d", "e"));

    Enunciate enunciate = new Enunciate() {
      @Override
      protected Map<String, ? extends EnunciateModule> getActiveModules() {
        return modules;
      }
    };

    enunciate.composeEngine().toBlocking().single();
    assertEquals(6, moduleCallOrder.size());

    List<String> firstThree = moduleCallOrder.subList(0, 3);
    //the first three need to have a, b, c
    assertTrue(firstThree.contains("a"));
    assertTrue(firstThree.contains("b"));
    assertTrue(firstThree.contains("c"));

    //the next two have to be d or e
    List<String> nextTwo = moduleCallOrder.subList(3, 5);
    assertTrue(nextTwo.contains("d"));
    assertTrue(nextTwo.contains("e"));

    //the last one has to be f
    assertEquals("f", moduleCallOrder.get(moduleCallOrder.size() - 1));
  }

  private class TestModule implements EnunciateModule, DependingModuleAware {

    private final String name;
    private final Set<String> moduleDependencies;
    private Set<String> dependingModules;
    private final List<String> moduleCallOrder;

    private TestModule(String name, List<String> moduleCallOrder, String... moduleDependencies) {
      this.name = name;
      this.moduleCallOrder = moduleCallOrder;
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

    @Override
    public void call(EnunciateOutput output) {
      this.moduleCallOrder.add(getName());
    }
  }
}
