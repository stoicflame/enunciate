package org.codehaus.enunciate.modules.amf;

import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class TestAMFMapperIntrospector extends TestCase {

  public void testGetAMFMapperPerformance() {
    // prime the pump for the cache
    AMFMapperIntrospector.getAMFMapper(Long.class, long.class);

    long start = System.currentTimeMillis();
    for (int i = 0; i < 200; i++) {
      AMFMapperIntrospector.getAMFMapper(Long.class, long.class);
    }

    long duration = System.currentTimeMillis() - start;
    assertTrue("Took too long to get mapper that should have been cache. duration " + duration, duration < 2000);
  }

  public void testGetAMFMapperAdaptedType() throws Exception {
    //first make sure that JAXB handles the case...
    TestObject to = new TestObject();
    HashMap<String, String> p = new HashMap<String, String>();
    p.put("key", "value");
    to.setProp(p);
    to.setEntries(new Map.Entry[] {new AbstractMap.SimpleEntry("key1", "value1")});
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JAXBContext jaxbContext = JAXBContext.newInstance(TestObject.class);
    jaxbContext.createMarshaller().marshal(to, out);
    to = (TestObject) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(out.toByteArray()));
    assertEquals("value", to.getProp().get("key"));
    assertEquals("value1", to.getEntries()[0].getValue());

//    MapCarryObject[] obj = new MapCarryObject[]{new MapCarryObject("key", "value")};
//    PropertyDescriptor prop = AMFUtils.findProperty(TestObject.class, "prop");
//
//    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(null, prop.getPropertyType(), AMFUtils.findTypeAdapter(prop), AMFUtils.findXmlElement(prop));
//
//    Map<String, String> result = (Map<String, String>) mapper.toJAXB(obj, new AMFMappingContext());
//
//    assertEquals(result.size(), 1);
//    assertNotNull(result.get("key"));
//    assertEquals(result.get("key"), "value");
//
//    assertTrue("Wrong mapper type.", mapper instanceof AdaptingAMFMapper);

  }

  /**
   * tests the application of an xml adapter when the adapter on a property is adapting the component type.
   */
  public void testAdaptingComponentType() throws Exception {
//    MapCarryObject[] obj = new MapCarryObject[]{new MapCarryObject("key", "value")};
//    PropertyDescriptor prop2 = AMFUtils.findProperty(TestObject.class, "entries");
//
//    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(null, prop2.getPropertyType(), AMFUtils.findTypeAdapter(prop2), AMFUtils.findXmlElement(prop2));
//
//    Map.Entry[] result2 = (Map.Entry[]) mapper.toJAXB(obj, new AMFMappingContext());
//
//    assertEquals(result2.length, 1);
//    assertNotNull(result2[0].getKey());
//    assertEquals(result2[0].getKey(), "value");
//
//    assertTrue("Wrong mapper type.", mapper instanceof AdaptingAMFMapper);
  }
}
