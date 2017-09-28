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

import com.webcohesion.enunciate.Enunciate;
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

  public JaxbSchemaFile(Enunciate enunciate, String artifactId, EnunciateJaxbContext context, SchemaInfo schema, FacetFilter facetFilter, Map<String, String> namespacePrefixes) {
    super(enunciate, artifactId, schema.getFilename(), namespacePrefixes, facetFilter);
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
