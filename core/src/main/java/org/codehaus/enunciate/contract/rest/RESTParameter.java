/*
 * Copyright 2006-2008 Web Cohesion
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

import net.sf.jelly.apt.decorations.declaration.DecoratedParameterDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.rest.annotations.*;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.common.rest.RESTResourceParameter;
import org.codehaus.enunciate.contract.common.rest.RESTResourceParameterType;
import org.codehaus.enunciate.contract.common.rest.RESTResourcePayload;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.ArrayType;

import java.util.Collection;

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
public class RESTParameter extends DecoratedParameterDeclaration implements RESTResourceParameter, RESTResourcePayload {


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
   * Whether this REST parameter is a context parameter.
   *
   * @return Whether this REST parameter is a context parameter.
   */
  public boolean isContextParam() {
    return getAnnotation(ContextParameter.class) != null;
  }

  /**
   * Whether this REST parameter is a complex adjective.
   *
   * @return Whether this REST parameter is a complex adjective.
   */
  public boolean isComplexAdjective() {
    Adjective adjectiveInfo = getAnnotation(Adjective.class);
    return adjectiveInfo != null && adjectiveInfo.complex();
  }

  /**
   * Whether this REST parameter is a content type parameter.
   *
   * @return Whether this REST parameter is a content type parameter.
   */
  public boolean isContentTypeParameter() {
    return getAnnotation(ContentTypeParameter.class) != null;
  }

  /**
   * Whether this REST parameter is optional.
   *
   * @return Whether this REST parameter is optional.
   */
  public boolean isOptional() {
    if (isProperNoun()) {
      return getAnnotation(ProperNoun.class).optional();
    }
    else if (isNounValue()) {
      return getAnnotation(NounValue.class).optional();
    }
    else if (isContextParam()) {
      return false;
    }
    else if (getAnnotation(Adjective.class) != null) {
      return getAnnotation(Adjective.class).optional();
    }
    else if (getType() instanceof PrimitiveType) {
      return false;
    }

    return true;
  }

  /**
   * The name of the adjective.
   *
   * @return The name of the adjective.
   */
  public String getAdjectiveName() {
    String adjectiveName = getSimpleName();

    Adjective adjectiveInfo = getAnnotation(Adjective.class);
    if ((adjectiveInfo != null) && (!"##default".equals(adjectiveInfo.name()))) {
      adjectiveName = adjectiveInfo.name();
    }

    return adjectiveName;
  }

  /**
   * The name of the context parameter.
   *
   * @return The name of the context parameter.
   */
  public String getContextParameterName() {
    if (!isContextParam()) {
      throw new UnsupportedOperationException("Not a context parameter.");
    }

    return getAnnotation(ContextParameter.class).value();
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
  public XmlType getXmlType() {
    try {
      return XmlTypeFactory.getXmlType(getType());
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
  }

  /**
   * Whether the type of this REST parameter is custom.
   *
   * @return Whether the type of this REST parameter is custom.
   */
  public boolean isCustomType() {
    return isDataHandler((DecoratedTypeMirror) getType()) || isDataHandlers((DecoratedTypeMirror) getType());
  }

  private boolean isDataHandler(DecoratedTypeMirror type) {
    return type.isDeclared()
      && ((DeclaredType) type).getDeclaration() != null
      && "javax.activation.DataHandler".equals(((DeclaredType) type).getDeclaration().getQualifiedName());
  }

  private boolean isDataHandlers(DecoratedTypeMirror type) {
    if (type.isCollection()) {
      Collection<TypeMirror> typeArgs = ((DeclaredType) type).getActualTypeArguments();
      if ((typeArgs != null) && (typeArgs.size() == 1)) {
        return isDataHandler((DecoratedTypeMirror) typeArgs.iterator().next());
      }
    }
    else if (type.isArray()) {
      return isDataHandler((DecoratedTypeMirror) ((ArrayType) type).getComponentType());
    }
    return false;
  }

  // Inherited.
  public String getResourceParameterName() {
    return isProperNoun() ? getSimpleName() : isContextParam() ? getContextParameterName() : getAdjectiveName();
  }

  // Inherited.
  public RESTResourceParameterType getResourceParameterType() {
    return isProperNoun() || isContextParam() ? RESTResourceParameterType.PATH : RESTResourceParameterType.QUERY;
  }
}
