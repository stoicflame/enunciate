package org.codehaus.enunciate.main;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Collections;

import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.APIImport;
import org.codehaus.enunciate.modules.DeploymentModule;

/**
 * @author Ryan Heaton
 */
public class TestEnunciate extends TestCase {

  /**
   * tests for classes to import.
   */
  public void testScanForClassesToImport() throws Exception {
    Enunciate enunciate = new Enunciate();
    //first set up a classpath.

    File dirEntry = enunciate.createTempDir();
    createClassFile("org.codehaus.enunciate.pckg1.SampleClassOne", dirEntry);
    createSourceFile("org.codehaus.enunciate.pckg1.SampleClassOne", dirEntry);
    createClassFile("org.codehaus.enunciate.pckg1.with.nested.pckg.SampleClassTwo", dirEntry);
    createSourceFile("org.codehaus.enunciate.pckg1.with.nested.pckg.SampleClassTwo", dirEntry);
    createClassFile("org.codehaus.enunciate.pckg1.SampleClassThree", dirEntry);
    createSourceFile("org.codehaus.enunciate.pckg1.SampleClassThree", dirEntry);
    createClassFile("org.codehaus.enunciate.pckg1.SampleClassFour", dirEntry);
    createClassFile("org.codehaus.enunciate.pckg1.SampleClassFour$SomeInner", dirEntry);
    File dirToZipUp = enunciate.createTempDir();
    createClassFile("org.codehaus.enunciate.pckg2.SampleClassFive", dirToZipUp);
    createSourceFile("org.codehaus.enunciate.pckg2.SampleClassFive", dirToZipUp);
    createClassFile("org.codehaus.enunciate.pckg2.with.nested.pckg.SampleClassSix", dirToZipUp);
    createSourceFile("org.codehaus.enunciate.pckg2.with.nested.pckg.SampleClassSix", dirToZipUp);
    createClassFile("org.codehaus.enunciate.pckg2.SampleClassSeven", dirToZipUp);
    createSourceFile("org.codehaus.enunciate.pckg2.SampleClassSeven", dirToZipUp);
    createClassFile("org.codehaus.enunciate.pckg2.SampleClassEight", dirToZipUp);
    createClassFile("org.codehaus.enunciate.pckg2.SampleClassEight$SomeInner", dirToZipUp);
    File jarFile = File.createTempFile("enunciatetest", ".jar");
    enunciate.zip(jarFile, dirToZipUp);
    dirToZipUp = enunciate.createTempDir();
    File apiExports = new File(dirToZipUp, "META-INF/enunciate/api-exports");
    apiExports.getParentFile().mkdirs();
    FileOutputStream fos = new FileOutputStream(apiExports);
    fos.write("org.codehaus.enunciate.pckg3.SampleClassNine\n".getBytes("utf-8"));
    fos.flush();
    fos.close();
    createClassFile("org.codehaus.enunciate.pckg3.SampleClassNine", dirToZipUp);
    createSourceFile("org.codehaus.enunciate.pckg3.SampleClassNine", dirToZipUp);
    createClassFile("org.codehaus.enunciate.pckg3.SampleClassTen", dirToZipUp);
    createSourceFile("org.codehaus.enunciate.pckg3.SampleClassTen", dirToZipUp);
    File jarFile2 = File.createTempFile("enunciatetest", ".jar");
    enunciate.zip(jarFile2, dirToZipUp);
    enunciate.setRuntimeClasspath(dirEntry.getAbsolutePath() + File.pathSeparator + jarFile.getAbsolutePath() + File.pathSeparator + jarFile2.getAbsolutePath());
    EnunciateConfiguration config = new EnunciateConfiguration(Collections.<DeploymentModule>emptyList());
    APIImport apiImport = new APIImport();
    apiImport.setPattern("org.codehaus.enunciate.pckg1.SampleClassOne");
    config.addAPIImport(apiImport);
    apiImport = new APIImport();
    apiImport.setPattern("org.codehaus.enunciate.pckg1.with.**");
    config.addAPIImport(apiImport);
    apiImport = new APIImport();
    apiImport.setPattern("org.codehaus.enunciate.pckg2.*");
    config.addAPIImport(apiImport);
    apiImport = new APIImport();
    apiImport.setPattern("some.other.class.that.is.Explicit");
    config.addAPIImport(apiImport);
    enunciate.setConfig(config);
    Map<String,File> classes2Import = enunciate.scanForClassesToImport();
    assertTrue(classes2Import.containsKey("some.other.class.that.is.Explicit"));
    assertNull(classes2Import.get("some.other.class.that.is.Explicit"));
    assertNotNull(classes2Import.get("org.codehaus.enunciate.pckg1.SampleClassOne"));
    assertNotNull(classes2Import.get("org.codehaus.enunciate.pckg1.with.nested.pckg.SampleClassTwo"));
    assertFalse(classes2Import.containsKey("org.codehaus.enunciate.pckg1.SampleClassThree"));
    assertFalse(classes2Import.containsKey("org.codehaus.enunciate.pckg1.SampleClassFour"));
    assertFalse(classes2Import.containsKey("org.codehaus.enunciate.pckg1.SampleClassFour.SomeInner"));
    assertFalse(classes2Import.containsKey("org.codehaus.enunciate.pckg1.SampleClassFour$SomeInner"));
    assertNotNull(classes2Import.get("org.codehaus.enunciate.pckg2.SampleClassFive"));
    assertFalse(classes2Import.containsKey("org.codehaus.enunciate.pckg2.with.nested.pckg.SampleClassSix"));
    assertNotNull(classes2Import.get("org.codehaus.enunciate.pckg2.SampleClassSeven"));
    assertTrue(classes2Import.containsKey("org.codehaus.enunciate.pckg2.SampleClassEight"));
    assertNull(classes2Import.get("org.codehaus.enunciate.pckg2.SampleClassEight"));
    assertFalse(classes2Import.containsKey("org.codehaus.enunciate.pckg2.SampleClassEight.SomeInner"));
    assertFalse(classes2Import.containsKey("org.codehaus.enunciate.pckg2.SampleClassEight$SomeInner"));
    assertNotNull(classes2Import.get("org.codehaus.enunciate.pckg3.SampleClassNine"));
    assertFalse(classes2Import.containsKey("org.codehaus.enunciate.pckg3.SampleClassTen"));
  }

  private void createClassFile(String classname, File dir) throws IOException {
    File classFile = new File(dir, classname.replace('.', File.separatorChar) + ".class");
    classFile.getParentFile().mkdirs();
    classFile.createNewFile();
  }

  private void createSourceFile(String classname, File dir) throws IOException {
    File classFile = new File(dir, classname.replace('.', File.separatorChar) + ".java");
    classFile.getParentFile().mkdirs();
    classFile.createNewFile();
  }

}
