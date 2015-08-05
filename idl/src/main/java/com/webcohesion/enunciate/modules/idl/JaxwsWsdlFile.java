package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxws.WsdlInfo;

import java.net.URL;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JaxwsWsdlFile extends BaseXMLInterfaceDescriptionFile {

  private final WsdlInfo wsdlInfo;
  private final String baseUri;
  private final EnunciateJaxbContext context;

  public JaxwsWsdlFile(WsdlInfo wsdlInfo, EnunciateJaxbContext context, String baseUri, Map<String, String> namespacePrefixes, FacetFilter facetFilter) {
    super(wsdlInfo.getFilename(), namespacePrefixes, facetFilter);
    this.wsdlInfo = wsdlInfo;
    this.baseUri = baseUri;
    this.context = context;
  }

  @Override
  protected Map<String, Object> createModel() {
    Map<String, Object> model = super.createModel();
    model.put("wsdl", this.wsdlInfo);
    model.put("baseUri", this.baseUri);
    SchemaInfo schema = context.getSchemas().get(wsdlInfo.getNamespace());
    if (schema != null) {
      model.put("isDefinedGlobally", new IsDefinedGloballyMethod(schema));
    }
    model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());
    model.put("qnameForType", new QNameForTypeMethod(context));
    return model;
  }

  @Override
  protected URL getTemplateURL() {
    return EnunciateIDLModule.class.getResource("wsdl.fmt");
  }
}
