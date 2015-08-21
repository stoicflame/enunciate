package com.webcohesion.enunciate.modules.objc_client;

import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestObjCDeploymentModule extends TestCase {

  /**
   * tests scrubbing a c identifie.
   */
  public void testScrubIdentifier() throws Exception {
    assertEquals("hello_me", ObjCClientModule.scrubIdentifier("hello-me"));
  }

  public void testPackageIdentifier() throws Exception {
    String[] subpackages = "org.codehaus.enunciate.samples.objc.whatever".split("\\.", 9);
    assertEquals("ENUNCIATEORGOBJC", String.format("%3$S%1$S%5$S", subpackages));

  }

}
