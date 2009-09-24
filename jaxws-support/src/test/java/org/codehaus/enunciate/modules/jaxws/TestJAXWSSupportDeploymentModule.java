/*
 * Copyright 2006-2008 Web Cohesion
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

package org.codehaus.enunciate.modules.jaxws;

import com.sun.mirror.declaration.ClassDeclaration;
import freemarker.template.TemplateException;
import junit.framework.Test;
import org.codehaus.enunciate.EnunciateException;
import static org.codehaus.enunciate.EnunciateTestUtil.getAllJavaFiles;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.SpecifiedOutputDirectoryFileTransform;
import org.codehaus.enunciate.apt.EnunciateAnnotationProcessor;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.DeploymentModule;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.apache.commons.beanutils.BeanUtils;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.lang.annotation.Annotation;

/**
 * @author Ryan Heaton
 */
public class TestJAXWSSupportDeploymentModule extends InAPTTestCase {

  /**
   * tests doing the freemarker generate.
   */
  public void testDoFreemarkerGenerate() throws Exception {
    Enunciate enunciate = new Enunciate(new String[0]);
    final EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.jaxws.AnotherEndpointInterface")));
    model.add(new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.jaxws.BasicEndpointInterface")));
    FreemarkerModel.set(model);

    final HashMap<URL, Integer> counts = new HashMap<URL, Integer>();

    JAXWSSupportDeploymentModule module = new JAXWSSupportDeploymentModule() {
      @Override
      public void processTemplate(URL templateURL, Object mdl) throws IOException, TemplateException {
        assertSame(mdl, model);
        int count = counts.get(templateURL) == null ? 1 : counts.get(templateURL) + 1;
        counts.put(templateURL, count);
      }

      @Override
      protected boolean isUpToDate(File genDir) {
        return false;
      }

      @Override
      protected EnunciateFreemarkerModel getModelInternal() {
        return model;
      }
    };
    module.init(enunciate);
    module.doFreemarkerGenerate();
    assertEquals(3, counts.size());
    for (URL url : counts.keySet()) {
      if (url.toString().endsWith("fault-bean.fmt")) {
        //each fault only gets processed once.
        assertEquals(new Integer(2), counts.get(url));
      }
      else {
        //the request wrappers, response wrappers get processed 4 times.
        assertEquals(new Integer(4), counts.get(url));
      }
    }
  }

  /**
   * Tests the request bean template.
   */
  public void testRequestBeanTemplate() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.jaxws.BasicEndpointInterface"));
    WebMethod notConflictingMethod = null;
    for (WebMethod webMethod : ei.getWebMethods()) {
      if ("notConflictingMethod".equals(webMethod.getSimpleName())) {
        notConflictingMethod = webMethod;
      }
    }

    RequestWrapper requestWrapper = new RequestWrapper(notConflictingMethod);

    Class generatedClass = createWrapperClass(requestWrapper);
    XmlRootElement rootElementInfo = (XmlRootElement) generatedClass.getAnnotation(XmlRootElement.class);
    assertNotNull(rootElementInfo);
    assertEquals(requestWrapper.getElementNamespace(), rootElementInfo.namespace());
    assertEquals(requestWrapper.getElementName(), rootElementInfo.name());

    XmlType typeInfo = (XmlType) generatedClass.getAnnotation(XmlType.class);
    assertNotNull(typeInfo);
    assertEquals(requestWrapper.getElementNamespace(), typeInfo.namespace());
    assertEquals(requestWrapper.getElementName(), typeInfo.name());
    assertEquals(2, typeInfo.propOrder().length);
    assertEquals("s", typeInfo.propOrder()[0]);
    assertEquals("l", typeInfo.propOrder()[1]);
        
    Map<String, Object> properties = new HashMap<String, Object>();
    Short shortValue = Short.valueOf((short) 123);
    Long longValue = Long.valueOf((long) 987654);
    properties.put("s", shortValue);
    properties.put("l", longValue);

    Object requestBean = generatedClass.newInstance();
    BeanUtils.populate(requestBean, properties);
    Map description = BeanUtils.describe(requestBean);
    description.remove("class");
    assertEquals(2, description.size());
    assertEquals(shortValue.toString(), description.get("s"));
    assertEquals(longValue.toString(), description.get("l"));

  }

  /**
   * Tests the response bean template.
   */
  public void testResponseBeanTemplate() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.jaxws.BasicEndpointInterface"));
    WebMethod notConflictingMethod = null;
    for (WebMethod webMethod : ei.getWebMethods()) {
      if ("notConflictingMethod".equals(webMethod.getSimpleName())) {
        notConflictingMethod = webMethod;
      }
    }

    ResponseWrapper responseWrapper = new ResponseWrapper(notConflictingMethod);

    Class generatedClass = createWrapperClass(responseWrapper);
    XmlRootElement rootElementInfo = (XmlRootElement) generatedClass.getAnnotation(XmlRootElement.class);
    assertNotNull(rootElementInfo);
    assertEquals(responseWrapper.getElementNamespace(), rootElementInfo.namespace());
    assertEquals(responseWrapper.getElementName(), rootElementInfo.name());

    XmlType typeInfo = (XmlType) generatedClass.getAnnotation(XmlType.class);
    assertNotNull(typeInfo);
    assertEquals(responseWrapper.getElementNamespace(), typeInfo.namespace());
    assertEquals(responseWrapper.getElementName(), typeInfo.name());
    assertEquals(1, typeInfo.propOrder().length);
    assertEquals("return", typeInfo.propOrder()[0]);

