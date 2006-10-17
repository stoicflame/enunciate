package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.handler.CustomFaultHandler;
import org.codehaus.xfire.service.MessagePartInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.util.ClassLoaderUtils;
import org.codehaus.xfire.util.stax.JDOMStreamWriter;

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

  @Override
  protected void handleFault(MessageContext context, XFireFault fault, Throwable cause, MessagePartInfo faultPart) throws XFireFault {
    Object faultBean = getFaultBean(fault, faultPart, context);
    Service service = context.getService();
    AegisBindingProvider provider = (AegisBindingProvider) service.getBindingProvider();
    Type type = provider.getType(service, faultBean.getClass());
    JDOMStreamWriter writer = new JDOMStreamWriter(fault.getDetail());
    type.writeObject(faultBean, new ElementWriter(writer), context);
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
