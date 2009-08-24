package org.codehaus.enunciate.modules.c;

import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestCDeploymentModule extends TestCase {

  /**
   * tests scrubbing a c identifie.
   */
  public void testScrubIdentifier() throws Exception {
    assertEquals("hello_me", CDeploymentModule.scrubIdentifier("hello-me"));
  }

}
