package com.webcohesion.enunciate.modules.jaxrs.model.util;

import java.util.Collections;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public final class MediaType implements Comparable<MediaType> {

  private final String mediaType;
  private final float qs;
  private final Map<String, String> params;

  public MediaType(String mediaType, float qs) {
    this(mediaType, qs, Collections.<String, String>emptyMap());
  }

  public MediaType(String mediaType, float qs, final Map<String, String> params) {
    this.mediaType = mediaType;
    this.qs = qs;
    this.params = params;
  }

  public String getMediaType() {
    return mediaType;
  }

  public float getQualityOfSource() {
    return qs;
  }

  public Map<String, String> getParams() {
    return params;
  }

  @Override
  public String toString() {
    return getMediaType();
  }

  @Override
  public int compareTo(MediaType o) {
    return this.mediaType.compareTo(o.mediaType);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof MediaType && this.mediaType.equals(((MediaType) o).mediaType);
  }

  @Override
  public int hashCode() {
    return mediaType.hashCode();
  }
}
