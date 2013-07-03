package org.codehaus.enunciate.doc;

import org.codehaus.enunciate.Facet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for excluding a method, field, type, etc. from the documentation. As of March 2011, this annotation is only honored on JAXB root elements and
 * JAXB type definitions.
 *
 * @author Ryan Heaton
 * @deprecated Use {@link org.codehaus.enunciate.Facet}s for excluding from documentation...
 */
@Target ( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD } )
@Retention ( RetentionPolicy.RUNTIME )
@Facet( name = "org.codehaus.enunciate.doc.ExcludeFromDocumentation" )
public @interface ExcludeFromDocumentation {

  /**
   * Whether to exclude this element from the interface definition language (XML schema, WSDL, WADL, etc.)
   *
   * @return Whether to exclude this element from the interface definition language (XML schema, WSDL, WADL, etc.)
   */
  boolean excludeFromIDL() default false;
}
