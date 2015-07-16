package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceEntityParameter;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;

import java.util.ArrayList;
import java.util.List;
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
    return this.resourceMethod.getEntityParameter().getJavaDoc().toString();
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    Set<String> consumes = this.resourceMethod.getConsumesMediaTypes();
    ArrayList<MediaTypeDescriptor> mts = new ArrayList<MediaTypeDescriptor>(consumes.size());
    for (String mt : consumes) {
      DecoratedTypeMirror type = (DecoratedTypeMirror) this.entityParameter.getType();
      for (Syntax syntax : this.resourceMethod.getContext().getContext().getApiRegistry().getSyntaxes()) {
        MediaTypeDescriptor descriptor = syntax.findMediaTypeDescriptor(mt, type);
        if (descriptor != null) {
          mts.add(descriptor);
        }
      }
    }
    return mts;
  }
}
