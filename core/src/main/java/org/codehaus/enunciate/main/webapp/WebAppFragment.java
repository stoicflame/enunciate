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
 * Interface for modules that produce a part of a web application.
 *
 * @author Ryan Heaton
 */
public interface WebAppFragment {

  /**
   * Unique ID used to uniquely identify this webapp fragment.
   *
   * @return Unique ID used to uniquely identify this webapp fragment.
   */
  String getId();

  /**
   * The web app context parameters.
   *
   * @return The web app context parameters.
   */
  Map<String, String> getContextParameters();

  /**
   * The servlet filters.
   *
   * @return The servlet filters.
   */
  List<WebAppComponent> getFilters();

  /**
   * The listeners.
   *
   * @return The listeners.
   */
  List<String> getListeners();

  /**
   * The servlets.
   *
   * @return The servlets.
   */
  List<WebAppComponent> getServlets();

  /**
   * The mime mappings (extension-to-mime type).
   *
   * @return The mime mappings (extension-to-mime type).
   */
  Map<String, String> getMimeMappings();

  /**
   * The directory whose contents will be copied to the webapp base.
   *
   * @return The directory whose contents will be copied to the webapp base.
   */
  File getBaseDir();
}
