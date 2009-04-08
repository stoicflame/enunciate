/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.jaxws;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ArrayType;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;
import org.codehaus.enunciate.contract.jaxb.ImplicitRootElement;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterUtil;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.util.MapType;
import org.codehaus.enunciate.util.MapTypeUtil;
import org.codehaus.enunciate.soap.annotations.WebFaultPropertyOrder;

import javax.xml.namespace.QName;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

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
      ClassType faultInfoType = (ClassType) faultInfoProperty.getPropertyType();
      if (faultInfoType.getDeclaration() == null) {
        throw new ValidationException(getPosition(), "Class not found: " + faultInfoType + ".");
      }

      boolean messageConstructorFound = false;
      boolean messageAndThrowableConstructorFound = false;
      Collection<ConstructorDeclaration> constructors = getConstructors();
      for (ConstructorDeclaration constructor : constructors) {
        if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
          ParameterDeclaration[] parameters = constructor.getParameters().toArray(new ParameterDeclaration[constructor.getParameters().size()]);
          if (parameters.length >= 2) {
            DecoratedTypeMirror param0Type = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(parameters[0].getType());
            DecoratedTypeMirror param1Type = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(parameters[1].getType());
            if (parameters.length == 2) {
              messageConstructorFound |= param0Type.isInstanceOf(String.class.getName()) && param1Type.isInstanceOf(faultInfoType.getDeclaration().getQualifiedName());
            }
            else if (parameters.length == 3) {
              DecoratedTypeMirror param2Type = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(parameters[2].getType());
              messageAndThrowableConstructorFound |= param0Type.isInstanceOf(String.class.getName())
                && param1Type.isInstanceOf(faultInfoType.getDeclaration().getQualifiedName())
                && param2Type.isInstanceOf(Throwable.class.getName());
            }
          }
        }
      }

      if (messageConstructorFound && messageAndThrowableConstructorFound) {
        explicitFaultBean = new RootElementDeclaration(faultInfoType.getDeclaration(), null);
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
   * @return {@link org.codehaus.enunciate.contract.jaxws.WebMessagePart.ParticleType#ELEMENT}
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

    Set<ImplicitChildElement> childElements = new TreeSet<ImplicitChildElement>(new Comparator<ImplicitChildElement>() {
      public int compare(ImplicitChildElement o1, ImplicitChildElement o2) {
        return o1.getElementName().compareTo(o2.getElementName());
      }
    });

    for (PropertyDeclaration property : getAllFaultProperties(this)) {
      String propertyName = property.getPropertyName();
      if (("cause".equals(propertyName)) || ("localizedMessage".equals(propertyName)) || ("stackTrace".equals(propertyName))) {
        continue;
      }

      childElements.add(new FaultBeanChildElement(property, this));
    }

    final WebFaultPropertyOrder propOrder = getAnnotation(WebFaultPropertyOrder.class);
    if (propOrder != null) {
      Set<ImplicitChildElement> resorted = new TreeSet<ImplicitChildElement>(new Comparator<ImplicitChildElement>() {
        public int compare(ImplicitChildElement o1, ImplicitChildElement o2) {
          int index1 = -1;
          int index2 = -1;
          for (int i = 0; i < propOrder.value().length; i++) {
            String prop = propOrder.value()[i];
            if (o1.getElementName().equals(prop)) {
              index1 = i;
            }
            if (o2.getElementName().equals(prop)) {
              index2 = i;
            }
          }


          if (index1 < 0) {
            throw new ValidationException(WebFault.this.getPosition(), "@WebFaultPropertyOrder doesn't specify a property '" + o1.getElementName() + "'.");
          }
          else if (index2 < 0) {
            throw new ValidationException(WebFault.this.getPosition(), "@WebFaultPropertyOrder doesn't specify a property '" + o2.getElementName() + "'.");
          }
          else {
            return index1 - index2;
          }
        }
      });
      resorted.addAll(childElements);
      childElements = resorted;
    }

    return childElements;
  }

  /**
   * Gets all properties, including properties from the superclass.
   *
   * @param declaration The declaration from which to get all properties.
   * @return All properties.
   */
  protected Collection<PropertyDeclaration> getAllFaultProperties(DecoratedClassDeclaration declaration) {
    ArrayList<PropertyDeclaration> properties = new ArrayList<PropertyDeclaration>();

    while ((declaration != null) && (!Object.class.getName().equals(declaration.getQualifiedName()))) {
      for (PropertyDeclaration property : declaration.getProperties()) {
        if (property.getGetter() != null &&
          property.getAnnotation(XmlTransient.class) == null &&
          property.getAnnotation(org.codehaus.enunciate.XmlTransient.class) == null) {
          //only the readable properties that are not marked with @XmlTransient
          properties.add(property);
        }
      }

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
    return false;
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

  public static class FaultBeanChildElement implements Adaptable, ImplicitChildElement {

    private final PropertyDeclaration property;
    private final int minOccurs;
    private final String maxOccurs;
    private final AdapterType adaperType;
    private final WebFault webFault;

    private FaultBeanChildElement(PropertyDeclaration property, WebFault webFault) {
      DecoratedTypeMirror propertyType = (DecoratedTypeMirror) property.getPropertyType();
      this.adaperType = AdapterUtil.findAdapterType(property.getGetter());
      int minOccurs = propertyType.isPrimitive() ? 1 : 0;
      boolean unbounded = propertyType.isCollection() || propertyType.isArray();
      if (propertyType.isArray()) {
        TypeMirror componentType = ((ArrayType) propertyType).getComponentType();
        //special case for byte[]
        if ((componentType instanceof PrimitiveType) && (((PrimitiveType) componentType).getKind() == PrimitiveType.Kind.BYTE)) {
          unbounded = false;
        }
      }
      String maxOccurs = unbounded ? "unbounded" : "1";

      this.property = property;
      this.minOccurs = minOccurs;
      this.maxOccurs = maxOccurs;
      this.webFault = webFault;
    }

    public PropertyDeclaration getProperty() {
      return property;
    }

    public String getElementName() {
      return property.getPropertyName();
    }

    public String getTargetNamespace() {
      return webFault.getTargetNamespace();
    }

    public String getElementDocs() {
      String docs = property.getJavaDoc().toString();
      if (docs.trim().length() == 0) {
        docs = null;
      }
      return docs;
    }

    public XmlType getXmlType() {
      try {
        XmlType xmlType = XmlTypeFactory.findSpecifiedType(this);
        if (xmlType == null) {
          xmlType = XmlTypeFactory.getXmlType(getType());
        }
        return xmlType;
      }
      catch (XmlTypeException e) {
        throw new ValidationException(property.getPosition(), "Error with property '" + property.getPropertyName() + "' of fault '" +
          webFault.getQualifiedName() + "'. " + e.getMessage());
      }
    }

    public QName getTypeQName() {
      return getXmlType().getQname();
    }

    public int getMinOccurs() {
      return minOccurs;
    }

    public String getMaxOccurs() {
      return maxOccurs;
    }

    public TypeMirror getType() {
      TypeMirror propertyType = property.getPropertyType();
      MapType mapType = MapTypeUtil.findMapType(propertyType);
      if (mapType != null) {
        propertyType = mapType;
      }
      return propertyType;
    }

    public boolean isAdapted() {
      return this.adaperType != null;
    }

    public AdapterType getAdapterType() {
      return this.adaperType;
    }
  }

}
