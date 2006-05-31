package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import net.sf.enunciate.contract.ValidationException;
import net.sf.jelly.apt.decorations.DeclarationDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;

import javax.xml.bind.annotation.*;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public abstract class TypeDefinition extends DecoratedClassDeclaration {

  protected final XmlType xmlType;
  private final Schema schema;

  protected TypeDefinition(ClassDeclaration delegate) {
    super(delegate);

    this.xmlType = getAnnotation(XmlType.class);
    this.schema = new Schema(delegate.getPackage());
  }

  /**
   * The name of the xml type element.
   *
   * @return The name of the xml type element.
   */
  public String getName() {
    String name = getSimpleName();

    if ((xmlType != null) && (!"##default".equals(xmlType.name()))) {
      name = xmlType.name();

      if ("".equals(name)) {
        name = null;
      }
    }

    return name;
  }

  /**
   * The namespace of the xml type element.
   *
   * @return The namespace of the xml type element.
   */
  public String getTargetNamespace() {
    String namespace = getPackage().getNamespace();

    if ((xmlType != null) && (!"##default".equals(xmlType.namespace()))) {
      namespace = xmlType.namespace();
    }

    return namespace;
  }

  /**
   * The default access type for the beans in this package.
   *
   * @return The default access type for the beans in this package.
   */
  public AccessType getAccessType() {
    AccessType accessType = getPackage().getAccessType();

    XmlAccessorType xmlAccessorType = getAnnotation(XmlAccessorType.class);
    if (xmlAccessorType != null) {
      accessType = xmlAccessorType.value();
    }
    else {
      AccessType inheritedAccessType = getInheritedAccessType(this);
      if (inheritedAccessType != null) {
        accessType = inheritedAccessType;
      }
    }

    return accessType;
  }

  /**
   * Get the inherited accessor type of the given class, or null if none is found.
   *
   * @param declaration The inherited accessor type.
   * @return The inherited accessor type of the given class, or null if none is found.
   */
  protected AccessType getInheritedAccessType(ClassDeclaration declaration) {
    ClassType superclass = declaration.getSuperclass();
    if (superclass != null) {
      ClassDeclaration superDeclaration = superclass.getDeclaration();
      if ((superDeclaration != null) && (!Object.class.getName().equals(superDeclaration.getQualifiedName()))) {
        XmlAccessorType xmlAccessorType = superDeclaration.getAnnotation(XmlAccessorType.class);
        if (xmlAccessorType != null) {
          return xmlAccessorType.value();
        }
        else {
          return getInheritedAccessType(superDeclaration);
        }
      }
    }

    return null;
  }

  /**
   * The property order of this xml type.
   *
   * @return The property order of this xml type.
   */
  public String[] getPropertyOrder() {
    String[] propertyOrder = null;

    if (xmlType != null) {
      String[] propOrder = xmlType.propOrder();
      if ((propOrder != null) && (propOrder.length > 0) && ((propOrder.length > 1) || !("".equals(propOrder[0])))) {
        propertyOrder = propOrder;
      }
    }

    return propertyOrder;
  }

  /**
   * The default accessor order of the beans in this package.
   *
   * @return The default accessor order of the beans in this package.
   */
  public AccessorOrder getAccessorOrder() {
    AccessorOrder order = getPackage().getAccessorOrder();

    XmlAccessorOrder xmlAccessorOrder = getAnnotation(XmlAccessorOrder.class);
    if (xmlAccessorOrder != null) {
      order = xmlAccessorOrder.value();
    }

    return order;
  }

  /**
   * The (correctly sorted) accessors for this element.
   *
   * @return The (correctly sorted) accessors for this element.
   */
  public SortedSet<Accessor> getAccessors() {
    AccessorComparator comparator = new AccessorComparator(getPropertyOrder(), getAccessorOrder());
    SortedSet<Accessor> accessors = new TreeSet<Accessor>(comparator);
    AccessorFilter filter = new AccessorFilter(getAccessType());

    //first go through the fields.
    for (FieldDeclaration field : getFields()) {
      if (filter.accept(field)) {
        FieldAccessor accessor = new FieldAccessor(field);
        if (!accessors.add(accessor)) {
          throw new ValidationException(field.getPosition() + ": duplicate accessor name: " + accessor.getPropertyName());
        }
      }
    }

    HashMap<String, PropertyGetter> getters = new HashMap<String, PropertyGetter>();
    HashMap<String, PropertySetter> setters = new HashMap<String, PropertySetter>();
    for (MethodDeclaration method : getMethods()) {
      DecoratedMethodDeclaration decoratedMethod = (DecoratedMethodDeclaration) DeclarationDecorator.decorate(method);

      if (decoratedMethod.isGetter()) {
        PropertyGetter getter = new PropertyGetter(decoratedMethod);
        if (filter.accept(getter)) {
          getters.put(getter.getPropertyName(), getter);
        }
      }
      else if (decoratedMethod.isSetter()) {
        PropertySetter setter = new PropertySetter(decoratedMethod);
        if (filter.accept(setter)) {
          setters.put(setter.getPropertyName(), setter);
        }
      }
    }

    //now iterate through the getters and setters and pair them up....
    Set<String> propertyNames = getters.keySet();
    for (String propertyName : propertyNames) {
      PropertyGetter getter = getters.get(propertyName);
      PropertySetter setter = setters.get(propertyName);
      if (isPaired(getter, setter)) {
        accessors.add(new PropertyAccessor(getter, setter));
      }
    }

    return accessors;
  }

  /**
   * Whether a specified getter and setter are paired.
   *
   * @param getter The getter.
   * @param setter The setter.
   * @return Whether a specified getter and setter are paired.
   */
  protected boolean isPaired(PropertyGetter getter, PropertySetter setter) {
    if ((getter == null) || (setter == null)) {
      //if a getter or setter doesn't have a mirror, it's not a property.
      //todo: should we allow the weird case that says java.util.List doesn't need a setter?
      //todo: should we throw an exception?
      return false;
    }

    if (!getter.getPropertyName().equals(setter.getPropertyName())) {
      return false;
    }

    if (getter.getParameters().size() != 0) {
      return false;
    }

    Collection<ParameterDeclaration> setterParams = setter.getParameters();
    if ((setterParams == null) || (setterParams.size() != 1) || (!getter.getReturnType().equals(setterParams.iterator().next().getType()))) {
      return false;
    }

    return true;
  }

  /**
   * Whether a declaration is xml transient.
   *
   * @param declaration The declaration on which to determine xml transience.
   * @return Whether a declaration is xml transient.
   */
  protected boolean isXmlTransient(Declaration declaration) {
    return (declaration.getAnnotation(XmlTransient.class) != null);
  }

  /**
   * The schema for this complex type.
   *
   * @return The schema for this complex type.
   */
  public Schema getSchema() {
    return schema;
  }

  // Inherited.
  @Override
  public Schema getPackage() {
    return getSchema();
  }

}
