package net.sf.enunciate.contract.rest;

import net.sf.jelly.apt.decorations.declaration.DecoratedParameterDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.enunciate.rest.annotations.ProperNoun;
import net.sf.enunciate.rest.annotations.Adjective;
import net.sf.enunciate.rest.annotations.NounValue;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import com.sun.mirror.declaration.ParameterDeclaration;

/**
 * A parameter declaration decorated as a REST parameter.  A REST parameter is one and only one of the following:
 *
 * <ul>
 *   <li>A proper noun</li>
 *   <li>A noun value</li>
 *   <li>An adjective</li>
 * </ul>
 * 
 * @author Ryan Heaton
 */
public class RESTParameter extends DecoratedParameterDeclaration {

  public RESTParameter(ParameterDeclaration delegate) {
    super(delegate);
  }

  /**
   * Whether this REST parameter is a proper noun.
   *
   * @return Whether this REST parameter is a proper noun.
   */
  public boolean isProperNoun() {
    return getAnnotation(ProperNoun.class) != null;
  }

  /**
   * Whether this REST parameter is the noun value.
   *
   * @return Whether this REST parameter is the noun value.
   */
  public boolean isNounValue() {
    return getAnnotation(NounValue.class) != null;
  }

  /**
   * The name of the adjective.
   *
   * @return The name of the adjective.
   */
  public String getAdjectiveName() {
    String adjectiveName = getSimpleName();

    Adjective adjectiveInfo = getAnnotation(Adjective.class);
    if (adjectiveInfo != null) {
      adjectiveName = adjectiveInfo.name();
    }

    return adjectiveName;
  }

  /**
   * Whether this REST parameter is a collection or an array.
   *
   * @return Whether this REST parameter is a collection or an array.
   */
  public boolean isCollectionType() {
    DecoratedTypeMirror type = (DecoratedTypeMirror) getType();
    return type.isArray() || type.isCollection();
  }

  /**
   * The XML type of this REST parameter.
   *
   * @return The XML type of this REST parameter.
   */
  public XmlTypeMirror getXmlType() {
    try {
      return ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(getType());
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
  }

}
