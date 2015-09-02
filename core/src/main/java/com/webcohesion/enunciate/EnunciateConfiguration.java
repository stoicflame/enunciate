package com.webcohesion.enunciate;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedPackageElement;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

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
  private FacetFilter facetFilter;

  public EnunciateConfiguration() {
    this(new XMLConfiguration());
  }

  public EnunciateConfiguration(XMLConfiguration source) {
    this.source = source;
  }

  public void setBase(File base) {
    this.base = base;
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

  public String readDescription(EnunciateContext context) {
    String descriptionPackage = this.source.getString("description[@package]", null);
    if (descriptionPackage != null) {
      DecoratedPackageElement packageElement = (DecoratedPackageElement) context.getProcessingEnvironment().getElementUtils().getPackageElement(descriptionPackage);
      if (packageElement != null) {
        String docValue = packageElement.getDocValue();
        if (docValue != null) {
          return docValue;
        }
      }
    }

    return this.source.getString("description", this.defaultDescription);
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

    File file = resolveFile(filePath);
    try {
      FileReader reader = new FileReader(file);
      StringWriter writer = new StringWriter();
      char[] chars = new char[100];
      int read = reader.read(chars) ;
      while (read >= 0) {
        writer.write(chars, 0, read);
      }
      reader.close();
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

    File downloadFile = new File(filePath);

    if (!downloadFile.isAbsolute()) {
      //try to relativize this file to the directory of the config file.
      File base = this.base;
      if (base == null) {
        File configFile = getSource().getFile();
        if (configFile != null) {
          base = configFile.getAbsoluteFile().getParentFile();
        }
      }

      if (base != null) {
        downloadFile = new File(base, filePath);
      }
    }
    return downloadFile;
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
    if (classExcludes.isEmpty()) {
      //if no excludes have been explicitly set, we'll provide a default set.
      classExcludes.addAll(getDefaultApiExcludeClasses());
    }
    return classExcludes;
  }

  public Set<String> getDefaultApiExcludeClasses() {
    TreeSet<String> defaultExcludes = new TreeSet<String>();
    defaultExcludes.add("java.**");
    defaultExcludes.add("javax.**");
    defaultExcludes.add("com.sun.**");
    defaultExcludes.add("org.glassfish.**");
    return defaultExcludes;
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
