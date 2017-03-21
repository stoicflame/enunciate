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
package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.util.AccessorOverridesAnotherMethod;
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
    String baseUri = this.baseUri;
    if (baseUri == null) {
      baseUri = "http://localhost:8080/"; //if the base uri isn't configured, we have to make something up.
    }
    model.put("baseUri", baseUri);
    SchemaInfo schema = context.getSchemas().get(wsdlInfo.getTargetNamespace());
    if (schema != null) {
      model.put("isDefinedGlobally", new IsDefinedGloballyMethod(schema));
    }
    model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());
    model.put("qnameForType", new QNameForTypeMethod(context));
    return model;
  }

  @Override
  protected URL getTemplateURL() {
    return IDLModule.class.getResource("wsdl.fmt");
  }
}
