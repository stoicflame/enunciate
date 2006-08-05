package net.sf.enunciate.template.freemarker;

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
