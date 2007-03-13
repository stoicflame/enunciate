package org.codehaus.enunciate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the test method is okay to run outside APT.
 *
 * @author Ryan Heaton
 * @see InAPTTestCase
 */
@Retention (
  RetentionPolicy.RUNTIME
)
public @interface OutsideAPTOkay {
}
