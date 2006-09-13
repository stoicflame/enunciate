package net.sf.enunciate.config;

import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Ryan Heaton
 */
public class TestEnunciateConfiguration {

  /**
   * load
   */
  @Test
  public void testLoad() throws Exception {
    new EnunciateConfiguration().load(new File("/home/heatonra/sandbox/enunciate/src/samples/basic/enunciate.xml"));
  }
}
