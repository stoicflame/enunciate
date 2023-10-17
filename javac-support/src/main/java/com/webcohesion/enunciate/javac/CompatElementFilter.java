package com.webcohesion.enunciate.javac;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified utility for handling java.lang.Record on JDK < 16.
 */
public final class CompatElementFilter {
  private CompatElementFilter() {
  }

  /**
   * Get all fields in the class. If it is a java.lang.Record then get all the record components.
   *
   * @param clazz the class to
   * @return a list of elements
   */
  public static List<Element> fieldsOrRecordComponentsIn(TypeElement clazz) {
    if (RecordCompatibility.isRecord(clazz)) {
      List<Element> elements = new ArrayList<>();
      for (Element element : clazz.getEnclosedElements()) {
        if (RecordCompatibility.isRecordComponent(element)) {
          elements.add(element);
        }
      }
      return elements;
    }
    else {
      return new ArrayList<>(ElementFilter.fieldsIn(clazz.getEnclosedElements()));
    }
  }
}
