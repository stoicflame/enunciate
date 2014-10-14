package org.codehaus.enunciate.util;

import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestPackageInfoWriter extends TestCase {

  /**
   * tests writing package-info
   */
  public void testWritePackageInfo() throws Exception {
    JaxbPackageInfoWriter piw = new JaxbPackageInfoWriter();
    String source = piw.write(TestPackageInfoWriter.class.getResourceAsStream("/org/codehaus/enunciate/util/testpckg/package-info.class"));
//    System.out.println(source);
    assertTrue(source.length() > 5);
  }

}
