package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A fault that is declared potentially thrown in some web service call.
 *
 * @author Ryan Heaton
 */
public class WebFault extends DecoratedClassDeclaration implements WebMessage, WebMessagePart, ImplicitRootElement {

  private final javax.xml.ws.WebFault annotation;
  private final RootElementDeclaration explicitFaultBean;

  public WebFault(ClassDeclaration delegate) {
    super(delegate);

    this.annotation = getAnnotation(javax.xml.ws.WebFault.class);

    RootElementDeclaration explicitFaultBean = null;
    Collection<PropertyDeclaration> properties = getProperties();
    PropertyDeclaration faultInfoProperty = null;
    for (PropertyDeclaration propertyDeclaration : properties) {
      if ("faultInfo".equals(propertyDeclaration.getPropertyName())) {
        faultInfoProperty = propertyDeclaration;
        break;
      }
    }

    if ((faultInfoProperty != null) && (faultInfoProperty.getPropertyType() instanceof ClassType)) {
      AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
      Types typeUtils = env.getTypeUtils();
      DeclaredType stringType = typeUtils.getDeclaredType(env.getTypeDeclaration(String.class.getName()));
      DeclaredType throwableType = typeUtils.getDeclaredType(env.getTypeDeclaration(Throwable.class.getName()));
      ClassType faultInfoType = (ClassType) faultInfoProperty.getPropertyType();

      boolean messageConstructorFound = false;
      boolean messageAndThrowableConstructorFound = false;
      Collection<ConstructorDeclaration> constructors = getConstructors();
      for (ConstructorDeclaration constructor : constructors) {
        if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
          ParameterDeclaration[] parameters = constructor.getParameters().toArray(new ParameterDeclaration[0]);
          messageConstructorFound |= (parameters.length == 2 && parameters[0].getType().equals(stringType) && parameters[1].getType().equals(faultInfoType));
          messageAndThrowableConstructorFound |= (parameters.length == 3 && parameters[0].getType().equals(stringType) && parameters[1].getType().equals(faultInfoType) && parameters[2].getType().equals(throwableType));
        }
      }

      if (messageConstructorFound && messageAndThrowableConstructorFound) {
        ClassDeclaration explicitFaultClass = faultInfoType.getDeclaration();
        if (explicitFaultClass.getAnnotation(XmlRootElement.class) != null) {
          explicitFaultBean = new RootElementDeclaration(explicitFaultClass, null);
        }
        else {
          throw new ValidationException(getPosition(), "The faultInfo bean for a web fault must be a root element.  " + explicitFaultClass.getQualifiedName()
            + " must be annotated with @XmlRootElement.");
        }
      }
    }

