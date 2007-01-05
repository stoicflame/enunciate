package net.sf.enunciate.modules.rest;

import junit.framework.TestCase;
import net.sf.enunciate.rest.annotations.VerbType;

import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author Ryan Heaton
 */
public class TestRESTOperation extends TestCase {

  /**
   * tests the "ping" method as a REST operation.
   */
  public void testPing() throws Exception {
    RESTOperation operation = new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("ping"));
    assertEquals(VerbType.read, operation.getVerb());
    assertNull(operation.getProperNounType());
    assertNull(operation.getNounValueType());
    assertTrue(operation.getAdjectiveTypes().isEmpty());
    assertNotNull(operation.getSerializationContext());
    assertNull(operation.getResultType());
  }

  /**
   * tests the "badReturnType" method as a REST operation.
   */
  public void testBadReturnType() throws Exception {
    try {
      new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("badReturnType"));
      fail("shouldn't have accepted a return type that isn't an XML root element.");
    }
    catch (IllegalStateException e) {
      //fall through...
    }
  }

  /**
   * tests the "properNoun" method as a REST operation.
   */
  public void testProperNoun() throws Exception {
    RESTOperation operation = new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("properNoun", String.class));
    assertEquals(VerbType.read, operation.getVerb());
    assertNull(operation.getNounValueType());
    assertTrue(operation.getAdjectiveTypes().isEmpty());
    assertNotNull(operation.getSerializationContext());

    Class properNounType = operation.getProperNounType();
    assertNotNull(properNounType);
    assertEquals(String.class, properNounType);
    assertEquals(RootElementExample.class, operation.getResultType());
  }

  /**
   * tests the "badProperNoun" method as a REST operation.
   */
  public void testBadProperNoun() throws Exception {
    try {
      new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("badProperNoun", String[].class));
      fail("shouldn't have allowed a non-simple proper noun type.");
    }
    catch (IllegalStateException e) {

    }
  }

  /**
   * tests the "twoProperNouns" method as a REST operation.
   */
  public void testTwoProperNouns() throws Exception {
    try {
      new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("twoProperNouns", String.class, String.class));
      fail("shouldn't have allowed two proper nouns.");
    }
    catch (IllegalStateException e) {

    }
  }

  /**
   * tests the "nounValue" method as a REST operation.
   */
  public void testNounValue() throws Exception {
    RESTOperation operation = new RESTOperation(VerbType.update, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("nounValue", RootElementExample.class));
    assertEquals(VerbType.update, operation.getVerb());
    assertNull(operation.getProperNounType());
    assertTrue(operation.getAdjectiveTypes().isEmpty());
    assertNotNull(operation.getSerializationContext());

    Class nounValueType = operation.getNounValueType();
    assertNotNull(nounValueType);
    assertEquals(RootElementExample.class, nounValueType);
    assertEquals(RootElementExample.class, operation.getResultType());
  }

  /**
   * tests the "badNounValue" method as a REST operation.
   */
  public void testBadNounValue() throws Exception {
    try {
      new RESTOperation(VerbType.update, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("badNounValue", Object.class));
      fail("shouldn't have allowed a non-root xml noun value type.");
    }
    catch (IllegalStateException e) {

    }
  }

  /**
   * tests the "twoNounValues" method as a REST operation.
   */
  public void testTwoNounValues() throws Exception {
    try {
      new RESTOperation(VerbType.update, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("twoNounValues", RootElementExample.class, RootElementExample.class));
      fail("shouldn't have allowed two noun values.");
    }
    catch (IllegalStateException e) {

    }
  }

  /**
   * tests the "defaultAdjectives" method as a REST operation.
   */
  public void testDefaultAdjectives() throws Exception {
    RESTOperation operation = new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("defaultAdjectives", String.class, Double.TYPE));
    assertEquals(VerbType.read, operation.getVerb());
    assertNull(operation.getProperNounType());
    assertNull(operation.getNounValueType());
    assertNotNull(operation.getSerializationContext());
    assertNull(operation.getResultType());

    Map<String, Class> adjectiveTypes = operation.getAdjectiveTypes();
    assertEquals(2, adjectiveTypes.size());
    assertTrue(adjectiveTypes.containsKey("arg0"));
    assertEquals(String.class, adjectiveTypes.get("arg0"));
    assertTrue(adjectiveTypes.containsKey("arg1"));
    assertEquals(Double.TYPE, adjectiveTypes.get("arg1"));
  }

  /**
   * tests the "customAdjectives" method as a REST operation.
   */
  public void testCustomAdjectives() throws Exception {
    RESTOperation operation = new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("customAdjectives", String.class, Double.TYPE));
    assertEquals(VerbType.read, operation.getVerb());
    assertNull(operation.getProperNounType());
    assertNull(operation.getNounValueType());
    assertNotNull(operation.getSerializationContext());
    assertNull(operation.getResultType());

    Map<String, Class> adjectiveTypes = operation.getAdjectiveTypes();
    assertEquals(2, adjectiveTypes.size());
    assertTrue(adjectiveTypes.containsKey("howdy"));
    assertEquals(String.class, adjectiveTypes.get("howdy"));
    assertTrue(adjectiveTypes.containsKey("doody"));
    assertEquals(Double.TYPE, adjectiveTypes.get("doody"));
  }

  /**
   * tests the "adjectivesAsLists" method as a REST operation.
   */
  public void testAdjectivesAsLists() throws Exception {
    RESTOperation operation = new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("adjectivesAsLists", boolean[].class, Collection.class));
    assertEquals(VerbType.read, operation.getVerb());
    assertNull(operation.getProperNounType());
    assertNull(operation.getNounValueType());
    assertNotNull(operation.getSerializationContext());
    assertNull(operation.getResultType());

    Map<String, Class> adjectiveTypes = operation.getAdjectiveTypes();
    assertEquals(2, adjectiveTypes.size());
    assertTrue(adjectiveTypes.containsKey("bools"));
    assertEquals(boolean[].class, adjectiveTypes.get("bools"));
    assertTrue(adjectiveTypes.containsKey("ints"));
    assertEquals(Integer[].class, adjectiveTypes.get("ints"));
  }

  /**
   * Tests the invoke operation.
   */
  public void testInvoke() throws Exception {
    RESTOperation operation = new RESTOperation(VerbType.read, new RESTOperationExamples(), RESTOperationExamples.class.getMethod("invokeableOp", RootElementExample.class, String.class, Float.TYPE, Collection.class));
    HashMap<String, Object> adjectives = new HashMap<String, Object>();
    adjectives.put("hi", new Float(1234.5));
    adjectives.put("arg1", "adjective1Value");
    adjectives.put("arg3", new Short[] {8, 7, 6});
    RootElementExample ex = new RootElementExample();
    assertSame(ex, operation.invoke(null, adjectives, ex));
  }

}
