package org.codehaus.enunciate.template.strategies.jaxws;

import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.Iterator;

/**
 * Loop through the thrown web faults of a given web method.
 *
 * @author Ryan Heaton
 */
public class ThrownWebFaultLoopStrategy extends EnunciateTemplateLoopStrategy<WebFault> {

  private WebMethod webMethod;
  private String var = "webFault";

  //Inherited.
  protected Iterator<WebFault> getLoop(TemplateModel model) throws TemplateException {
    WebMethod webMethod = this.webMethod;
    if (webMethod == null) {
      throw new MissingParameterException("webMethod");
    }

    return webMethod.getWebFaults().iterator();
  }

  //Inherited.
  @Override
  protected void setupModelForLoop(TemplateModel model, WebFault webFault, int index) throws TemplateException {
    super.setupModelForLoop(model, webFault, index);

    if (var != null) {
      model.setVariable(var, webFault);
    }
  }

  /**
   * The web method to iterate over each thrown web fault.
   *
   * @return The web method to iterate over each thrown web fault.
   */
  public WebMethod getWebMethod() {
    return webMethod;
  }

  /**
   * The web method to iterate over each thrown web fault.
   *
   * @param webMethod The web method to iterate over each thrown web fault.
   */
  public void setWebMethod(WebMethod webMethod) {
    this.webMethod = webMethod;
  }

  /**
   * The variable into which to put the current thrown web fault.
   *
   * @return The variable into which to put the current thrown web fault.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to put the current thrown web fault.
   *
   * @param var The variable into which to put the current thrown web fault.
   */
  public void setVar(String var) {
    this.var = var;
  }

}
