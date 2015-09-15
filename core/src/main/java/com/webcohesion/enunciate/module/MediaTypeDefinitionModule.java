package com.webcohesion.enunciate.module;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface MediaTypeDefinitionModule {

  void setDefaultDataTypeDetectionStrategy(ApiRegistryProviderModule.DataTypeDetectionStrategy strategy);

  void addDataTypeDefinitions(TypeMirror type, Set<String> declaredMediaTypes, LinkedList<Element> contextStack);
}
