package com.webcohesion.enunciate.util;

import javax.lang.model.type.TypeMirror;

/**
 * @author Ryan Heaton
 */
public interface HasClientConvertibleType {

  TypeMirror getClientConvertibleType();

}
