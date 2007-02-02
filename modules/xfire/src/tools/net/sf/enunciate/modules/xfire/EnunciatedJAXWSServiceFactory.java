package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.annotations.AnnotationException;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.annotations.WebAnnotations;
import org.codehaus.xfire.annotations.WebServiceAnnotation;
import org.codehaus.xfire.annotations.jsr181.Jsr181WebAnnotations;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.fault.FaultSender;
import org.codehaus.xfire.handler.OutMessageSender;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.PostInvocationHandler;
import org.codehaus.xfire.service.binding.ServiceInvocationHandler;
import org.codehaus.xfire.soap.AbstractSoapBinding;
import org.codehaus.xfire.soap.SoapConstants;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.lang.reflect.Method;
import java.beans.Introspector;
import java.util.Map;

/**
 * The enunciate implementation of the JAXWS service factory.
 *
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSServiceFactory extends AnnotationServiceFactory {

  /**
   * An annotation service factory that is initialized with the {@link org.codehaus.xfire.annotations.jsr181.Jsr181WebAnnotations} and
   * the default transport manager.
   */
  public EnunciatedJAXWSServiceFactory() {
    super(new Jsr181WebAnnotations(),
          XFireFactory.newInstance().getXFire().getTransportManager());
  }


  /**
   * Ensures that any service created has MTOM enabled.
   * 
   * @param clazz The class.
   * @param name The name.
   * @param namespace The namespace.
   * @param properties The properties.
   * @return The service.
   */
  @Override
  public Service create(final Class clazz, String name, String namespace, Map properties) {
    Service service = super.create(clazz, name, namespace, properties);
    service.setProperty(SoapConstants.MTOM_ENABLED, Boolean.TRUE.toString());
    return service;
  }

  /**
   * The handlers for a service include the defaults, with the exception of the {@link net.sf.enunciate.modules.xfire.EnunciatedJAXWSWebFaultHandler}.
   *
   * @param service The service for which to register the handlers.
   */
  protected void registerHandlers(Service service) {
    service.addInHandler(new ServiceInvocationHandler());
    service.addInHandler(new PostInvocationHandler());
    service.addOutHandler(new OutMessageSender());
    service.addFaultHandler(new FaultSender());
    service.addFaultHandler(new EnunciatedJAXWSWebFaultHandler());
  }

  /**
   * The service name according to the JAXWS specification is looked up with the metadata on the endpoint
   * interface and defaults to the simple name of the endpoint interface + "Service" if not specified.
   *
   * @param clazz      The class for which to lookup the service name.
   * @param annotation The relevant annotation to use.
   * @param current    A suggested name (ignored).
   * @return The service name.
   */
  @Override
  protected String createServiceName(Class clazz, WebServiceAnnotation annotation, String current) {
    WebAnnotations webAnnotations = getAnnotations();
    Class endpointInterface = clazz;
    String eiValue = annotation.getEndpointInterface();
    if (eiValue != null && eiValue.length() > 0) {
      //the metadata is supplied on another class...
      try {
        endpointInterface = loadClass(annotation.getEndpointInterface());
        if (!webAnnotations.hasWebServiceAnnotation(endpointInterface)) {
          throw new AnnotationException("Endpoint interface " + endpointInterface.getName() + " does not have a WebService annotation");
        }

        WebServiceAnnotation eiAnnotation = webAnnotations.getWebServiceAnnotation(endpointInterface);
        String serviceName = eiAnnotation.getServiceName();
        if ((serviceName != null) && (serviceName.length() > 0)) {
          return serviceName;
        }
      }
      catch (ClassNotFoundException e) {
        throw new AnnotationException("Couldn't find endpoint interface " + annotation.getEndpointInterface(), e);
      }
    }
    else {
      String serviceName = annotation.getServiceName();
      if ((serviceName != null) && (serviceName.length() > 0)) {
        return serviceName;
      }
    }

    return endpointInterface.getSimpleName() + "Service";
  }


  @Override
  public WebAnnotations getAnnotations() {
    return super.getAnnotations();
  }

  /**
   * The faults don't need to be initialized.  The {@link net.sf.enunciate.modules.xfire.EnunciatedJAXWSWebFaultHandler}
   * will handle it.
   *
   * @param service The service.
   * @param op      The operation.
   */
  @Override
  protected void initializeFaults(final Service service, final OperationInfo op) {
    //no-op....
  }

  /**
   * The serializer for a SOAP message.  For enunciated it is a {@link net.sf.enunciate.modules.xfire.EnunciatedJAXWSMessageBinding}.
   *
   * @param binding The binding.
   * @return The default serializer for the binding.
   */
  @Override
  protected MessageSerializer getSerializer(AbstractSoapBinding binding) {
    return new EnunciatedJAXWSMessageBinding();
  }

  /**
   * The input message name depends on the metadata of the operation.  If the operation is rpc/lit, the
   * input message name is the operation name.  If the operation is doc/lit bare, the input message name
   * is the element name of the input parameter.  Otherwise (doc/lit wrapped), the input message name
   * is wrapped with the wrapper element, as described in the JAXWS specification.
   *
   * @param op The operation for which to determine the input message name.
   * @return The input message name.
   */
  @Override
  protected QName createInputMessageName(OperationInfo op) {
    Method method = op.getMethod();
    Class ei = method.getDeclaringClass();
    SOAPBinding.Style style = SOAPBinding.Style.DOCUMENT;
    SOAPBinding.ParameterStyle paramStyle = SOAPBinding.ParameterStyle.WRAPPED;

    if (method.isAnnotationPresent(SOAPBinding.class)) {
      SOAPBinding annotation = method.getAnnotation(SOAPBinding.class);
      style = annotation.style();
      paramStyle = annotation.parameterStyle();
    }
    else if (ei.isAnnotationPresent(SOAPBinding.class)) {
      SOAPBinding annotation = ((SOAPBinding) ei.getAnnotation(SOAPBinding.class));
      style = annotation.style();
      paramStyle = annotation.parameterStyle();
    }

    if (style == SOAPBinding.Style.RPC) {
      //if it's an rpc-style method call, the message name is the operation name.
      String namespace = ((WebService) ei.getAnnotation(WebService.class)).targetNamespace();
      if ("".equals(namespace)) {
        namespace = calculateNamespaceURI(ei);
      }

      String operationName = method.getName();
      if (method.isAnnotationPresent(WebMethod.class)) {
        WebMethod annotation = method.getAnnotation(WebMethod.class);
        if (annotation.operationName().length() > 0) {
          operationName = annotation.operationName();
        }
      }

      return new QName(namespace, operationName);
    }
    else if (paramStyle == SOAPBinding.ParameterStyle.BARE) {
      // for a bare parameter style, the message name of the name of the
      // xml root element that must be the first (and only) parameter.
      Class bareType = method.getParameterTypes()[0];
      XmlRootElement rootElement = (XmlRootElement) bareType.getAnnotation(XmlRootElement.class);
      if (rootElement == null) {
        throw new XFireRuntimeException("Unable to create the message name for " + method + ": " + bareType.getName() + " is not a root element!");
      }

      String namespace = rootElement.namespace();
      if ("##default".equals(namespace)) {
        namespace = "";
        Package pckg = bareType.getPackage();
        if ((pckg != null) && (pckg.isAnnotationPresent(XmlSchema.class))) {
          namespace = pckg.getAnnotation(XmlSchema.class).namespace();
        }
      }

      String name = rootElement.name();
      if ("##default".equals(name)) {
        name = Introspector.decapitalize(bareType.getSimpleName());
      }

      return new QName(namespace, name);
    }
    else {
      //default doc/lit behavior.
      String namespace = ((WebService) ei.getAnnotation(WebService.class)).targetNamespace();
      if ("".equals(namespace)) {
        namespace = calculateNamespaceURI(ei);
      }

      String name = method.getName();
      if (method.isAnnotationPresent(RequestWrapper.class)) {
        RequestWrapper wrapper = method.getAnnotation(RequestWrapper.class);

        if (!"".equals(wrapper.targetNamespace())) {
          namespace = wrapper.targetNamespace();
        }

        if (!"".equals(wrapper.localName())) {
          name = wrapper.localName();
        }
      }

      return new QName(namespace, name);
    }
  }

  @Override
  protected QName createOutputMessageName(OperationInfo op) {
    Method method = op.getMethod();
    Class ei = method.getDeclaringClass();
    SOAPBinding.Style style = SOAPBinding.Style.DOCUMENT;
    SOAPBinding.ParameterStyle paramStyle = SOAPBinding.ParameterStyle.WRAPPED;

    if (method.isAnnotationPresent(SOAPBinding.class)) {
      SOAPBinding annotation = method.getAnnotation(SOAPBinding.class);
      style = annotation.style();
      paramStyle = annotation.parameterStyle();
    }
    else if (ei.isAnnotationPresent(SOAPBinding.class)) {
      SOAPBinding annotation = ((SOAPBinding) ei.getAnnotation(SOAPBinding.class));
      style = annotation.style();
      paramStyle = annotation.parameterStyle();
    }

    if (style == SOAPBinding.Style.RPC) {
      //if it's an rpc-style method call, the message name is the operation name.
      String namespace = ((WebService) ei.getAnnotation(WebService.class)).targetNamespace();
      if ("".equals(namespace)) {
        namespace = calculateNamespaceURI(ei);
      }

      String operationName = method.getName();
      if (method.isAnnotationPresent(WebMethod.class)) {
        WebMethod annotation = method.getAnnotation(WebMethod.class);
        if (annotation.operationName().length() > 0) {
          operationName = annotation.operationName();
        }
      }

      return new QName(namespace, operationName);
    }
    else if (paramStyle == SOAPBinding.ParameterStyle.BARE) {
      // for a bare parameter style, the message name of the name of the
      // xml root element that must be the return type.
      Class bareType = method.getReturnType();
      XmlRootElement rootElement = (XmlRootElement) bareType.getAnnotation(XmlRootElement.class);
      if (rootElement == null) {
        throw new XFireRuntimeException("Unable to create the message name for " + method + ": " + bareType.getName() + " is not a root element!");
      }

      String namespace = rootElement.namespace();
      if ("##default".equals(namespace)) {
        namespace = "";
        Package pckg = bareType.getPackage();
        if ((pckg != null) && (pckg.isAnnotationPresent(XmlSchema.class))) {
          namespace = pckg.getAnnotation(XmlSchema.class).namespace();
        }
      }

      String name = rootElement.name();
      if ("##default".equals(name)) {
        name = Introspector.decapitalize(bareType.getSimpleName());
      }

      return new QName(namespace, name);
    }
    else {
      //default doc/lit behavior.
      String namespace = ((WebService) ei.getAnnotation(WebService.class)).targetNamespace();
      if ("".equals(namespace)) {
        namespace = calculateNamespaceURI(ei);
      }

      String name = method.getName();
      if (method.isAnnotationPresent(ResponseWrapper.class)) {
        ResponseWrapper wrapper = method.getAnnotation(ResponseWrapper.class);

        if (!"".equals(wrapper.targetNamespace())) {
          namespace = wrapper.targetNamespace();
        }

        if (!"".equals(wrapper.localName())) {
          name = wrapper.localName();
        }
      }

      return new QName(namespace, name);
    }
  }

  /**
   * Overridden to fix a bug in XFire.
   *
   * @param method The method.
   * @param paramIndex The parameter index.
   * @return Whether the parameter index is an out param.
   */
  @Override
  protected boolean isOutParam(Method method, int paramIndex) {
    //xfire 1.2.1 chokes on -1...
    return paramIndex == -1 || super.isOutParam(method, paramIndex);
  }

  /**
   * Overridden to fix a bug in XFire.
   *
   * @param method The method.
   * @param paramIndex The parameter index.
   * @return Whether the parameter index is an in param.
   */
  @Override
  protected boolean isInParam(Method method, int paramIndex) {
    //xfire 1.2.1 chokes on -1...
    return paramIndex != -1 && super.isInParam(method, paramIndex);
  }

  /**
   * XFire defaults the name of the return value to "out".  The JAXWS spec says "return"....
   *
   * @param service The service.
   * @param op The operation.
   * @param method The method.
   * @param paramNumber The parameter number.
   * @param doc Whether its doc-style binding.
   * @return The out parameter name.
   */
  @Override
  protected QName getOutParameterName(final Service service, final OperationInfo op, final Method method, final int paramNumber, final boolean doc) {
    QName parameterName = super.getOutParameterName(service, op, method, paramNumber, doc);

    if (paramNumber == -1) {
      WebResult webResult = method.getReturnType().getAnnotation(WebResult.class);
      if ((webResult == null) || ("".equals(webResult.name()))) {
        parameterName = new QName(parameterName.getNamespaceURI(), "return");
      }
    }

    return parameterName;
  }

  /**
   * Calculates a namespace URI for a given package.  Default implementation uses the algorithm defined in
   * section 3.2 of the jax-ws spec.
   *
   * @param jaxwsClass The class for which to calculate the namespace based on the JAXWS spec.
   * @return The calculated namespace uri.
   */
  protected String calculateNamespaceURI(Class jaxwsClass) {
    Package pckg = jaxwsClass.getPackage();
    String[] tokens = pckg.getName().split("\\.");
    String uri = "http://";
    for (int i = tokens.length - 1; i >= 0; i--) {
      uri += tokens[i];
      if (i != 0) {
        uri += ".";
      }
    }
    uri += "/";
    return uri;
  }

}
