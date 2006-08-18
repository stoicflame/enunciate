package net.sf.enunciate.modules.xfire;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

import java.util.LinkedHashMap;

/**
 * Converts a fully-qualified class name to its alternate client fully-qualified class name.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends ClientPackageForMethod {

  public ClientClassnameForMethod(LinkedHashMap<String, String> conversions) {
    super(conversions);
  }

  @Override
  protected String convert(TypeDeclaration declaration) {
    return convert(declaration.getQualifiedName());
  }

  @Override
  protected String convert(PackageDeclaration packageDeclaration) {
    throw new UnsupportedOperationException("packages don't have a client classname.");
  }

  protected String convert(String from) {
    int lastDot = from.lastIndexOf('.');
    if (lastDot < 0) {
      return from;
    }

    String simpleName = from.substring(lastDot + 1);
    String convertedPackage = from.substring(0, lastDot);
    return super.convert(convertedPackage) + "." + simpleName;
  }

}
