package com.webcohesion.enunciate.modules.objc_client;

import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
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
 * Gets a C-style, unambiguous name for a given type definition.
 *
 * @author Ryan Heaton
 */
public class NameForTypeDefinitionMethod implements TemplateMethodModelEx {

  private final String pattern;
  private final String projectLabel;
  private final Map<String, String> namespaces2ids;
  private final Map<String, String> packages2ids;

  public NameForTypeDefinitionMethod(String pattern, String projectLabel, Map<String, String> namespaces2ids, Map<String, String> packages2ids) {
    this.pattern = pattern;
    this.packages2ids = packages2ids;
    this.projectLabel = EnunciateObjCClientModule.scrubIdentifier(projectLabel);
    this.namespaces2ids = namespaces2ids;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The nameForTypeDefinition method must have a type definition as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().unwrap(from);
    if (!(unwrapped instanceof TypeDefinition)) {
      throw new TemplateModelException("The nameForTypeDefinition method must have a type definition as a parameter.");
    }

    return calculateName((TypeDefinition) unwrapped);
  }

  public Object calculateName(TypeDefinition typeDefinition) {
    String name = EnunciateObjCClientModule.scrubIdentifier(typeDefinition.getName());
    String simpleName = EnunciateObjCClientModule.scrubIdentifier(typeDefinition.getSimpleName().toString());
    String clientName = EnunciateObjCClientModule.scrubIdentifier(typeDefinition.getClientSimpleName());
    String simpleNameDecap = EnunciateObjCClientModule.scrubIdentifier(Introspector.decapitalize(simpleName));
    String clientNameDecap = EnunciateObjCClientModule.scrubIdentifier(Introspector.decapitalize(clientName));
    if (name == null) {
      name = "anonymous_" + clientNameDecap;
    }
    PackageElement pckg = typeDefinition.getPackage().getDelegate();
    String packageName = pckg == null ? "" : pckg.getQualifiedName().toString();
    String packageIdentifier = this.packages2ids.containsKey(packageName) ? EnunciateObjCClientModule.scrubIdentifier(this.packages2ids.get(packageName)) : EnunciateObjCClientModule.scrubIdentifier(packageName);
    String nsid = EnunciateObjCClientModule.scrubIdentifier(namespaces2ids.get(typeDefinition.getNamespace()));
    return String.format(this.pattern, this.projectLabel, nsid, name, clientName, clientNameDecap, simpleName, simpleNameDecap, packageIdentifier);
  }
}
