package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.jaxb2.AttachmentMarshaller;
import org.codehaus.xfire.jaxb2.AttachmentUnmarshaller;
import org.codehaus.xfire.jaxws.type.HolderType;
import org.codehaus.xfire.service.MessagePartInfo;
import org.codehaus.xfire.transport.Channel;
import org.codehaus.xfire.util.stax.DOMStreamWriter;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.Holder;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.util.*;

/**
 * A special JAXWS Type.  Since it uses JAXB 2.0, it only needs to handle the types that are possible
 * service inputs/outputs.
 *
 * @author Ryan Heaton
 */
public class JAXWSType extends org.codehaus.xfire.aegis.type.Type {

  /**
   * The delegate is used to "unwrap" things like collections and holders.
   */
  private final org.codehaus.xfire.aegis.type.Type delegate;
  private JAXBContext jaxbContext;
  private boolean collection = false;

  public JAXWSType(Type type) {
    if (type instanceof Class) {
      Class clazz = (Class) type;
      setTypeClass(clazz);
      if (clazz.isArray()) {
        this.delegate = new JAXWSType(clazz.getComponentType());
        this.collection = true;
      }
      else {
        this.delegate = null;
      }
    }
    else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class)) {
        throw new XFireRuntimeException("Illegal JAXWS type: " + rawType);
      }

      Class rawClass = (Class) rawType;
      setTypeClass(rawClass);

      if (Map.class.isAssignableFrom(rawClass)) {
        //todo: support maps.
        throw new XFireRuntimeException("Enunciate doesn't yet support maps.");
      }
      else if (Holder.class.isAssignableFrom(rawClass)) {
        this.delegate = new HolderType(new JAXWSType(stripFirstTypeArg(parameterizedType)));
      }
      else if (Collection.class.isAssignableFrom(rawClass)) {
        this.delegate = new JAXWSType(stripFirstTypeArg(parameterizedType));
        this.collection = true;
      }
      else if (rawClass.isArray()) {
        this.delegate = new JAXWSType(rawClass.getComponentType());
        this.collection = true;
      }
      else {
        this.delegate = null;
      }
    }
    else if (type instanceof GenericArrayType) {
      this.delegate = new JAXWSType(((GenericArrayType) type).getGenericComponentType());
      this.collection = true;
    }
    else if (type instanceof TypeVariable) {
      Type[] bounds = ((TypeVariable) type).getBounds();
      if ((bounds == null) || (bounds.length == 0)) {
        setTypeClass(Object.class);
        this.delegate = null;
      }
      else {
        this.delegate = new JAXWSType(bounds[0]);
      }
    }
    else if (type instanceof WildcardType) {
      Type[] bounds = ((WildcardType) type).getUpperBounds();
      if ((bounds == null) || (bounds.length == 0)) {
        setTypeClass(Object.class);
        this.delegate = null;
      }
      else {
        this.delegate = new JAXWSType(bounds[0]);
      }
    }
    else {
      throw new XFireRuntimeException("Unknown JAXWS type: " + type);
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
      setAbstract(true);
    }
  }

  /**
   * Strips the first type argument from a parameterized type.
   *
   * @param parameterizedType The parameterized type.
   * @return The first type argument.
   */
  protected Type stripFirstTypeArg(ParameterizedType parameterizedType) {
    Type[] typeArgs = parameterizedType.getActualTypeArguments();
    if (typeArgs.length == 0) {
      return Object.class;
    }
    else {
      return typeArgs[0];
    }
  }

  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    if (this.delegate != null) {
      if (collection) {
        Collection collection = newCollection();
        QName firstItemName = reader.getName();
        while (firstItemName.equals(reader.getName())) {
          collection.add(this.delegate.readObject(reader, context));
        }

        if (getTypeClass().isArray()) {
          if (collection.size() > 0) {
            Object first = collection.iterator().next();
            if (first != null) {
              Object[] array = (Object[]) Array.newInstance(first.getClass(), 0);
              return collection.toArray(array);
            }
          }

          return Array.newInstance(getTypeClass().getComponentType(), 0);
        }
        else {
          return collection;
        }
      }
      else {
        return this.delegate.readObject(reader, context);
      }
    }
    else {
      try {
        Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
        unmarshaller.setAttachmentUnmarshaller(new AttachmentUnmarshaller(context));
        Object result;
        if (isAbstract()) {
          result = unmarshaller.unmarshal(reader.getXMLStreamReader(), getTypeClass());
        }
        else {
          result = unmarshaller.unmarshal(reader.getXMLStreamReader());
        }

        if (result instanceof JAXBElement) {
          result = ((JAXBElement) result).getValue();
        }

        return result;
      }
      catch (JAXBException e) {
        throw new XFireRuntimeException("Unable to unmarshal type.", e);
      }
    }
  }

  protected Collection newCollection() {
    if (getTypeClass().isArray()) {
      return new ArrayList();
    }
    else if (!(Modifier.isAbstract(getTypeClass().getModifiers()) || (Modifier.isInterface(getTypeClass().getModifiers())))) {
      try {
        return (Collection) getTypeClass().newInstance();
      }
      catch (Exception e) {
        //fall through.. try something else.
      }
    }

    if (SortedSet.class.isAssignableFrom(getTypeClass())) {
      return new TreeSet();
    }
    else if (Set.class.isAssignableFrom(getTypeClass())) {
      return new HashSet();
    }
    else {
      return new ArrayList();
    }
  }

  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    if (this.delegate != null) {
      if (collection) {
        for (Object item : ((Collection) object)) {
          this.delegate.writeObject(item, writer, context);
        }
      }
      else {
        this.delegate.writeObject(object, writer, context);
      }
    }
    else {
      try {
        Marshaller marshaller = getJAXBContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.setAttachmentMarshaller(new AttachmentMarshaller(context));

        if (isAbstract()) {
          //if we're abstract, it's assumed we're in a RPC/literal message part or a header message part...
          MessagePartInfo part = (MessagePartInfo) context.getProperty(AegisBindingProvider.CURRENT_MESSAGE_PART);
          object = new JAXBElement(part.getName(), getTypeClass(), object);
        }

        XMLStreamWriter streamWriter = ((ElementWriter) writer).getXMLStreamWriter();
        OutputStream os = (OutputStream) context.getOutMessage().getProperty(Channel.OUTPUTSTREAM);
        if (os != null && !(streamWriter instanceof DOMStreamWriter)) {
          streamWriter.writeCharacters("");
          streamWriter.flush();
          marshaller.setProperty(Marshaller.JAXB_ENCODING, context.getOutMessage().getEncoding());
          marshaller.marshal(object, os);
        }
        else {
          marshaller.marshal(object, streamWriter);
        }

      }
      catch (JAXBException e) {
        throw new XFireRuntimeException("Unable to marshal type.", e);
      }
      catch (XMLStreamException e) {
        throw new XFireRuntimeException("Unable to marshal type.", e);
      }
    }
  }

  /**
   * Gets the JAXB context, or creates it if not created yet.
   *
   * @return The JAXB context.
   */
  protected JAXBContext getJAXBContext() throws JAXBException {
    if (this.jaxbContext == null) {
      this.jaxbContext = JAXBContext.newInstance(getTypeClass());
    }

    return this.jaxbContext;
  }

}
