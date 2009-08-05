package org.codehaus.enunciate.modules.c;

import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.ClientName;
import org.codehaus.enunciate.contract.jaxb.EnumTypeDefinition;

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
    this.projectLabel = CDeploymentModule.scrubIdentifier(projectLabel);
    this.namespaces2ids = namespaces2ids;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 2) {
      throw new TemplateModelException("The nameForEnumConstant method must have an enum type definition and an enum constant declaration as parameters.");
    }

    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(0));
    if (!(unwrapped instanceof EnumTypeDefinition)) {
      throw new TemplateModelException("The nameForEnumConstant method must have an enum type definition as a parameter.");
    }
    EnumTypeDefinition typeDefinition = (EnumTypeDefinition) unwrapped;

    unwrapped = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(1));
    if (!(unwrapped instanceof EnumConstantDeclaration)) {
      throw new TemplateModelException("The nameForEnumConstant method must have an enum constant declaration as a parameter.");
    }
    EnumConstantDeclaration constant = (EnumConstantDeclaration) unwrapped;

    String name = CDeploymentModule.scrubIdentifier(typeDefinition.getName());
    String simpleName = CDeploymentModule.scrubIdentifier(typeDefinition.getSimpleName());
    String clientName = CDeploymentModule.scrubIdentifier(typeDefinition.getClientSimpleName());
    String simpleNameDecap = CDeploymentModule.scrubIdentifier(Introspector.decapitalize(simpleName));
    String clientNameDecap = CDeploymentModule.scrubIdentifier(Introspector.decapitalize(clientName));
    if (name == null) {
      name = "anonymous_" + clientNameDecap;
    }
    PackageDeclaration pckg = ((TypeDeclaration) typeDefinition).getPackage();
    String packageUnderscored = CDeploymentModule.scrubIdentifier(pckg != null ? pckg.getQualifiedName().replace('.', '_') :"");
    String nsid = CDeploymentModule.scrubIdentifier(namespaces2ids.get(typeDefinition.getNamespace()));

    String constantName = CDeploymentModule.scrubIdentifier(constant.getSimpleName());
    String constantClientName = CDeploymentModule.scrubIdentifier(constant.getAnnotation(ClientName.class) != null ? constant.getAnnotation(ClientName.class).value() : constantName);
    return String.format(this.pattern, this.projectLabel, nsid, name, clientName, clientNameDecap, simpleName, simpleNameDecap, packageUnderscored, constantClientName, constantName);
  }

}