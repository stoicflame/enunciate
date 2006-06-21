package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.type.ClassType;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import javax.xml.bind.annotation.*;
import java.util.*;

/**
 * A xml type definition.
 *
 * @author Ryan Heaton
 */
public abstract class TypeDefinition extends DecoratedClassDeclaration {

  private final XmlType xmlType;
  private final Schema schema;
  private final SortedSet<Element> elements;
  private final Collection<Attribute> attributes;
  private final Value xmlValue;

  protected TypeDefinition(ClassDeclaration delegate) {
    super(delegate);

    this.xmlType = getAnnotation(XmlType.class);
    this.schema = new Schema(delegate.getPackage());

    ElementComparator comparator = new ElementComparator(getPropertyOrder(), getAccessorOrder());
    AccessorFilter filter = new AccessorFilter(getAccessType());
    SortedSet<Element> elementAccessors = new TreeSet<Element>(comparator);
    Collection<Attribute> attributeAccessors = new ArrayList<Attribute>();
    Value value = null;

    ArrayList<MemberDeclaration> accessors = new ArrayList<MemberDeclaration>();
    accessors.addAll(getFields());
    accessors.addAll(getProperties());
    for (MemberDeclaration accessor : accessors) {
      if (filter.accept(accessor)) {
        if (isAttribute(accessor)) {
          attributeAccessors.add(new Attribute(accessor, this));
        }
        else if (isValue(accessor)) {
          if (value != null) {
            throw new ValidationException(accessor.getPosition(), "A type definition cannot have more than one xml value.");
          }

          value = new Value(accessor, this);
        }
        else if (isElementRef(accessor)) {
          if (!elementAccessors.add(new ElementRef(accessor, this))) {
            throw new ValidationException(accessor.getPosition(), "Duplicate XML element.");
          }
        }
        else if (isUnsupported(accessor)) {
          //todo: support xml-mixed?
          throw new ValidationException(accessor.getPosition(), "Sorry, we currently don't support mixed or wildard elements. Maybe someday...");
        }
        else {
          //its an element accessor.
          if (!elementAccessors.add(new Element(accessor, this))) {
            throw new ValidationException(accessor.getPosition(), "Duplicate XML element.");
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
   * Whether a declaration is an xml element ref.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an xml element ref.
   */
  protected boolean isElementRef(MemberDeclaration declaration) {
    return ((declaration.getAnnotation(XmlElementRef.class) != null) || (declaration.getAnnotation(XmlElementRefs.class) != null));
  }

  /**
   * Whether a declaration is an xml-mixed property.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an mixed.
   */
  protected boolean isUnsupported(MemberDeclaration declaration) {
    return (declaration.getAnnotation(XmlMixed.class) != null)
      || (declaration.getAnnotation(XmlAnyElement.class) != null)
      || (declaration.getAnnotation(XmlAnyAttribute.class) != null);
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
   * The default access type for the beans in this class.
   *
   * @return The default access type for the beans in this class.
   */
  public XmlAccessType getAccessType() {
    XmlAccessType accessType = getPackage().getAccessType();

    XmlAccessorType xmlAccessorType = getAnnotation(XmlAccessorType.class);
    if (xmlAccessorType != null) {
      accessType = xmlAccessorType.value();
    }
    else {
      XmlAccessType inheritedAccessType = getInheritedAccessType(this);
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
  protected XmlAccessType getInheritedAccessType(ClassDeclaration declaration) {
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
  public XmlAccessOrder getAccessorOrder() {
    XmlAccessOrder order = getPackage().getAccessorOrder();

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
  public SortedSet<Element> getElements() {
    return elements;
  }

  /**
   * The attributes of this type definition.
   *
   * @return The attributes of this type definition.
   */
  public Collection<Attribute> getAttributes() {
    return attributes;
  }

  /**
   * The value of this type definition.
   *
   * @return The value of this type definition.
   */
  public Value getValue() {
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
   * Whether this xml type is anonymous.
   *
   * @return Whether this xml type is anonymous.
   */
  public boolean isAnonymous() {
    return getName() == null;
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

  /**
   * Accept a validator.
   *
   * @param validator The validator to accept.
   * @return The validation results.
   */
  public abstract ValidationResult accept(Validator validator);

}
