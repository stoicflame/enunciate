package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceEntityParameter;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;

import javax.lang.model.element.AnnotationMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class RequestEntityImpl implements Entity {

  private final ResourceMethod resourceMethod;
  private final ResourceEntityParameter entityParameter;

  public RequestEntityImpl(ResourceMethod resourceMethod, ResourceEntityParameter entityParameter) {
    this.resourceMethod = resourceMethod;
    this.entityParameter = entityParameter;
  }

  @Override
  public String getDescription() {
    return this.entityParameter.getDocValue();
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    Set<String> consumes = this.resourceMethod.getConsumesMediaTypes();
    ArrayList<MediaTypeDescriptor> mts = new ArrayList<MediaTypeDescriptor>(consumes.size());
    for (String mt : consumes) {
      boolean descriptorFound = false;
      DecoratedTypeMirror type = (DecoratedTypeMirror) this.entityParameter.getType();
      for (Syntax syntax : this.resourceMethod.getContext().getContext().getApiRegistry().getSyntaxes()) {
        MediaTypeDescriptor descriptor = syntax.findMediaTypeDescriptor(mt, type);
        if (descriptor != null) {
          mts.add(descriptor);
          descriptorFound = true;
        }
      }

      if (!descriptorFound) {
        mts.add(new CustomMediaTypeDescriptor(mt));
      }
    }
    return mts;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.entityParameter.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.entityParameter.getJavaDoc();
  }
}
