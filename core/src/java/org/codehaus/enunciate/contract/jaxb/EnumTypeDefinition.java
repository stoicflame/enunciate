package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.type.MirroredTypeException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeMirror;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.Validator;
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
  private final Map<String, String> enumValues;

  public EnumTypeDefinition(EnumDeclaration delegate) {
    super(delegate);

    this.xmlEnum = getAnnotation(XmlEnum.class);
    enumValues = new LinkedHashMap<String, String>();
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

      this.enumValues.put(enumConstant.getSimpleName(), value);
    }

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
    return this.enumValues;
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
