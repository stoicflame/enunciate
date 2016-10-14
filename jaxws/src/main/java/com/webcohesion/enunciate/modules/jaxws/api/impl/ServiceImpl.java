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

import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.services.Operation;
import com.webcohesion.enunciate.api.services.Service;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.Label;
import com.webcohesion.enunciate.modules.jaxws.model.EndpointInterface;
import com.webcohesion.enunciate.modules.jaxws.model.WebMethod;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ServiceImpl implements Service {

  private final EndpointInterface ei;
  private final String contextPath;

  public ServiceImpl(EndpointInterface ei, String contextPath) {
    this.ei = ei;
    this.contextPath = contextPath;
  }


  @Override
  public String getLabel() {
    Label label = this.ei.getAnnotation(Label.class);
    if (label != null) {
      return label.value();
    }

    JavaDoc.JavaDocTagList tags = this.ei.getJavaDoc().get("label");
    if (tags != null && tags.size() > 0) {
      String tag = tags.get(0).trim();
      if (!tag.isEmpty()) {
        return tag;
      }
    }


    String serviceName = this.ei.getServiceName();
    if (serviceName.equals(this.ei.getSimpleName() + "Service")) {
      serviceName = this.ei.getSimpleName().toString();
    }
    return serviceName;
  }

  @Override
  public String getPath() {
    return this.contextPath + this.ei.getPath();
  }

  @Override
  public String getNamespace() {
    return this.ei.getTargetNamespace();
  }

  @Override
  public ServiceGroup getGroup() {
    return this.ei.getContext().getWsdls().get(this.ei.getTargetNamespace());
  }

  @Override
  public String getSlug() {
    return "service_" + this.ei.getContext().getJaxbContext().getNamespacePrefixes().get(this.ei.getTargetNamespace()) + "_" + this.ei.getServiceName();
  }

  @Override
  public String getDescription() {
    return this.ei.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.ei);
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.ei.getJavaDoc().get("since");
    return tags == null ? null : tags.toString();
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.ei.getJavaDoc().get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public List<? extends Operation> getOperations() {
    ArrayList<Operation> operations = new ArrayList<Operation>();
    for (WebMethod webMethod : this.ei.getWebMethods()) {
      operations.add(new OperationImpl(webMethod, this));
    }
    return operations;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.ei.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.ei.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.ei.getJavaDoc();
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.ei, this.ei.getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
