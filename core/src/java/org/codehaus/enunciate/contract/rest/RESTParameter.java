package org.codehaus.enunciate.contract.rest;

import net.sf.jelly.apt.decorations.declaration.DecoratedParameterDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.rest.annotations.ProperNoun;
import org.codehaus.enunciate.rest.annotations.Adjective;
import org.codehaus.enunciate.rest.annotations.NounValue;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeMirror;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
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

  private final int parameterPosition;

  public RESTParameter(ParameterDeclaration delegate, int parameterPosition) {
    super(delegate);

    this.parameterPosition = parameterPosition;
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
    String adjectiveName = "arg" + parameterPosition;

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
