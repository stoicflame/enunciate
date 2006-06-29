package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

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

  public EnumTypeDefinition(EnumDeclaration delegate) {
    super(delegate);

    this.xmlEnum = getAnnotation(XmlEnum.class);
  }

  // Inherited.
  @Override
  public XmlTypeMirror getBaseType() {
    Class enumClass = java.lang.String.class;
    if (xmlEnum != null) {
      enumClass = xmlEnum.value();
    }

    try {
      return ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(enumClass);
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
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
        throw new ValidationException(enumConstant.getPosition(), "Duplicate enum value: " + value);
      }
    }

    return enumValues;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return true;
  }

  @Override
  public ValidationResult accept(Validator validator) {
    return validator.validateEnumType(this);
  }

}
