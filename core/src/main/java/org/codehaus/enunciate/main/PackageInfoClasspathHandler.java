package org.codehaus.enunciate.main;

import org.codehaus.enunciate.util.PackageInfoWriter;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
* @author Ryan Heaton
*/
public class PackageInfoClasspathHandler implements ClasspathHandler {

  private final Enunciate enunciate;
  private final Map<String, File> packageInfoSources = new HashMap<String, File>();
  private final File tempSourcesDir;

  public PackageInfoClasspathHandler(Enunciate enunciate) throws IOException {
    this.enunciate = enunciate;
    this.tempSourcesDir = enunciate.createTempDir();
  }

  public void startPathEntry(File pathEntry) {
  }

  public void handleResource(ClasspathResource resource) {
    String path = resource.getPath();

    //we're only going to notice the packages packed up in jars. This is to avoid duplicate package-info.java files. Someday, we may need
    //to revisit this.
    if (resource instanceof JarClasspathResource) {
      if (path.endsWith("package-info.class")) {
        //APT has a bug where it won't find the package-info file unless it's on the source path. So we have to generate
        //the source from bytecode.
        File sourceFile = new File(tempSourcesDir, path.substring(0, path.length() - 6) + ".java");
        if (!packageInfoSources.containsKey(resource.getPath())) {
          enunciate.debug("Generating package source file %s...", sourceFile);
          try {
            InputStream resourceStream = resource.read();
            writePackageSourceFile(resourceStream, sourceFile);
            resourceStream.close();
            packageInfoSources.put(resource.getPath(), sourceFile);
          }
          catch (IOException e) {
            enunciate.warn("Unable to generate package source file %s (%s).", sourceFile, e.getMessage());
          }
        }
      }
      else if (path.endsWith("package-info.java")) {
        File sourceFile = new File(tempSourcesDir, path);
        enunciate.debug("Noticed the source for %s, extracting to %s.", path, sourceFile);
        try {
          InputStream in = resource.read();
          sourceFile.getParentFile().mkdirs();
          FileOutputStream out = new FileOutputStream(sourceFile);
          byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
          int len;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          out.close();
          in.close();
          packageInfoSources.put(resource.getPath(), sourceFile);
        }
        catch (IOException e) {
          enunciate.warn("Unable to extract source file %s (%s).", sourceFile, e.getMessage());
        }
      }
    }
  }

  public boolean endPathEntry(File pathEntry) {
    return false;
  }

  /**
   * Write the package-info.java source to the specified file.
   *
   * @param bytecode          The bytecode for the package-info.class
   * @param packageSourceFile The source file.
   */
  protected void writePackageSourceFile(InputStream bytecode, File packageSourceFile) throws IOException {
    packageSourceFile.getParentFile().mkdirs();
    PackageInfoWriter writer = new PackageInfoWriter(new FileWriter(packageSourceFile));
    writer.write(bytecode);
    writer.close();
  }

  public Set<File> getPackageInfoSources() {
    return new HashSet<File>(packageInfoSources.values());
  }
}