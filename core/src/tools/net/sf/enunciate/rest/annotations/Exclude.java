package net.sf.enunciate.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Indicates that a web method  should be excluded from consideration in the REST API.
 * 
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( {ElementType.METHOD} )
public @interface Exclude {
}
