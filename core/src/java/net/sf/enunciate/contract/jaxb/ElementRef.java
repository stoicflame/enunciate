package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.XmlClassType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An accessor that is marshalled in xml to an xml element.
 *
 * @author Ryan Heaton
 */
public class ElementRef extends Element {

  private final XmlElementRef xmlElementRef;
  private final Collection<ElementRef> choices;
  private final RootElementDeclaration ref;

  public ElementRef(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef);

    XmlElementRef xmlElementRef = getAnnotation(XmlElementRef.class);
    XmlElementRefs xmlElementRefs = getAnnotation(XmlElementRefs.class);

    if (xmlElementRefs != null) {
      XmlElementRef[] elementRefChoices = xmlElementRefs.value();
      if (elementRefChoices.length == 0) {
        xmlElementRefs = null;
      }
      else if ((xmlElementRef == null) && (elementRefChoices.length == 1)) {
        xmlElementRef = elementRefChoices[0];
        xmlElementRefs = null;
      }
    }

    this.xmlElementRef = xmlElementRef;
    this.choices = new ArrayList<ElementRef>();
    XmlTypeMirror baseType = getBaseType();
    if (xmlElementRefs != null) {
      for (XmlElementRef elementRef : xmlElementRefs.value()) {
        this.choices.add(new ElementRef((MemberDeclaration) getDelegate(), getTypeDefinition(), elementRef));
      }
    }
    else if (isCollectionType()) {
      //if it's a parametric collection type, we need to provide a choice between all subclasses of the base type.
      //todo: what if it's a parameteric collection type of JAXBElements?
      if (baseType instanceof TypeMirror) {
        TypeMirror typeMirror = (TypeMirror) baseType;
        AnnotationProcessorEnvironment env = getEnv();
        Types typeUtils = env.getTypeUtils();
        EnunciateFreemarkerModel model = ((EnunciateFreemarkerModel) FreemarkerModel.get());
        for (TypeDeclaration type : env.getTypeDeclarations()) {
          XmlRootElement xmlRootElement = type.getAnnotation(XmlRootElement.class);
          DeclaredType declaredType = typeUtils.getDeclaredType(type);

          if ((xmlRootElement != null) && (typeUtils.isSubtype(declaredType, typeMirror)) && (declaredType.getDeclaration() instanceof ClassDeclaration)) {
            RootElementDeclaration ref = model.findRootElementDeclaration((ClassDeclaration) declaredType.getDeclaration());
            this.choices.add(new ElementRef((MemberDeclaration) getDelegate(), getTypeDefinition(), ref));
          }
        }
      }

      if (this.choices.isEmpty()) {
        throw new ValidationException(getPosition(), String.format("No known root element subtypes of {%s}%s.", baseType.getNamespace(), baseType.getName()));
      }
    }
    else {
      this.choices.add(this);
    }

