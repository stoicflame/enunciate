package net.sf.enunciate.modules.xml.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * The set of rules to add for the XML module configuration.
 *
 * @author Ryan Heaton
 */
public class XMLRuleSet extends RuleSetBase {

  public void addRuleInstances(Digester digester) {
    //todo: make these rules independent of the position in the xml config file.
    //todo: i.e. make sure we don't need the "enunciate/modules"

    digester.addObjectCreate("enunciate/modules/xml/schema", SchemaConfig.class);
    digester.addSetProperties("enunciate/modules/xml/schema");
    digester.addSetNext("enunciate/modules/xml/schema", "addSchemaConfig");

    digester.addObjectCreate("enunciate/modules/xml/wsdl", WsdlConfig.class);
    digester.addSetProperties("enunciate/modules/xml/wsdl");
    digester.addSetNext("enunciate/modules/xml/wsdl", "addWsdlConfig");
  }

  @Override
  public String getNamespaceURI() {
    return "http://enunciate.sf.net";
  }
}
