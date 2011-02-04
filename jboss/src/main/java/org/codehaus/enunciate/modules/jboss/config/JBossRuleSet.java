package org.codehaus.enunciate.modules.jboss.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * @author Ryan Heaton
 */
public class JBossRuleSet extends RuleSetBase {

  @Override
  public void addRuleInstances(Digester digester) {
    //allow jboss options to be added.
    digester.addCallMethod("enunciate/modules/jboss/option", "addOption", 2);
    digester.addCallParam("enunciate/modules/jboss/option", 0, "name");
    digester.addCallParam("enunciate/modules/jboss/option", 1, "value");
  }
}
