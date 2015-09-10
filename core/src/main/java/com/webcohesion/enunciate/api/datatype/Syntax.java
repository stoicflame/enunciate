package com.webcohesion.enunciate.api.datatype;

import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface Syntax extends Comparable<Syntax> {

  String getId();

  String getSlug();

  String getLabel();

  boolean isEmpty();

  List<Namespace> getNamespaces();

  MediaTypeDescriptor findMediaTypeDescriptor(String mediaType, DecoratedTypeMirror typeMirror);

}
