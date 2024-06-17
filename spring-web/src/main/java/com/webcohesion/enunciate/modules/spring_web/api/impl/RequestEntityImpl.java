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
package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.modules.spring_web.model.ResourceEntityParameter;
import com.webcohesion.enunciate.modules.spring_web.model.RequestMapping;
import com.webcohesion.enunciate.modules.spring_web.model.formdata.FormDataMediaTypeDescriptor;
import com.webcohesion.enunciate.util.BeanValidationUtils;
import com.webcohesion.enunciate.util.ExampleUtils;
import com.webcohesion.enunciate.util.MediaTypeUtils;
import com.webcohesion.enunciate.util.TypeHintUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class RequestEntityImpl implements Entity {

  private final RequestMapping requestMapping;
  private final ResourceEntityParameter entityParameter;
  private final ApiRegistrationContext registrationContext;

  public RequestEntityImpl(RequestMapping requestMapping, ResourceEntityParameter entityParameter, ApiRegistrationContext registrationContext) {
    this.requestMapping = requestMapping;
    this.entityParameter = entityParameter;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getDescription() {
    return this.entityParameter.getDocValue(this.registrationContext.getTagHandler());
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    Set<String> consumes = this.requestMapping.getConsumesMediaTypes();
    ArrayList<MediaTypeDescriptor> mts = new ArrayList<MediaTypeDescriptor>(consumes.size());
    for (String mt : consumes) {
      boolean descriptorFound = false;
      DecoratedTypeMirror type = (DecoratedTypeMirror) this.entityParameter.getType();
      for (Syntax syntax : this.requestMapping.getContext().getContext().getApiRegistry().getSyntaxes(this.registrationContext)) {
        MediaTypeDescriptor descriptor = syntax.findMediaTypeDescriptor(mt, type);
        if (descriptor != null) {
          mts.add(new MediaTypeDescriptorImpl(descriptor, loadExample(syntax, descriptor)));
          descriptorFound = true;
        }
      }
      
      if (!descriptorFound && MediaTypeUtils.isUrlEncodedFormData(mt)) {
        mts.add(new FormDataMediaTypeDescriptor(mt, this.requestMapping, this.registrationContext));
        descriptorFound = true;
      }

      if (!descriptorFound) {
        CustomMediaTypeDescriptor descriptor = new CustomMediaTypeDescriptor(mt);
        descriptor.setDataType(new CustomDataTypeReference(BaseType.fromType(this.entityParameter.getType())));
        CustomSyntax syntax = new CustomSyntax(descriptor);
        descriptor.setExample(loadExample(syntax, descriptor));
        mts.add(descriptor);
      }
    }
    return mts;
  }

  protected Example loadExample(Syntax syntax, MediaTypeDescriptor descriptor) {
    Example example = ExampleUtils.loadCustomExample(syntax, "requestExample", this.requestMapping, this.requestMapping.getContext().getContext());

    if (example == null) {
      example = descriptor.getExample();
      if (this.entityParameter != null) {
        DocumentationExample documentationExample = this.entityParameter.getAnnotation(DocumentationExample.class);
        if (documentationExample != null) {
          TypeMirror typeHint = TypeHintUtils.getTypeHint(documentationExample.type(), this.requestMapping.getContext().getContext().getProcessingEnvironment(), null);
          if (typeHint instanceof DeclaredType) {
            Element element = ((DeclaredType) typeHint).asElement();
            if (element instanceof TypeElement) {
              List<DataType> dataTypes = syntax.findDataTypes(((TypeElement) element).getQualifiedName().toString());
              if (dataTypes != null && !dataTypes.isEmpty()) {
                example = dataTypes.get(0).getExample();
              }
            }
          }
        }
      }
    }
    return example;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.entityParameter.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.entityParameter.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.entityParameter.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public boolean isRequired() {
    return BeanValidationUtils.isNotNull(entityParameter.getDelegate(), this.requestMapping.getContext().getContext().getProcessingEnvironment());
  }
}
