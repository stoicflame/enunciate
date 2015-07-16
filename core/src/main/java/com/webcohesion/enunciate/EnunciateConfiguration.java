package com.webcohesion.enunciate;

import com.webcohesion.enunciate.facets.FacetFilter;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
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

}
