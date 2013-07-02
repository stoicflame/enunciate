package org.codehaus.enunciate.jaxrs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Generates a sample response in the documentation.
 *
 * @author mklette
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ({ ElementType.METHOD })
public @interface SampleResponse {

    /**
     * JSON or XML.
     */
    String sampleType() default "JSON";

    /**
     * The HTTP Status Code to show in de sample response.
     */
    int responseCode() default 200;
}
