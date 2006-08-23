package net.sf.enunciate.config;

import net.sf.enunciate.modules.DeploymentModule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;

/**
 * An enunciate digester adds rules when a deployment module is pushed onto the stack.
 *
 * @author Ryan Heaton
 */
public class EnunciateDigester extends Digester {

  @Override
  public void push(Object object) {
    if (object instanceof DeploymentModule) {
      RuleSet rules = ((DeploymentModule) object).getConfigurationRules();
      if (rules != null) {
        addRuleSet(rules);
      }
    }

    super.push(object);
  }

  @Override
  public void push(String string, Object object) {
    if (object instanceof DeploymentModule) {
      RuleSet rules = ((DeploymentModule) object).getConfigurationRules();
      if (rules != null) {
        addRuleSet(rules);
      }
    }

    super.push(string, object);
  }


}
