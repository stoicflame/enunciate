package org.codehaus.enunciate;

import net.sf.jelly.apt.freemarker.transforms.FileTransform;
import net.sf.jelly.apt.strategies.FileStrategy;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.io.*;

/**
 * @author Ryan Heaton
 */
public class SpecifiedOutputDirectoryFileTransform extends FileTransform {

  private final File outputDir;

  public SpecifiedOutputDirectoryFileTransform(File outputDir) {
    super(null);
    this.outputDir = outputDir;
  }


  //Inherited.
  @Override
  public FileStrategy newStrategy() {
    return new FileStrategy() {
      @Override
      public PrintWriter getWriter() throws IOException, MissingParameterException {
        File packageDir = new File(outputDir, getPackage().replace('.', File.separatorChar));
        packageDir.mkdirs();
        File file = new File(packageDir, getName());
        return new PrintWriter(file);
      }
    };
  }
}
