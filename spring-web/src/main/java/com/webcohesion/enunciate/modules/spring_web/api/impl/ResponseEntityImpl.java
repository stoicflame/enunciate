package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.spring_web.model.RequestMapping;
import com.webcohesion.enunciate.modules.spring_web.model.ResourceRepresentationMetadata;

import javax.lang.model.element.AnnotationMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ResponseEntityImpl implements Entity {

  private RequestMapping requestMapping;
  private ResourceRepresentationMetadata responseMetadata;

  public ResponseEntityImpl(RequestMapping requestMapping, ResourceRepresentationMetadata responseMetadata) {
    this.requestMapping = requestMapping;
    this.responseMetadata = responseMetadata;
  }

  @Override
  public String getDescription() {
    return this.responseMetadata.getDocValue();
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    Set<String> produces = requestMapping.getProducesMediaTypes();
    ArrayList<MediaTypeDescriptor> mts = new ArrayList<MediaTypeDescriptor>(produces.size());
    for (String mt : produces) {
      boolean descriptorFound = false;
      DecoratedTypeMirror type = (DecoratedTypeMirror) this.responseMetadata.getDelegate();
      for (Syntax syntax : this.requestMapping.getContext().getContext().getApiRegistry().getSyntaxes()) {
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
    return Collections.emptyMap();
  }
}
