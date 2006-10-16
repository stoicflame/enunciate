package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

/**
 * An XFire type for a generated wrapper bean.
 *
 * @author Ryan Heaton
 */
public class GeneratedWrapperBeanType extends Type {

  private final Class beanClass;
  private final PropertyDescriptor[] beanProperties;

  public GeneratedWrapperBeanType(Class beanType) {
    this.beanClass = beanType;

    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(beanType);
    }
    catch (IntrospectionException e) {
      throw new XFireRuntimeException("Error introspecting " + beanType.getName(), e);
    }

    beanProperties = beanInfo.getPropertyDescriptors();
  }

  // Inherited.
  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    Object instance;
    try {
      instance = beanClass.newInstance();
    }
    catch (Exception e) {
      throw new XFireFault("Unable to instantiate the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
    }

    while (reader.hasMoreElementReaders()) {
      MessageReader elementReader = reader.getNextElementReader();
      String elementName = elementReader.getLocalName();
      PropertyDescriptor pd = findProperty(elementName);
      if (pd != null) {
        Method getter = pd.getReadMethod();
        Class propertyType = getter.getReturnType();
        if (propertyType.isArray()) {
          Object item = getTypeMapping().getType(propertyType.getComponentType()).readObject(elementReader, context);
          try {
            Method addTo = getAddToMethod(pd.getName());
            addTo.invoke(instance, new Object[] {item});
          }
          catch (Exception e) {
            throw new XFireFault("Unable to add to property " + pd.getName() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
          }
        }
        else if (Collection.class.isAssignableFrom(propertyType)) {
          try {
            Method addTo = getAddToMethod(pd.getName());
            Class componentType = addTo.getParameterTypes()[0];
            Object item = getTypeMapping().getType(componentType).readObject(elementReader, context);
            addTo.invoke(instance, new Object[] {item});
          }
          catch (Exception e) {
            throw new XFireFault("Unable to add to property " + pd.getName() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
          }
        }
        else {
          Object value = getTypeMapping().getType(propertyType).readObject(elementReader, context);

          try {
            pd.getWriteMethod().invoke(instance, new Object[] {value});
          }
          catch (Exception e) {
            throw new XFireFault("Unable to invoke " + pd.getWriteMethod() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
          }
        }
      }
    }

    return instance;
  }

  /**
   * Get the "addTo" method for the specified property.
   *
   * @param propertyName The property name.
   * @return The addToMethod
   */
  protected Method getAddToMethod(String propertyName) throws NoSuchMethodException {
    propertyName = Character.toString(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1);
    Method[] methods = beanClass.getMethods();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      if (("addTo" + propertyName).equals(method.getName())) {
        return method;
      }
    }

    throw new NoSuchMethodException();
  }

  /**
   * Finds a property by name.
   *
   * @param name The name of the property to find.
   * @return The found property.
   */
  protected PropertyDescriptor findProperty(String name) {
    for (int i = 0; i < beanProperties.length; i++) {
      PropertyDescriptor property = beanProperties[i];
      if (property.getName().equals(name)) {
        return property;
      }
    }

    return null;
  }

  // Inherited.
  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    QName wrapperQName = ((GeneratedWrapperBean) object).getWrapperQName();
    MessageWriter elementWriter = writer.getElementWriter(wrapperQName);
    for (int n = 0; n < beanProperties.length; n++) {
      PropertyDescriptor property = beanProperties[n];
      Method getter = property.getReadMethod();
      Object propertyValue;
      try {
        propertyValue = getter.invoke(object, null);
      }
      catch (Exception e) {
        throw new XFireFault("Unable to invoke " + getter.getName() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
      }

      if (propertyValue != null) {
        Class propertyType = getter.getReturnType();
        if (propertyType.isArray()) {
          Class componentType = propertyType.getComponentType();
          Type type = getTypeMapping().getType(componentType);
          int length = Array.getLength(propertyValue);
          for (int i = 0; i < length; i++) {
            Object item = Array.get(propertyValue, i);
            type.writeObject(item, elementWriter.getElementWriter(wrapperQName.getNamespaceURI(), property.getName()), context);
          }
        }
        else if (Collection.class.isAssignableFrom(propertyType)) {
          Class componentType = null;
          try {
            componentType = getAddToMethod(property.getName()).getParameterTypes()[0];
          }
          catch (NoSuchMethodException e) {
            throw new XFireFault("Unable to map property " + property.getName() + " for the wrapper bean: unknown component type." + beanClass.getName(), e, XFireFault.RECEIVER);
          }
          Type type = getTypeMapping().getType(componentType);

          Iterator it = ((Collection) object).iterator();
          while (it.hasNext()) {
            Object item = it.next();
            type.writeObject(item, elementWriter.getElementWriter(wrapperQName.getNamespaceURI(), property.getName()), context);
          }
        }
        else {
          Type type = getTypeMapping().getType(propertyType);
          type.writeObject(propertyValue, elementWriter.getElementWriter(wrapperQName.getNamespaceURI(), property.getName()), context);
        }
      }
    }
  }

}
