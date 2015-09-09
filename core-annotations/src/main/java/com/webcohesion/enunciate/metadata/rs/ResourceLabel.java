package com.webcohesion.enunciate.metadata.rs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author MnlK
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD, ElementType.TYPE } )
public @interface ResourceLabel {

    /**
     * The label
     *
     * @return The label
     */
    String value() default "##default";

    /**
     * A alphanumeric sort key for ordering resources when displayed in a list.
     *
     * @return A alphanumeric sort key for ordering resources when displayed in a list.
     */
    String sortKey() default "##default";

}
