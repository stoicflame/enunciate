package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.util.AccessorOverridesAnotherMethod;

import java.net.URL;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JaxbSchemaFile extends BaseXMLInterfaceDescriptionFile {

  private final EnunciateJaxbContext context;
  private final SchemaInfo schema;

  public JaxbSchemaFile(EnunciateJaxbContext context, SchemaInfo schema, FacetFilter facetFilter, Map<String, String> namespacePrefixes) {
    super(schema.getFilename(), namespacePrefixes, facetFilter);
    this.context = context;
    this.schema = schema;
  }

  @Override
  protected Map<String, Object> createModel() {
    Map<String, Object> model = super.createModel();
    model.put("isDefinedGlobally", new IsDefinedGloballyMethod(schema));
    model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());
    model.put("qnameForType", new QNameForTypeMethod(context));
    model.put("schema", this.schema);
    return model;
  }

  @Override
  protected URL getTemplateURL() {
    return IDLModule.class.getResource("schema.fmt");
  }


}
