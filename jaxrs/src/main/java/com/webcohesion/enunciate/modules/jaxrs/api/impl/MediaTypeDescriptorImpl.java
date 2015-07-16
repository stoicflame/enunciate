package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

/**
 * @author Ryan Heaton
 */
public class MediaTypeDescriptorImpl implements MediaTypeDescriptor {

  private final String mediaType;
  private final DecoratedTypeMirror typeMirror;
  private final ApiRegistry registry;

  public MediaTypeDescriptorImpl(String mediaType, DecoratedTypeMirror typeMirror, ApiRegistry registry) {
    this.mediaType = mediaType;
    this.typeMirror = typeMirror;
    this.registry = registry;
  }

  @Override
  public String getMediaType() {
    return this.mediaType;
  }

  @Override
  public DataTypeReference getDataType() {
    if (this.typeMirror != null) {
      for (Syntax syntax : this.registry.getSyntaxes()) {
        if (syntax.isCompatible(this.mediaType)) {
          return syntax.findDataTypeReference(this.typeMirror);
        }
      }
    }

    return null;
  }
}
