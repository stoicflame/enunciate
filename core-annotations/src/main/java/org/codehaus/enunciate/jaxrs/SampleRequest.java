package org.codehaus.enunciate.jaxrs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Generates a sample request in the documentation. The sample can be in JSON, XML or plain text.
 *
 * @author mklette
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ({ ElementType.METHOD })
public @interface SampleRequest {

    /**
     * JSON or XML.
     */
    String sampleType() default "JSON";
}
