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

package org.codehaus.enunciate.modules.spring_app.config;

import org.codehaus.enunciate.EnunciateException;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Configuration for the war.
 *
 * @author Ryan Heaton
 */
public class WarConfig {

  private boolean includeClasspathLibs = true;
  private boolean excludeDefaultLibs = true;
  private final List<IncludeExcludeLibs> excludeLibs = new ArrayList<IncludeExcludeLibs>();
  private final List<IncludeExcludeLibs> includeLibs = new ArrayList<IncludeExcludeLibs>();
  private String name;
  private URL webXMLTransformURL;
  private String preBase;
  private String postBase;
  private String docsDir;
  private String gwtAppDir;
  private final Manifest manifest = getDefaultManifest();

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
   * Whether to include the default libs.
   *
   * @return Whether to include the default libs.
   */
  public boolean isIncludeClasspathLibs() {
    return includeClasspathLibs;
  }

  /**
   * Whether to include the default libs.
   *
   * @param includeClasspathLibs Whether to include the default libs.
   */
  public void setIncludeClasspathLibs(boolean includeClasspathLibs) {
    this.includeClasspathLibs = includeClasspathLibs;
  }

  /**
   * Whether to exclude the default libs.
   *
   * @return Whether to exclude the default libs.
   */
  public boolean isExcludeDefaultLibs() {
    return excludeDefaultLibs;
  }

  /**
   * Whether to exclude the default libs.
   *
   * @param excludeDefaultLibs Whether to exclude the default libs.
   */
  public void setExcludeDefaultLibs(boolean excludeDefaultLibs) {
    this.excludeDefaultLibs = excludeDefaultLibs;
  }

  /**
   * Whether to exclude the default libs. (Support for backwards-compatability to account for a typo).
   *
   * @param excludeDefaultLibs Whether to exclude the default libs.
   */
  public void setExludeDefaultLibs(boolean excludeDefaultLibs) {
    this.excludeDefaultLibs = excludeDefaultLibs;
  }

  /**
   * Add a war lib.
   *
   * @param warLib The war lib to add.
   * @deprecated use "addIncludeJars"
   */
  public void addWarLib(WarLib warLib) throws EnunciateException {
    throw new EnunciateException("The \"lib\" element has been replaced by the more flexible \"includeLibs\" element.  See the Enunciate docs for details.");
  }

  /**
   * Add a exclude jars.
   *
   * @param excludeLibs The exclude jars to add.
   */
  public void addExcludeLibs(IncludeExcludeLibs excludeLibs) {
    this.excludeLibs.add(excludeLibs);
  }

  /**
   * Get the list of exclude jars.
   *
   * @return The list of exclude jars.
   */
  public List<IncludeExcludeLibs> getExcludeLibs() {
    return excludeLibs;
  }

  /**
   * Add a include jars.
   *
   * @param includeLibs The include jars to add.
   */
  public void addIncludeLibs(IncludeExcludeLibs includeLibs) {
    this.includeLibs.add(includeLibs);
  }

  /**
   * Get the list of include jars.
   *
   * @return The list of include jars.
   */
  public List<IncludeExcludeLibs> getIncludeLibs() {
    return includeLibs;
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
   * The base of the war directory before copying enunciate-specific files.
   *
   * @return The base of the war directory before copying enunciate-specific files.
   */
  public String getPreBase() {
    return preBase;
  }

  /**
   * The base of the war directory before copying enunciate-specific files.
   *
   * @param preBase The base of the war directory before copying enunciate-specific files.
   */
  public void setPreBase(String preBase) {
    this.preBase = preBase;
  }

  /**
   * The base of the war directory after copying enunciate-specific files.
   *
   * @return The base of the war directory after copying enunciate-specific files.
   */
  public String getPostBase() {
    return postBase;
  }

  /**
   * The base of the war directory after copying enunciate-specific files.
   *
   * @param postBase The base of the war directory after copying enunciate-specific files.
   */
  public void setPostBase(String postBase) {
    this.postBase = postBase;
  }

  /**
   * The directory in the war in which to expand the documentation (including the WSDLs and schemas).
   *
   * @return The directory in the war in which to expand the documentation (including the WSDLs and schemas).
   */
  public String getDocsDir() {
    return docsDir;
  }

  /**
   * The directory in the war in which to expand the documentation (including the WSDLs and schemas).
   *
   * @param docsDir The directory in the war in which to expand the documentation (including the WSDLs and schemas).
   */
  public void setDocsDir(String docsDir) {
    this.docsDir = docsDir;
  }

  /**
   * The directory in the war in which to expand the gwt app(s).
   *
   * @return The directory in the war in which to expand the gwt app(s).
   */
  public String getGwtAppDir() {
    return gwtAppDir;
  }

  /**
   * The directory in the war in which to expand the gwt app(s).
   *
   * @param gwtAppDir The directory in the war in which to expand the gwt app(s).
   */
  public void setGwtAppDir(String gwtAppDir) {
    this.gwtAppDir = gwtAppDir;
  }

  /**
   * The manifest for this war.
   *
   * @return The manifest for this war.
   */
  public Manifest getManifest() {
    return manifest;
  }

  /**
   * Adds a manifest entry to this war's manifest.
   *
   * @param section The section.  If null, the main section is assumed.
   * @param name The name of the attribute.
   * @param value The value of the attribute.
   */
  public void addManifestAttribute(String section, String name, String value) {
    Attributes attributes;
    if (section == null) {
      attributes = this.manifest.getMainAttributes();
    }
    else {
      attributes = this.manifest.getAttributes(section);
      if (attributes == null) {
        attributes = new Attributes();
        this.manifest.getEntries().put(section, attributes);
      }
    }
    attributes.putValue(name, value);
  }

  /**
   * Get the default manifest for a war file.
   *
   * @return The default manifest for a war file.
   */
  public static Manifest getDefaultManifest() {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
    manifest.getMainAttributes().putValue("Created-By", "Enunciate");
    return manifest;
  }

}
