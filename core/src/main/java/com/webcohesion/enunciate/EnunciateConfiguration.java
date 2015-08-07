package com.webcohesion.enunciate;

import com.webcohesion.enunciate.facets.FacetFilter;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class EnunciateConfiguration {

  private String defaultLabel;
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

  public void setDefaultLabel(String defaultLabel) {
    this.defaultLabel = defaultLabel;
  }

  public String getLabel() {
    return this.source.getString("[@label]", this.defaultLabel);
  }

  public String getApplicationRoot() {
    String root = this.source.getString("application[@root]", "/");
    if (!root.endsWith("/")) {
      root = root + "/";
    }
    return root;
  }

  public String getGeneratedCodeLicenseFile() {
    return this.source.getString("[@generatedCodeLicenseFile]", null);
  }

  public String readGeneratedCodeLicense() {
    String filePath = getGeneratedCodeLicenseFile();
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
      //try to relativize this download file to the directory of the config file.
      File configFile = getSource().getFile();
      if (configFile != null) {
        downloadFile = new File(configFile.getAbsoluteFile().getParentFile(), filePath);
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
    Set<String> facetIncludes = new TreeSet<String>();
    for (Object include : includes) {
      facetIncludes.add(String.valueOf(include));
    }
    return facetIncludes;
  }

  public Set<String> getApiExcludeClasses() {
    List<Object> excludes = this.source.getList("api-classes.exclude[@pattern]");
    Set<String> facetExcludes = new TreeSet<String>();
    for (Object exclude : excludes) {
      facetExcludes.add(String.valueOf(exclude));
    }
    return facetExcludes;
  }

}
