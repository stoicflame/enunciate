package net.sf.enunciate.contract.rest;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.ReferenceType;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.rest.annotations.Noun;
import net.sf.enunciate.rest.annotations.Verb;
import net.sf.enunciate.rest.annotations.VerbType;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A REST method.
 *
 * @author Ryan Heaton
 */
public class RESTMethod extends DecoratedMethodDeclaration {

  private final RESTParameter properNoun;
  private final RESTParameter nounValue;
  private final Collection<RESTParameter> adjectives;
  private final Collection<RESTError> RESTErrors;

  public RESTMethod(MethodDeclaration delegate) {
    super(delegate);

    RESTParameter properNoun = null;
    RESTParameter nounValue = null;
    this.adjectives = new ArrayList<RESTParameter>();
    for (ParameterDeclaration parameterDeclaration : getParameters()) {
      RESTParameter restParameter = new RESTParameter(parameterDeclaration);
      if (restParameter.isProperNoun()) {
        if (properNoun != null) {
          throw new ValidationException(properNoun.getPosition(), "REST method has more than one proper noun.  The other found at " + restParameter.getPosition());
        }
        
        properNoun = restParameter;
      }
      else if (restParameter.isNounValue()) {
        if (nounValue != null) {
          throw new ValidationException(nounValue.getPosition(), "REST method has more than one noun value.  The other found at " + restParameter.getPosition());
        }

        nounValue = restParameter;
      }
      else {
        adjectives.add(restParameter);
      }
    }

    this.nounValue = nounValue;
    this.properNoun = properNoun;

    this.RESTErrors = new ArrayList<RESTError>();
    for (ReferenceType referenceType : getThrownTypes()) {
      ClassDeclaration throwableDeclaration = ((ClassType) referenceType).getDeclaration();
      this.RESTErrors.add(new RESTError(throwableDeclaration));
    }

  }

  /**
   * The noun for this method.
   *
   * @return The noun for this method.
   */
  public String getNoun() {
    String noun = getSimpleName();

    Noun nounInfo = getAnnotation(Noun.class);
    if ((nounInfo != null) && (!"".equals(nounInfo.value()))) {
      noun = nounInfo.value();
    }

    return noun;
  }

  /**
   * The verb for this method.
   *
   * @return The verb for this method.
   */
  public VerbType getVerb() {
    VerbType verb = VerbType.read;

    Verb verbInfo = getAnnotation(Verb.class);
    if (verbInfo != null) {
      verb = verbInfo.value();
    }

    return verb;
  }

  /**
   * The proper noun for this method.
   *
   * @return The proper noun for this method.
   */
  public RESTParameter getProperNoun() {
    return this.properNoun;
  }

  /**
   * The noun value for this method.
   *
   * @return The noun value for this method.
   */
  public RESTParameter getNounValue() {
    return this.nounValue;
  }

  /**
   * The adjectives for this REST method.
   *
   * @return The adjectives for this REST method.
   */
  public Collection<RESTParameter> getAdjectives() {
    return adjectives;
  }
}
