package com.webcohesion.enunciate;

import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.DependingModuleAware;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;
import org.reflections.util.ClasspathHelper;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Ryan Heaton
 */
public class EnunciateTest {

  @Test
  public void testBuildModuleGraph() throws Exception {
    final Map<String, TestModule> myModules = new HashMap<String, TestModule>();
    List<String> moduleCallOrder = new ArrayList<String>();
    myModules.put("a", new TestModule("a", moduleCallOrder));
    myModules.put("b", new TestModule("b", moduleCallOrder));
    myModules.put("c", new TestModule("c", moduleCallOrder));
    myModules.put("d", new TestModule("d", moduleCallOrder, "a"));
    myModules.put("e", new TestModule("e", moduleCallOrder, "b", "c"));
    myModules.put("f", new TestModule("f", moduleCallOrder, "d", "e"));

    Enunciate enunciate = new Enunciate();
    DirectedGraph<String, DefaultEdge> graph = enunciate.buildModuleGraph(myModules);
    assertEquals(1, myModules.get("a").dependingModules.size());
    assertEquals("d", myModules.get("a").dependingModules.iterator().next());
    assertEquals(1, myModules.get("b").dependingModules.size());
    assertEquals("e", myModules.get("b").dependingModules.iterator().next());
    assertEquals(1, myModules.get("c").dependingModules.size());
    assertEquals("e", myModules.get("c").dependingModules.iterator().next());
    assertEquals(1, myModules.get("d").dependingModules.size());
    assertEquals("f", myModules.get("d").dependingModules.iterator().next());
    assertEquals(1, myModules.get("e").dependingModules.size());
    assertEquals("f", myModules.get("e").dependingModules.iterator().next());

    myModules.put("a", new TestModule("a", moduleCallOrder, "f")); //replace 'a' with a circular dependency.
    try {
      enunciate.buildModuleGraph(myModules);
      fail();
    }
    catch (IllegalStateException e) {
      //fall through...
    }
  }

  @Test
  public void testCallOrder() throws Exception {
    final Map<String, TestModule> myModules = new HashMap<String, TestModule>();
    List<String> moduleCallOrder = Collections.synchronizedList(new ArrayList<String>());
    myModules.put("a", new TestModule("a", moduleCallOrder));
    myModules.put("b", new TestModule("b", moduleCallOrder));
    myModules.put("c", new TestModule("c", moduleCallOrder));
    myModules.put("d", new TestModule("d", moduleCallOrder, "a"));
    myModules.put("e", new TestModule("e", moduleCallOrder, "b", "c"));
    myModules.put("f", new TestModule("f", moduleCallOrder, "d", "e"));

    Enunciate enunciate = new Enunciate();
    enunciate.composeEngine(new EnunciateContext(configuration, enunciate.getLogger(), null, null), myModules, enunciate.buildModuleGraph(myModules)).toBlocking().single();
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

  @Test
  public void testClasspathScanning() throws Exception {
    Enunciate enunciate = new Enunciate();
    enunciate.setClasspath(ClasspathHelper.forClassLoader());
    Set<String> classpathEntries = enunciate.findIncludedTypes();
    assertFalse(classpathEntries.isEmpty());
  }

  private class TestModule implements EnunciateModule, DependingModuleAware, DependencySpec {

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
    public boolean isEnabled() {
      return true;
    }

    @Override
    public List<DependencySpec> getDependencies() {
      return Arrays.asList((DependencySpec)this);
    }

    @Override
    public boolean accept(EnunciateModule module) {
      return this.moduleDependencies.contains(module.getName());
    }

    @Override
    public boolean isFulfilled() {
      return true;
    }

    @Override
    public void init(EnunciateContext context) {
      //no-op.
    }

    @Override
    public void call(EnunciateContext context) {
      this.moduleCallOrder.add(getName());
    }
  }
}
