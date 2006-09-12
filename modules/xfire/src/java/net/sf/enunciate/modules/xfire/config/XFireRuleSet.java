package net.sf.enunciate.modules.xfire.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * Rules for the configuration of the XFire client module.
 *
 * @author Ryan Heaton
 */
public class XFireRuleSet extends RuleSetBase {

  public void addRuleInstances(Digester digester) {
    //allow the war file to be created.
    digester.addObjectCreate("enunciate/modules/xfire/war", WarConfig.class);
    digester.addSetProperties("enunciate/modules/xfire/war");
    digester.addSetNext("enunciate/modules/xfire/war/lib", "setWarCofnig");

    digester.addObjectCreate("enunciate/modules/xfire/war/lib", WarLib.class);
    digester.addSetProperties("enunciate/modules/xfire/war/lib");
    digester.addSetNext("enunciate/modules/xfire/war/lib", "addWarLib");

    //todo: add rules for configuration of invocation handlers?

    //todo: add rules for configuration of xfire in/out handlers?

    //todo: add rules for configuration of spring interceptors for the service bean?
  }

  @Override
  public String getNamespaceURI() {
    return "http://enunciate.sf.net";
  }
}