    this.explicitFaultBean = explicitFaultBean;
  }

  /**
   * The message name of this fault.
   *
   * @return The message name of this fault.
   */
  public String getMessageName() {
    return getSimpleName();
  }

  /**
   * The message documentation for a fault is the documentation for its type.
   *
   * @return The documentation for its type.
   */
  public String getMessageDocs() {
    return getElementDocs();
  }

  /**
   * The element name of the implicit web fault bean, or null if this isn't an implicit web fault.
   *
   * @return The element name of the implicit web fault, or null.
   */
  public String getElementName() {
    String name = null;
    
    if (isImplicitSchemaElement()) {
      name = getSimpleName();

      if ((annotation != null) && (annotation.name() != null) && (!"".equals(annotation.name()))) {
        name = annotation.name();
      }
    }

    return name;
  }

  /**
   * The comments on the fault itself.
   *
   * @return The comments on the fault itself.
   */
  public String getElementDocs() {
    String docs = getJavaDoc().toString();
    if (docs.trim().length() == 0) {
      docs = null;
    }
    return docs;
  }

  /**
   * The part name of this web fault as it would appear in wsdl.
   *
   * @return The part name of this web fault as it would appear in wsdl.
   */
  public String getPartName() {
    return getSimpleName();
  }

  /**
   * @return null.
   */
  public String getPartDocs() {
    return null;
  }

  /**
   * The qualified name of the implicit fault bean of this web fault, or null if this web fault
   * does not define an implicit faul bean.
   *
   * @return The qualified name of the implicit fault bean of this web fault.
   */
  public String getImplicitFaultBeanQualifiedName() {
    String faultBean = null;

    if (isImplicitSchemaElement()) {
      faultBean = getPackage().getQualifiedName() + ".jaxws." + getSimpleName() + "Bean";

      if ((annotation != null) && (annotation.faultBean() != null) && (!"".equals(annotation.faultBean()))) {
        faultBean = annotation.faultBean();
      }
    }

    return faultBean;
  }

  /**
   * A web fault has an explicit fault bean if all three of the following are present:
   * <p/>
   * <ol>
   * <li>A getFaultInfo method that returns the bean instance of a class type.
   * <li>A constructor taking a message and bean instance.
   * <li>A constructor taking a message, a bean instance, and a cause.
   * </ol>
   *
   * @return The explicit fault bean of this web fault, if exists, or null otherwise.
   */
  public RootElementDeclaration getExplicitFaultBean() {
    return this.explicitFaultBean;
  }

  /**
   * @return {@link net.sf.enunciate.contract.jaxws.WebMessagePart.ParticleType#ELEMENT}
   */
  public ParticleType getParticleType() {
    return ParticleType.ELEMENT;
  }

  /**
   * The qname reference to the fault info.
   *
   * @return The qname reference to the fault info.
   */
  public QName getParticleQName() {
    if (this.explicitFaultBean != null) {
      return new QName(this.explicitFaultBean.getNamespace(), this.explicitFaultBean.getName());
    }
    else {
      return new QName(getTargetNamespace(), getElementName());
    }
  }

  /**
   * Gets the target namespace of the implicit fault bean, or null if this web fault defines
   * an explicit fault info bean.
   *
   * @return the target namespace of the implicit fault bean, or null.
   */
  public String getTargetNamespace() {
    String targetNamespace = null;

    if (isImplicitSchemaElement()) {
      if (annotation != null) {
        targetNamespace = annotation.targetNamespace();
      }

      if ((targetNamespace == null) || ("".equals(targetNamespace))) {
        targetNamespace = calculateNamespaceURI();
      }
    }

    return targetNamespace;
  }


  /**
   * Calculates a namespace URI for a given package.  Default implementation uses the algorithm defined in
   * section 3.2 of the jax-ws spec.
   *
   * @return The calculated namespace uri.
   */
  protected String calculateNamespaceURI() {
    PackageDeclaration pkg = getPackage();
    if ((pkg == null) || ("".equals(pkg.getQualifiedName()))) {
      throw new ValidationException(getPosition(), "A web service in no package must specify a target namespace.");
    }

    String[] tokens = pkg.getQualifiedName().split("\\.");
    String uri = "http://";
    for (int i = tokens.length - 1; i >= 0; i--) {
      uri += tokens[i];
      if (i != 0) {
        uri += ".";
      }
    }
    uri += "/";
    return uri;
  }

  /**
   * If there is an explicit fault bean, it will be a root schema element referencing its own type. Otherwise,
   * the type is anonymous.
   *
   * @return null.
   */
  public QName getTypeQName() {
    return null;
  }

  /**
   * This web fault defines an implicit schema element if it does not have an explicit fault bean.
   *
   * @return Whether this web fault defines an implicit schema element.
   */
  public boolean isImplicitSchemaElement() {
    return (this.explicitFaultBean == null);
  }

  /**
   * If this is an implicit fault bean, return the child elements.
   *
   * @return The child elements of the bean, or null if none.
   */
  public Collection<ImplicitChildElement> getChildElements() {
    if (!isImplicitSchemaElement()) {
      return Collections.emptyList();
    }

    Collection<ImplicitChildElement> childElements = new ArrayList<ImplicitChildElement>();

    EnunciateFreemarkerModel model = ((EnunciateFreemarkerModel) FreemarkerModel.get());
    for (PropertyDeclaration property : getAllProperties(this)) {
      String propertyName = property.getPropertyName();
      if (("cause".equals(propertyName)) || ("localizedMessage".equals(propertyName)) || ("stackTrace".equals(propertyName))) {
        continue;
      }

      try {
        DecoratedTypeMirror propertyType = (DecoratedTypeMirror) property.getPropertyType();
        XmlTypeMirror xmlType = model.getXmlType(propertyType);
        if (xmlType.isAnonymous()) {
          throw new ValidationException(property.getPosition(), "Implicit fault bean properties must not be anonymous types.");
        }
        int minOccurs = propertyType.isPrimitive() ? 1 : 0;
        String maxOccurs = propertyType.isArray() || propertyType.isCollection() ? "unbounded" : "1";

        childElements.add(new FaultBeanChildElement(property, xmlType, minOccurs, maxOccurs));
      }
      catch (XmlTypeException e) {
        throw new ValidationException(property.getPosition(), e.getMessage());
      }
    }

    return childElements;
  }

  /**
   * Gets all properties, including properties from the superclass.
   *
   * @param declaration The declaration from which to get all properties.
   * @return All properties.
   */
  protected Collection<PropertyDeclaration> getAllProperties(DecoratedClassDeclaration declaration) {
    ArrayList<PropertyDeclaration> properties = new ArrayList<PropertyDeclaration>();

    while ((declaration != null) && (!Object.class.getName().equals(declaration.getQualifiedName()))) {
      properties.addAll(declaration.getProperties());

      declaration = (DecoratedClassDeclaration) declaration.getSuperclass().getDeclaration();
    }

    return properties;
  }

  /**
   * There's only one part to a web fault.
   *
   * @return this.
   */
  public Collection<WebMessagePart> getParts() {
    return new ArrayList<WebMessagePart>(Arrays.asList(this));
  }

  /**
   * @return false
   */
  public boolean isInput() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isOutput() {
    return true;
  }

  /**
   * @return false
   */
  public boolean isHeader() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isFault() {
    return true;
  }

  public static class FaultBeanChildElement implements ImplicitChildElement {

    private final PropertyDeclaration property;
    private final XmlTypeMirror xmlType;
    private final int minOccurs;
    private final String maxOccurs;

    public FaultBeanChildElement(PropertyDeclaration property, XmlTypeMirror xmlType, int minOccurs, String maxOccurs) {
      this.property = property;
      this.xmlType = xmlType;
      this.minOccurs = minOccurs;
      this.maxOccurs = maxOccurs;
    }

    public PropertyDeclaration getProperty() {
      return property;
    }

    public String getElementName() {
      return property.getPropertyName();
    }

    public String getElementDocs() {
      String docs = property.getJavaDoc().toString();
      if (docs.trim().length() == 0) {
        docs = null;
      }
      return docs;
    }

    public QName getTypeQName() {
      return xmlType.getQname();
    }

    public int getMinOccurs() {
      return minOccurs;
    }

    public String getMaxOccurs() {
      return maxOccurs;
    }

    public TypeMirror getType() {
      return getProperty().getPropertyType();
    }

  }

}
