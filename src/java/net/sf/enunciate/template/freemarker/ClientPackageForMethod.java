package net.sf.enunciate.template.freemarker;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Converts a package to its alternate client package.
 *
 * @author Ryan Heaton
 */
public class ClientPackageForMethod implements TemplateMethodModel {

  private final LinkedHashMap<String, String> conversions;

  public ClientPackageForMethod(LinkedHashMap<String, String> conversions) {
    if (conversions == null) {
      conversions = new LinkedHashMap<String, String>();
    }
    this.conversions = conversions;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The convertPackage method must have the class or package as a parameter.");
    }

    String from = (String) list.get(0);
    return convert(from);
  }

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
