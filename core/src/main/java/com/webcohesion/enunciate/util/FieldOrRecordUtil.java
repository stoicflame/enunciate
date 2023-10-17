package com.webcohesion.enunciate.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * FieldOrRecordUtil.
 */
public final class FieldOrRecordUtil {

  public static List<Element> fieldsOrRecordComponentsIn(TypeElement clazz) {
    if (isRecord(clazz)) {
      try {
        List<Element> elements = new ArrayList<>();
        for(Element element : clazz.getEnclosedElements()) {
          if(element.getKind().name().equals("RECORD_COMPONENT")) {
            elements.add(element);
          }
        }
        return elements;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    else {
      return new ArrayList<>(ElementFilter.fieldsIn(clazz.getEnclosedElements()));
    }
  }


  private static boolean isRecord(TypeElement clazz) {
    if (clazz.getSuperclass() == null) {
      return false;
    }

    return clazz.getSuperclass().toString().equals("java.lang.Record");
  }
}
