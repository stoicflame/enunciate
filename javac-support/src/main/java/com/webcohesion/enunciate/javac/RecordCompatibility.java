package com.webcohesion.enunciate.javac;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

/**
 * Aids in adding record compatibility for JDK < 16.
 */
public final class RecordCompatibility {
  public static final String KIND_RECORD = "RECORD";
  public static final String KIND_RECORD_COMPONENT = "RECORD_COMPONENT";

  private RecordCompatibility() {
  }

  /**
   * Check if the element is a record component.
   *
   * @param element the element to test
   * @return true if it's a record component
   */
  public static boolean isRecordComponent(Element element) {
    return element != null && element.getKind().name().equals(KIND_RECORD_COMPONENT);
  }

  /**
   * Check if the element is a record.
   *
   * @param element the element to test
   * @return true if it's a record
   */
  public static boolean isRecord(Element element) {
    return element.getKind().name().equals(KIND_RECORD);
  }

  /**
   * Check if the element is a class or record.
   *
   * @param element the element to test
   * @return true if it's a class or record
   */
  public static boolean isClassOrRecord(Element element) {
    return element.getKind() == ElementKind.CLASS || isRecord(element);
  }
}
