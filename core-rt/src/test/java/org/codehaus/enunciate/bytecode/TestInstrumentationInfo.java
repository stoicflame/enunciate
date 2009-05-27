package org.codehaus.enunciate.bytecode;

import junit.framework.TestCase;

import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * @author Ryan Heaton
 */
public class TestInstrumentationInfo extends TestCase {

  /**
   * tests writing and reading instrumentation info.
   */
  public void testWriteRead() throws Exception {
    HashMap<MethodKey, String[]> parameterNames = new HashMap<MethodKey, String[]>();
    MethodKey key1 = new MethodKey("1", "2", "3");
    parameterNames.put(key1, new String[]{"4", "5"});
    MethodKey key2 = new MethodKey("6", "7", "8");
    parameterNames.put(key2, new String[]{"9", "0"});
    InstrumentationInfo info = new InstrumentationInfo(parameterNames);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    info.writeTo(bytes);
    bytes.flush();
    bytes.close();
    InstrumentationInfo from = InstrumentationInfo.loadFrom(new ByteArrayInputStream(bytes.toByteArray()));
    assertEquals(2, from.getParameterNames().size());
    assertEquals(2, from.getParameterNames().get(key1).length);
    assertEquals(2, from.getParameterNames().get(key2).length);
  }

}
