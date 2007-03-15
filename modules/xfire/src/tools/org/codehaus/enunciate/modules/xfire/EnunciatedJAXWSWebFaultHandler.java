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

package org.codehaus.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.handler.CustomFaultHandler;
import org.codehaus.xfire.jaxb2.AttachmentMarshaller;
import org.codehaus.xfire.service.MessagePartInfo;
import org.codehaus.xfire.util.ClassLoaderUtils;
import org.codehaus.xfire.util.stax.JDOMStreamWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.WebFault;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSWebFaultHandler extends CustomFaultHandler {

  private static final Log LOG = LogFactory.getLog(EnunciatedJAXWSWebFaultHandler.class);

  @Override
  public void invoke(MessageContext context) throws Exception {
    XFireFault fault = (XFireFault) context.getExchange().getFaultMessage().getBody();
    Throwable cause = fault.getCause();
    if ((cause != null) && (cause.getClass().isAnnotationPresent(WebFault.class))) {
      handleFault(context, fault, cause, null /*fault info is ignored*/);
    }
  }

  @Override
  protected void handleFault(MessageContext context, XFireFault fault, Throwable cause, MessagePartInfo faultPart) throws XFireFault {
    Object faultBean = getFaultBean(cause, faultPart, context);
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(faultBean.getClass());
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      marshaller.setAttachmentMarshaller(new AttachmentMarshaller(context));
      JDOMStreamWriter writer = new JDOMStreamWriter(fault.getDetail());
      marshaller.marshal(faultBean, writer);
    }
    catch (JAXBException e) {
      LOG.error("Unable to marshal the fault bean of type " + faultBean.getClass().getName() + ".", e);
      //fall through... let the fault be handled by something else...
    }
  }

  @Override
  protected Object getFaultBean(Throwable fault, MessagePartInfo faultPart, MessageContext context) {
    Class<? extends Throwable> faultClass = fault.getClass();

    boolean conformsToJAXWSFaultPattern = conformsToJAXWSFaultPattern(faultClass);
    if (conformsToJAXWSFaultPattern) {
      try {
        return faultClass.getMethod("getFaultInfo").invoke(fault);
      }
      catch (NoSuchMethodException e) {
        //fall through.  doesn't conform to the spec pattern.
      }
      catch (IllegalAccessException e) {
        throw new XFireRuntimeException("Couldn't invoke getFaultInfo method.", e);
      }
      catch (InvocationTargetException e) {
        throw new XFireRuntimeException("Couldn't invoke getFaultInfo method.", e);
      }
    }

    //doesn't conform to the spec pattern, use the generated fault bean class.
    Class faultBeanClass = getFaultBeanClass(faultClass);

    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(faultBeanClass, Object.class);
      Object faultBean = faultBeanClass.newInstance();
      for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
        if ((property.getWriteMethod() != null) && (property.getReadMethod() != null)) {
          Method getter = faultClass.getMethod(property.getReadMethod().getName());
          property.getWriteMethod().invoke(faultBean, getter.invoke(fault));
        }
      }
      return faultBean;
    }
    catch (IntrospectionException e) {
      throw new XFireRuntimeException("Unable to introspect fault bean class.", e);
    }
    catch (IllegalAccessException e) {
      throw new XFireRuntimeException("Unable to create fault bean.", e);
    }
    catch (InstantiationException e) {
      throw new XFireRuntimeException("Unable to create fault bean.", e);
    }
    catch (NoSuchMethodException e) {
      throw new XFireRuntimeException("The fault " + faultClass.getName() + " doesn't have a needed getter method used to fill in its fault bean.", e);
    }
    catch (InvocationTargetException e) {
      throw new XFireRuntimeException("Unable to create fault bean.", e);
    }
  }

  /**
   * Get the fault bean class for the specified fault class.  This method assumes that the fault class
   * doesn't conform to the JAXWS pattern.
   *
   * @param faultClass The fault class.
   * @return The fault bean class.
   */
  public static Class getFaultBeanClass(Class<? extends Throwable> faultClass) {
    String faultBeanClassName;
    WebFault webFaultInfo = faultClass.getAnnotation(WebFault.class);
    if ((webFaultInfo != null) && (webFaultInfo.faultBean() != null) && (webFaultInfo.faultBean().length() > 0)) {
      faultBeanClassName = webFaultInfo.faultBean();
    }
    else {
      StringBuilder builder = new StringBuilder();
      Package pckg = faultClass.getPackage();
      if ((pckg != null) && (!"".equals(pckg.getName()))) {
        builder.append(pckg.getName()).append(".");
      }
      builder.append("jaxws.");
      builder.append(faultClass.getSimpleName()).append("Bean");
      faultBeanClassName = builder.toString();
    }

    Class faultBeanClass;
    try {
      faultBeanClass = ClassLoaderUtils.loadClass(faultBeanClassName, faultClass);
    }
    catch (ClassNotFoundException e) {
      throw new XFireRuntimeException("Unable to load fault bean class.", e);
    }
    return faultBeanClass;
  }

  /**
   * Determine whether the fault conforms to the fault pattern described in the JAXWS spec, section 2.5.
   *
   * @param faultClass The fault class.
   * @return Whether the fault class conforms to the pattern.
   */
  public static boolean conformsToJAXWSFaultPattern(Class<? extends Throwable> faultClass) {
    boolean conformsToJAXWSFaultPattern = false;
    try {
      //1. needs to have a getFaultInfo method.
      Method getFaultInfoMethod = faultClass.getMethod("getFaultInfo");
      //2. needs to have a constructor matching WebFault(String message, FaultBean faultBean) {...}
      faultClass.getConstructor(String.class, getFaultInfoMethod.getReturnType());
      //3. needs to have a constructor matching WebFault(String message, FaultBean faultBean, Throwable cause) {...}
      faultClass.getConstructor(String.class, getFaultInfoMethod.getReturnType(), Throwable.class);

      conformsToJAXWSFaultPattern = true;
    }
    catch (NoSuchMethodException e) {
      //fall through.  doesn't conform to the spec pattern.
    }
    return conformsToJAXWSFaultPattern;
  }

}
