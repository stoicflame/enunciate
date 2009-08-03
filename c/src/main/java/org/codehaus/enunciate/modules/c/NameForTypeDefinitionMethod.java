package org.codehaus.enunciate.modules.c;

import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.beans.Introspector;
import java.util.List;
import java.util.Map;

import org.codehaus.enunciate.contract.jaxb.TypeDefinition;

/**
 * Gets a C-style, unambiguous name for a given type definition.
 *
 * @author Ryan Heaton
 */
public class NameForTypeDefinitionMethod implements TemplateMethodModelEx {

  private final String pattern;
  private final String projectLabel;
  private final Map<String, String> namespaces2ids;

  public NameForTypeDefinitionMethod(String pattern, String projectLabel, Map<String, String> namespaces2ids) {
    this.pattern = pattern;
    this.projectLabel = projectLabel;
    this.namespaces2ids = namespaces2ids;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The nameForTypeDefinition method must have a type definition as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (!(unwrapped instanceof TypeDefinition)) {
      throw new TemplateModelException("The nameForTypeDefinition method must have a type definition as a parameter.");
    }

    return calculateName((TypeDefinition) unwrapped);
  }

  public Object calculateName(TypeDefinition typeDefinition) {
    String name = typeDefinition.getName();
    String simpleName = typeDefinition.getSimpleName();
    String clientName = typeDefinition.getClientSimpleName();
    String simpleNameDecap = Introspector.decapitalize(simpleName);
    String clientNameDecap = Introspector.decapitalize(clientName);
    PackageDeclaration pckg = ((TypeDeclaration) typeDefinition).getPackage();
    String packageUnderscored = pckg != null ? pckg.getQualifiedName().replace('.', '_') :"";
    String nsid = namespaces2ids.get(typeDefinition.getNamespace());
    return String.format(this.pattern, this.projectLabel, nsid, name, clientName, clientNameDecap, simpleName, simpleNameDecap, packageUnderscored);
  }
}
