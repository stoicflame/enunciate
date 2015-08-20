package org.codehaus.enunciate.modules.c;

import com.webcohesion.enunciate.modules.c_client.EnunciateCClientModule;
import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestCDeploymentModule extends TestCase {

  /**
   * tests scrubbing a c identifie.
   */
  public void testScrubIdentifier() throws Exception {
    assertEquals("hello_me", EnunciateCClientModule.scrubIdentifier("hello-me"));
  }

}
