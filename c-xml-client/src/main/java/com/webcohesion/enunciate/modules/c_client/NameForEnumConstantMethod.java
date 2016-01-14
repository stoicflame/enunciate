package com.webcohesion.enunciate.modules.c_client;

import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.EnumValue;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
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

    BeansWrapper wrapper = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();
    Object unwrapped = wrapper.unwrap((TemplateModel) list.get(0));
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