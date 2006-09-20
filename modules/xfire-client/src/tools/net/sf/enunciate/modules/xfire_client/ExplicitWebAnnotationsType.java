package net.sf.enunciate.modules.xfire_client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.basic.BeanType;
import org.codehaus.xfire.aegis.type.basic.StringType;
import org.codehaus.xfire.aegis.type.collection.CollectionType;
import org.codehaus.xfire.aegis.type.collection.MapType;
import org.codehaus.xfire.annotations.*;
import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Set;

/**
 * Type for explicit web annotations (since they need to be serialized).
 *
 * @author Ryan Heaton
 */
class ExplicitWebAnnotationsType extends Type {

  private static final Log LOG = LogFactory.getLog(ExplicitWebAnnotationsType.class);

  protected static final String NAMESPACE = "urn:explicit-annotations";

  public ExplicitWebAnnotationsType() {
    setTypeMapping(new DefaultTypeMappingRegistry(true).createTypeMapping(true));
  }

  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    while (reader.hasMoreElementReaders()) {
      MessageReader childReader = reader.getNextElementReader();
      if (("class2WebService".equals(childReader.getLocalName())) && (NAMESPACE.equals(childReader.getNamespace()))) {
        Type childType = getTypeMapping().getType(annotations.class2WebService.getClass());
        annotations.class2WebService = (Map) childType.readObject(childReader, context);
      }
      else if (("class2SOAPBinding".equals(childReader.getLocalName())) && (NAMESPACE.equals(childReader.getNamespace()))) {
        Type childType = getTypeMapping().getType(annotations.class2SOAPBinding.getClass());
        annotations.class2SOAPBinding = (Map) childType.readObject(childReader, context);
      }
      else if (("class2HandlerChain".equals(childReader.getLocalName())) && (NAMESPACE.equals(childReader.getNamespace()))) {
        Type childType = getTypeMapping().getType(annotations.class2HandlerChain.getClass());
        annotations.class2HandlerChain = (Map) childType.readObject(childReader, context);
      }
      else if (("method2WebMethod".equals(childReader.getLocalName())) && (NAMESPACE.equals(childReader.getNamespace()))) {
        Type childType = getTypeMapping().getType(annotations.method2WebMethod.getClass());
        annotations.method2WebMethod = (Map) childType.readObject(childReader, context);
      }
      else if (("method2WebResult".equals(childReader.getLocalName())) && (NAMESPACE.equals(childReader.getNamespace()))) {
        Type childType = getTypeMapping().getType(annotations.method2WebResult.getClass());
        annotations.method2WebResult = (Map) childType.readObject(childReader, context);
      }
      else if (("method2WebParam".equals(childReader.getLocalName())) && (NAMESPACE.equals(childReader.getNamespace()))) {
        Type childType = getTypeMapping().getType(annotations.method2WebParam.getClass());
        annotations.method2WebParam = (Map) childType.readObject(childReader, context);
      }
      else if (("oneWayMethods".equals(childReader.getLocalName())) && (NAMESPACE.equals(childReader.getNamespace()))) {
        Type childType = getTypeMapping().getType(annotations.oneWayMethods.getClass());
        annotations.oneWayMethods = (Set) childType.readObject(childReader, context);
      }
      else {
        LOG.error("Unknown child element for explicit web annotations (ignoring): " + childReader.getName());
      }
    }
    return annotations;
  }

  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    ExplicitWebAnnotations annotations = (ExplicitWebAnnotations) object;

    MessageWriter childWriter = writer.getElementWriter("class2WebService", NAMESPACE);
    Type type = new MapType(new QName(NAMESPACE, "class2WebService"), new StringType(), createBeanType(WebServiceAnnotation.class));
    type.writeObject(annotations.class2WebService, childWriter, context);

    childWriter = writer.getElementWriter("class2SOAPBinding", NAMESPACE);
    type = new MapType(new QName(NAMESPACE, "class2SOAPBinding"), new StringType(), createBeanType(SOAPBindingAnnotation.class));
    type.writeObject(annotations.class2SOAPBinding, childWriter, context);

    childWriter = writer.getElementWriter("class2HandlerChain", NAMESPACE);
    type = new MapType(new QName(NAMESPACE, "class2HandlerChain"), new StringType(), createBeanType(HandlerChainAnnotation.class));
    type.writeObject(annotations.class2HandlerChain, childWriter, context);

    childWriter = writer.getElementWriter("method2WebMethod", NAMESPACE);
    type = new MapType(new QName(NAMESPACE, "method2WebMethod"), new StringType(), createBeanType(WebMethodAnnotation.class));
    type.writeObject(annotations.method2WebMethod, childWriter, context);

    childWriter = writer.getElementWriter("method2WebResult", NAMESPACE);
    type = new MapType(new QName(NAMESPACE, "method2WebResult"), new StringType(), createBeanType(WebResultAnnotation.class));
    type.writeObject(annotations.method2WebResult, childWriter, context);

    childWriter = writer.getElementWriter("method2WebParam", NAMESPACE);
    type = new MapType(new QName(NAMESPACE, "method2WebParam"), new StringType(), createBeanType(WebParamAnnotation.class));
    type.writeObject(annotations.method2WebParam, childWriter, context);

    childWriter = writer.getElementWriter("oneWayMethods", NAMESPACE);
    type = new CollectionType(new StringType());
    type.writeObject(annotations.oneWayMethods, childWriter, context);

  }

  /**
   * Create a bean type for the specified class.
   *
   * @param clazz The class.
   * @return The bean type for the specified class.
   */
  protected BeanType createBeanType(Class clazz) {
    BeanType beanType = new BeanType();
    beanType.setTypeMapping(getTypeMapping());
    beanType.setSchemaType(new QName(NAMESPACE, clazz.getSimpleName()));
    beanType.setTypeClass(clazz);
    return beanType;
  }

}
