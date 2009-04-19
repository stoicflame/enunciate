package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;

import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;
import java.util.Collection;

import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;

/**
 * A declaration of a "local" element (defined by a registry).
 *
 * @author Ryan Heaton
 */
public class LocalElementDeclaration extends DecoratedMethodDeclaration {

  private final TypeDeclaration elementTypeDeclaration;
  private final XmlElementDecl elementDecl;
  private final Registry registry;

  public LocalElementDeclaration(MethodDeclaration delegate, Registry registry) {
    super(delegate);
    this.registry = registry;
    elementDecl = delegate.getAnnotation(XmlElementDecl.class);
    if (elementDecl == null) {
      throw new IllegalArgumentException(getPosition() + ": a local element declaration must be annotated with @XmlElementDecl.");
    }

    Collection<ParameterDeclaration> params = getParameters();
    if (params.size() != 1) {
      throw new IllegalArgumentException(getPosition() + ": a local element declaration must have only one parameter.");
    }
    ParameterDeclaration param = params.iterator().next();
    if (!(param.getType() instanceof DeclaredType)) {
      throw new IllegalArgumentException(getPosition() + ": parameter type must be a declared type.");
    }
    elementTypeDeclaration = ((DeclaredType) param.getType()).getDeclaration();
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
   * The scope of the local element.
   *
   * @return The scope of the local element.
   */
  public TypeDeclaration getElementScope() {
    TypeDeclaration declaration = null;
    try {
      if (elementDecl.scope() != XmlElementDecl.GLOBAL.class) {
        Class typeClass = elementDecl.scope();
        declaration = getEnv().getTypeDeclaration(typeClass.getName());
      }
    }
    catch (MirroredTypeException e) {
      //This exception implies the ref is within the source base.
      TypeMirror typeMirror = e.getTypeMirror();
      if (typeMirror instanceof DeclaredType) {
        declaration = ((DeclaredType) typeMirror).getDeclaration();
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
  public String getDefaultValue() {
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
  public TypeDeclaration getElementTypeDeclaration() {
    return elementTypeDeclaration;
  }

  /**
   * The element xml type.
   *
   * @return The element xml type.
   */
  public XmlType getElementXmlType() {
    try {
      return XmlTypeFactory.getXmlType(getParameters().iterator().next().getType());
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
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
