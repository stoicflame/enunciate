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
package com.webcohesion.enunciate;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedPackageElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class EnunciateConfiguration {

  private String defaultSlug = "api";
  private String defaultVersion = null;
  private String defaultTitle = "Web Service API";
  private String defaultDescription = null;
  private String defaultCopyright = null;
  private License defaultApiLicense = null;
  private List<Contact> defaultContacts = new ArrayList<Contact>();
  private final XMLConfiguration source;
  private File base;
  private File configFile;
  private FacetFilter facetFilter;
  private Map<String, String> annotationStyles;
  private Boolean modulesEnabledByDefault;

  public EnunciateConfiguration() {
    this(createDefaultConfigurationSource());
  }

  public static XMLConfiguration createDefaultConfigurationSource() {
    XMLConfiguration xmlConfig = new XMLConfiguration();
    xmlConfig.setDelimiterParsingDisabled(true);
    return xmlConfig;
  }

  public EnunciateConfiguration(XMLConfiguration source) {
    this.source = source;
  }

  public void setBase(File base) {
    this.base = base;
  }

  public File getConfigFile() {
    return configFile;
  }

  public void setConfigFile(File configFile) {
    this.configFile = configFile;
    if (configFile != null) {
      base = configFile.getParentFile();
    }
  }

  public XMLConfiguration getSource() {
    return source;
  }

  public String getSlug() {
    return this.source.getString("[@slug]", this.defaultSlug);
  }

  public void setDefaultSlug(String defaultSlug) {
    this.defaultSlug = defaultSlug;
  }

  public String getVersion() {
    return this.source.getString("[@version]", this.defaultVersion);
  }

  public void setDefaultVersion(String defaultVersion) {
    this.defaultVersion = defaultVersion;
  }

  public String getTitle() {
    return this.source.getString("title", this.defaultTitle);
  }

  public void setDefaultTitle(String defaultTitle) {
    this.defaultTitle = defaultTitle;
  }

  public String getCopyright() {
    return this.source.getString("copyright", this.defaultCopyright);
  }

  public void setDefaultCopyright(String defaultCopyright) {
    this.defaultCopyright = defaultCopyright;
  }

  public String getTerms() {
    return this.source.getString("terms", null);
  }

  public String readDescription(EnunciateContext context, boolean raw, JavaDocTagHandler tagHandler) {
    String descriptionPackage = this.source.getString("description[@package]", null);
    if (descriptionPackage != null) {
      DecoratedPackageElement packageElement = (DecoratedPackageElement) context.getProcessingEnvironment().getElementUtils().getPackageElement(descriptionPackage);
      if (packageElement != null) {
        String docValue = packageElement.getDocValue(tagHandler);
        if (docValue != null) {
          return docValue;
        }
      }
    }

    String description = null;
    String descriptionFile = this.source.getString("description[@file]", null);
    if (descriptionFile != null) {
      description = readFile(descriptionFile);
    }

    String specifiedDescription = this.source.getString("description", null);
    if (specifiedDescription != null) {
      description = specifiedDescription;
    }

    if (description != null && "markdown".equalsIgnoreCase(this.source.getString("description[@format]", "html")) && !raw) {
      description = new PegDownProcessor(Extensions.ALL).markdownToHtml(description);
    }

    return description == null ? this.defaultDescription : description;
  }

  public void setDefaultDescription(String defaultDescription) {
    this.defaultDescription = defaultDescription;
  }

  public String getDefaultNamespace() {
    return this.source.getString("namespaces[@default]", null);
  }

  public Map<String, String> getNamespaces() {
    Map<String, String> namespacePrefixes = new HashMap<String, String>();
    List<HierarchicalConfiguration> namespaceConfigs = this.source.configurationsAt("namespaces.namespace");
    for (HierarchicalConfiguration namespaceConfig : namespaceConfigs) {
      String uri = namespaceConfig.getString("[@uri]", null);
      String prefix = namespaceConfig.getString("[@id]", null);

      if (uri != null && prefix != null) {
        if (prefix.isEmpty()) {
          continue;
        }

        if ("".equals(uri)) {
          uri = null;
        }

        namespacePrefixes.put(uri, prefix);
      }
    }

    return namespacePrefixes;
  }

  public String getApplicationRoot() {
    String root = this.source.getString("application[@root]", null);
    if (root != null && !root.endsWith("/")) {
      root = root + "/";
    }
    return root;
  }

  public License getGeneratedCodeLicense() {
    String text = this.source.getString("code-license", null);
    List<HierarchicalConfiguration> configs = this.source.configurationsAt("code-license");
    for (HierarchicalConfiguration licenseConfig : configs) {
      String file = licenseConfig.getString("[@file]", null);
      String name = licenseConfig.getString("[@name]", null);
      String url = licenseConfig.getString("[@url]", null);
      return new License(name, url, file, text);
    }
    return null;
  }

  public License getApiLicense() {
    String text = this.source.getString("code-license", null);
    List<HierarchicalConfiguration> configs = this.source.configurationsAt("api-license");
    for (HierarchicalConfiguration licenseConfig : configs) {
      String file = licenseConfig.getString("[@file]", null);
      String name = licenseConfig.getString("[@name]", null);
      String url = licenseConfig.getString("[@url]", null);
      return text == null && file == null && name == null && url == null ? this.defaultApiLicense : new License(name, url, file, text);
    }

    return this.defaultApiLicense;
  }

  public void setDefaultApiLicense(License defaultApiLicense) {
    this.defaultApiLicense = defaultApiLicense;
  }

  public List<Contact> getContacts() {
    List<HierarchicalConfiguration> contacts = this.source.configurationsAt("contact");
    ArrayList<Contact> results = new ArrayList<Contact>(contacts.size());
    for (HierarchicalConfiguration configuration : contacts) {
      results.add(new Contact(configuration.getString("[@name]", null), configuration.getString("[@url]", null), configuration.getString("[@email]", null)));
    }
    return results.isEmpty() ? this.defaultContacts : results;
  }

  public void setDefaultContacts(List<Contact> defaultContacts) {
    this.defaultContacts = defaultContacts;
  }

  public String readGeneratedCodeLicenseFile() {
    License license = getGeneratedCodeLicense();
    String filePath = license == null ? null : license.getFile();
    if (filePath == null) {
      return null;
    }

    return readFile(filePath);
  }

  public String readFile(String filePath) {
    File file = resolveFile(filePath);
    try {
      FileReader reader = new FileReader(file);
      StringWriter writer = new StringWriter();
      char[] chars = new char[100];
      int read = -1;
      while ((read = reader.read(chars)) >= 0) {
        writer.write(chars, 0, read);
      }
      reader.close();
      writer.flush();
      writer.close();
      return writer.toString();
    }
    catch (IOException e) {
      throw new EnunciateException(e);
    }
  }

  public File resolveFile(String filePath) {
    if (File.separatorChar != '/') {
      filePath = filePath.replace('/', File.separatorChar); //normalize on the forward slash...
    }

    File resolved = new File(filePath);

    if (!resolved.isAbsolute()) {
      //try to relativize this file to the directory of the config file.
      File base = this.base;
      if (base == null) {
        File configFile = getSource().getFile();
        if (configFile != null) {
          base = configFile.getAbsoluteFile().getParentFile();
        }
      }

      if (base != null) {
        resolved = new File(base, filePath);
      }
    }
    return resolved;
  }

  public FacetFilter getFacetFilter() {
    if (this.facetFilter == null) {
      this.facetFilter = new FacetFilter(getFacetIncludes(), getFacetExcludes());
    }

    return this.facetFilter;
  }

  public Set<String> getFacetIncludes() {
    List<Object> includes = this.source.getList("facets.include[@name]");
    Set<String> facetIncludes = new TreeSet<String>();
    for (Object include : includes) {
      facetIncludes.add(String.valueOf(include));
    }
    return facetIncludes;
  }

  public Set<String> getFacetExcludes() {
    List<Object> excludes = this.source.getList("facets.exclude[@name]");
    Set<String> facetExcludes = new TreeSet<String>();
    for (Object exclude : excludes) {
      facetExcludes.add(String.valueOf(exclude));
    }
    return facetExcludes;
  }

  public Map<String, String> getAnnotationStyles() {
    if (this.annotationStyles == null) {
      this.annotationStyles = loadAnnotationStyles();
    }

    return annotationStyles;
  }

  protected Map<String, String> loadAnnotationStyles() {
    TreeMap<String, String> annotationStyles = new TreeMap<String, String>();

    List<HierarchicalConfiguration> configs = this.source.configurationsAt("styles.annotation");
    for (HierarchicalConfiguration annotationStyleConfig : configs) {
      String name = annotationStyleConfig.getString("[@name]", null);
      String style = annotationStyleConfig.getString("[@style]", null);
      if (name != null && style != null) {
        annotationStyles.put(name, style);
      }
    }

    return annotationStyles;
  }

  public Set<String> getApiIncludeClasses() {
    List<Object> includes = this.source.getList("api-classes.include[@pattern]");
    Set<String> classIncludes = new TreeSet<String>();
    for (Object include : includes) {
      classIncludes.add(String.valueOf(include));
    }
    return classIncludes;
  }

  public Set<String> getApiExcludeClasses() {
    List<Object> excludes = this.source.getList("api-classes.exclude[@pattern]");
    Set<String> classExcludes = new TreeSet<String>();
    for (Object exclude : excludes) {
      classExcludes.add(String.valueOf(exclude));
    }
    return classExcludes;
  }

  public Map<String, String> getFacetPatterns() {
    List<HierarchicalConfiguration> configs = this.source.configurationsAt("api-classes.facet");
    HashMap<String, String> facets = new HashMap<String, String>();
    for (HierarchicalConfiguration config : configs) {
      String pattern = config.getString("[@pattern]");
      String name = config.getString("[@name]");
      if (pattern != null && name != null) {
        facets.put(pattern, name);
      }
    }
    return facets;
  }

  public Set<String> getDisabledWarnings() {
    List<Object> warnings = this.source.getList("warnings.disable[@name]");
    Set<String> disabled = new TreeSet<String>();
    for (Object warning : warnings) {
      disabled.add(String.valueOf(warning));
    }
    return disabled;
  }

  public boolean isModulesEnabledByDefault() {
    if (modulesEnabledByDefault == null) {
      modulesEnabledByDefault = !source.getBoolean("modules[@disabledByDefault]", false);
    }
    return modulesEnabledByDefault;
  }

  public static final class License {

    private final String name;
    private final String url;
    private final String file;
    private final String text;

    public License(String name, String url, String file, String text) {
      this.name = name;
      this.url = url;
      this.file = file;
      this.text = text;
    }

    public String getName() {
      return name;
    }

    public String getUrl() {
      return url;
    }

    public String getFile() {
      return file;
    }

    public String getText() {
      return text;
    }

  }

  public static final class Contact {

    private final String name;
    private final String url;
    private final String email;

    public Contact(String name, String url, String email) {
      this.name = name;
      this.url = url;
      this.email = email;
    }

    public String getName() {
      return name;
    }

    public String getUrl() {
      return url;
    }

    public String getEmail() {
      return email;
    }
  }

}