    this.ref = loadRef();
  }

  /**
   * Construct an element accessor with a specific element ref annotation.
   *
   * @param delegate      The delegate.
   * @param typedef       The type definition.
   * @param xmlElementRef The specific element ref annotation.
   */
  private ElementRef(MemberDeclaration delegate, TypeDefinition typedef, XmlElementRef xmlElementRef) {
    super(delegate, typedef);
    this.xmlElementRef = xmlElementRef;
    this.choices = new ArrayList<ElementRef>();
    this.choices.add(this);
    this.ref = loadRef();
  }

  /**
   * Construct an element accessor with a specific base type.
   *
   * @param delegate The delegate.
   * @param typedef  The type definition.
   * @param ref      The referenced root element.
   */
  private ElementRef(MemberDeclaration delegate, TypeDefinition typedef, RootElementDeclaration ref) {
    super(delegate, typedef);
    this.xmlElementRef = null;
    this.choices = new ArrayList<ElementRef>();
    this.choices.add(this);
    this.ref = ref;
  }

  /**
   * Load the referenced root element declaration.
   *
   * @return the referenced root element declaration.
   */
  protected RootElementDeclaration loadRef() {
    TypeDeclaration declaration = null;
    String elementDeclaration;
    RootElementDeclaration refElement;
    if ((xmlElementRef != null) && (xmlElementRef.type() != XmlElementRef.DEFAULT.class)) {
      declaration = getEnv().getTypeDeclaration(xmlElementRef.type().getName());
      elementDeclaration = xmlElementRef.type().getName();
    }
    else {
      TypeMirror accessorType = isCollectionType() ? getCollectionItemType() : getAccessorType();
      elementDeclaration = accessorType.toString();
      if (accessorType instanceof DeclaredType) {
        declaration = ((DeclaredType) accessorType).getDeclaration();
      }
    }

    if ((declaration instanceof ClassDeclaration) && (declaration.getAnnotation(XmlRootElement.class) != null)) {
      ClassDeclaration classDeclaration = (ClassDeclaration) declaration;
      refElement = new RootElementDeclaration(classDeclaration, ((EnunciateFreemarkerModel) FreemarkerModel.get()).findTypeDefinition(classDeclaration));
    }
    else {
      throw new ValidationException(getPosition(), elementDeclaration + " is not a root element declaration.");
    }

    return refElement;
  }

  /**
   * The name of an element ref is the name of the element it references, unless the type is a JAXBElement, in which
   * case the name is specified.
   *
   * @return The name of the element ref.
   */
  @Override
  public String getName() {
    XmlTypeMirror baseType = getBaseType();
    if ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).isInstanceOf(JAXBElement.class.getName()))) {
      //todo: do some null checking?  What if XmlElementRefs is specified?
      //todo: what if this is referencing a non-global element for this namespace?
      return xmlElementRef.name();
    }
    else {
      //todo: do some null checking, validation checking? verify it's a declared type, verify XmlRootElement exists?
      TypeDeclaration declaration = ((DeclaredType) baseType).getDeclaration();
      XmlRootElement rootElement = declaration.getAnnotation(XmlRootElement.class);

      if ((rootElement != null) && (!"##default".equals(rootElement.name()))) {
        return rootElement.name();
      }

      return declaration.getSimpleName();
    }
  }

  /**
   * The namespace of an element ref is the name of the element it references, unless the type is a JAXBElement, in which
   * case the namespace is specified.
   *
   * @return The namespace of the element ref.
   */
  @Override
  public String getNamespace() {
    XmlTypeMirror baseType = getBaseType();
    if ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).isInstanceOf(JAXBElement.class.getName()))) {
      //todo: do some null checking?  What if XmlElementRefs is specified?
      return xmlElementRef.namespace();
    }
    else {
      //todo: do some null checking, validation checking? verify it's a declared type, verify XmlRootElement exists?
      TypeDeclaration declaration = ((DeclaredType) baseType).getDeclaration();
      XmlRootElement rootElement = declaration.getAnnotation(XmlRootElement.class);

      if ((rootElement != null) && (!"##default".equals(rootElement.name()))) {
        return rootElement.namespace();
      }
      else {
        return new Schema(declaration.getPackage()).getNamespace();
      }
    }
  }

  @Override
  public QName getRef() {
    return new QName(this.ref.getTargetNamespace(), this.ref.getName());
  }

  /**
   * @return An element ref is not nillable.
   */
  @Override
  public boolean isNillable() {
    return false;
  }

  /**
   * @return An element ref is required if there is only one choice.
   */
  @Override
  public boolean isRequired() {
    return getChoices().size() <= 1;
  }

  /**
   * The min occurs of this element.
   *
   * @return The min occurs of this element.
   */
  @Override
  public int getMinOccurs() {
    return isRequired() ? 1 : 0;
  }

  /**
   * The choices for this element.
   *
   * @return The choices for this element.
   */
  @Override
  public Collection<ElementRef> getChoices() {
    return choices;
  }

  /**
   * The current environment.
   *
   * @return The current environment.
   */
  protected AnnotationProcessorEnvironment getEnv() {
    return Context.getCurrentEnvironment();
  }

}
