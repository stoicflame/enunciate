package com.webcohesion.enunciate.metadata.rs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents expected response headers for a resource or resource method.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ({ ElementType.TYPE, ElementType.METHOD })
public @interface ResponseHeaders {

  ResponseHeader[] value();

}
