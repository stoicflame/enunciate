package org.codehaus.enunciate.modules.objc;

import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestObjCDeploymentModule extends TestCase {

  /**
   * tests scrubbing a c identifie.
   */
  public void testScrubIdentifier() throws Exception {
    assertEquals("hello_me", ObjCDeploymentModule.scrubIdentifier("hello-me"));
  }

}
