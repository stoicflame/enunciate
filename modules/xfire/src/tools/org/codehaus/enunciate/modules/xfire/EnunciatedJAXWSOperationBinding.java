package org.codehaus.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.transport.Channel;
import org.codehaus.xfire.jaxb2.AttachmentUnmarshaller;
import org.codehaus.xfire.jaxb2.AttachmentMarshaller;
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
import org.codehaus.xfire.service.MessagePartInfo;
import org.codehaus.xfire.util.ClassLoaderUtils;
import org.codehaus.xfire.util.stax.DOMStreamWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.*;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.jws.soap.SOAPBinding;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;

/**
 * The binding for a JAXWS operation.
 *
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSOperationBinding implements MessageSerializer {

  private static final Log LOG = LogFactory.getLog(EnunciatedJAXWSOperationBinding.class);

  private final JAXBContext jaxbContext;
  private final OperationBeanInfo requestInfo;
  private final OperationBeanInfo responseInfo;

  public EnunciatedJAXWSOperationBinding(OperationInfo op) throws XFireFault {
    this.requestInfo = getRequestInfo(op);
    this.responseInfo = getResponseInfo(op);
    ArrayList<Class> contextClasses = new ArrayList<Class>(2);
    if (this.requestInfo != null) {
      contextClasses.add(this.requestInfo.getBeanClass());
    }
    if (this.responseInfo != null) {
      contextClasses.add(this.responseInfo.getBeanClass());
    }

    try {
      this.jaxbContext = JAXBContext.newInstance(contextClasses.toArray(new Class[contextClasses.size()]));
    }
    catch (JAXBException e) {
      throw new XFireFault("Unable to create a binding for " + op.getMethod() + ".", e, XFireFault.RECEIVER);
    }
  }

  /**
   * Loads the set of input properties for the specified operation.
   *
   * @param op The operation.
   * @return The input properties, or null if none were found.
   */
  protected OperationBeanInfo getRequestInfo(OperationInfo op) throws XFireFault {
    Method method = op.getMethod();
    Class ei = method.getDeclaringClass();
    Package pckg = ei.getPackage();
    SOAPBinding.ParameterStyle paramStyle = SOAPBinding.ParameterStyle.WRAPPED;
    if (method.isAnnotationPresent(SOAPBinding.class)) {
      SOAPBinding annotation = method.getAnnotation(SOAPBinding.class);
      paramStyle = annotation.parameterStyle();
    }
    else if (ei.isAnnotationPresent(SOAPBinding.class)) {
      SOAPBinding annotation = ((SOAPBinding) ei.getAnnotation(SOAPBinding.class));
      paramStyle = annotation.parameterStyle();
    }

    if (paramStyle == SOAPBinding.ParameterStyle.BARE) {
      //return a bare operation info.
      return new OperationBeanInfo(method.getParameterTypes()[0], null);
    }
    else {
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
        LOG.error("Unabled to find request wrapper class " + requestWrapperClassName + "... Operation " + op.getQName() + " will not be able to recieve...");
        return null;
      }
      
      return new OperationBeanInfo(wrapperClass, loadOrderedProperties(wrapperClass));
    }

  }

  /**
   * Loads the set of output properties for the specified operation.
   *
   * @param op The operation.
   * @return The output properties.
   */
  protected OperationBeanInfo getResponseInfo(OperationInfo op) throws XFireFault {
    Method method = op.getMethod();
    Class ei = method.getDeclaringClass();
    Package pckg = ei.getPackage();
    SOAPBinding.ParameterStyle paramStyle = SOAPBinding.ParameterStyle.WRAPPED;
    if (method.isAnnotationPresent(SOAPBinding.class)) {
      SOAPBinding annotation = method.getAnnotation(SOAPBinding.class);
      paramStyle = annotation.parameterStyle();
    }
    else if (ei.isAnnotationPresent(SOAPBinding.class)) {
      SOAPBinding annotation = ((SOAPBinding) ei.getAnnotation(SOAPBinding.class));
      paramStyle = annotation.parameterStyle();
    }

    if (paramStyle == SOAPBinding.ParameterStyle.BARE) {
      //bare return type.
      return new OperationBeanInfo(method.getReturnType(), null);
    }
    else {
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
        LOG.debug("Unabled to find request wrapper class " + responseWrapperClassName + "... Operation " + op.getQName() + " will not be able to send...");
        return null;
      }

      return new OperationBeanInfo(wrapperClass, loadOrderedProperties(wrapperClass));
    }
  }

  /**
   * Loads the property descriptors for the ordered properties of the specified class.
   *
   * @param wrapperClass The wrapper class.
   * @return The ordered property descriptors.
   */
  protected PropertyDescriptor[] loadOrderedProperties(Class wrapperClass) throws XFireFault {
    XmlType typeInfo = (XmlType) wrapperClass.getAnnotation(XmlType.class);
    if ((typeInfo == null) || (typeInfo.propOrder() == null) || ((typeInfo.propOrder().length == 1) && "".equals(typeInfo.propOrder()[0]))) {
      throw new XFireFault("Unable use use " + wrapperClass.getName() + " as a wrapper class: no propOrder specified.", XFireFault.RECEIVER);
    }

    String[] propOrder = typeInfo.propOrder();
    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(wrapperClass, Object.class);
    }
    catch (IntrospectionException e) {
      throw new XFireFault("Unable to introspect " + wrapperClass.getName(), e, XFireFault.RECEIVER);
    }

    PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
    PropertyDescriptor[] props = new PropertyDescriptor[propOrder.length];
    RESPONSE_PROPERTY_LOOP :
    for (int i = 0; i < propOrder.length; i++) {
      String property = propOrder[i];
      for (PropertyDescriptor descriptor : pds) {
        if (descriptor.getName().equals(property)) {
          props[i] = descriptor;
          continue RESPONSE_PROPERTY_LOOP;
        }
      }

      throw new XFireFault("Unknown property " + property + " on wrapper " + wrapperClass.getName(), XFireFault.RECEIVER);
    }

    return props;
  }

  public void readMessage(InMessage message, MessageContext context) throws XFireFault {
    if (this.requestInfo == null) {
      throw new XFireFault("Unable to read message: no request info was found!", XFireFault.RECEIVER);
    }

    Object bean;
    try {
      Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
      unmarshaller.setAttachmentUnmarshaller(new AttachmentUnmarshaller(context));
      bean = unmarshaller.unmarshal(message.getXMLStreamReader());
    }
    catch (JAXBException e) {
      throw new XFireRuntimeException("Unable to unmarshal type.", e);
    }

    List<Object> parameters = new ArrayList<Object>();
    if (this.requestInfo.isBare()) {
      //bare method, doesn't need to be unwrapped.
      parameters.add(bean);
    }
    else {
      for (PropertyDescriptor descriptor : this.requestInfo.getPropertyOrder()) {
        try {
          parameters.add(descriptor.getReadMethod().invoke(bean));
        }
        catch (IllegalAccessException e) {
          throw new XFireFault("Problem with property " + descriptor.getName() + " on " + this.requestInfo.getBeanClass().getName() + ".", e, XFireFault.RECEIVER);
        }
        catch (InvocationTargetException e) {
          throw new XFireFault("Problem with property " + descriptor.getName() + " on " + this.requestInfo.getBeanClass().getName() + ".", e, XFireFault.RECEIVER);
        }
      }
    }

    message.setBody(parameters);
  }

  public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context) throws XFireFault {
    if (this.responseInfo == null) {
      throw new XFireFault("Unable to write message: no response info was found!", XFireFault.SENDER);
    }

    Class beanClass = this.responseInfo.getBeanClass();
    Object[] params = (Object[]) message.getBody();
    Object bean;
    if (this.responseInfo.isBare()) {
      //bare response.  we don't need to wrap it up.
      bean = params[0];
    }
    else {
      try {
        bean = beanClass.newInstance();
      }
      catch (Exception e) {
        throw new XFireFault("Problem instantiating response wrapper " + beanClass.getName() + ".", e, XFireFault.RECEIVER);
      }

      PropertyDescriptor[] properties = this.responseInfo.getPropertyOrder();
      if (properties.length > 0) { //no properties implies a void method...
        if (properties.length != params.length) {
          throw new XFireFault("There are " + params.length + " parameters to the out message but "
            + properties.length + " properties on " + beanClass.getName(), XFireFault.RECEIVER);
        }

        for (int i = 0; i < properties.length; i++) {
          PropertyDescriptor descriptor = properties[i];
          try {
            descriptor.getWriteMethod().invoke(bean, params[i]);
          }
          catch (IllegalAccessException e) {
            throw new XFireFault("Problem with property " + descriptor.getName() + " on " + beanClass.getName() + ".", e, XFireFault.RECEIVER);
          }
          catch (InvocationTargetException e) {
            throw new XFireFault("Problem with property " + descriptor.getName() + " on " + beanClass.getName() + ".", e, XFireFault.RECEIVER);
          }
        }
      }
    }

    try {
      Marshaller marshaller = this.jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      marshaller.setAttachmentMarshaller(new AttachmentMarshaller(context));
      marshaller.marshal(bean, writer);
    }
    catch (JAXBException e) {
      throw new XFireRuntimeException("Unable to marshal type.", e);
    }
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
  public static class OperationBeanInfo {

    private Class beanClass;
    private PropertyDescriptor[] propertyOrder;

    public OperationBeanInfo(Class wrapperClass, PropertyDescriptor[] properties) {
      this.beanClass = wrapperClass;
      this.propertyOrder = properties;
    }

    /**
     * The wrapper class.
     *
     * @return The wrapper class.
     */
    public Class getBeanClass() {
      return beanClass;
    }

    /**
     * Whether the operation bean is bare.
     *
     * @return Whether the operation bean is bare.
     */
    public boolean isBare() {
      return propertyOrder == null;
    }

    /**
     * The ordered list of wrapper properties.
     *
     * @return The ordered list of wrapper properties.
     */
    public PropertyDescriptor[] getPropertyOrder() {
      return propertyOrder;
    }

  }

}
