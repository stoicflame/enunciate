package org.codehaus.enunciate.doc;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Used to mark a method as an example method to be used in the generated documentation.
 *
 * @author Ryan Heaton
 */
@Target ( { ElementType.TYPE, ElementType.METHOD } )
public @interface DocumentationExample {
}
