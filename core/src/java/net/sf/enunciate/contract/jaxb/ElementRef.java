package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.ExplicitXmlType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
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
  private final XmlTypeMirror ref;

  public ElementRef(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef, null);

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
    if (xmlElementRefs != null) {
      for (XmlElementRef elementRef : xmlElementRefs.value()) {
        this.choices.add(new ElementRef((MemberDeclaration) getDelegate(), getTypeDefinition(), elementRef));
      }

      this.ref = null;
    }
    else if (((DecoratedTypeMirror) getBareAccessorType()).isInstanceOf(JAXBElement.class.getName())) {
      //this is either a single-valued JAXBElement, or a parametric collection of them...
      //todo: throw an exception if this is referencing a non-global element for this namespace?
      this.choices.add(this);
      this.ref = new ExplicitXmlType(xmlElementRef.name(), xmlElementRef.namespace());
    }
    else if (isCollectionType()) {
      //if it's a parametric collection type, we need to provide a choice between all subclasses of the base type.
      TypeMirror typeMirror = getBareAccessorType();
      if (typeMirror instanceof DeclaredType) {
        String fqn = ((DeclaredType) typeMirror).getDeclaration().getQualifiedName();
        EnunciateFreemarkerModel model = ((EnunciateFreemarkerModel) FreemarkerModel.get());
        for (RootElementDeclaration rootElement : model.getRootElementDeclarations()) {
          if (isInstanceOf(rootElement, fqn)) {
            this.choices.add(new ElementRef((MemberDeclaration) getDelegate(), getTypeDefinition(), rootElement));
          }
        }
      }

      if (this.choices.isEmpty()) {
        throw new ValidationException(getPosition(), String.format("No known root element subtypes of %s", typeMirror));
      }

      this.ref = null;
    }
    else {
      this.choices.add(this);
      this.ref = loadRef();
    }
  }

  /**
   * Determines whether the class declaration is an instance of the declared type of the given fully-qualified name.
   *
   * @param classDeclaration The class declaration.
   * @param fqn The FQN.
   * @return Whether the class declaration is an instance of the declared type of the given fully-qualified name.
   */
  protected boolean isInstanceOf(ClassDeclaration classDeclaration, String fqn) {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Types utils = env.getTypeUtils();
    DeclaredType declaredType = utils.getDeclaredType(env.getTypeDeclaration(classDeclaration.getQualifiedName()));
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(declaredType);
    return decorated.isInstanceOf(fqn);
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
    this.ref = new ExplicitXmlType(ref.getName(), ref.getNamespace());
  }

  /**
   * Load the referenced root element declaration.
   *
   * @return the referenced root element declaration.
   */
  protected XmlTypeMirror loadRef() {
    TypeDeclaration declaration = null;
    String elementDeclaration;
    try {
      if ((xmlElementRef != null) && (xmlElementRef.type() != XmlElementRef.DEFAULT.class)) {
        Class typeClass = xmlElementRef.type();
        elementDeclaration = typeClass.getName();
        declaration = getEnv().getTypeDeclaration(typeClass.getName());
      }
      else {
        TypeMirror accessorType = getAccessorType();
        elementDeclaration = accessorType.toString();
        if (accessorType instanceof DeclaredType) {
          declaration = ((DeclaredType) accessorType).getDeclaration();
        }
      }
    }
    catch (MirroredTypeException e) {
      //This exception implies the ref is within the source base.
      TypeMirror typeMirror = e.getTypeMirror();
      elementDeclaration = typeMirror.toString();
      if (typeMirror instanceof DeclaredType) {
        declaration = ((DeclaredType) typeMirror).getDeclaration();
      }
    }

    RootElementDeclaration refElement;
    if ((declaration instanceof ClassDeclaration) && (declaration.getAnnotation(XmlRootElement.class) != null)) {
      ClassDeclaration classDeclaration = (ClassDeclaration) declaration;
      refElement = new RootElementDeclaration(classDeclaration, ((EnunciateFreemarkerModel) FreemarkerModel.get()).findTypeDefinition(classDeclaration));
    }
    else {
      throw new ValidationException(getPosition(), elementDeclaration + " is not a root element declaration.");
    }

    return new ExplicitXmlType(refElement.getName(), refElement.getNamespace());
  }

  /**
   * The name of an element ref is the name of the element it references, unless the type is a JAXBElement, in which
   * case the name is specified.
   *
   * @return The name of the element ref.
   */
  @Override
  public String getName() {
    if (this.ref == null) {
      throw new UnsupportedOperationException("No single reference for this element: multiple choices.");
    }

    return this.ref.getName();
  }

  /**
   * The namespace of an element ref is the namespace of the element it references, unless the type is a JAXBElement, in which
   * case the namespace is specified.
   *
   * @return The namespace of the element ref.
   */
  @Override
  public String getNamespace() {
    if (this.ref == null) {
      throw new UnsupportedOperationException("No single reference for this element: multiple choices.");
    }

    return this.ref.getNamespace();
  }

  @Override
  public QName getRef() {
    if (this.ref == null) {
      throw new UnsupportedOperationException("No single reference for this element: multiple choices.");
    }

    return new QName(this.ref.getNamespace(), this.ref.getName());
  }


  /**
   * There is no base type for an element ref.
   *
   * @throws UnsupportedOperationException Because there is no such things as a base type for an element ref.
   *
   */
  @Override
  public XmlTypeMirror getBaseType() {
    throw new UnsupportedOperationException("There is no base type for an element ref.");
  }

  /**
   * An element ref is not nillable.
   *
   * @return false
   */
  @Override
  public boolean isNillable() {
    return false;
  }

  /**
   * An element ref is not required.
   *
   * @return false.
   */
  @Override
  public boolean isRequired() {
    return false;
  }

  /**
   * The min occurs of an element ref is 0
   *
   * @return 0
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
