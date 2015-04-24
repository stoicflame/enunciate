package com.webcohesion.enunciate.metadata.rs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author MnlK
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface ResourceLabel {

    /**
     * The label
     * @return The label
     */
    String value();

}
