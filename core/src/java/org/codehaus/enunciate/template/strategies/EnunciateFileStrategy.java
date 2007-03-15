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

package org.codehaus.enunciate.template.strategies;

import net.sf.jelly.apt.strategies.FileStrategy;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A file strategy that takes into account a base directory.
 *
 * @author Ryan Heaton
 */
public class EnunciateFileStrategy extends FileStrategy {

  private final File outputDirectory;

  public EnunciateFileStrategy(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Override
  public PrintWriter getWriter() throws IOException, MissingParameterException {
    File dir = this.outputDirectory;
    if (dir == null) {
      //no output directory is specified,
      return super.getWriter();
    }
    else {
      String pckg = getPackage();
      if ((pckg != null) && (pckg.trim().length() > 0)) {
        String[] dirnames = pckg.split("\\.");
        for (String dirname : dirnames) {
          dir = new File(dir, dirname);
        }
      }
      dir.mkdirs();

      String charset = getCharset();
      if (charset != null) {
        return new PrintWriter(new File(dir, getName()), charset);
      }
      else {
        return new PrintWriter(new File(dir, getName()));
      }
    }
  }
}
