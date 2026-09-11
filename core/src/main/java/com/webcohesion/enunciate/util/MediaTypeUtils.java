package com.webcohesion.enunciate.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MediaTypeUtils {

  /**
   * The top-level media types whose payloads are binary by convention.
   */
  private static final Set<String> BINARY_TYPES = new HashSet<String>(Arrays.asList("image", "audio", "video", "font"));

  /**
   * The "application" subtypes whose payloads are binary by convention. This isn't intended to be exhaustive;
   * anything else can be declared binary (or not) with the "media-types" element of the configuration.
   */
  private static final Set<String> BINARY_APPLICATION_SUBTYPES = new HashSet<String>(Arrays.asList(
    "octet-stream", "pdf", "zip", "gzip", "x-gzip", "x-bzip2", "x-tar", "x-7z-compressed", "x-rar-compressed",
    "x-zip-compressed", "java-archive", "wasm", "cbor", "msword", "vnd.ms-excel", "vnd.ms-powerpoint",
    "x-protobuf", "protobuf", "x-msdownload", "x-shockwave-flash"
  ));

  /**
   * The "application" subtype prefixes whose payloads are binary by convention.
   */
  private static final String[] BINARY_APPLICATION_SUBTYPE_PREFIXES = {
    "vnd.openxmlformats-officedocument.", "vnd.oasis.opendocument."
  };

  /**
   * The structured syntax suffixes that indicate a textual payload, no matter the media type that carries them.
   */
  private static final String[] TEXTUAL_SUFFIXES = {"+json", "+xml", "+yaml", "+csv", "+text"};

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

  /**
   * Normalize a media type for comparison purposes: strip any parameters and lowercase it.
   *
   * @param mediaType The media type.
   * @return The normalized media type.
   */
  public static String normalize(String mediaType) {
    return StringUtils.lowerCase(stripParams(mediaType));
  }

  public static boolean isUrlEncodedFormData(String mediaType) {
    return StringUtils.equals(stripParams(mediaType), "application/x-www-form-urlencoded");
  }
  
  public static boolean isMultipartFormData(String mediaType) {
    return StringUtils.equals(stripParams(mediaType), "multipart/form-data");
  }

  /**
   * Whether the payload of the given media type is binary (i.e. opaque bytes) by convention. Media types that
   * carry a structured syntax suffix (e.g. "+json", "+xml") are never considered binary, and neither are
   * wildcards, since nothing can be assumed about the payload they end up carrying.
   *
   * @param mediaType The media type.
   * @return Whether the payload of the given media type is binary by convention.
   */
  public static boolean isBinary(String mediaType) {
    mediaType = normalize(mediaType);

    if (StringUtils.isEmpty(mediaType) || mediaType.indexOf('*') >= 0) {
      //nothing can be assumed about a wildcard.
      return false;
    }

    String type = StringUtils.substringBefore(mediaType, "/");
    String subtype = StringUtils.substringAfter(mediaType, "/");
    if (subtype.isEmpty()) {
      return false;
    }

    for (String textualSuffix : TEXTUAL_SUFFIXES) {
      if (subtype.endsWith(textualSuffix)) {
        return false;
      }
    }

    if (BINARY_TYPES.contains(type)) {
      return true;
    }

    if ("application".equals(type)) {
      if (BINARY_APPLICATION_SUBTYPES.contains(subtype)) {
        return true;
      }

      for (String binaryPrefix : BINARY_APPLICATION_SUBTYPE_PREFIXES) {
        if (subtype.startsWith(binaryPrefix)) {
          return true;
        }
      }
    }

    return false;
  }
}
