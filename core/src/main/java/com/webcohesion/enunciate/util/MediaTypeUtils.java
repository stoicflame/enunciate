package com.webcohesion.enunciate.util;

import java.util.Collection;

public class MediaTypeUtils {
  public static boolean isJsonCompatible(Collection<String> declaredMediaTypes) {
    for (String mediaType : declaredMediaTypes) {
      int semicolon = mediaType.indexOf(';');
      if (semicolon >= 0) {
        while (semicolon > 0 && Character.isWhitespace(mediaType.charAt(semicolon - 1))) {
          --semicolon;
        }
        mediaType = mediaType.substring(0, semicolon);
      }
      if ("*/*".equals(mediaType) || "text/*".equals(mediaType) || "application/*".equals(mediaType) || "application/json".equals(mediaType) || mediaType.endsWith("+json")) {
        return true;
      }
    }
    return false;
  }
}
