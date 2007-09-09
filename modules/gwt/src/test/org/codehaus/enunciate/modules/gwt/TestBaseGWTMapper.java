/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.gwt;

import junit.framework.TestCase;

import java.util.*;
import java.net.URI;

/**
 * @author Ryan Heaton
 */
public class TestBaseGWTMapper extends TestCase {

  /**
   * Tests the basic mappings.
   */
  public void testBasicMappings() throws Exception {
    GWTMappingContext context = new GWTMappingContext();
    GWTMapper mapper = GWTMapperIntrospector.getGWTMapper(BeanOne.class, null);
    BeanOne jaxbObject = new BeanOne();
    jaxbObject.setProperty1("value1");
    jaxbObject.setProperty2(987654);
    Calendar cal = Calendar.getInstance();
    jaxbObject.setProperty3(cal);
    BeanOneDotOne oneDotOne = new BeanOneDotOne();
    UUID uuid1 = UUID.randomUUID();
    oneDotOne.setProperty1(uuid1);
    double[] doubles1 = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
    oneDotOne.setProperty2(doubles1);
    jaxbObject.setProperty4(oneDotOne);
    BeanOneDotTwo oneDotTwo1 = new BeanOneDotTwo();
    UUID uuid2 = UUID.randomUUID();
    oneDotTwo1.setProperty1(uuid2);
    double[] doubles2 = new double[]{6.6, 7.7, 8.8, 9.9};
    oneDotTwo1.setProperty2(doubles2);
    oneDotTwo1.setProperty3(new URI("uri:one"));
    List<String> stringsOne = Arrays.asList("my", "mother", "told", "me", "to");
    oneDotTwo1.setProperty4(stringsOne);
    BeanOneDotTwo oneDotTwo2 = new BeanOneDotTwo();
    UUID uuid3 = UUID.randomUUID();
    oneDotTwo2.setProperty1(uuid3);
    double[] doubles3 = new double[]{10.10, 11.11, 12.12, 13.13, 14.14};
    oneDotTwo2.setProperty2(doubles3);
    oneDotTwo2.setProperty3(new URI("uri:two"));
    List<String> strings2 = Arrays.asList("to", "pick", "the", "best", "one");
    oneDotTwo2.setProperty4(strings2);
    jaxbObject.setProperty5(new BeanOneDotTwo[] {oneDotTwo1, oneDotTwo2, oneDotTwo1});
    jaxbObject.setProperty6(BeanOne.BeanOneEnum.sad);
    Map<String, BeanOneMapValue> map = new HashMap<String, BeanOneMapValue>();
    map.put("one", new BeanOneMapValue(1L));
    map.put("two", new BeanOneMapValue(2L));
    map.put("three", new BeanOneMapValue(3L));
    jaxbObject.setProperty7(map);

    GWTBeanOne gwtBeanOne = (GWTBeanOne) mapper.toGWT(jaxbObject, context);
    assertEquals("value1", gwtBeanOne.getProperty1());
    assertEquals(987654, gwtBeanOne.getProperty2());
    assertEquals(cal.getTime(), gwtBeanOne.getProperty3());
    GWTBeanOneDotOne gwtBeanOneDotOne = gwtBeanOne.getProperty4();
    assertEquals(uuid1.toString(), gwtBeanOneDotOne.getProperty1());
    assertTrue(Arrays.equals(doubles1, gwtBeanOneDotOne.getProperty2()));
    GWTBeanOneDotTwo[] gwtBeanOneDotTwos = gwtBeanOne.getProperty5();
    assertEquals(3, gwtBeanOneDotTwos.length);
    assertEquals(uuid2.toString(), gwtBeanOneDotTwos[0].getProperty1());
    assertTrue(Arrays.equals(doubles2, gwtBeanOneDotTwos[0].getProperty2()));
    assertEquals("uri:one", gwtBeanOneDotTwos[0].getProperty3());
    assertEquals(stringsOne, gwtBeanOneDotTwos[0].getProperty4());
    assertEquals(uuid3.toString(), gwtBeanOneDotTwos[1].getProperty1());
    assertTrue(Arrays.equals(doubles3, gwtBeanOneDotTwos[1].getProperty2()));
    assertEquals("uri:two", gwtBeanOneDotTwos[1].getProperty3());
    assertEquals(strings2, gwtBeanOneDotTwos[1].getProperty4());
    assertSame(gwtBeanOneDotTwos[0], gwtBeanOneDotTwos[2]);
    assertEquals("sad", gwtBeanOne.getProperty6());
    Map<String, GWTBeanOneMapValue> gwtMap = gwtBeanOne.getProperty7();
    assertEquals(3, gwtMap.size());
    assertEquals(new Long(3), gwtMap.get("three").getProperty1());
    assertEquals(new Long(2), gwtMap.get("two").getProperty1());
    assertEquals(new Long(1), gwtMap.get("one").getProperty1());

    jaxbObject = (BeanOne) mapper.toJAXB(gwtBeanOne, context);
    assertEquals("value1", jaxbObject.getProperty1());
    assertEquals(987654, jaxbObject.getProperty2());
    assertEquals(cal, jaxbObject.getProperty3());
    BeanOneDotOne beanOneDotOne = jaxbObject.getProperty4();
    assertEquals(uuid1, beanOneDotOne.getProperty1());
    assertTrue(Arrays.equals(doubles1, beanOneDotOne.getProperty2()));
    BeanOneDotTwo[] oneDotTwos = jaxbObject.getProperty5();
    assertEquals(3, oneDotTwos.length);
    assertEquals(uuid2, oneDotTwos[0].getProperty1());
    assertTrue(Arrays.equals(doubles2, oneDotTwos[0].getProperty2()));
    assertEquals(new URI("uri:one"), oneDotTwos[0].getProperty3());
    assertEquals(stringsOne, oneDotTwos[0].getProperty4());
    assertEquals(uuid3, oneDotTwos[1].getProperty1());
    assertTrue(Arrays.equals(doubles3, oneDotTwos[1].getProperty2()));
    assertEquals(new URI("uri:two"), oneDotTwos[1].getProperty3());
    assertEquals(strings2, oneDotTwos[1].getProperty4());
    assertSame(oneDotTwos[0], oneDotTwos[2]);
    assertSame(BeanOne.BeanOneEnum.sad, jaxbObject.getProperty6());
    map = jaxbObject.getProperty7();
    assertEquals(new Long(3), map.get("three").getProperty1());
    assertEquals(new Long(2), map.get("two").getProperty1());
    assertEquals(new Long(1), map.get("one").getProperty1());
  }

}
