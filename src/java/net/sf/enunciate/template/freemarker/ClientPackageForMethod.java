package net.sf.enunciate.template.freemarker;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Gets the qualified package name for a package or type.
 *
 * @author Ryan Heaton
 */
public class ClientPackageForMethod implements TemplateMethodModelEx {

  private final LinkedHashMap<String, String> conversions;

  /**
   * @param conversions The conversions.
   */
  public ClientPackageForMethod(LinkedHashMap<String, String> conversions) {
    if (conversions == null) {
      conversions = new LinkedHashMap<String, String>();
    }
    this.conversions = conversions;
  }

  /**
   * Gets the client-side package for the type, type declaration, package, or their string values.
   *
   * @param list The arguments.
   * @return The string value of the client-side package.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The convertPackage method must have the class or package as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if ((unwrapped instanceof DeclaredType) || (unwrapped instanceof TypeDeclaration)) {
      TypeDeclaration declaration;
      if (unwrapped instanceof DeclaredType) {
        declaration = ((DeclaredType) unwrapped).getDeclaration();
      }
      else {
        declaration = (TypeDeclaration) unwrapped;
      }

      return convert(declaration);
    }
    else if (unwrapped instanceof PackageDeclaration) {
      return convert((PackageDeclaration) unwrapped);
    }
    else {
      return convert(String.valueOf(unwrapped));
    }

  }

  /**
   * Returns the client-side package value for the given type declaration.
   *
   * @param declaration The declaration.
   * @return The client-side package value for the declaration.
   */
  protected String convert(TypeDeclaration declaration) {
    return convert(declaration.getPackage());
  }

  /**
   * Converts the package declaration to its client-side package value.
   *
   * @param packageDeclaration The package declaration.
   * @return The package declaration.
   */
  protected String convert(PackageDeclaration packageDeclaration) {
    return convert(packageDeclaration.getQualifiedName());
  }

  /**
   * Converts the possible package to the specified client-side package, if any conversions are specified.
   *
   * @param from The package fqn to convert.
   * @return The converted package, or the original if no conversions were specified for this value.
   */
  protected String convert(String from) {
    //todo: support for regular expressions?
    for (String pkg : this.conversions.keySet()) {
      if (from.equals(pkg)) {
        return conversions.get(pkg);
      }
    }
    return from;
  }

}
