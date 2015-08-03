package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxws.WsdlInfo;

import java.net.URL;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JaxwsWsdlFile extends BaseXMLInterfaceDescriptionFile {

  private final WsdlInfo wsdlInfo;
  private final String baseUri;

  public JaxwsWsdlFile(WsdlInfo wsdlInfo, String baseUri, Map<String, String> namespacePrefixes, FacetFilter facetFilter) {
    super(wsdlInfo.getFilename(), namespacePrefixes, facetFilter);
    this.wsdlInfo = wsdlInfo;
    this.baseUri = baseUri;
  }

  @Override
  protected Map<String, Object> createModel() {
    Map<String, Object> model = super.createModel();
    model.put("wsdl", this.wsdlInfo);
    model.put("baseUri", this.baseUri);
    return model;
  }

  @Override
  protected URL getTemplateURL() {
    return IDLDeploymentModule.class.getResource("wsdl.fmt");
  }
}
