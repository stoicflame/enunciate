package net.sf.enunciate.modules.xfire.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the war.
 *
 * @author Ryan Heaton
 */
public class WarConfig {

  private final List<String> warLibs = new ArrayList<String>();
  private String name;
  private String file;

  /**
   * The name of the war.
   *
   * @return The name of the war.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the war.
   *
   * @param name The name of the war.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The full path to the war file.
   *
   * @return The full path to the war file.
   */
  public String getFile() {
    return file;
  }

  /**
   * The full path to the war file.
   *
   * @param file The full path to the war file.
   */
  public void setFile(String file) {
    this.file = file;
  }

  /**
   * The list of libraries to include in the war.
   *
   * @return The list of libraries to include in the war.
   */
  public List<String> getWarLibs() {
    return warLibs;
  }

  /**
   * Add a war lib.
   *
   * @param warLib The war lib to add.
   */
  public void addWarLib(WarLib warLib) {
    this.warLibs.add(warLib.getPath());
  }


}
