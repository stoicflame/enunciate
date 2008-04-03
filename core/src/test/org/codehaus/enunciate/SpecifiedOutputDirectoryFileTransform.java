/*
 * Copyright 2006-2008 Web Cohesion
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

package org.codehaus.enunciate;

import net.sf.jelly.apt.freemarker.transforms.FileTransform;
import net.sf.jelly.apt.strategies.FileStrategy;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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
