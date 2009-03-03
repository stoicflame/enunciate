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

package org.codehaus.enunciate.modules.xml.config;

/**
 * The object used to configure the generation of a schema, overrides the defaults.
 *
 * @author Ryan Heaton
 */
public class SchemaConfig {

  private String namespace;
  private String file;
  private String location;
  private String jaxbBindingVersion;
  private String appinfo;

  /**
   * The target namespace.
   *
   * @return The target namespace.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * The target namespace.
   *
   * @param namespace The target namespace.
   */
  public void setNamespace(String namespace) {
    if ("".equals(namespace)) {
      namespace = null;
    }

    this.namespace = namespace;
  }

  /**
   * The file to which to write this schema.
   *
   * @return The file to which to write this schema.
   */
  public String getFile() {
    return file;
  }

  /**
   * The file to which to write this schema.
   *
   * @param file The file to which to write this schema.
   */
  public void setFile(String file) {
    this.file = file;
  }

  /**
   * The schema location.
   *
   * @return The schema location.
   */
  public String getLocation() {
    return location;
  }

  /**
   * The schema location.
   *
   * @param location The schema location.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Data to put in the appinfo section.
   *
   * @return Data to put in the appinfo section.
   */
  public String getAppinfo() {
    return appinfo;
  }

  /**
   * Data to put in the appinfo section.
   *
   * @param appinfo Data to put in the appinfo section.
   */
  public void setAppinfo(String appinfo) {
    this.appinfo = appinfo;
  }

  /**
   * The JAXB binding version.
   *
   * @return The JAXB binding version.
   */
  public String getJaxbBindingVersion() {
    return jaxbBindingVersion;
  }

  /**
   * The JAXB binding version.
   *
   * @param jaxbBindingVersion The JAXB binding version.
   */
  public void setJaxbBindingVersion(String jaxbBindingVersion) {
    this.jaxbBindingVersion = jaxbBindingVersion;
  }
}
