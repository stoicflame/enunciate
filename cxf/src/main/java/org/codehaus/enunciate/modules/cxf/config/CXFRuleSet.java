package org.codehaus.enunciate.modules.cxf.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * @author Ryan Heaton
 */
public class CXFRuleSet extends RuleSetBase {

  @Override
  public void addRuleInstances(Digester digester) {
    //allow jaxws properties to be added.
    digester.addCallMethod("enunciate/modules/cxf/jaxws-property", "addJaxwsProperty", 2);
    digester.addCallParam("enunciate/modules/cxf/jaxws-property", 0, "name");
    digester.addCallParam("enunciate/modules/cxf/jaxws-property", 1, "value");
  }
}
