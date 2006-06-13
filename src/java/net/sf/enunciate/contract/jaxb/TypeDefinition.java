package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.type.ClassType;
import net.sf.enunciate.contract.ValidationException;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

import javax.xml.bind.annotation.*;
import java.util.*;

/**
 * A xml type definition.
 *
 * @author Ryan Heaton
 */
public class TypeDefinition extends DecoratedClassDeclaration {

  protected final XmlType xmlType;
  private final Schema schema;

  private SortedSet<ElementAccessor> elements;
  private Collection<AttributeAccessor> attributes;
  private ValueAccessor xmlValue;

  protected TypeDefinition(ClassDeclaration delegate) {
    super(delegate);

    this.xmlType = getAnnotation(XmlType.class);
    this.schema = new Schema(delegate.getPackage());
    init();
  }

  /**
   * Initialize the type definition.
   */
  protected void init() {
    ElementAccessorComparator comparator = new ElementAccessorComparator(getPropertyOrder(), getAccessorOrder());
    AccessorFilter filter = new AccessorFilter(getAccessType());
    SortedSet<ElementAccessor> elementAccessors = new TreeSet<ElementAccessor>(comparator);
    Collection<AttributeAccessor> attributeAccessors = new ArrayList<AttributeAccessor>();
    ValueAccessor value = null;

    //first go through the fields.
    for (FieldDeclaration field : getFields()) {
      if (filter.accept(field)) {
        if (isAttribute(field)) {
          attributeAccessors.add(new AttributeAccessor(field));
        }
        else if (isValue(field)) {
          if (value != null) {
            throw new ValidationException(field.getPosition() + ": a type definition cannot have more than one xml value.");
          }

          value = new ValueAccessor(field);
        }
        else if (isMixed(field)) {
          //todo: support xml-mixed?
          throw new ValidationException(field.getPosition() + ": sorry, enunciate currently doesn't support mixed complex types. maybe someday.");
        }
        else {
          //its an element accessor.
          if (!elementAccessors.add(new ElementAccessor(field))) {
            throw new ValidationException(field.getPosition() + ": duplicate XML element.");
          }
        }
      }
    }

    //then go through the properties.
    for (PropertyDeclaration property : getProperties()) {
      if (filter.accept(property)) {
        if (isAttribute(property)) {
          attributeAccessors.add(new AttributeAccessor(property));
        }
        else if (isValue(property)) {
          if (value != null) {
            throw new ValidationException(property.getPosition() + ": a type definition cannot have more than one xml value.");
          }

          value = new ValueAccessor(property);
        }
        else {
          //its an element accessor.
          if (!elementAccessors.add(new ElementAccessor(property))) {
            throw new ValidationException(property.getPosition() + ": duplicate XML element.");
          }
        }
      }
    }

    this.elements = Collections.unmodifiableSortedSet(elementAccessors);
    this.attributes = Collections.unmodifiableCollection(attributeAccessors);
    this.xmlValue = value;
  }

  /**
   * Whether a declaration is an xml attribute.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an attribute.
   */
  protected boolean isAttribute(MemberDeclaration declaration) {
    //todo: the attribute wildcard?
    return (declaration.getAnnotation(XmlAttribute.class) != null);
  }

  /**
   * Whether a declaration is an xml value.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an value.
   */
  protected boolean isValue(MemberDeclaration declaration) {
    return (declaration.getAnnotation(XmlValue.class) != null);
  }

  /**
   * Whether a declaration is an xml-mixed property.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an mixed.
   */
  protected boolean isMixed(MemberDeclaration declaration) {
    return (declaration.getAnnotation(XmlMixed.class) != null);
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
   * The elements of this type definition.
   *
   * @return The elements of this type definition.
   */
  public SortedSet<ElementAccessor> getElements() {
    return elements;
  }

  /**
   * The attributes of this type definition.
   *
   * @return The attributes of this type definition.
   */
  public Collection<AttributeAccessor> getAttributes() {
    return attributes;
  }

  /**
   * The value of this type definition.
   *
   * @return The value of this type definition.
   */
  public ValueAccessor getValue() {
    return xmlValue;
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
