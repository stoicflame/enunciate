/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.SourcePosition;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public abstract class BasicGeneratingModule extends BasicEnunicateModule {

  /**
   * Whether all files in the specified directory are newer than all the source files.
   *
   * @param destDir The directory.
   * @return Whether the destination directory is up-to-date.
   */
  public boolean isUpToDateWithSources(File destDir) {
    Set<Element> apiElements = this.context.getApiElements();
    DecoratedProcessingEnvironment env = this.context.getProcessingEnvironment();
    long newestSourceTimestamp = 0;
    File configFile = this.context.getConfiguration().getConfigFile();
    if (configFile != null && configFile.exists()) {
      newestSourceTimestamp = configFile.lastModified();
    }

    for (Element apiElement : apiElements) {
      long sourceTimestamp = findSourceTimestamp(env, apiElement);
      newestSourceTimestamp = Math.max(newestSourceTimestamp, sourceTimestamp);
    }

    return isUpToDate(newestSourceTimestamp, destDir);
  }

  public long findSourceTimestamp(DecoratedProcessingEnvironment env, Element apiElement) {
    SourcePosition sp = env.findSourcePosition(apiElement);
    URI uri = sp == null ? null : sp.getPath() == null ? null : sp.getPath().getCompilationUnit() == null ? null : sp.getPath().getCompilationUnit().getSourceFile() == null ? null : sp.getPath().getCompilationUnit().getSourceFile().toUri();
    if (uri != null && "file".equalsIgnoreCase(uri.getScheme())) {
      //it's a file uri.
      return new File(uri.getPath()).lastModified();
    }

    return 0;
  }

  protected boolean isUpToDate(long newestSourceTimestamp, File destFile) {
    List<File> destFiles;
    if ((destFile == null) || (!destFile.exists())) {
      debug("%s is NOT up-to-date because it doesn't exist.", destFile);
      return false;
    }
    else if (!destFile.isDirectory()) {
      destFiles = Arrays.asList(destFile);
    }
    else {
      destFiles = new ArrayList<File>();
      buildFileList(destFiles, destFile);
    }

    if (destFiles.isEmpty()) {
      debug("%s is NOT up-to-date because it's an empty directory.", destFile);
      return false;
    }
    else {
      File oldestDest = getOldest(destFiles);

      if (newestSourceTimestamp < oldestDest.lastModified()) {
        debug("%s is up-to-date because its oldest file, %s, is younger than the youngest source file.", destFile, oldestDest);
        return true;
      }
      else {
        debug("%s is NOT up-to-date because its oldest file, %s, is older than the youngest source file.", destFile, oldestDest);
        return false;
      }
    }
  }

  /**
   * Get the latest modified file.
   *
   * @param files The files.
   * @return The latest modified.
   */
  protected File getYoungest(List<File> files) {
    if ((files == null) || (files.isEmpty())) {
      return null;
    }

    File latest = files.get(0);
    for (File file : files) {
      latest = latest.lastModified() > file.lastModified() ? latest : file;
    }
    return latest;
  }

  /**
   * Get the earliest modified file.
   *
   * @param files The files.
   * @return The earliest modified.
   */
  protected File getOldest(List<File> files) {
    if ((files == null) || (files.isEmpty())) {
      return null;
    }

    File earliest = files.get(0);
    for (File file : files) {
      earliest = earliest.lastModified() < file.lastModified() ? earliest : file;
    }
    return earliest;
  }

  /**
   * Adds all files in specified directories to a list.
   *
   * @param list The list.
   * @param dirs The directories.
   */
  protected void buildFileList(List<File> list, File... dirs) {
    for (File dir : dirs) {
      for (File file : dir.listFiles()) {
        if (file.isDirectory()) {
          buildFileList(list, file);
        }
        else {
          list.add(file);
        }
      }
    }
  }
}
