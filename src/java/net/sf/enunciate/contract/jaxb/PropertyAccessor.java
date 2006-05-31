package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.TypeMirror;
import net.sf.enunciate.contract.ValidationException;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * A property accessor.  Represents the getter/setter pair (since annotation can reside on either,
 * but not both).
 *
 * @author Ryan Heaton
 */
public class PropertyAccessor extends DecoratedMethodDeclaration implements Accessor {

  private final PropertyGetter getter;
  private final PropertySetter setter;

  public PropertyAccessor(PropertyGetter getter, PropertySetter setter) {
    super(getter);
    DecoratedTypeMirror propertyType = (DecoratedTypeMirror) getReturnType();

    this.getter = getter;
    this.setter = setter;
    if (this.setter == null) {
      if (!propertyType.isInstanceOf(List.class.getName())) {
        throw new ValidationException(getPosition() + ": A setter must be supplied along with this getter, unless it's a java.util.List");
      }
    }
    else {
      Collection<ParameterDeclaration> parameters = this.setter.getParameters();
      if ((parameters == null) || (parameters.size() != 1) || (!propertyType.equals(parameters.iterator().next().getType()))) {
        throw new ValidationException(this.setter.getPosition() + ": invalid setter for " + propertyType);
      }
    }

    if (getAnnotation(XmlTransient.class) != null) {
      throw new ValidationException("An xml-transient property cannot be an accessor.");
    }
  }

  /**
   * The property name of the accessor.
   *
   * @return The property name of the accessor.
   */
  public String getPropertyName() {
    String explicitPropertyName = this.getter.getExplicitPropertyName();
    if (explicitPropertyName == null) {
      explicitPropertyName = this.setter.getExplicitPropertyName();
    }

    if (explicitPropertyName == null) {
      return super.getPropertyName();
    }
    else {
      return explicitPropertyName;
    }
  }

  // Inherited.
  public boolean isXmlValue() {
    return getAnnotation(XmlValue.class) != null;
  }

  // Inherited.
  public boolean isXmlMixed() {
    return getAnnotation(XmlMixed.class) != null;
  }

  // Inherited.
  public TypeMirror getPropertyType() {
    return getReturnType();
  }

  //Inherited.
  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    A annotation = super.getAnnotation(annotationType);

    if ((this.setter != null) && (annotationType.getPackage().getName().startsWith(XmlElement.class.getPackage().getName()))) {
      //if this is in the "javax.xml.bind.annotation" package, we'll check the setter, too.

      A setterAnnotation = this.setter.getAnnotation(annotationType);
      if ((annotation != null) && (setterAnnotation != null)) {
        throw new ValidationException(getPosition() + ": annotation " + annotationType.getName() + " is on both the setter and the getter.");
      }

      annotation = setterAnnotation == null ? annotation : setterAnnotation;
    }

    return annotation;
  }

}
