package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.*;
import net.sf.enunciate.util.QName;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A class declaration decorated so as to be able to describe itself as an XML-Schema type definition.
 *
 * @author Ryan Heaton
 * @see "The JAXB 2.0 Specification"
 * @see <a href="http://www.w3.org/TR/2004/REC-xmlschema-1-20041028/structures.html">XML Schema Part 1: Structures Second Edition</a>
 */
public class TypeDefinition extends DecoratedClassDeclaration {

  protected final XmlType xmlType;
  protected final Map<String, String> ns2prefix;
  private final Schema pkg;

  public TypeDefinition(ClassDeclaration delegate, Map<String, String> ns2prefix) {
    super(delegate);

    this.ns2prefix = ns2prefix;
    xmlType = getAnnotation(XmlType.class);
    pkg = new Schema(super.getPackage());

    validate();
  }

  /**
   * Validates this xml type.
   */
  protected void validate() {
    if (xmlType != null) {
      if ((getDeclaringType() != null) && (!getModifiers().contains(Modifier.STATIC))) {
        throw new IllegalStateException("A xml type must be either a top-level class or a nested static class.");
      }

      Class factoryClass = xmlType.factoryClass();
      String factoryMethod = xmlType.factoryMethod();
      if (factoryMethod == null) {
        factoryMethod = "";
      }

      if ((factoryClass != XmlType.DEFAULT.class) || (!"".equals(factoryMethod))) {
        try {
          Method method = factoryClass.getMethod(factoryMethod, null);
          if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            //todo: is this really a requirement?
            throw new IllegalStateException(getPosition() + ": '" + factoryMethod + "' must be a static, no-arg method on '" + factoryClass.getName() + "'.");
          }
        }
        catch (NoSuchMethodException e) {
          throw new IllegalStateException(getPosition() + ": Unknown factory method '" + factoryMethod + "' on class '" + factoryClass.getName() + "'.");
        }
      }
      else if (getAnnotation(XmlJavaTypeAdapter.class) != null) {
        //todo: how to check to see if this is a valid type adapter?
      }
      else {
        //check for a zero-arg constructor...
        boolean hasNoArgConstructor = false;
        Collection<ConstructorDeclaration> constructors = getConstructors();
        for (ConstructorDeclaration constructor : constructors) {
          if ((constructor.getModifiers().contains(Modifier.PUBLIC)) && (constructor.getParameters().size() == 0)) {
            hasNoArgConstructor = true;
            break;
          }
        }

        if (!hasNoArgConstructor) {
          throw new IllegalStateException(getPosition() + ": an TypeDefinition must have a public no-arg constructor or be annotated with a factory method.");
        }
      }
    }
  }

  /**
   * The qname of the xml type.
   *
   * @return The qname of the xml type.
   */
  public QName getTypeQName() {
    String namespace = getTypeNamespace();
    String prefix = ns2prefix.get(namespace);

    if (prefix == null) {
      throw new IllegalStateException("No prefix configured for '" + namespace + "' one should have been generated (?)");
    }

    return new QName(namespace, getTypeName(), prefix);
  }

  /**
   * The namespace of the xml type element.
   *
   * @return The namespace of the xml type element.
   */
  public String getTypeNamespace() {
    String namespace = getPackage().getNamespace();

    if ((xmlType != null) && (!"##default".equals(xmlType))) {
      namespace = xmlType.namespace();
    }

    return namespace;

  }

  /**
   * The name of the xml type element.
   *
   * @return The name of the xml type element.
   */
  public String getTypeName() {
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
   * Whether this xml type is an extension of another.
   *
   * @return Whether this xml type is an extension of another.
   */
  public boolean isExtension() {
    ClassDeclaration superclassDeclaration = getSuperclass().getDeclaration();
    return ((superclassDeclaration != null) && (!Object.class.getName().equals(superclassDeclaration.getQualifiedName())));
  }

  /**
   * The extended type.
   *
   * @return The extended type.
   */
  public TypeDefinition getExtendedType() {
    ClassDeclaration superclassDeclaration = getSuperclass().getDeclaration();
    if (superclassDeclaration == null) {
      return null;
    }

    return new TypeDefinition(superclassDeclaration, ns2prefix);
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

    return accessType;
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
  protected SortedSet<JAXBAccessorDeclaration> getAccessors() {
    JAXBAccessorComparator comparator = new JAXBAccessorComparator(getPropertyOrder(), getAccessorOrder());
    SortedSet<JAXBAccessorDeclaration> accessors = new TreeSet<JAXBAccessorDeclaration>(comparator);

    AccessType accessType = getAccessType();
    if (accessType != AccessType.PROPERTY) {
      for (FieldDeclaration field : getFields()) {
        if (field.getAnnotation(XmlTransient.class) == null) {
          boolean useField = false;

          Collection<AnnotationMirror> annotationMirrors = field.getAnnotationMirrors();
          for (AnnotationMirror annotationMirror : annotationMirrors) {
            //          annotationMirror.getAnnotationType().
          }

          field.getModifiers().contains(Modifier.PUBLIC);
          if (accessType == AccessType.NONE) {
//            useField &= field.getAnnotation()
          }
        }
      }
    }

    if (accessType != AccessType.FIELD) {

    }

    return null;
  }

  @Override
  public Schema getPackage() {
    return pkg;
  }
}
