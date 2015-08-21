package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JaxrsWadlFile extends BaseXMLInterfaceDescriptionFile {

  private final EnunciateJaxrsContext jaxrsContext;
  private final List<SchemaInfo> schemas;
  private final String stylesheetUri;
  private final String baseUri;

  public JaxrsWadlFile(EnunciateJaxrsContext jaxrsContext, List<SchemaInfo> schemas, String stylesheetUri, String baseUri, Map<String, String> namespacePrefixes, FacetFilter facetFilter) {
    super("application.wadl", namespacePrefixes, facetFilter);
    this.jaxrsContext = jaxrsContext;
    this.schemas = schemas;
    this.stylesheetUri = stylesheetUri;
    this.baseUri = baseUri;
  }

  @Override
  protected Map<String, Object> createModel() {
    Map<String, Object> model = super.createModel();
    model.put("wadlStylesheetUri", this.stylesheetUri);
    model.put("pathResourceGroups", this.jaxrsContext.getResourceGroupsByPath());
    model.put("schemas", this.schemas);
    model.put("baseUri", this.baseUri);
    return model;
  }

  @Override
  protected URL getTemplateURL() {
    return IDLModule.class.getResource("wadl.fmt");
  }
}