    Map<String, Object> properties = new HashMap<String, Object>();
    Double doubleValue = Double.valueOf(12345.6789);
    properties.put("return", doubleValue);

    Object responseBean = generatedClass.newInstance();
    BeanUtils.populate(responseBean, properties);
    Map description = BeanUtils.describe(responseBean);
    description.remove("class");
    assertEquals(1, description.size());
    assertEquals(doubleValue.toString(), description.get("return"));
  }

  /**
   * Tests the fault bean template.
   */
  public void testFaultBeanTemplate() throws Exception {
    WebFault faultWrapper = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.jaxws.NonConflictingFault"));

    Class generatedClass = createWrapperClass(faultWrapper);
    XmlRootElement rootElementInfo = (XmlRootElement) generatedClass.getAnnotation(XmlRootElement.class);
    assertNotNull(rootElementInfo);
    assertEquals(faultWrapper.getTargetNamespace(), rootElementInfo.namespace());
    assertEquals(faultWrapper.getElementName(), rootElementInfo.name());

    Annotation typeAnnotation = generatedClass.getAnnotation(XmlType.class);
    assertNotNull(typeAnnotation);

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("hi", "me");
    properties.put("hello", "World");

    Object faultBean = generatedClass.newInstance();
    BeanUtils.populate(faultBean, properties);
    Map description = BeanUtils.describe(faultBean);
    description.remove("class");
    assertEquals(3, description.size());
    assertEquals("me", description.get("hi"));
    assertEquals("World", description.get("hello"));
    assertNull(description.get("message"));
  }

  /**
   * Creates the wrapper class for the specified request wrapper.
   * 
   * @param requestWrapper The request wrapper.
   * @return The wrapper class that was generated.
   */
  public Class createWrapperClass(RequestWrapper requestWrapper) throws Exception {
    return createWrapperClass(requestWrapper, JAXWSSupportDeploymentModule.class.getResource("request-bean.fmt"), requestWrapper.getRequestBeanName());
  }

  /**
   * Creates the wrapper class for the specified response wrapper.
   *
   * @param responseWrapper The response wrapper.
   * @return The wrapper class that was generated.
   */
  public Class createWrapperClass(ResponseWrapper responseWrapper) throws Exception {
    return createWrapperClass(responseWrapper, JAXWSSupportDeploymentModule.class.getResource("response-bean.fmt"), responseWrapper.getResponseBeanName());
  }

  /**
   * Creates the wrapper class for the specified fault wrapper.
   *
   * @param faultWrapper The fault wrapper.
   * @return The wrapper class that was generated.
   */
  public Class createWrapperClass(WebFault faultWrapper) throws Exception {
    return createWrapperClass(faultWrapper, JAXWSSupportDeploymentModule.class.getResource("fault-bean.fmt"), faultWrapper.getImplicitFaultBeanQualifiedName());
  }

  /**
   * Create the wrapper class for the given web messgae.
   *
   * @param message The web message.
   * @param templateURL The url for the template to use to create the wrapper class.
   * @param beanFQN The fqn of the bean.
   * @return The wrapper class.
   */
  protected Class createWrapperClass(WebMessage message, URL templateURL, String beanFQN) throws Exception {
    setupDefaultModel();

    //generate the java source file.
    Enunciate enunciate = new Enunciate(new String[0]);
    File genDir = enunciate.createTempDir();
    enunciate.setGenerateDir(genDir);
    JAXWSSupportDeploymentModule module = new JAXWSSupportDeploymentModule() {
      @Override
      protected EnunciateFreemarkerModel getModelInternal() {
        return (EnunciateFreemarkerModel) FreemarkerModel.get();
      }
    };
    module.init(enunciate);
    EnunciateFreemarkerModel model = module.getModel();
    model.put("file", new SpecifiedOutputDirectoryFileTransform(genDir));
    model.put("message", message);
    module.processTemplate(templateURL, model);

    Collection<String> srcFiles = enunciate.getJavaFiles(genDir);
    assertEquals("The wrapper bean should have been generated.", 1, srcFiles.size());
    srcFiles.addAll(getAllJavaFiles(getSamplesDir()));
    File buildDir = enunciate.createTempDir();
    enunciate.invokeJavac(getInAPTClasspath(), buildDir, srcFiles.toArray(new String[srcFiles.size()]));
    URLClassLoader loader = new URLClassLoader(new URL[] {buildDir.toURL()}, getClass().getClassLoader());
    Class generatedClass = Class.forName(beanFQN, true, loader);
    assertNotNull(generatedClass);

    return generatedClass;
  }

  /**
   * Sets up the default model.
   */
  protected void setupDefaultModel() throws EnunciateException {
    //set up the default root model.
    EnunciateConfiguration config = new EnunciateConfiguration(new ArrayList<DeploymentModule>());
    config.setValidator(new BaseValidator()); //skip the validation...
    Enunciate enunciate = new Enunciate(new String[0]);
    enunciate.setConfig(config);
    new EnunciateAnnotationProcessor(enunciate).process();
  }

  public static Test suite() {
    return createSuite(TestJAXWSSupportDeploymentModule.class);
  }

}
