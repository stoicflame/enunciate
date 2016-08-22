package com.webcohesion.enunciate.modules.jaxrs.model.util;

/**
 * @author Ryan Heaton
 */
public final class MediaType implements Comparable<MediaType> {

  private final String mediaType;
  private final float qs;

  public MediaType(String mediaType, float qs) {
    this.mediaType = mediaType;
    this.qs = qs;
  }

  public String getMediaType() {
    return mediaType;
  }

  public float getQualityOfSource() {
    return qs;
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
