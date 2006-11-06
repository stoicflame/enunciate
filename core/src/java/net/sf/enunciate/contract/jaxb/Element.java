package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.XmlClassType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
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
        try {
          Class clazz = element.type();
          if ((clazz == null) || (clazz == XmlElement.DEFAULT.class)) {
            throw new ValidationException(getPosition(), "An element choice must have its type specified.");
          }
          else if ((clazz.isArray()) || (Collection.class.isAssignableFrom(clazz))) {
            throw new ValidationException(getPosition(), "An element choice must not be a collection or an array.");
          }
        }
        catch (MirroredTypeException e) {
          // Fall through.
          // If the mirrored type exception is thrown, we couldn't load the class.  This probably
          // implies that the type is valid and it's in the source base.
        }

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
  protected Element(MemberDeclaration delegate, TypeDefinition typedef, XmlElement xmlElement) {
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
    String namespace = null;

    if (getTypeDefinition().getSchema().getElementFormDefault() == XmlNsForm.QUALIFIED) {
      namespace = getTypeDefinition().getNamespace();
    }

    if ((xmlElement != null) && (!"##default".equals(xmlElement.namespace()))) {
      namespace = xmlElement.namespace();
    }

    return namespace;
  }

  /**
   * The qname for the referenced element, if this element is a reference to a global element, or null if
   * this element is not a reference element.
   *
   * @return The qname for the referenced element, if exists.
   */
  public QName getRef() {
    QName ref = null;

    //check to see if this is an implied ref as per the jaxb spec, section 8.9.1.2
    XmlTypeMirror baseType = getBaseType();
    if (baseType.isAnonymous()) {
      TypeDefinition baseTypeDef = ((XmlClassType) baseType).getTypeDefinition();
      if (baseTypeDef.getAnnotation(XmlRootElement.class) != null) {
        RootElementDeclaration rootElement = new RootElementDeclaration((ClassDeclaration) baseTypeDef.getDelegate(), baseTypeDef);
        ref = new QName(rootElement.getNamespace(), rootElement.getName());
      }
    }

    return ref;
  }

  /**
   * The type of an element accessor can be specified by an annotation.
   *
   * @return The accessor type.
   */
  @Override
  public TypeMirror getAccessorType() {
    try {
      if ((xmlElement != null) && (xmlElement.type() != XmlElement.DEFAULT.class)) {
        Class clazz = xmlElement.type();
        return getAccessorType(clazz);
      }
    }
    catch (MirroredTypeException e) {
      // The mirrored type exception implies that the specified type is within the source base.
      return TypeMirrorDecorator.decorate(e.getTypeMirror());
    }

    return super.getAccessorType();
  }

  /**
   * Get the accessor type for the specified class.
   *
   * @param clazz The class.
   * @return The accessor type.
   */
  protected TypeMirror getAccessorType(Class clazz) {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    TypeMirror undecorated;
    if (clazz.isPrimitive()) {
      if (Boolean.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.BOOLEAN);
      }
      else if (Byte.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.BYTE);
      }
      else if (Character.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.CHAR);
      }
      else if (Double.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.DOUBLE);
      }
      else if (Float.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.FLOAT);
      }
      else if (Integer.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.INT);
      }
      else if (Long.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.LONG);
      }
      else if (Short.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.SHORT);
      }
      else {
        throw new IllegalArgumentException("Unknown primitive type: " + clazz.getName());
      }
    }
    else if (clazz.isArray()) {
      undecorated = env.getTypeUtils().getArrayType(getAccessorType(clazz.getComponentType()));
    }
    else {
      TypeDeclaration typeDeclaration = env.getTypeDeclaration(clazz.getName());
      //todo: worry about the formal type parameters?
      undecorated = env.getTypeUtils().getDeclaredType(typeDeclaration);
    }

    return TypeMirrorDecorator.decorate(undecorated);
  }

  /**
   * The base type of an element accessor can be specified by an annotation.
   *
   * @return The base type.
   */
  @Override
  public XmlTypeMirror getBaseType() {
    if (xmlElement != null) {
      Class typeClass = null;
      TypeMirror typeMirror = null;
      try {
        typeClass = xmlElement.type();
      }
      catch (MirroredTypeException e) {
        typeMirror = e.getTypeMirror();
      }

      try {
        if (typeClass == null) {
          return ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(typeMirror);
        }
        else if (typeClass != XmlElement.DEFAULT.class) {
          return ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(typeClass);
        }
      }
      catch (XmlTypeException e) {
        throw new ValidationException(getPosition(), e.getMessage());
      }
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

    TypeMirror accessorType = getAccessorType();
    boolean primitive = (accessorType instanceof PrimitiveType);
    if ((!primitive) && (accessorType instanceof ArrayType)) {
      //we have to check if the component type if its an array type, too.
      primitive = (((ArrayType) accessorType).getComponentType() instanceof PrimitiveType);
    }

    return primitive ? 1 : 0;
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
    String namespace = getTypeDefinition().getNamespace();

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
