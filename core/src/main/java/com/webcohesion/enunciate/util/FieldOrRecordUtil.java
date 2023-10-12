package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * FieldOrRecordUtil.
 */
public final class FieldOrRecordUtil {

  @SuppressWarnings({"unchecked"})
  public static List<Element> extractFieldElements(TypeElement clazz) {
    if (isRecord(clazz)) {
      try {
        Method method = clazz.getClass().getMethod("getRecordComponents");
        List<? extends Element> elements = (List<? extends Element>) method.invoke(clazz);
        return new ArrayList<>(elements);
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
