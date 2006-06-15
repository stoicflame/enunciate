package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.Types;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An accessor that is marshalled in xml to an xml element.
 *
 * @author Ryan Heaton
 */
public class Element extends Accessor {

  private final XmlElement xmlElement;
  private final Collection<Element> choices;

  public Element(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef);

    XmlElement xmlElement = getAnnotation(XmlElement.class);
    XmlElements xmlElements = getAnnotation(XmlElements.class);
    if (xmlElements != null) {
      XmlElement[] elementChoices = xmlElements.value();
      if (elementChoices.length == 0) {
        xmlElements = null;
      }
      else if ((xmlElement == null) && (elementChoices.length == 1)) {
        xmlElement = elementChoices[0];
        xmlElements = null;
      }
    }

    this.xmlElement = xmlElement;
    this.choices = new ArrayList<Element>();
    if (xmlElements != null) {
      for (XmlElement element : xmlElements.value()) {
        this.choices.add(new Element((MemberDeclaration) getDelegate(), getTypeDefinition(), element));
      }
    }
    else {
      this.choices.add(this);
    }
  }

  /**
   * Construct an element accessor with a specific element annotation.
   *
   * @param delegate   The delegate.
   * @param typedef    The type definition.
   * @param xmlElement The specific element annotation.
   */
  private Element(MemberDeclaration delegate, TypeDefinition typedef, XmlElement xmlElement) {
    super(delegate, typedef);
    this.xmlElement = xmlElement;
    this.choices = new ArrayList<Element>();
    this.choices.add(this);
  }

  // Inherited.
  public String getName() {
    String propertyName = getSimpleName();

    if ((xmlElement != null) && (!"##default".equals(xmlElement.name()))) {
      propertyName = xmlElement.name();
    }

    return propertyName;
  }

  // Inherited.
  public String getNamespace() {
    String namespace = getTypeDefinition().getTargetNamespace();

    if ((xmlElement != null) && (!"##default".equals(xmlElement.namespace()))) {
      namespace = xmlElement.namespace();
    }

    return namespace;
  }

  /**
   * The base type of an element accessor can be specified by an annotation.
   *
   * @return The base type.
   */
  @Override
  public DecoratedTypeMirror getBaseType() {
    if ((xmlElement != null) && (xmlElement.type() != XmlElement.DEFAULT.class)) {
      AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
      Types types = env.getTypeUtils();
      TypeDeclaration declaration = env.getTypeDeclaration(xmlElement.type().getName());
      return (DecoratedTypeMirror) TypeMirrorDecorator.decorate(types.getDeclaredType(declaration));
    }

    return super.getBaseType();
  }

  /**
   * Whether this element is nillable.
   *
   * @return Whether this element is nillable.
   */
  public boolean isNillable() {
    boolean nillable = false;

    if (xmlElement != null) {
      nillable = xmlElement.nillable();
    }

    return nillable;
  }

  /**
   * Whether this element is required.
   *
   * @return Whether this element is required.
   */
  public boolean isRequired() {
    boolean required = false;

    if (xmlElement != null) {
      required = xmlElement.required();
    }

    return required;
  }

  /**
   * The min occurs of this element.
   *
   * @return The min occurs of this element.
   */
  public int getMinOccurs() {
    if (isRequired()) {
      return 1;
    }

    return getBaseType().isPrimitive() ? 1 : 0;
  }

  /**
   * The max occurs of this element.
   *
   * @return The max occurs of this element.
   */
  public String getMaxOccurs() {
    return isCollectionType() ? "unbounded" : "1";
  }

  /**
   * Whether the accessor type is a collection type.
   *
   * @return Whether the accessor type is a collection type.
   */
  public boolean isCollectionType() {
    DecoratedTypeMirror accessorType = getAccessorType();
    return accessorType.isArray() || accessorType.isCollection();
  }

  /**
   * The default value, or null if none exists.
   *
   * @return The default value, or null if none exists.
   */
  public String getDefaultValue() {
    String defaultValue = null;

    if ((xmlElement != null) && (!"\u0000".equals(xmlElement.defaultValue()))) {
      defaultValue = xmlElement.defaultValue();
    }

    return defaultValue;
  }

  /**
   * The choices for this element.
   *
   * @return The choices for this element.
   */
  public Collection<? extends Element> getChoices() {
    return choices;
  }

  /**
   * Whether this xml element is wrapped.
   *
   * @return Whether this xml element is wrapped.
   */
  public boolean isWrapped() {
    return (isCollectionType() && (getAnnotation(XmlElementWrapper.class) != null));
  }

  /**
   * The name of the wrapper element.
   *
   * @return The name of the wrapper element.
   */
  public String getWrapperName() {
    String name = getSimpleName();

    XmlElementWrapper xmlElementWrapper = getAnnotation(XmlElementWrapper.class);
    if ((xmlElementWrapper != null) && (!"##default".equals(xmlElementWrapper.name()))) {
      name = xmlElementWrapper.name();
    }

    return name;
  }

  /**
   * The namespace of the wrapper element.
   *
   * @return The namespace of the wrapper element.
   */
  public String getWrapperNamespace() {
    String namespace = getNamespace();

    XmlElementWrapper xmlElementWrapper = getAnnotation(XmlElementWrapper.class);
    if ((xmlElementWrapper != null) && (!"##default".equals(xmlElementWrapper.namespace()))) {
      namespace = xmlElementWrapper.namespace();
    }

    return namespace;
  }

  /**
   * Whether the wrapper is nillable.
   *
   * @return Whether the wrapper is nillable.
   */
  public boolean isWrapperNillable() {
    boolean nillable = false;

    XmlElementWrapper xmlElementWrapper = getAnnotation(XmlElementWrapper.class);
    if (xmlElementWrapper != null) {
      nillable = xmlElementWrapper.nillable();
    }

    return nillable;
  }

}
