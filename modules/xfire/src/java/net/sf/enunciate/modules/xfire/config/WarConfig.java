package net.sf.enunciate.modules.xfire.config;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Configuration for the war.
 *
 * @author Ryan Heaton
 */
public class WarConfig {

  private final List<String> warLibs = new ArrayList<String>();
  private String name;
  private URL webXMLTransformURL;

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
   * The list of libraries to include in the war.
   *
   * @return The list of libraries to include in the war.
   */
  public List<String> getWarLibs() {
    return warLibs;
  }

  /**
   * The (optional) stylesheet transformation through which to pass the generated web.xml file.
   *
   * @return The stylesheet transformation through which to pass the generated web.xml file.
   */
  public URL getWebXMLTransformURL() {
    return webXMLTransformURL;
  }

  /**
   * The (optional) stylesheet transformation through which to pass the generated web.xml file.
   *
   * @param stylesheet The stylesheet transformation through which to pass the generated web.xml file.
   */
  public void setWebXMLTransformURL(URL stylesheet) {
    this.webXMLTransformURL = stylesheet;
  }

  /**
   * The (optional) stylesheet transformation through which to pass the generated web.xml file.
   *
   * @param stylesheet The stylesheet transformation through which to pass the generated web.xml file.
   */
  public void setWebXMLTransform(File stylesheet) throws MalformedURLException {
    this.webXMLTransformURL = stylesheet.toURL();
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
