package net.sf.enunciate.modules.xfire_client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.TypeCreator;
import org.codehaus.xfire.aegis.type.TypeMapping;

import javax.xml.namespace.QName;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * An XFire type creator that will introspect on the desired class to look up its XFire type.
 * <p/>
 * If any type (e.g. "Foo") has a corresponding "XFireType" class (e.g. "FooXFireType"), an instance will
 * be returned.  Otherwise, the default XFire type for the class will be returned.
 *
 * @author Ryan Heaton
 */
public class IntrospectingTypeCreator implements TypeCreator {

  private static final Log LOG = LogFactory.getLog(IntrospectingTypeCreator.class);
  private static final Map INTROSPECTED_TYPES = Collections.synchronizedMap(new WeakHashMap());

  private final TypeCreator defaultDelegate;

  /**
   * Create a new introspecting type creator with the specified delegate to use for defaults.
   *
   * @param defaultDelegate The default delegate.
   */
  public IntrospectingTypeCreator(TypeCreator defaultDelegate) {
    this.defaultDelegate = defaultDelegate;
  }

  /**
   * Get the suggested qname of the element for the parameter at index <code>index</code> for method <code>method</code>.
   *
   * @param method The method.
   * @param index  The parameter index.
   * @return The suggested qname.
   */
  public QName getElementName(Method method, int index) {
    //todo: implement this better?
    return this.defaultDelegate.getElementName(method, index);
  }

  /**
   * Look up the type for the parameter at index <code>index</code> for method <code>method</code>.
   *
   * @param method The method.
   * @param index  The index.
   * @return The type.
   */
  public Type createType(Method method, int index) {
    Class clazz;
    if (index > -1) {
      clazz = method.getParameterTypes()[index];
    }
    else {
      clazz = method.getReturnType();
    }

    return createType(clazz);
  }

  /**
   * Looks up the type for the property.
   *
   * @param property The property.
   * @return The type.
   */
  public Type createType(PropertyDescriptor property) {
    return createType(property.getPropertyType());
  }

  /**
   * Looks up the type for the field.
   *
   * @param field The field.
   * @return The type.
   */
  public Type createType(Field field) {
    return createType(field.getType());
  }

  /**
   * Looks up the type for the specified class.
   *
   * @param clazz The class.
   * @return The type.
   */
  public Type createType(Class clazz) {
    Type type;
    if (GeneratedWrapperBean.class.isAssignableFrom(clazz)) {

    }
    else {
      type = introspectForType(clazz);
    }

    return type != null ? type : this.defaultDelegate.createType(clazz);
  }

  /**
   * Just a delegate method.
   */
  public void setTypeMapping(TypeMapping typeMapping) {
    this.defaultDelegate.setTypeMapping(typeMapping);
  }

  /**
   * Introspects for the type associated with <code>clazz</code>.
   *
   * @param clazz The class.
   * @return The class for the type, or null if non found.
   */
  protected Type introspectForType(Class clazz) {
    if (INTROSPECTED_TYPES.containsKey(clazz)) {
      return (Type) INTROSPECTED_TYPES.get(clazz);
    }

    try {
      Class typeClass = Class.forName(clazz.getName() + "XFireType");

      if (!Type.class.isAssignableFrom(typeClass)) {
        LOG.error(clazz.getName() + "XFireType isn't an instanceof " + Type.class.getName());
        return null;
      }

      Type type = (Type) typeClass.newInstance();
      INTROSPECTED_TYPES.put(clazz, type);
      return type;
    }
    catch (ClassNotFoundException e) {
      return null;
    }
    catch (IllegalAccessException e) {
      LOG.error("Unable to instantiate type for " + clazz.getName() + ".", e);
      return null;
    }
    catch (InstantiationException e) {
      LOG.error("Unable to instantiate type for " + clazz.getName() + ".", e);
      return null;
    }
  }
}
