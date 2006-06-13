package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.jaxb.validation.JAXBValidator;
import net.sf.jelly.apt.Context;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An enum type definition.
 *
 * @author Ryan Heaton
 */
public class EnumTypeDefinition extends SimpleTypeDefinition {

  private final XmlEnum xmlEnum;

  public EnumTypeDefinition(EnumDeclaration delegate, JAXBValidator validator) {
    super(delegate);

    this.xmlEnum = getAnnotation(XmlEnum.class);
    validator.validate(this);
  }

  // Inherited.
  @Override
  public TypeMirror getBaseType() {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Types types = env.getTypeUtils();
    TypeDeclaration declaration = env.getTypeDeclaration(xmlEnum.value().getName());
    return types.getDeclaredType(declaration);
  }

  /**
   * The enum constant values.
   *
   * @return The enum constant values.
   */
  public Set<String> getEnumValues() {
    Collection<EnumConstantDeclaration> enumConstants = ((EnumDeclaration) getDelegate()).getEnumConstants();
    HashSet<String> enumValues = new HashSet<String>(enumConstants.size());
    for (EnumConstantDeclaration enumConstant : enumConstants) {
      String value = enumConstant.getSimpleName();
      XmlEnumValue enumValue = enumConstant.getAnnotation(XmlEnumValue.class);
      if (enumValue != null) {
        value = enumValue.value();
      }

      if (!enumValues.add(value)) {
        throw new ValidationException(enumConstant.getPosition() + ": duplicate enum value: " + value);
      }
    }

    return enumValues;
  }

}
