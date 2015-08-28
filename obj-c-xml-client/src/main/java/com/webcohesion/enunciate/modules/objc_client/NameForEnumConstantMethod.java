package com.webcohesion.enunciate.modules.objc_client;

import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
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
  private final Map<String, String> packages2ids;

  public NameForEnumConstantMethod(String pattern, String projectLabel, Map<String, String> namespaces2ids, Map<String, String> packages2ids) {
    this.pattern = pattern;
    this.packages2ids = packages2ids;
    this.projectLabel = ObjCXMLClientModule.scrubIdentifier(projectLabel);
    this.namespaces2ids = namespaces2ids;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 2) {
      throw new TemplateModelException("The nameForEnumConstant method must have an enum type definition and an enum constant declaration as parameters.");
    }

    BeansWrapper wrapper = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();
    Object unwrapped = wrapper.unwrap((TemplateModel) list.get(0));
    if (!(unwrapped instanceof EnumTypeDefinition)) {
      throw new TemplateModelException("The nameForEnumConstant method must have an enum type definition as a parameter.");
    }
    EnumTypeDefinition typeDefinition = (EnumTypeDefinition) unwrapped;

    unwrapped = wrapper.unwrap((TemplateModel) list.get(1));
    if (!(unwrapped instanceof VariableElement)) {
      throw new TemplateModelException("The nameForEnumConstant method must have an enum constant declaration as a parameter.");
    }
    VariableElement constant = (VariableElement) unwrapped;

    String name = ObjCXMLClientModule.scrubIdentifier(typeDefinition.getName());
    String simpleName = ObjCXMLClientModule.scrubIdentifier(typeDefinition.getSimpleName().toString());
    String clientName = ObjCXMLClientModule.scrubIdentifier(typeDefinition.getClientSimpleName());
    String simpleNameDecap = ObjCXMLClientModule.scrubIdentifier(Introspector.decapitalize(simpleName));
    String clientNameDecap = ObjCXMLClientModule.scrubIdentifier(Introspector.decapitalize(clientName));
    if (name == null) {
      name = "anonymous_" + clientNameDecap;
    }
    PackageElement pckg = typeDefinition.getPackage().getDelegate();
    String packageName = pckg == null ? "" : pckg.getQualifiedName().toString();
    String packageIdentifier = this.packages2ids.containsKey(packageName) ? ObjCXMLClientModule.scrubIdentifier(this.packages2ids.get(packageName)) : ObjCXMLClientModule.scrubIdentifier(packageName);
    String nsid = ObjCXMLClientModule.scrubIdentifier(namespaces2ids.get(typeDefinition.getNamespace()));

    String constantName = ObjCXMLClientModule.scrubIdentifier(constant.getSimpleName().toString());
    String constantClientName = ObjCXMLClientModule.scrubIdentifier(constant.getAnnotation(ClientName.class) != null ? constant.getAnnotation(ClientName.class).value() : constantName);
    return String.format(this.pattern, this.projectLabel, nsid, name, clientName, clientNameDecap, simpleName, simpleNameDecap, packageIdentifier, constantClientName, constantName);
  }

}