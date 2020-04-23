/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.c_client;

import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.EnumValue;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.PackageElement;
import java.beans.Introspector;
import java.util.List;
import java.util.Map;

/**
 * Gets a C-style, unambiguous name for an enum contant.
 *
 * @author Ryan Heaton
 */
public class NameForEnumConstantMethod implements TemplateMethodModelEx {

  private final String pattern;
  private final String projectLabel;
  private final Map<String, String> namespaces2ids;

  public NameForEnumConstantMethod(String pattern, String projectLabel, Map<String, String> namespaces2ids) {
    this.pattern = pattern;
    this.projectLabel = CXMLClientModule.scrubIdentifier(projectLabel);
    this.namespaces2ids = namespaces2ids;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The nameForEnumConstant method must have an enum type definition and an enum constant declaration as parameters.");
    }

    Object unwrapped = FreemarkerUtil.unwrap((TemplateModel) list.get(0));
    if (!(unwrapped instanceof EnumValue)) {
      throw new TemplateModelException("The nameForEnumConstant method must have an enum value as a parameter.");
    }
    EnumValue enumValue = (EnumValue) unwrapped;
    EnumTypeDefinition typeDefinition = enumValue.getTypeDefinition();

    String name = CXMLClientModule.scrubIdentifier(typeDefinition.getName());
    String simpleName = CXMLClientModule.scrubIdentifier(typeDefinition.getSimpleName().toString());
    String clientName = CXMLClientModule.scrubIdentifier(typeDefinition.getClientSimpleName());
    String simpleNameDecap = CXMLClientModule.scrubIdentifier(Introspector.decapitalize(simpleName));
    String clientNameDecap = CXMLClientModule.scrubIdentifier(Introspector.decapitalize(clientName));
    if (name == null) {
      name = "anonymous_" + clientNameDecap;
    }
    PackageElement pckg = typeDefinition.getPackage().getDelegate();
    String packageUnderscored = CXMLClientModule.scrubIdentifier(pckg != null ? pckg.getQualifiedName().toString().replace('.', '_') : "");
    String nsid = CXMLClientModule.scrubIdentifier(namespaces2ids.get(typeDefinition.getNamespace()));

    String constantName = CXMLClientModule.scrubIdentifier(enumValue.getSimpleName().toString());
    String constantClientName = CXMLClientModule.scrubIdentifier(enumValue.getAnnotation(ClientName.class) != null ? enumValue.getAnnotation(ClientName.class).value() : constantName);
    return String.format(this.pattern, this.projectLabel, nsid, name, clientName, clientNameDecap, simpleName, simpleNameDecap, packageUnderscored, constantClientName, constantName);
  }

}