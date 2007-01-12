package net.sf.enunciate.template.freemarker;

import net.sf.jelly.apt.freemarker.transforms.FileTransform;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.strategies.FileStrategy;
import net.sf.enunciate.template.strategies.EnunciateFileStrategy;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;

import java.io.File;

/**
 * Since Enunciate uses multiple modules, it needs a special transform to handle creating
 * new files that creates the files in a specified output directory.
 *
 * @author Ryan Heaton
 */
public class EnunciateFileTransform extends FileTransform {

  private File outputDirectory = null;

  // Inherited.
  public EnunciateFileTransform(String namespace) {
    super(namespace);
  }


  //Inherited.
  @Override
  public FileStrategy newStrategy() {
    EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
    File outputDir = (model != null) ? model.getFileOutputDirectory() : null;
    return new EnunciateFileStrategy(outputDir);
  }

  @Override
  public String getTransformName() {
    return "file";
  }

  /**
   * The directory into which to put the new files.
   *
   * @return The directory into which to put the new files.
   */
  public File getOutputDirectory() {
    return outputDirectory;
  }

  /**
   * The directory into which to put the new files. <code>null</code> means
   * use the default annotation processing environment directory.
   *
   * @param outputDirectory The directory into which to put the new files.
   */
  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }
}
