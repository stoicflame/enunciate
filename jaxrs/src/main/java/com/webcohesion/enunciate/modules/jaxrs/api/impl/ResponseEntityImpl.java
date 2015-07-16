package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceRepresentationMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ResponseEntityImpl implements Entity {

  private ResourceMethod resourceMethod;
  private ResourceRepresentationMetadata responseMetadata;

  public ResponseEntityImpl(ResourceMethod resourceMethod, ResourceRepresentationMetadata responseMetadata) {
    this.resourceMethod = resourceMethod;
    this.responseMetadata = responseMetadata;
  }

  @Override
  public String getDescription() {
    return this.responseMetadata.getDocValue();
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    Set<String> produces = resourceMethod.getProducesMediaTypes();
    ArrayList<MediaTypeDescriptor> mts = new ArrayList<MediaTypeDescriptor>(produces.size());
    for (String mt : produces) {
      DecoratedTypeMirror type = (DecoratedTypeMirror) this.responseMetadata.getDelegate();
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
