/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.services.Parameter;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxws.model.WebParam;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ParameterImpl implements Parameter {

  private final WebParam param;
  private ApiRegistrationContext registrationContext;

  public ParameterImpl(WebParam param, ApiRegistrationContext registrationContext) {
    this.param = param;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getName() {
    String name = this.param.getSimpleName().toString();
    if (!name.equals(param.getBaseParamName())) {
      name += " (" + param.getBaseParamName() + ")";
    }
    return name;
  }

  @Override
  public String getDescription() {
    return this.param.getDocValue();
  }

  @Override
  public DataTypeReference getDataType() {
    return new DataTypeReferenceImpl(this.param.getXmlType(), false, registrationContext);
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.param.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.param.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.param.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.param, this.param.getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
