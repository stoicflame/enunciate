/*
 * © 2024 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.jackson.model.types;

public class SpeciallyFormattedKnownJsonType implements JsonType {
  
  private final JsonType delegate;
  private final String format;

  public SpeciallyFormattedKnownJsonType(JsonType delegate, String format) {
    this.delegate = delegate;
    this.format = format;
  }

  @Override
  public boolean isObject() {
    return delegate.isObject();
  }

  @Override
  public boolean isString() {
    return delegate.isString();
  }

  @Override
  public boolean isNumber() {
    return delegate.isNumber();
  }

  @Override
  public boolean isWholeNumber() {
    return delegate.isWholeNumber();
  }

  @Override
  public boolean isBoolean() {
    return delegate.isBoolean();
  }

  @Override
  public boolean isArray() {
    return delegate.isArray();
  }

  @Override
  public String getFormat() {
    return this.format;
  }
}
