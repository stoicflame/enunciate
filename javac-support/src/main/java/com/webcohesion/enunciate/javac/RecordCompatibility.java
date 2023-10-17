package com.webcohesion.enunciate.javac;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.security.PublicKey;

/**
 * Aids in adding record compatibility for JDK < 16.
 */

public final class RecordCompatibility {
  private RecordCompatibility() {
  }

  /**
   * Check if the element is a record component.
   *
   * @param element the element to test
   * @return true if it's a record component
   */
  public static boolean isRecordComponent(Element element) {
    return element != null && element.getKind().name().equals("RECORD_COMPONENT");
  }

  /**
   * Check if the element is a record.
   *
   * @param element the element to test
   * @return true if it's a record
   */
  public static boolean isRecord(TypeElement element) {
    if (element.getSuperclass() == null) {
      return false;
    }

    return element.getSuperclass().toString().equals("java.lang.Record");
  }
}
