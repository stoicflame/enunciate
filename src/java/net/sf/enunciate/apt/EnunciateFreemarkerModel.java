package net.sf.enunciate.apt;

import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;

import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnunciateFreemarkerModel extends FreemarkerModel {

  private final Map<String, String> namespacesToPrefixes;
  private final Map<String, SchemaInfo> namespacesToSchemas;
  private final Map<String, WsdlInfo> namespacesToWsdls;

  public EnunciateFreemarkerModel(Map<String, String> ns2prefix, Map<String, SchemaInfo> ns2schema, Map<String, WsdlInfo> ns2wsdl) {
    this.namespacesToPrefixes = ns2prefix;
    this.namespacesToSchemas = ns2schema;
    this.namespacesToWsdls = ns2wsdl;
    setVariable("ns2prefix", ns2prefix);
    setVariable("ns2schema", ns2schema);
    setVariable("ns2wsdl", ns2wsdl);
  }


  /**
   * A map of namespace URIs to their associated prefixes.
   *
   * @return A map of namespace URIs to their associated prefixes.
   */
  public Map<String, String> getNamespacesToPrefixes() {
    return namespacesToPrefixes;
  }

  /**
   * A map of namespace URIs to their associated schema information.
   *
   * @return A map of namespace URIs to their associated schema information.
   */
  public Map<String, SchemaInfo> getNamespacesToSchemas() {
    return namespacesToSchemas;
  }

  /**
   * A map of namespace URIs to their associated WSDL information.
   *
   * @return A map of namespace URIs to their associated WSDL information.
   */
  public Map<String, WsdlInfo> getNamespacesToWSDLs() {
    return namespacesToWsdls;
  }
}
