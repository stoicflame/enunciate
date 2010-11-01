package org.codehaus.enunciate.main;

import org.codehaus.enunciate.util.JaxbPackageInfoWriter;

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

    if (path.startsWith("com/sun/tools")) {
      //we're assuming gwt doesn't have any enunciate-model-relevant annotations...
      return;
    }

    if (path.startsWith("com/google/gwt")) {
      //we're assuming gwt doesn't have any enunciate-model-relevant annotations... 
      return;
    }

    //we're only going to notice the packages packed up in jars. This is to avoid duplicate package-info.java files. Someday, we may need
    //to revisit this.
    if (resource instanceof JarClasspathResource) {
      if (path.endsWith("package-info.class")) {
        //APT has a bug where it won't find the package-info file unless it's on the source path. So we have to generate
        //the source from bytecode.
        if (!packageInfoSources.containsKey(path)) {
          File sourceFile = new File(tempSourcesDir, path.substring(0, path.length() - 6) + ".java");
          enunciate.debug("Generating package source file %s...", sourceFile);
          try {
            InputStream resourceStream = resource.read();
            if (writePackageSourceFile(resourceStream, sourceFile)) {
              packageInfoSources.put(path, sourceFile);
            }
            resourceStream.close();
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
          packageInfoSources.put(path, sourceFile);
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
   * @return Whether the file was actually written.
   */
  protected boolean writePackageSourceFile(InputStream bytecode, File packageSourceFile) throws IOException {
    JaxbPackageInfoWriter writer = new JaxbPackageInfoWriter();
    String info = writer.write(bytecode);
    if (info != null) {
      packageSourceFile.getParentFile().mkdirs();
      FileWriter out = new FileWriter(packageSourceFile);
      out.write(info);
      out.flush();
      out.close();
      return true;
    }

    return false;
  }

  public Set<File> getPackageInfoSources() {
    return new HashSet<File>(packageInfoSources.values());
  }
}