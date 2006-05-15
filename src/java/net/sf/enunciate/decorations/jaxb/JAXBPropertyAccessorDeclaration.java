package net.sf.enunciate.decorations.jaxb;

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import net.sf.jelly.apt.decorations.DeclarationDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A property accessor.  Represents the getter/setter pair (since annotation can reside on either,
 * but not both).
 *
 * @author Ryan Heaton
 */
public class JAXBPropertyAccessorDeclaration extends DecoratedMethodDeclaration implements JAXBAccessorDeclaration {

  private final DecoratedMethodDeclaration setter;
  private final Map<String, String> ns2prefix;

  public JAXBPropertyAccessorDeclaration(MethodDeclaration getter, MethodDeclaration setter, Map<String, String> ns2prefix) {
    super(getter);
    this.ns2prefix = ns2prefix;

    if (!isGetter()) {
      throw new IllegalArgumentException("I can only decorate a getter.");
    }
    DecoratedTypeMirror propertyType = (DecoratedTypeMirror) getReturnType();

    this.setter = (DecoratedMethodDeclaration) DeclarationDecorator.decorate(setter);
    if (this.setter == null) {
      if (!propertyType.isInstanceOf(List.class.getName())) {
        throw new IllegalArgumentException(getPosition() + ": A setter must be supplied along with this getter, unless it's a java.util.List");
      }
    }
    else {
      Collection<ParameterDeclaration> parameters = this.setter.getParameters();
      if ((parameters == null) || (parameters.size() != 1) || (!propertyType.equals(parameters.iterator().next().getType()))) {
        throw new IllegalStateException(this.setter.getPosition() + ": invalid setter for " + propertyType);
      }
    }

    if (getAnnotation(XmlTransient.class) != null) {
      throw new IllegalArgumentException("An xml-transient property cannot be an accessor.");
    }
  }

  public String getPropertyName() {
    return null;
  }

  //Inherited.
  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    A annotation = super.getAnnotation(annotationType);

    if ((this.setter != null) && (annotationType.getPackage().getName().startsWith(XmlElement.class.getPackage().getName()))) {
      //if this is in the "javax.xml.bind.annotation" package, we'll check the setter, too.

      A setterAnnotation = this.setter.getAnnotation(annotationType);
      if ((annotation != null) && (setterAnnotation != null)) {
        throw new IllegalStateException(getPosition() + ": annotation " + annotationType.getName() + " is on both the setter and the getter.");
      }

      annotation = setterAnnotation == null ? annotation : setterAnnotation;
    }

    return annotation;
  }

}
