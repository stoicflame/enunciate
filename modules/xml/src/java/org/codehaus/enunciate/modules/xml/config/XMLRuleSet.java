package org.codehaus.enunciate.modules.xml.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * The set of rules to add for the XML module configuration.
 *
 * @author Ryan Heaton
 */
public class XMLRuleSet extends RuleSetBase {

  public void addRuleInstances(Digester digester) {
    digester.addObjectCreate("enunciate/modules/xml/schema", SchemaConfig.class);
    digester.addSetProperties("enunciate/modules/xml/schema");
    digester.addSetNext("enunciate/modules/xml/schema", "addSchemaConfig");

    digester.addObjectCreate("enunciate/modules/xml/wsdl", WsdlConfig.class);
    digester.addSetProperties("enunciate/modules/xml/wsdl");
    digester.addSetNext("enunciate/modules/xml/wsdl", "addWsdlConfig");

    //todo: option to not inline request/response wrappers into the wsdl.
  }

}