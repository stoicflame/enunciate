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
package com.webcohesion.enunciate.modules.jaxrs.model.formdata;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceParameter;
import com.webcohesion.enunciate.util.MediaTypeUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ryan Heaton
 */
public class FormDataMediaTypeDescriptor implements MediaTypeDescriptor {

  private final String mediaType;
  private final float qs;
  
  private final ResourceMethod method;
  
  private final ApiRegistrationContext registrationContext;

  public FormDataMediaTypeDescriptor(String mediaType, float qs, ResourceMethod method, ApiRegistrationContext registrationContext) {
    this.mediaType = mediaType;
    this.qs = qs;
    this.method = method;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getMediaType() {
    return this.mediaType;
  }

  @Override
  public DataTypeReference getDataType() {
    return null;
  }
  
  public DataType getAnonymousDataType() { return new FormDataType(); }

  @Override
  public String getSyntax() {
    return null;
  }

  @Override
  public float getQualityOfSourceFactor() {
    return this.qs;
  }

  public ResourceMethod getMethod() {
    return method;
  }

  private String getLabel() {
    return this.method.getLabel() + " Data";
  }

  private String getSlug() {
    return method.getSlug() + "_data";
  }

  @Override
  public Example getExample() {
    return MediaTypeUtils.isMultipartFormData(this.mediaType) ? new MultipartFormEncodedExample() : new UrlEncodedExample();
  }
  
  private class FormDataType implements DataType {

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return method.getAnnotation(annotationType);
    }

    @Override
    public Map<String, AnnotationMirror> getAnnotations() {
      return method.getAnnotations();
    }

    @Override
    public Set<String> getStyles() {
      return Collections.emptySet();
    }

    @Override
    public String getLabel() {
      return FormDataMediaTypeDescriptor.this.getLabel();
    }

    @Override
    public String getSlug() {
      return FormDataMediaTypeDescriptor.this.getSlug();
    }

    @Override
    public String getDescription() {
      return method.getJavaDoc(registrationContext.getTagHandler()).toString();
    }

    @Override
    public String getDeprecated() {
      return null;
    }

    @Override
    public Namespace getNamespace() {
      return null;
    }

    @Override
    public Syntax getSyntax() {
      return null;
    }

    @Override
    public BaseType getBaseType() {
      return BaseType.object;
    }

    @Override
    public List<DataTypeReference> getSupertypes() {
      return Collections.emptyList();
    }

    @Override
    public Set<DataTypeReference> getInterfaces() {
      return Collections.emptySet();
    }

    @Override
    public List<DataTypeReference> getSubtypes() {
      return Collections.emptyList();
    }

    @Override
    public String getSince() {
      return null;
    }

    @Override
    public List<String> getSeeAlso() {
      return Collections.emptyList();
    }

    @Override
    public String getVersion() {
      return null;
    }

    @Override
    public boolean isAbstract() {
      return false;
    }

    @Override
    public Example getExample() {
      return FormDataMediaTypeDescriptor.this.getExample();
    }

    @Override
    public List<? extends Value> getValues() {
      return null;
    }

    @Override
    public Property findProperty(String name) {
      return getProperties().stream().filter(p -> StringUtils.equals(p.getName(), name)).findFirst().get();
    }

    @Override
    public List<? extends Property> getProperties() {
      return method.getResourceParameters().stream().filter(param -> StringUtils.contains(param.getTypeName(), "form")).map(ParameterProperty::new).collect(Collectors.toList());
    }

    @Override
    public Value findValue(String name) {
      return null;
    }

    @Override
    public Map<String, String> getPropertyMetadata() {
      return Collections.emptyMap();
    }

    @Override
    public JavaDoc getJavaDoc() {
      return method.getJavaDoc(registrationContext.getTagHandler());
    }

    @Override
    public Element getJavaElement() {
      return method;
    }

    @Override
    public Set<Facet> getFacets() {
      return method.getFacets();
    }
  }
  
  private class ParameterProperty implements Property {
    
    private final ResourceParameter parameter;

    public ParameterProperty(ResourceParameter parameter) {
      this.parameter = parameter;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return this.parameter.getAnnotation(annotationType);
    }

    @Override
    public Map<String, AnnotationMirror> getAnnotations() {
      return this.parameter.getAnnotations();
    }

    @Override
    public Set<String> getStyles() {
      return Styles.gatherStyles(this.parameter, this.parameter.getContext().getContext().getContext().getConfiguration().getAnnotationStyles());
    }

    @Override
    public String getName() {
      return this.parameter.getParameterName();
    }

    @Override
    public String getDescription() {
      return getJavaDoc().toString();
    }

    @Override
    public DataTypeReference getDataType() {
      return new CustomDataTypeReference(BaseType.string);
    }

    @Override
    public String getDeprecated() {
      return null;
    }

    @Override
    public boolean isRequired() {
      return false;
    }

    @Override
    public boolean isReadOnly() {
      return false;
    }

    @Override
    public JavaDoc getJavaDoc() {
      return this.parameter.getJavaDoc(registrationContext.getTagHandler());
    }

    @Override
    public String getSince() {
      return null;
    }

    @Override
    public Set<Facet> getFacets() {
      return method.getFacets();
    }
  }
  
  private class UrlEncodedExample implements Example {
    @Override
    public String getLang() {
      return "txt";
    }

    @Override
    public String getBody() {
      return method.getResourceParameters().stream().filter(param -> StringUtils.contains(param.getTypeName(), "form")).map(param -> param.getParameterName() + "=...").collect(Collectors.joining("&"));
    }
  }

  private class MultipartFormEncodedExample implements Example {
    @Override
    public String getLang() {
      return "txt";
    }

    @Override
    public String getBody() {
      return method.getResourceParameters().stream().filter(param -> StringUtils.contains(param.getTypeName(), "form"))
         .map(param -> "-----boundary\nContent-Disposition: form-data; name=\"" + param.getParameterName() + "\"\n\n...").collect(Collectors.joining("\n"));
    }
  }
}
