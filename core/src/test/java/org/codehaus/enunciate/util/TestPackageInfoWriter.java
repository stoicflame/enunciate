package org.codehaus.enunciate.util;

import junit.framework.TestCase;

import java.io.StringWriter;

/**
 * @author Ryan Heaton
 */
public class TestPackageInfoWriter extends TestCase {

  /**
   * tests writing package-info
   */
  public void testWritePackageInfo() throws Exception {
    StringWriter writer = new StringWriter();
    PackageInfoWriter piw = new PackageInfoWriter(writer);
    piw.write(TestPackageInfoWriter.class.getResourceAsStream("/org/codehaus/enunciate/util/testpckg/package-info.class"));
    piw.close();
    String source = writer.toString();
//    System.out.println(source);
    assertTrue(source.length() > 5);
  }

}
