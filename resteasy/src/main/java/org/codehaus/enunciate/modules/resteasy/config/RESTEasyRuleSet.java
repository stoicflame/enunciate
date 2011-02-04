package org.codehaus.enunciate.modules.resteasy.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * @author Ryan Heaton
 */
public class RESTEasyRuleSet extends RuleSetBase {

  @Override
  public void addRuleInstances(Digester digester) {
    //allow jaxws properties to be added.
    digester.addCallMethod("enunciate/modules/resteasy/option", "addOption", 2);
    digester.addCallParam("enunciate/modules/resteasy/option", 0, "name");
    digester.addCallParam("enunciate/modules/resteasy/option", 1, "value");
  }
}
