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

package org.codehaus.enunciate.modules.amf;

import junit.framework.TestCase;
import org.codehaus.enunciate.main.Enunciate;
import static org.easymock.EasyMock.*;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.messaging.amf.io.AMF3Deserializer;
import org.granite.messaging.amf.io.AMF3Serializer;
import org.granite.messaging.webapp.HttpGraniteContext;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Ryan Heaton
 */
public class TestEnunciateExternalizer extends TestCase {

  /**
   * Tests the externalizer.
   */
  public void testMyBeanExternalizer() throws Exception {
    FilterConfig filterConfig = createMock(FilterConfig.class);
    ServletContext servletContext = createMock(ServletContext.class);
    expect(filterConfig.getServletContext()).andReturn(servletContext);
    expect(servletContext.getAttribute((String) anyObject())).andReturn(null);
    File graniteFile = File.createTempFile("granite", "xml");
    new Enunciate(new String[0]).copyResource("/org/codehaus/enunciate/modules/amf/granite-test-config.xml", graniteFile);
    expect(filterConfig.getInitParameter("graniteConfigPath")).andReturn(graniteFile.getAbsolutePath());
    expect(filterConfig.getServletContext()).andReturn(servletContext);
    expect(servletContext.getRealPath(graniteFile.getAbsolutePath())).andReturn(graniteFile.getAbsolutePath());
    expect(filterConfig.getServletContext()).andReturn(servletContext);
    servletContext.setAttribute((String) anyObject(), anyObject());
    replay(filterConfig, servletContext);
    HttpGraniteContext.createThreadIntance(GraniteConfig.loadConfig(filterConfig), new ServicesConfig(), null, null, null);
    MyBeanExternalizer externalizer = new MyBeanExternalizer();
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    MyBean bean = new MyBean();
    bean.setProperty1("my special property");
    bean.setProperty2((float) 123.456);
    bean.setProperty3(9876);
    bean.setProperty4(true);
    Date date1 = new Date();
    Date date2 = new Date();
    Date date3 = new Date();
    bean.setProperty5(Arrays.asList(date1, date2, date3));
    HashMap<String, String> property6 = new HashMap<String, String>();
    property6.put("qwer", "tyui");
    property6.put("asdf", "ghjk");
    property6.put("zxcv", "bnm,");
    bean.setProperty6(property6);
    bean.setObject(new Object());
    byte[] bytesProperty = new byte[]{1, 2, 3, 4, 4, 3, 2, 1};
    bean.setBytes(bytesProperty);
//    bean.setCalendar(new ); //no support for XmlGregorianCalendar
//    bean.setDuration(); //no support for Duration
    bean.setDataHandler(new DataHandler(new FileDataSource(graniteFile)));
//    bean.setImage(); //no support for image.
//    bean.setSource(); //no support for source.
    bean.setUri(new URI("uri:mine"));
    UUID uuid = UUID.randomUUID();
    bean.setUuid(uuid);
    bean.setMyEnum(MyEnum.hello);

    AMF3Serializer amf3Serializer = new AMF3Serializer(bytes);
    externalizer.writeExternal(bean, amf3Serializer);
    amf3Serializer.flush();
    amf3Serializer.close();
    bean = new MyBean();
    externalizer.readExternal(bean, new AMF3Deserializer(new ByteArrayInputStream(bytes.toByteArray())));
    assertEquals("my special property", bean.getProperty1());
    assertEquals((float) 123.456, bean.getProperty2());
    assertEquals(9876, bean.getProperty3());
    assertEquals(Boolean.TRUE, bean.getProperty4());
    assertEquals(3, bean.getProperty5().size());
    Date otherdate1 = bean.getProperty5().iterator().next();
    assertNotSame(date1, otherdate1);
    assertEquals(date1, otherdate1);
    assertEquals("tyui", bean.getProperty6().get("qwer"));
    assertEquals("ghjk", bean.getProperty6().get("asdf"));
    assertEquals("bnm,", bean.getProperty6().get("zxcv"));
    assertNotNull(bean.getObject());
    assertTrue(Arrays.equals(bytesProperty, bean.getBytes()));
    DataHandler dataHandler = bean.getDataHandler();
    ByteArrayOutputStream attachmentOut = new ByteArrayOutputStream();
    dataHandler.writeTo(attachmentOut);
    assertEquals(graniteFile.length(), attachmentOut.toByteArray().length);
    assertEquals(new URI("uri:mine"), bean.getUri());
    assertEquals(uuid, bean.getUuid());
    assertSame(MyEnum.hello, bean.getMyEnum());
  }

}
