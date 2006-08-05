package net.sf.enunciate.template.strategies.jaxws;

import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;

import java.util.Iterator;

/**
 * Strategy that loops through each WSDL.
 *
 * @author Ryan Heaton
 */
public class WsdlLoopStrategy extends EnunciateTemplateLoopStrategy<WsdlInfo> {

  private String var = "wsdl";
  private WsdlInfo currentWsdl;

  /**
   * The loop through the WSDLs.
   *
   * @return The loop through the WSDLs.
   */
  protected Iterator<WsdlInfo> getLoop(TemplateModel model) throws TemplateException {
    return getNamespacesToWSDLs().values().iterator();
  }

  /**
   * Sets the namespace variable value, and establishes the namespace and endpoint interfaces for the current wsdl.
   */
  @Override
  protected void setupModelForLoop(TemplateModel model, WsdlInfo wsdlInfo, int index) throws TemplateException {
    super.setupModelForLoop(model, wsdlInfo, index);

    if (var != null) {
      getModel().setVariable(var, wsdlInfo);
    }

    this.currentWsdl = wsdlInfo;
  }

  /**
   * The variable to which to assign the current wsdl in the loop.
   *
   * @return The variable to which to assign the current wsdl in the loop.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable to which to assign the current wsdl in the loop.
   *
   * @param var The variable to which to assign the current wsdl in the loop.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * The current wsdl in the loop.
   *
   * @return The current wsdl in the loop.
   */
  public WsdlInfo getCurrentWsdl() {
    return currentWsdl;
  }
}
