/*
 * © 2022 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.jackson.model;

import com.fasterxml.jackson.annotation.JsonFormat;

public class FormatInfo {

  private final String pattern;
  private final JsonFormat.Shape shape;
  private final String timezone;
  private final String locale;
  
  public FormatInfo(JsonFormat format) {
    String pattern = format.pattern();
    if ("".equals(pattern)) {
      pattern = null;
    }
    this.pattern = pattern;

    JsonFormat.Shape shape = format.shape();
    if (shape == JsonFormat.Shape.ANY) {
      shape = null;
    }
    this.shape = shape;

    String timezone = format.timezone();
    if ("##default".equals(timezone)) {
      timezone = null;
    }
    this.timezone = timezone;

    String locale = format.locale();
    if ("##default".equals(locale)) {
      locale = null;
    }
    this.locale = locale;
  }

  public String getPattern() {
    return pattern;
  }

  public JsonFormat.Shape getShape() {
    return shape;
  }

  public String getTimezone() {
    return timezone;
  }

  public String getLocale() {
    return locale;
  }
}
