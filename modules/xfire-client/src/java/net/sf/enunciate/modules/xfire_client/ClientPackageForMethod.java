package net.sf.enunciate.modules.xfire_client;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
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

    String conversion;
    if (unwrapped instanceof TypeMirror) {
      conversion = convert((TypeMirror) unwrapped);
    }
    else if (unwrapped instanceof TypeDeclaration) {
      conversion = convert((TypeDeclaration) unwrapped);
    }
    else if (unwrapped instanceof PackageDeclaration) {
      conversion = convert((PackageDeclaration) unwrapped);
    }
    else {
      if (unwrapped != null) {
        throw new TemplateModelException("Unable to package-convert an instance of " + unwrapped.getClass());
      }
      else {
        return null;
      }
    }

    return conversion;
  }

  /**
   * Returns the client-side package value for the given type.
   *
   * @param typeMirror The type.
   * @return The client-side package value for the type.
   * @throws TemplateModelException If the type mirror cannot be converted for some reason.
   */
  protected String convert(TypeMirror typeMirror) throws TemplateModelException {
    String conversion;
    if (typeMirror instanceof DeclaredType) {
      conversion = convert(((DeclaredType) typeMirror).getDeclaration());
    }
    else if (typeMirror instanceof ArrayType) {
      conversion = convert(((ArrayType) typeMirror).getComponentType());
    }
    else {
      conversion = String.valueOf(typeMirror);
    }
    return conversion;
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
   * @param packageFqn The package to convert.
   * @return The converted package, or the original if no conversions were specified for this value.
   */
  protected String convert(String packageFqn) {
    //todo: support for regular expressions or wildcards?
    for (String pkg : this.conversions.keySet()) {
      if (packageFqn.equals(pkg)) {
        return conversions.get(pkg);
      }
    }
    return packageFqn;
  }

}
