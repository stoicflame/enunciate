package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.node.ObjectNode;
import org.jdom.Comment;
import org.jdom.output.XMLOutputter;

import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.util.Collection;

/**
 * A declaration of a "local" element (defined by a registry).
 *
 * @author Ryan Heaton
 */
public class LocalElementDeclaration extends DecoratedMethodDeclaration implements ElementDeclaration {

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
      throw new ValidationException(getPosition(), "Method " + getSimpleName() + " of " + registry.getQualifiedName() + ": " + e.getMessage());
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

  /**
   * Generate some example XML for this root element.
   *
   * @return Some example XML.
   */
  public String generateExampleXml() {
    try {
      String namespace = getNamespace();
      EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
      String prefix = namespace == null ? null : model.getNamespacesToPrefixes().get(namespace);
      org.jdom.Element rootElement = new org.jdom.Element(getName(), org.jdom.Namespace.getNamespace(prefix, namespace));
      TypeDeclaration elementTypeDeclaration = getElementTypeDeclaration();
      if (elementTypeDeclaration instanceof ClassDeclaration) {
        TypeDefinition typeDef = model.findTypeDefinition((ClassDeclaration) elementTypeDeclaration);
        if (typeDef != null) {
          typeDef.generateExampleXml(rootElement);
        }
        else {
          rootElement.addContent(new Comment("..."));
        }
      }
      else {
        rootElement.addContent(new Comment("..."));
      }
      org.jdom.Document document = new org.jdom.Document(rootElement);

      XMLOutputter out = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
      StringWriter sw = new StringWriter();
      out.output(document, sw);
      sw.flush();
      return sw.toString();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Generate some example JSON for this root element.
   *
   * @return Some example JSON for this root element.
   */
  public String generateExampleJson() {
    try {
      TypeDeclaration elementTypeDeclaration = getElementTypeDeclaration();
      if (elementTypeDeclaration instanceof ClassDeclaration) {
        EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
        TypeDefinition typeDef = model.findTypeDefinition((ClassDeclaration) elementTypeDeclaration);
        if (typeDef != null) {
          ObjectNode node = typeDef.generateExampleJson();
          StringWriter sw = new StringWriter();
          JsonGenerator generator = new JsonFactory().createJsonGenerator(sw);
          generator.useDefaultPrettyPrinter();
          node.serialize(generator, null);
          generator.flush();
          sw.flush();
          return sw.toString();
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    return "";
  }
}
