package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;

import java.net.URL;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JaxbSchemaFile extends BaseXMLInterfaceDescriptionFile {

  private final SchemaInfo schema;

  public JaxbSchemaFile(SchemaInfo schema, FacetFilter facetFilter, Map<String, String> namespacePrefixes) {
    super(schema.getFilename(), namespacePrefixes, facetFilter);
    this.schema = schema;
  }

  @Override
  protected Map<String, Object> createModel() {
    Map<String, Object> model = super.createModel();
    model.put("isDefinedGlobally", new IsDefinedGloballyMethod(schema));
    model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());
    model.put("qnameForType", new QNameForTypeMethod());
    model.put("schema", this.schema);
    return model;
  }

  @Override
  protected URL getTemplateURL() {
    return IDLDeploymentModule.class.getResource("schema.fmt");
  }


}
