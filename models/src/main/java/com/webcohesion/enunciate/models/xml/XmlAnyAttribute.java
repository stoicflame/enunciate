package com.webcohesion.enunciate.models.xml;

import com.webcohesion.enunciate.models.binding.CodeBindingMetadata;
import com.webcohesion.enunciate.models.binding.HasCodeBindingMetadata;
import com.webcohesion.enunciate.models.facets.Facet;
import com.webcohesion.enunciate.models.facets.HasFacets;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class XmlAnyAttribute implements HasFacets, HasCodeBindingMetadata {

  private Set<Facet> facets;
  private CodeBindingMetadata bindingMetadata;

  public Set<Facet> getFacets() {
    return facets;
  }

  public void setFacets(Set<Facet> facets) {
    this.facets = facets;
  }

  public CodeBindingMetadata getBindingMetadata() {
    return bindingMetadata;
  }

  public void setBindingMetadata(CodeBindingMetadata bindingMetadata) {
    this.bindingMetadata = bindingMetadata;
  }
}
