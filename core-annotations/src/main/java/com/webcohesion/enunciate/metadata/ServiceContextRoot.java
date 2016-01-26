package com.webcohesion.enunciate.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//KUSE-1828
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface ServiceContextRoot {
    
 public String context() default "";
}
