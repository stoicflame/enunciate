package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.type.MirroredTypeException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

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
    XmlTypeMirror typeMirror = KnownXmlType.STRING;

    if (xmlEnum != null) {
      try {
        try {
          Class enumClass = xmlEnum.value();
          typeMirror = ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(enumClass);
        }
        catch (MirroredTypeException e) {
          typeMirror = ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(e.getTypeMirror());
        }
      }
      catch (XmlTypeException e) {
        throw new ValidationException(getPosition(), e.getMessage());
      }
    }

    return typeMirror;
  }

  /**
   * The map of constant declarations (simple names) to their enum constant values.
   *
   * @return The map of constant declarations to their enum constant values.
   */
  public Map<String, String> getEnumValues() {
    Map<String, String> valueMap = new LinkedHashMap<String, String>();
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

      valueMap.put(enumConstant.getSimpleName(), value);
    }

    return valueMap;
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
