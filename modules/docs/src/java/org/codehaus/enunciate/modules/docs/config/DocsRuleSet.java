package org.codehaus.enunciate.modules.docs.config;

import org.apache.commons.digester.RuleSetBase;
import org.apache.commons.digester.Digester;

/**
 * The set of rules to add for the XML module configuration.
 *
 * @author Ryan Heaton
 */
public class DocsRuleSet extends RuleSetBase {

  public void addRuleInstances(Digester digester) {
    digester.addObjectCreate("enunciate/modules/docs/download", DownloadConfig.class);
    digester.addSetProperties("enunciate/modules/docs/download");
    digester.addSetNext("enunciate/modules/docs/download", "addDownload");
  }

}
