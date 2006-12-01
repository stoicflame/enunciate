package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.modules.xfire_client.annotations.RequestWrapperAnnotation;
import net.sf.enunciate.modules.xfire_client.annotations.ResponseWrapperAnnotation;
import org.codehaus.xfire.MessageContext;
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
import org.codehaus.xfire.util.ClassLoaderUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamWriter;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The binding for a JAXWS operation.
 * <p/>
 * This operation binding can makes the special assumption that its operations conform to one of the following schemes:
 *
 * <ul>
 *   <li>The operation is document/literal BARE.  In this case, the parameters are JAXB root elements and are used
 *       as the in and out messages</li>
 *   <li>The operation is document/literal WRAPPED.  In this case, the operations have request/response beans as defined by
 *       JAXWS.  However, the added constraint for the request/response beans is that they must be
 *       {@link net.sf.enunciate.modules.xfire_client.GeneratedWrapperBean}s so they can be correctly (de)serialized.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public class EnunciatedClientOperationBinding implements MessageSerializer {

  private static final Log LOG = LogFactory.getLog(EnunciatedClientOperationBinding.class);

  private final WrapperBeanInfo requestInfo;
  private final WrapperBeanInfo responseInfo;
  private final ExplicitWebAnnotations annotations;

  /**
   * Construct an operation binding for the specified operation info.  Annotations are needed to
   * determine how to read and write the operation's message.
   *
   * @param annotations The metadata to use for (de)serializing the message.
   * @param op The operation.
   */
  public EnunciatedClientOperationBinding(ExplicitWebAnnotations annotations, OperationInfo op) throws XFireFault {
    this.annotations = annotations;
    this.requestInfo = getRequestInfo(op);
    this.responseInfo = getResponseInfo(op);
  }

  /**
   * Construct an operation binding with the specified metadata.
   *
   * @param annotations The annotations.
   * @param requestInfo The request info.
   * @param responseInfo The response info.
   */
  protected EnunciatedClientOperationBinding(ExplicitWebAnnotations annotations, WrapperBeanInfo requestInfo, WrapperBeanInfo responseInfo) {
    this.annotations = annotations;
    this.requestInfo = requestInfo;
    this.responseInfo = responseInfo;
  }

  /**
   * Loads the set of input properties for the specified operation.
   *
   * @param op The operation.
   * @return The input properties, or null if the request info wasn't found.
   */
  protected WrapperBeanInfo getRequestInfo(OperationInfo op) throws XFireFault {
    Method method = op.getMethod();
    Class ei = method.getDeclaringClass();
    Package pckg = ei.getPackage();

    String requestWrapperClassName;
    RequestWrapperAnnotation requestWrapperInfo = this.annotations.getRequestWrapperAnnotation(method);
    if ((requestWrapperInfo != null) && (requestWrapperInfo.className() != null) && (requestWrapperInfo.className().length() > 0)) {
      requestWrapperClassName = requestWrapperInfo.className();
    }
    else {
      StringBuffer builder = new StringBuffer(pckg == null ? "" : pckg.getName());
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
      LOG.debug("Unabled to find request wrapper class " + requestWrapperClassName + "... Operation " + op.getQName() + " will not be able to send...");
      return null;
    }

    return new WrapperBeanInfo(wrapperClass, loadOrderedProperties(wrapperClass));
  }

  /**
   * Loads the set of output properties for the specified operation.
   *
   * @param op The operation.
   * @return The output properties.
   */
  protected WrapperBeanInfo getResponseInfo(OperationInfo op) throws XFireFault {
    Method method = op.getMethod();
    Class ei = method.getDeclaringClass();
    Package pckg = ei.getPackage();

    String responseWrapperClassName;
    ResponseWrapperAnnotation responseWrapperInfo = annotations.getResponseWrapperAnnotation(method);
    if ((responseWrapperInfo != null) && (responseWrapperInfo.className() != null) && (responseWrapperInfo.className().length() > 0)) {
      responseWrapperClassName = responseWrapperInfo.className();
    }
    else {
      StringBuffer builder = new StringBuffer(pckg == null ? "" : pckg.getName());
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
      LOG.debug("Unabled to find response wrapper class " + responseWrapperClassName + "... Operation " + op.getQName() + " will not be able to recieve...");
      return null;
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
    String[] propOrder = annotations.getPropertyOrder(wrapperClass);
    if (propOrder == null) {
      throw new XFireFault("Unable use use " + wrapperClass.getName() + " as a wrapper class: no propOrder specified.", XFireFault.RECEIVER);
    }

    BeanInfo responseBeanInfo;
    try {
      responseBeanInfo = Introspector.getBeanInfo(wrapperClass, Object.class);
    }
    catch (IntrospectionException e) {
      throw new XFireFault("Unable to introspect " + wrapperClass.getName(), e, XFireFault.RECEIVER);
    }

    PropertyDescriptor[] pds = responseBeanInfo.getPropertyDescriptors();
    PropertyDescriptor[] outputProperties = new PropertyDescriptor[propOrder.length];
    RESPONSE_PROPERTY_LOOP :
    for (int i = 0; i < propOrder.length; i++) {
      String property = propOrder[i];
      for (int j = 0; j < pds.length; j++) {
        PropertyDescriptor descriptor = pds[j];
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
    if (this.responseInfo == null) {
      throw new XFireFault("Message cannot be read: no response info was found.", XFireFault.RECEIVER);
    }

    WrapperBeanInfo wrapperBeanInfo = this.responseInfo;
    Class wrapperClass = wrapperBeanInfo.getWrapperClass();
    Service service = context.getService();
    AegisBindingProvider provider = (AegisBindingProvider) service.getBindingProvider();
    Type type = provider.getType(service, wrapperClass);
    Object wrapper = type.readObject(new ElementReader(message.getXMLStreamReader()), context);
    List parameters = new ArrayList();
    PropertyDescriptor[] pds = wrapperBeanInfo.getProperties();
    for (int i = 0; i < pds.length; i++) {
      PropertyDescriptor descriptor = pds[i];
      try {
        parameters.add(descriptor.getReadMethod().invoke(wrapper, null));
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
    if (this.requestInfo == null) {
      throw new XFireFault("Message cannot be sent: no request info was found.", XFireFault.RECEIVER);
    }

    WrapperBeanInfo wrapperBeanClass = this.requestInfo;
    Class wrapperClass = wrapperBeanClass.getWrapperClass();
    Object wrapper;
    try {
      wrapper = wrapperClass.newInstance();
    }
    catch (Exception e) {
      throw new XFireFault("Problem instantiating response wrapper " + wrapperClass.getName() + ".", e, XFireFault.RECEIVER);
    }

    Object[] params = (Object[]) message.getBody();
    PropertyDescriptor[] properties = wrapperBeanClass.getProperties();
    if (properties.length != params.length) {
      throw new XFireFault("There are " + params.length + " parameters to the out message but only "
        + properties.length + " properties on " + wrapperClass.getName(), XFireFault.RECEIVER);
    }

    for (int i = 0; i < properties.length; i++) {
      PropertyDescriptor descriptor = properties[i];
      try {
        descriptor.getWriteMethod().invoke(wrapper, new Object[] {params[i]});
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
