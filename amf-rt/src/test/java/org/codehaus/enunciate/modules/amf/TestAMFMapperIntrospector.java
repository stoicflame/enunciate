package org.codehaus.enunciate.modules.amf;

import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.ParameterizedType;
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

  public void testGetAMFMapperAdaptedTypeJAXBEnum() throws Exception {
    //first make sure that JAXB handles the case...
    TestObject to = new TestObject();
    HashMap<TestEnum, String> pEnum = new HashMap<TestEnum, String>();
    pEnum.put(TestEnum.VAL1, "valueE");
    to.setPropEnum(pEnum);

    HashMap<String, String> pString = new HashMap<String, String>();
    pString.put("key", "valueS");
    to.setPropString(pString);

    to.setEntries(new Map.Entry[] {new AbstractMap.SimpleEntry("key1", "value1")});

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JAXBContext jaxbContext = JAXBContext.newInstance(TestObject.class, TestEnum.class);
    jaxbContext.createMarshaller().marshal(to, out);


    to = (TestObject) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(out.toByteArray()));

    assertEquals("valueE", to.getPropEnum().get(TestEnum.VAL1));
    assertEquals("valueS", to.getPropString().get("key"));
    assertEquals("value1", to.getEntries()[0].getValue());
  }

  public void testGetAMFMapperAdaptedTypeWithAMFMapperEnum() {
    MapCarryObject[] obj = new MapCarryObject[]{new MapCarryObject("VAL1", "value")};
    PropertyDescriptor prop = AMFUtils.findProperty(new TestObject().getClass(), "propEnum");
    
    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(null, prop.getReadMethod().getGenericReturnType(), AMFUtils.findTypeAdapter(prop), AMFUtils.findXmlElement(prop));

    Map<TestEnum, String> result = (Map<TestEnum, String>) mapper.toJAXB(obj, new AMFMappingContext());

    assertEquals(result.size(), 1);
    assertNotNull(result.get(TestEnum.VAL1));
    assertEquals(result.get(TestEnum.VAL1), "value");

    assertTrue("Wrong mapper type.", mapper instanceof AdaptingAMFMapper);
  }

  public void testGetAMFMapperAdaptedTypeWithAMFMapperString() {
    MapCarryObject[] obj = new MapCarryObject[]{new MapCarryObject("key", "value")};
    PropertyDescriptor prop = AMFUtils.findProperty(TestObject.class, "propString");

    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(null, prop.getReadMethod().getGenericReturnType(), AMFUtils.findTypeAdapter(prop), AMFUtils.findXmlElement(prop));

    Map<String, String> result = (Map<String, String>) mapper.toJAXB(obj, new AMFMappingContext());

    assertEquals(result.size(), 1);
    assertNotNull(result.get("key"));
    assertEquals(result.get("key"), "value");

    assertTrue("Wrong mapper type.", mapper instanceof AdaptingAMFMapper);
  }

  public void testGetAMFMapperAdaptedTypeWithAMFMapperEntity() {
    MapCarryObject[] obj = new MapCarryObject[]{new MapCarryObject("key", "value")};
    PropertyDescriptor prop = AMFUtils.findProperty(TestObject.class, "entries");
    AMFMapper  mapper = AMFMapperIntrospector.getAMFMapper(null, prop.getPropertyType(), AMFUtils.findTypeAdapter(prop), AMFUtils.findXmlElement(prop));

    Map.Entry[] result2 = (Map.Entry[]) mapper.toJAXB(obj, new AMFMappingContext());

    assertEquals(result2.length, 1);
    assertNotNull(result2[0].getKey());
    assertEquals(result2[0].getKey(), "key");
    assertEquals(result2[0].getValue(), "value");
      
    assertTrue("Wrong mapper type.", mapper instanceof AdaptingAMFMapper);
  }
}
