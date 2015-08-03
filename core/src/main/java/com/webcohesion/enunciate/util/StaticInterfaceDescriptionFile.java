package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class StaticInterfaceDescriptionFile implements InterfaceDescriptionFile {

  private boolean written = false;
  private final File file;
  private final Enunciate enunciate;

  public StaticInterfaceDescriptionFile(File file, Enunciate enunciate) {
    this.file = file;
    this.enunciate = enunciate;
  }

  @Override
  public String getHref() {
    if (!written) {
      throw new IllegalStateException("No href available: file hasn't been written.");
    }

    return file.getName();
  }

  @Override
  public void writeTo(File directory, String apiRelativePath) throws IOException {
    this.enunciate.copyFile(this.file, this.file.getParentFile(), directory);
    this.written = true;
  }
}
