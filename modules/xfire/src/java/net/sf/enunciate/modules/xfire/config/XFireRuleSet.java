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
    digester.addSetNext("enunciate/modules/xfire/war", "setWarConfig");

    digester.addObjectCreate("enunciate/modules/xfire/war/lib", WarLib.class);
    digester.addSetProperties("enunciate/modules/xfire/war/lib");
    digester.addSetNext("enunciate/modules/xfire/war/lib", "addWarLib");

    digester.addObjectCreate("enunciate/modules/xfire/springImport", SpringImport.class);
    digester.addSetProperties("enunciate/modules/xfire/springImport");
    digester.addSetNext("enunciate/modules/xfire/springImport", "addSpringImport");

    digester.addObjectCreate("enunciate/modules/xfire/copyResources", CopyResources.class);
    digester.addSetProperties("enunciate/modules/xfire/copyResources");
    digester.addSetNext("enunciate/modules/xfire/copyResources", "addCopyResources");
  }

}
