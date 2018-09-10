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
import com.webcohesion.enunciate.api.datatype.CustomMediaTypeDescriptor;
import com.webcohesion.enunciate.api.datatype.CustomSyntax;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.modules.spring_web.model.RequestMapping;
import com.webcohesion.enunciate.modules.spring_web.model.ResourceRepresentationMetadata;
import com.webcohesion.enunciate.util.ExampleUtils;
import com.webcohesion.enunciate.util.TypeHintUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ResponseEntityImpl implements Entity {

  private RequestMapping requestMapping;
  private ResourceRepresentationMetadata responseMetadata;
  private final ApiRegistrationContext registrationContext;

  public ResponseEntityImpl(RequestMapping requestMapping, ResourceRepresentationMetadata responseMetadata, ApiRegistrationContext registrationContext) {
    this.requestMapping = requestMapping;
    this.responseMetadata = responseMetadata;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getDescription() {
    return this.responseMetadata.getDocValue(this.registrationContext.getTagHandler());
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    Set<String> produces = requestMapping.getProducesMediaTypes();
    ArrayList<MediaTypeDescriptor> mts = new ArrayList<MediaTypeDescriptor>(produces.size());
    for (String mt : produces) {
      boolean descriptorFound = false;
      DecoratedTypeMirror type = (DecoratedTypeMirror) this.responseMetadata.getDelegate();
      for (Syntax syntax : this.requestMapping.getContext().getContext().getApiRegistry().getSyntaxes(this.registrationContext)) {
        MediaTypeDescriptor descriptor = syntax.findMediaTypeDescriptor(mt, type);
        if (descriptor != null) {
          mts.add(new MediaTypeDescriptorImpl(descriptor, loadExample(syntax, descriptor)));
          descriptorFound = true;
        }
      }

      if (!descriptorFound) {
        CustomMediaTypeDescriptor descriptor = new CustomMediaTypeDescriptor(mt);
        CustomSyntax syntax = new CustomSyntax(descriptor);
        descriptor.setExample(loadExample(syntax, descriptor));
        mts.add(descriptor);
      }
    }
    return mts;
  }

  protected Example loadExample(Syntax syntax, MediaTypeDescriptor descriptor) {
    Example example = ExampleUtils.loadCustomExample(syntax, "responseExample", this.requestMapping, this.requestMapping.getContext().getContext());

    if (example == null) {
      example = descriptor.getExample();

      JavaDoc.JavaDocTagList tags = this.requestMapping.getJavaDoc().get("documentationType");
      if (tags != null && tags.size() > 0) {
        String tag = tags.get(0).trim();
        if (!tag.isEmpty()) {
          TypeElement typeElement = this.requestMapping.getContext().getContext().getProcessingEnvironment().getElementUtils().getTypeElement(tag);
          if (typeElement != null) {
            List<DataType> dataTypes = syntax.findDataTypes(typeElement.getQualifiedName().toString());
            if (dataTypes != null && !dataTypes.isEmpty()) {
              example = dataTypes.get(0).getExample();
            }
          }
          else {
            this.requestMapping.getContext().getContext().getLogger().warn("Invalid documentation type %s.", tag);
          }
        }
      }

      DocumentationExample documentationExample = this.requestMapping.getAnnotation(DocumentationExample.class);
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
    return example;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return Collections.emptyMap();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return JavaDoc.EMPTY;
  }

  @Override
  public boolean isRequired() {
    return true;
  }
}
