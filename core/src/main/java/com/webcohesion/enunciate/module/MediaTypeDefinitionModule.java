package com.webcohesion.enunciate.module;

import javax.lang.model.element.Element;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface MediaTypeDefinitionModule {

  public enum DataTypeDetectionStrategy {
    passive,
    aggressive,
    local
  }

  void setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy strategy);

  void addDataTypeDefinition(Element element, Set<String> declaredMediaTypes, LinkedList<Element> contextStack);
}
