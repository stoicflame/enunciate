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
import com.webcohesion.enunciate.api.services.Fault;
import com.webcohesion.enunciate.api.services.Operation;
import com.webcohesion.enunciate.api.services.Parameter;
import com.webcohesion.enunciate.api.services.Service;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxws.model.*;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class OperationImpl implements Operation {

  private final WebMethod webMethod;
  private final ServiceImpl service;
  private ApiRegistrationContext registrationContext;

  public OperationImpl(WebMethod webMethod, ServiceImpl service, ApiRegistrationContext registrationContext) {
    this.webMethod = webMethod;
    this.service = service;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getName() {
    return this.webMethod.getOperationName();
  }

  @Override
  public String getSlug() {
    return this.service.getSlug() + "_method_" + getName();
  }

  @Override
  public String getDescription() {
    return this.webMethod.getJavaDoc(this.registrationContext.getTagHandler()).toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.webMethod);
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.webMethod.getJavaDoc().get("since");
    return tags == null ? null : tags.toString();
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.webMethod.getJavaDoc().get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public DataTypeReference getReturnType() {
    if (this.webMethod.isOneWay() || this.webMethod.getReturnType().isVoid()) {
      return null;
    }
    else {
      return new DataTypeReferenceImpl(this.webMethod.getWebResult().getXmlType(), "unbounded".equals(this.webMethod.getWebResult().getMaxOccurs()), registrationContext);
    }
  }

  @Override
  public List<? extends Parameter> getInputParameters() {
    List<Parameter> params = new ArrayList<Parameter>();
    for (WebParam param : this.webMethod.getWebParameters()) {
      if (param.isInput()) {
        params.add(new ParameterImpl(param, registrationContext));
      }
    }

    return params;
  }

  @Override
  public List<? extends Parameter> getOutputParameters() {
    List<Parameter> params = new ArrayList<Parameter>();
    for (WebParam param : this.webMethod.getWebParameters()) {
      if (param.isOutput()) {
        params.add(new ParameterImpl(param, registrationContext));
      }
    }

    return params;
  }

  @Override
  public String getReturnDescription() {
    DecoratedTypeMirror returnType = (DecoratedTypeMirror) this.webMethod.getReturnType();
    return returnType.getDocValue();
  }

  @Override
  public List<? extends Fault> getFaults() {
    List<Fault> faults = new ArrayList<Fault>();
    for (WebFault webFault : this.webMethod.getWebFaults()) {
      faults.add(new FaultImpl(webFault));
    }

    return faults;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.webMethod.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.webMethod.getAnnotations();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.webMethod.getFacets();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.webMethod.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.webMethod, this.webMethod.getContext().getContext().getConfiguration().getAnnotationStyles());
  }

  public WebMethod getWebMethod() {
    return webMethod;
  }

  @Override
  public Service getService() {
    return service;
  }
}
