package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.util.ClassLoaderUtils;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.aegis.stax.ElementReader;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;

/**
 * The binding for a JAXWS operation.
 *
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSOperationBinding implements MessageSerializer {

  private final WrapperBeanInfo requestInfo;
  private final WrapperBeanInfo responseInfo;

  public EnunciatedJAXWSOperationBinding(OperationInfo op) throws XFireFault {
    this.requestInfo = getRequestInfo(op);
    this.responseInfo = getOutputProperties(op);
  }

  /**
   * Loads the set of input properties for the specified operation.
   *
   * @param op The operation.
   * @return The input properties.
   */
  protected WrapperBeanInfo getRequestInfo(OperationInfo op) throws XFireFault {
    Method method = op.getMethod();
    Class ei = method.getDeclaringClass();
    Package pckg = ei.getPackage();

    String requestWrapperClassName;
    RequestWrapper requestWrapperInfo = method.getAnnotation(RequestWrapper.class);
    if ((requestWrapperInfo != null) && (requestWrapperInfo.className() != null) && (requestWrapperInfo.className().length() > 0)) {
      requestWrapperClassName = requestWrapperInfo.className();
    }
    else {
      StringBuilder builder = new StringBuilder(pckg == null ? "" : pckg.getName());
      if (builder.length() > 0) {
        builder.append(".");
      }

      builder.append("jaxws.");

      String methodName = method.getName();
      builder.append(capitalize(methodName));
      requestWrapperClassName = builder.toString();
    }

    Class wrapperClass;
    try {
      wrapperClass = ClassLoaderUtils.loadClass(requestWrapperClassName, getClass());
    }
    catch (ClassNotFoundException e) {
      throw new XFireFault("Unable to load class " + requestWrapperClassName, XFireFault.RECEIVER);
    }

    return new WrapperBeanInfo(wrapperClass, loadOrderedProperties(wrapperClass));
  }

  /**
   * Loads the set of output properties for the specified operation.
   *
   * @param op The operation.
   * @return The output properties.
   */
  protected WrapperBeanInfo getOutputProperties(OperationInfo op) throws XFireFault {
    Method method = op.getMethod();
    Class ei = method.getDeclaringClass();
    Package pckg = ei.getPackage();

    String responseWrapperClassName;
    ResponseWrapper responseWrapperInfo = method.getAnnotation(ResponseWrapper.class);
    if ((responseWrapperInfo != null) && (responseWrapperInfo.className() != null) && (responseWrapperInfo.className().length() > 0)) {
      responseWrapperClassName = responseWrapperInfo.className();
    }
    else {
      StringBuilder builder = new StringBuilder(pckg == null ? "" : pckg.getName());
      if (builder.length() > 0) {
        builder.append(".");
      }

      builder.append("jaxws.");

      String methodName = method.getName();
      builder.append(capitalize(methodName)).append("Response");
      responseWrapperClassName = builder.toString();
    }

    Class wrapperClass;
    try {
      wrapperClass = ClassLoaderUtils.loadClass(responseWrapperClassName, getClass());
    }
    catch (ClassNotFoundException e) {
      throw new XFireFault("Unable to load class " + responseWrapperClassName, XFireFault.RECEIVER);
    }

    return new WrapperBeanInfo(wrapperClass, loadOrderedProperties(wrapperClass));
  }

  /**
   * Loads the property descriptors for the ordered properties of the specified class.
   *
   * @param wrapperClass The wrapper class.
   * @return The ordered property descriptors.
   */
  protected PropertyDescriptor[] loadOrderedProperties(Class wrapperClass) throws XFireFault {
    XmlType responseTypeInfo = (XmlType) wrapperClass.getAnnotation(XmlType.class);
    if ((responseTypeInfo == null) || (responseTypeInfo.propOrder() == null) || ((responseTypeInfo.propOrder().length == 1) && "".equals(responseTypeInfo.propOrder()[0]))) {
      throw new XFireFault("Unable use use " + wrapperClass.getName() + " as a wrapper class: no propOrder specified.", XFireFault.RECEIVER);
    }

    String[] responsePropOrder = responseTypeInfo.propOrder();
    BeanInfo responseBeanInfo = null;
    try {
      responseBeanInfo = Introspector.getBeanInfo(wrapperClass);
    }
    catch (IntrospectionException e) {
      throw new XFireFault("Unable to introspect " + wrapperClass.getName(), e, XFireFault.RECEIVER);
    }

    PropertyDescriptor[] pds = responseBeanInfo.getPropertyDescriptors();
    PropertyDescriptor[] outputProperties = new PropertyDescriptor[responsePropOrder.length];
    RESPONSE_PROPERTY_LOOP : for (int i = 0; i < responsePropOrder.length; i++) {
      String property = responsePropOrder[i];
      for (PropertyDescriptor descriptor : pds) {
        if (descriptor.getName().equals(property)) {
          outputProperties[i] = descriptor;
          continue RESPONSE_PROPERTY_LOOP;
        }
      }

      throw new XFireFault("Unknown property " + property + " on wrapper " + wrapperClass.getName(), XFireFault.RECEIVER);
    }

    return outputProperties;
  }

  public void readMessage(InMessage message, MessageContext context) throws XFireFault {
    Class wrapperClass = this.requestInfo.getWrapperClass();
    Service service = context.getService();
    AegisBindingProvider provider = (AegisBindingProvider) service.getBindingProvider();
    Type type = provider.getType(service, wrapperClass);
    Object wrapper = type.readObject(new ElementReader(message.getXMLStreamReader()), context);
    List<Object> parameters = new ArrayList<Object>();
    for (PropertyDescriptor descriptor : this.requestInfo.getProperties()) {
      try {
        parameters.add(descriptor.getReadMethod().invoke(wrapper));
      }
      catch (IllegalAccessException e) {
        throw new XFireFault("Problem with property " + descriptor.getName() + " on " + wrapperClass.getName() + ".", e, XFireFault.RECEIVER);
      }
      catch (InvocationTargetException e) {
        throw new XFireFault("Problem with property " + descriptor.getName() + " on " + wrapperClass.getName() + ".", e, XFireFault.RECEIVER);
      }
    }

    message.setBody(parameters);
  }

  public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context) throws XFireFault {
    Class wrapperClass = this.responseInfo.getWrapperClass();
    Object wrapper;
    try {
      wrapper = wrapperClass.newInstance();
    }
    catch (Exception e) {
      throw new XFireFault("Problem instantiating response wrapper " + wrapperClass.getName() + ".", e, XFireFault.RECEIVER);
    }

    Object[] params = (Object[]) message.getBody();
    PropertyDescriptor[] properties = this.responseInfo.getProperties();
    if (properties.length != params.length) {
      throw new XFireFault("There are " + params.length + " parameters to the out message but only "
        + properties.length + " properties on " + wrapperClass.getName(), XFireFault.RECEIVER);
    }

    for (int i = 0; i < properties.length; i++) {
      PropertyDescriptor descriptor = properties[i];
      try {
        descriptor.getWriteMethod().invoke(wrapper, params[i]);
      }
      catch (IllegalAccessException e) {
        throw new XFireFault("Problem with property " + descriptor.getName() + " on " + wrapperClass.getName() + ".", e, XFireFault.RECEIVER);
      }
      catch (InvocationTargetException e) {
        throw new XFireFault("Problem with property " + descriptor.getName() + " on " + wrapperClass.getName() + ".", e, XFireFault.RECEIVER);
      }
    }

    Service service = context.getService();
    AegisBindingProvider provider = (AegisBindingProvider) service.getBindingProvider();
    Type type = provider.getType(service, wrapperClass);
    type.writeObject(wrapper, new ElementWriter(writer), context);
  }

  /**
   * Capitalizes a string.
   *
   * @param string The string to capitalize.
   * @return The capitalized value.
   */
  private String capitalize(String string) {
    return Character.toString(string.charAt(0)).toUpperCase() + string.substring(1);
  }

  /**
   * A simple bean info for a wrapper class.
   */
  public static class WrapperBeanInfo {

    private Class wrapperClass;
    private PropertyDescriptor[] properties;

    public WrapperBeanInfo(Class wrapperClass, PropertyDescriptor[] properties) {
      this.wrapperClass = wrapperClass;
      this.properties = properties;
    }

    /**
     * The wrapper class.
     *
     * @return The wrapper class.
     */
    public Class getWrapperClass() {
      return wrapperClass;
    }

    /**
     * The ordered list of wrapper properties.
     *
     * @return The ordered list of wrapper properties.
     */
    public PropertyDescriptor[] getProperties() {
      return properties;
    }

  }

}
