package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.jaxb2.JaxbType;
import org.codehaus.xfire.jaxws.type.HolderType;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * A special JAXWS Type.  Since it uses JAXB 2.0, it only needs to handle the types that are possible
 * service inputs/outputs.
 *
 * @author Ryan Heaton
 */
public class JAXWSType extends org.codehaus.xfire.aegis.type.Type {

  private final org.codehaus.xfire.aegis.type.Type delegate;

  public JAXWSType(Type type) throws IllegalJAXWSTypeException {
    setWriteOuter(false);

    if (type instanceof Class) {
      Class clazz = (Class) type;
      setTypeClass(clazz);
      this.delegate = new JaxbTypeInternal(clazz);
    }
    else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class)) {
        throw new IllegalJAXWSTypeException();
      }

      Class rawClass = (Class) rawType;
      setTypeClass(rawClass);

      if (Map.class.isAssignableFrom(rawClass)) {
        //todo: support maps.
        throw new IllegalJAXWSTypeException();
      }
      else if (Holder.class.isAssignableFrom(rawClass)) {
        Class holderClass = stripFirstTypeArg(parameterizedType);
        this.delegate = new HolderType(new JaxbTypeInternal(holderClass));
      }
      else if (Collection.class.isAssignableFrom(rawClass)) {
        Class itemClass = stripFirstTypeArg(parameterizedType);
        this.delegate = new JaxbTypeInternal(itemClass);
      }
      else if (rawClass.isArray()) {
        this.delegate = new JaxbTypeInternal(rawClass.getComponentType());
      }
      else {
        this.delegate = new JaxbTypeInternal(rawClass);
      }
    }
    else {
      throw new IllegalJAXWSTypeException();
    }

  }

  @Override
  public void setTypeClass(Class typeClass) {
    super.setTypeClass(typeClass);

    XmlRootElement rootElementInfo = (XmlRootElement) typeClass.getAnnotation(XmlRootElement.class);
    if (rootElementInfo != null) {
      String name = typeClass.getSimpleName();
      if ((rootElementInfo.name() != null) && (!"".equals(rootElementInfo.name()))) {
        name = rootElementInfo.name();
      }
      String namespace = rootElementInfo.namespace();

      setSchemaType(new QName(namespace, name));
      setAbstract(false);
    }
    else {
      setSchemaType(new JaxbType(typeClass).getSchemaType());
      setAbstract(true);
    }
  }

  /**
   * Strips the first type argument from a parameterized type.
   *
   * @param parameterizedType The parameterized type.
   * @return The first type argument.
   * @throws IllegalJAXWSTypeException If the first type arg isn't a class or a parameterized type.
   */
  protected Class stripFirstTypeArg(ParameterizedType parameterizedType) throws IllegalJAXWSTypeException {
    Type[] typeArgs = parameterizedType.getActualTypeArguments();
    Class typeArg;
    if (typeArgs.length == 0) {
      typeArg = Object.class;
    }
    else if (typeArgs[0] instanceof Class) {
      typeArg = (Class) typeArgs[0];
    }
    else if (typeArgs[0] instanceof ParameterizedType) {
      typeArg = (Class) ((ParameterizedType) typeArgs[0]).getRawType();
    }
    else {
      throw new IllegalJAXWSTypeException();
    }
    return typeArg;
  }

  private static class JaxbTypeInternal extends JaxbType {
    public JaxbTypeInternal(Class clazz) {
      super(clazz);
      setAbstract(false);
    }

    @Override
    public void initType() {
      //no-op
    }
  }

  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    return this.delegate.readObject(reader, context);
  }

  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    this.delegate.writeObject(object, writer, context);
  }

}
