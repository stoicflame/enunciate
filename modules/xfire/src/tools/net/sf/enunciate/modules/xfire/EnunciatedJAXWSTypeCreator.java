package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.aegis.type.DefaultTypeCreator;
import org.codehaus.xfire.aegis.type.Type;

import javax.xml.namespace.QName;
import javax.xml.bind.annotation.XmlRootElement;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.HashMap;

/**
 * A JAXWS Type creator.
 *
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSTypeCreator extends DefaultTypeCreator {

  private final HashMap<String, String[]> parameterNames;

  public EnunciatedJAXWSTypeCreator(HashMap<String, String[]> parameterNames) {
    this.parameterNames = parameterNames;
  }

  /**
   * The element name of the specified part.  From what I can tell, this qname will be used as the qname
   * for header elements and as the name of the RPC/literal web message parts.  It might be used for doc/lit BARE
   * parameters, too.  It won't be needed for the default doc/lit wrapped case as it uses JAXB 2.0 to
   * (de)serialize the request/response beans.
   *
   * @param method The method.
   * @param parameterIndex The index of the part.
   * @return The element name for a web message part.
   */
  public QName getElementName(Method method, int parameterIndex) {
    String name = "arg" + parameterIndex;
    Class endpointInterface = method.getDeclaringClass();
    String namespace = calculateNamespaceURI(endpointInterface);

    //let the @WebService annotation customize the target namespace.
    WebService wsInfo = (WebService) endpointInterface.getAnnotation(WebService.class);
    if (wsInfo != null) {
      if ((wsInfo.targetNamespace() != null) && (!"".equals(wsInfo.targetNamespace()))) {
        namespace = wsInfo.targetNamespace();
      }
    }

    //let the @XmlRootElement annotation customize the name and target namespace.
    Class paramType = parameterIndex < 0 ? method.getReturnType() : method.getParameterTypes()[parameterIndex];
    XmlRootElement rootElementInfo = (XmlRootElement) paramType.getAnnotation(XmlRootElement.class);
    if (rootElementInfo != null) {
      if ((rootElementInfo.namespace() != null) && (!"".equals(rootElementInfo.namespace()))) {
        namespace = rootElementInfo.namespace();
      }

      if ((rootElementInfo.name() != null) && (!"".equals(rootElementInfo.name()))) {
        name = rootElementInfo.name();
      }
    }

    //now let the specific @WebResult and @WebParam annotations customize the qname, too.
    if (parameterIndex < 0) {
      name = "return"; //the spec says "return" is the default element name for return paramters.

      //consult the @WebResult metadata as needed.
      WebResult webResult = method.getAnnotation(WebResult.class);
      if (webResult != null) {
        if ((webResult.partName() != null) && (!"".equals(webResult.partName()))) {
          name = webResult.partName();
        }
        else if ((webResult.name() != null) && (!"".equals(webResult.name()))) {
          name = webResult.name();
        }

        if ((webResult.targetNamespace() != null) && (!"".equals(webResult.targetNamespace()))) {
          namespace = webResult.targetNamespace();
        }
      }
    }
    else {
      //enunciate will use the parameter names before the jax-ws specified "argN".
      String methodName = endpointInterface.getName() + "." + method.getName();
      String[] parameterNames = this.parameterNames.get(methodName);
      if (parameterNames == null) {
        throw new IllegalArgumentException("Unknown web message " + methodName);
      }

      try {
        name = parameterNames[parameterIndex];
      }
      catch (IndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Unknown parameter for method " + methodName + ": " + parameterIndex);
      }

      //finally, consult the @WebParam annotation as needed.
      Annotation[] annotations = method.getParameterAnnotations()[parameterIndex];
      for (Annotation annotation : annotations) {
        if (annotation instanceof WebParam) {
          WebParam webParam = (WebParam) annotation;
          if ((webParam.partName() != null) && (!"".equals(webParam.partName()))) {
            name = webParam.partName();
          }
          else if ((webParam.name() != null) && (!"".equals(webParam.name()))) {
            name = webParam.name();
          }

          if ((webParam.targetNamespace() != null) && (!"".equals(webParam.targetNamespace()))) {
            namespace = webParam.targetNamespace();
          }
        }
      }
    }

    return new QName(namespace, name);
  }

  /**
   * Calculates a namespace URI for a given class.  Default implementation uses the algorithm defined in
   * section 3.2 of the jax-ws spec.
   *
   * @return The calculated namespace uri.
   */
  protected String calculateNamespaceURI(Class clazz) {
    Package pckg = clazz.getPackage();
    if ((pckg == null) || ("".equals(pckg.getName()))) {
      return "";
    }

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

  public Type createType(Method m, int index) {
    java.lang.reflect.Type type = index < 0 ? m.getGenericReturnType() : m.getGenericParameterTypes()[index];
    try {
      return new JAXWSType(type);
    }
    catch (IllegalJAXWSTypeException e) {
      throw new RuntimeException(e);
    }
  }

  public Type createType(PropertyDescriptor pd) {
    throw new UnsupportedOperationException("JAXWS uses JAXB binding, so there shouldn't be any property descriptors for which to create types.");
  }

  public Type createType(Field f) {
    throw new UnsupportedOperationException("JAXWS uses JAXB binding, so there shouldn't be any fields for which to create types.");
  }

  public Type createType(Class clazz) {
    try {
      return new JAXWSType(clazz);
    }
    catch (IllegalJAXWSTypeException e) {
      throw new RuntimeException(e);
    }
  }

}