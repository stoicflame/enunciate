package org.codehaus.enunciate.modules.xfire;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a method as needing schema validation.
 * 
 * @author Ryan Heaton
 */
@Target( {ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE} )
@Retention ( RetentionPolicy.RUNTIME)
public @interface SchemaValidate {
}
