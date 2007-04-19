/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.contract.rest;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.ReferenceType;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeMirror;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.rest.annotations.Noun;
import org.codehaus.enunciate.rest.annotations.Verb;
import org.codehaus.enunciate.rest.annotations.VerbType;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

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
    int parameterPosition = 0;
    for (ParameterDeclaration parameterDeclaration : getParameters()) {
      RESTParameter restParameter = new RESTParameter(parameterDeclaration, parameterPosition++);
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

  /**
   * The errors possibly thrown by this REST method.
   *
   * @return The errors possibly thrown by this REST method.
   */
  public Collection<RESTError> getRESTErrors() {
    return RESTErrors;
  }

  /**
   * The XML type of the return value.
   *
   * @return The XML type of the return value.
   */
  public XmlTypeMirror getXMLReturnType() {
    try {
      return ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(null, getReturnType());
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
  }
}
