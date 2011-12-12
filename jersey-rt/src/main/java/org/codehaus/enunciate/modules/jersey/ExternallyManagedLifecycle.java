package org.codehaus.enunciate.modules.jersey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying that this resource's lifecycle is to be managed external to the Jersey runtime.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.TYPE )
public @interface ExternallyManagedLifecycle {
}
