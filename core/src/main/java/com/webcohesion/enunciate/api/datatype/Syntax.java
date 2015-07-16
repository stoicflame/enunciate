package com.webcohesion.enunciate.api.datatype;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface Syntax {

  String getSlug();

  String getLabel();

  List<Namespace> getNamespaces();

  boolean isCompatible(String mediaType);

  DataTypeReference findDataTypeReference(DecoratedTypeMirror typeMirror);

}
