package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A declaration of a "local" element (defined by a registry).
 *
 * @author Ryan Heaton
 */
public class LocalElementDeclaration extends DecoratedExecutableElement implements HasFacets {

  private final TypeElement elementTypeDeclaration;
  private final XmlElementDecl elementDecl;
  private final Registry registry;
  private final Set<Facet> facets = new TreeSet<Facet>();
  private final EnunciateJaxbContext context;

  public LocalElementDeclaration(ExecutableElement element, Registry registry, EnunciateJaxbContext context) {
    super(element, context.getContext().getProcessingEnvironment());
    this.registry = registry;
    elementDecl = element.getAnnotation(XmlElementDecl.class);
    if (elementDecl == null) {
      throw new IllegalArgumentException(element + ": a local element declaration must be annotated with @XmlElementDecl.");
    }

    List<? extends VariableElement> params = element.getParameters();
    if (params.size() != 1) {
      throw new IllegalArgumentException(element + ": a local element declaration must have only one parameter.");
    }

    VariableElement param = params.iterator().next();
    TypeMirror paramType = param.asType();
    if (!(paramType.getKind() == TypeKind.DECLARED)) {
      throw new IllegalArgumentException(element + ": parameter type must be a declared type.");
    }

    elementTypeDeclaration = (TypeElement) ((DeclaredType) paramType).asElement();
    this.facets.addAll(Facet.gatherFacets(registry));
    this.facets.addAll(Facet.gatherFacets(element));
    this.context = context;
  }

  /**
   * The name of the local element.
   *
   * @return The name of the local element.
   */
  public String getName() {
    return elementDecl.name();
  }

  /**
   * The namespace of the local element.
   *
   * @return The namespace of the local element.
   */
  public String getNamespace() {
    String namespace = elementDecl.namespace();
    if ("##default".equals(namespace)) {
      namespace = this.registry.getSchema().getNamespace();
    }
    return "".equals(namespace) ? null : namespace;
  }

  /**
   * The qname of the element.
   *
   * @return The qname of the element.
   */
  public QName getQname() {
    return new QName(getNamespace(), getName());
  }

  /**
   * The scope of the local element.
   *
   * @return The scope of the local element.
   */
  public DecoratedTypeElement getElementScope() {
    DecoratedTypeElement declaration = null;
    try {
      if (elementDecl.scope() != XmlElementDecl.GLOBAL.class) {
        declaration = (DecoratedTypeElement) this.env.getElementUtils().getTypeElement(elementDecl.scope().getName());
      }
    }
    catch (MirroredTypeException e) {
      //This exception implies the ref is within the source base.
      TypeMirror typeMirror = e.getTypeMirror();
      if (typeMirror instanceof DeclaredType) {
        declaration = (DecoratedTypeElement) ElementDecorator.decorate(((DeclaredType) typeMirror).asElement(), this.env);
      }
    }

    return declaration;
  }

  /**
   * The name of the substitution head.
   *
   * @return The name of the substitution head.
   */
  public String getSubstitutionHeadName() {
    String shn = elementDecl.substitutionHeadName();
    if ("".equals(shn)) {
      shn = null;
    }
    return shn;
  }

  /**
   * The namespace of the substitution head.
   *
   * @return The namespace of the substitution head.
   */
  public String getSubstitutionHeadNamespace() {
    String shn = elementDecl.substitutionHeadNamespace();
    if ("##default".equals(shn)) {
      shn = this.registry.getSchema().getNamespace();
    }
    return shn;
  }

  /**
   * The substitution group qname.
   *
   * @return The substitution group qname.
   */
  public QName getSubstitutionGroupQName() {
    String localPart = getSubstitutionHeadName();
    if (localPart == null) {
      return null;
    }
    return new QName(getSubstitutionHeadNamespace(), localPart);
  }

  /**
   * The default value.
   *
   * @return The default value.
   */
  public String getDefaultElementValue() {
    String defaultValue = elementDecl.defaultValue();
    if ("\u0000".equals(defaultValue)) {
      defaultValue = null;
    }
    return defaultValue;
  }

  /**
   * The type definition for the local element.
   *
   * @return The type definition for the local element.
   */
  public TypeElement getElementType() {
    return elementTypeDeclaration;
  }

  /**
   * The element xml type.
   *
   * @return The element xml type.
   */
  public XmlType getElementXmlType() {
    return XmlTypeFactory.getXmlType(getParameters().get(0).asType(), this.context);
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }

}
