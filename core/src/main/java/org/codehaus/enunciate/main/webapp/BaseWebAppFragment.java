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

package org.codehaus.enunciate.main.webapp;

import java.util.Map;
import java.util.List;
import java.io.File;

/**
 * @author Ryan Heaton
 */
public class BaseWebAppFragment implements WebAppFragment {

  private final String id;
  private Map<String, String> contextParameters;
  private List<WebAppComponent> filters;
  private List<String> listeners;
  private List<WebAppComponent> servlets;
  private Map<String, String> mimeMappings;
  private File baseDir;

  public BaseWebAppFragment(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  /**
   * The web app context parameters.
   *
   * @return The web app context parameters.
   */
  public Map<String, String> getContextParameters() {
    return contextParameters;
  }

  /**
   * The web app context parameters.'
   *
   * @param contextParameters The web app context parameters.
   */
  public void setContextParameters(Map<String, String> contextParameters) {
    this.contextParameters = contextParameters;
  }

  /**
   * The servlet filters.
   *
   * @return The servlet filters.
   */
  public List<WebAppComponent> getFilters() {
    return filters;
  }

  /**
   * The servlet filters.
   *
   * @param filters The servlet filters.
   */
  public void setFilters(List<WebAppComponent> filters) {
    this.filters = filters;
  }

  /**
   * The listeners.
   *
   * @return The listeners.
   */
  public List<String> getListeners() {
    return listeners;
  }

  /**
   * The listeners.
   *
   * @param listeners The listeners.
   */
  public void setListeners(List<String> listeners) {
    this.listeners = listeners;
  }

  /**
   * The servlets.
   *
   * @return The servlets.
   */
  public List<WebAppComponent> getServlets() {
    return servlets;
  }

  /**
   * The servlets.
   *
   * @param servlets The servlets.
   */
  public void setServlets(List<WebAppComponent> servlets) {
    this.servlets = servlets;
  }

  /**
   * The mime mappings (extension-to-mime type).
   *
   * @return The mime mappings (extension-to-mime type).
   */
  public Map<String, String> getMimeMappings() {
    return mimeMappings;
  }

  /**
   * The mime mappings (extension-to-mime type).
   *
   * @param mimeMappings The mime mappings (extension-to-mime type).
   */
  public void setMimeMappings(Map<String, String> mimeMappings) {
    this.mimeMappings = mimeMappings;
  }

  /**
   * The directory whose contents will be copied to the webapp base.
   *
   * @return The directory whose contents will be copied to the webapp base.
   */
  public File getBaseDir() {
    return baseDir;
  }

  /**
   * The directory whose contents will be copied to the webapp base.
   *
   * @param baseDir The directory whose contents will be copied to the webapp base.
   */
  public void setBaseDir(File baseDir) {
    this.baseDir = baseDir;
  }
}
