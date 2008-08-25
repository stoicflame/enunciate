/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.rest;

import java.util.StringTokenizer;

/**
 * Helper class for content type.
 *
 * @author Ryan Heaton
 */
public class MimeType implements Comparable<MimeType> {

  private final String type;
  private final String subtype;
  private final float quality;

  public static MimeType parse(String spec) {
    float quality = 1;
    String type;
    String subType;

    StringTokenizer params = new StringTokenizer(spec, ";");
    String typeAndSubtype = params.nextToken();
    if (params.hasMoreTokens()) {
      while (params.hasMoreTokens()) {
        String param = params.nextToken();
        StringTokenizer paramValueTokens = new StringTokenizer(param, "=");
        String paramName = paramValueTokens.nextToken();
        if ("q".equalsIgnoreCase(paramName.trim()) && paramValueTokens.hasMoreTokens()) {
            quality = Float.parseFloat(paramValueTokens.nextToken().trim());
            break;
        }
      }
    }

    StringTokenizer typeTokens = new StringTokenizer(typeAndSubtype, "/");
    type = typeTokens.nextToken().trim();
    if (!typeTokens.hasMoreTokens()) {
      throw new IllegalArgumentException("Illegal content type: " + typeAndSubtype);
    }
    else {
      subType = typeTokens.nextToken().trim();
    }

    if (typeTokens.hasMoreTokens()) {
      throw new IllegalArgumentException("Illegal content type: " + typeAndSubtype);
    }

    return new MimeType(type, subType, quality);
  }

  public MimeType(String type, String subtype) {
    this(type, subtype, 1);
  }

  public MimeType(String type, String subtype, float quality) {
    if (type == null) {
      type = "*";
    }

    if (subtype == null) {
      subtype = "*";
    }

    this.type = type;
    this.subtype = subtype;
    this.quality = quality;
  }

  public String getType() {
    return type;
  }

  public String getSubtype() {
    return subtype;
  }

  public float getQuality() {
    return quality;
  }

  /**
   * Content type are ordered first by quality then by type, then by subtype.
   *
   * @param other Content type to compare to.
   * @return The comparison.
   */
  public int compareTo(MimeType other) {
    if (this.quality == other.quality) {
      int comparison = getType().compareTo(other.getType());
      if (comparison == 0) {
        comparison = getSubtype().compareTo(other.getSubtype());
      }
      return comparison;
    }
    else if (this.quality < other.quality) {
      return 1;
    }
    else {
      return -1;
    }
  }

  @Override
  public String toString() {
    return getType() + "/" + getSubtype();
  }

  /**
   * Whether the specified content type is acceptable by this content type.
   *
   * @param mimeType The content type.
   * @return Whether the specified content type is acceptable by this content type.
   */
  public boolean isAcceptable(MimeType mimeType) {
    boolean typeMatches = "*".equals(getType()) || getType().equals(mimeType.getType());
    boolean subtypeMatches = "*".equals(getSubtype()) || getSubtype().equals(mimeType.getSubtype());
    return typeMatches && subtypeMatches;
  }
}
