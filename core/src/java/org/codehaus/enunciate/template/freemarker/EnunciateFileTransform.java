/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.template.freemarker;

import net.sf.jelly.apt.freemarker.transforms.FileTransform;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.strategies.FileStrategy;
import org.codehaus.enunciate.template.strategies.EnunciateFileStrategy;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;

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
