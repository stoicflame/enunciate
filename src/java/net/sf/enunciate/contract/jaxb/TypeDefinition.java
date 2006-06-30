package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.type.ClassType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.util.QName;
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
  private final Accessor xmlID;

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
    Accessor xmlID = null;
    for (MemberDeclaration accessor : accessors) {
      if (filter.accept(accessor)) {
        Accessor added;
        if (isAttribute(accessor)) {
          Attribute attribute = new Attribute(accessor, this);
          attributeAccessors.add(attribute);
          added = attribute;
        }
        else if (isValue(accessor)) {
          if (value != null) {
            throw new ValidationException(accessor.getPosition(), "A type definition cannot have more than one xml value.");
          }

          value = new Value(accessor, this);
          added = value;
        }
        else if (isElementRef(accessor)) {
          ElementRef elementRef = new ElementRef(accessor, this);
          if (!elementAccessors.add(elementRef)) {
            throw new ValidationException(accessor.getPosition(), "Duplicate XML element.");
          }
          added = elementRef;
        }
        else if (isUnsupported(accessor)) {
          //todo: support xml-mixed?
          throw new ValidationException(accessor.getPosition(), "Sorry, we currently don't support mixed or wildard elements. Maybe someday...");
        }
        else {
          //its an element accessor.
          Element element = new Element(accessor, this);
          if (!elementAccessors.add(element)) {
            throw new ValidationException(accessor.getPosition(), "Duplicate XML element.");
          }
          added = element;
        }

        if (added.getAnnotation(XmlID.class) != null) {
          if (xmlID != null) {
            throw new ValidationException(added.getPosition(), "More than one XML id specified.");
          }

          xmlID = added;
        }

      }
    }

    this.elements = Collections.unmodifiableSortedSet(elementAccessors);
    this.attributes = Collections.unmodifiableCollection(attributeAccessors);
    this.xmlValue = value;
    this.xmlID = xmlID;
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
   * The qname of this type definition.
   *
   * @return The qname of this type definition.
   */
  public QName getQname() {
    return new QName(getTargetNamespace(), getName());
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
   * The accessor that is the xml id of this type definition, or null if none.
   *
   * @return The accessor that is the xml id of this type definition, or null if none.
   */
  public Accessor getXmlID() {
    return xmlID;
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
   * Whether this is a complex type.
   *
   * @return Whether this is a complex type.
   */
  public boolean isComplex() {
    return false;
  }

  /**
   * Whether this is a enum type.
   *
   * @return Whether this is a enum type.
   */
  public boolean isEnum() {
    return false;
  }

  /**
   * Whether this is a simple type.
   *
   * @return Whether this is a simple type.
   */
  public boolean isSimple() {
    return false;
  }

  /**
   * Accept a validator.
   *
   * @param validator The validator to accept.
   * @return The validation results.
   */
  public abstract ValidationResult accept(Validator validator);

  /**
   * The base type of this type definition.
   *
   * @return The base type of this type definition.
   */
  public abstract XmlTypeMirror getBaseType();

}
