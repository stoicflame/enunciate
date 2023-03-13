package com.webcohesion.enunciate.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class MediaTypeUtils {
  public static boolean isJsonCompatible(Collection<String> declaredMediaTypes) {
    for (String mediaType : declaredMediaTypes) {
      mediaType = stripParams(mediaType);
      if ("*/*".equals(mediaType) || "text/*".equals(mediaType) || "application/*".equals(mediaType) || "application/json".equals(mediaType) || mediaType.endsWith("+json")) {
        return true;
      }
    }
    return false;
  }

  private static String stripParams(String mediaType) {
    mediaType = StringUtils.substringBefore(mediaType, ';');
    mediaType = StringUtils.strip(mediaType);
    return mediaType;
  }

  public static boolean isUrlEncodedFormData(String mediaType) {
    return StringUtils.equals(stripParams(mediaType), "application/x-www-form-urlencoded");
  }
  
  public static boolean isMultipartFormData(String mediaType) {
    return StringUtils.equals(stripParams(mediaType), "multipart/form-data");
  }
}
