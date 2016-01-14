package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.SourcePosition;

import javax.lang.model.element.Element;
import java.io.File;
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
    File configFile = this.context.getConfiguration().getSource().getFile();
    if (configFile != null && configFile.exists()) {
      newestSourceTimestamp = configFile.lastModified();
    }

    for (Element apiElement : apiElements) {
      SourcePosition sp = env.findSourcePosition(apiElement);
      long sourceTimestamp = sp == null ? 0 : sp.getPath() == null ? 0 : sp.getPath().getCompilationUnit() == null ? 0 : sp.getPath().getCompilationUnit().getSourceFile() == null ? 0 : sp.getPath().getCompilationUnit().getSourceFile().getLastModified();
      newestSourceTimestamp = Math.max(newestSourceTimestamp, sourceTimestamp);
    }

    return isUpToDate(newestSourceTimestamp, destDir);
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
