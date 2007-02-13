package net.sf.enunciate.template.strategies.jaxws;

import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.WebMethod;
import net.sf.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.Iterator;

/**
 * A loop strategy for all the web methods of an endpoint interface.
 *
 * @author Ryan Heaton
 */
public class WebMethodLoopStrategy extends EnunciateTemplateLoopStrategy<WebMethod> {

  private String var = "webMethod";
  private EndpointInterface endpointInterface;

  protected Iterator<WebMethod> getLoop(TemplateModel model) throws TemplateException {
    EndpointInterface endpointInterface = this.endpointInterface;
    if (endpointInterface == null) {
      throw new MissingParameterException("endpointInterface");
    }

    return endpointInterface.getWebMethods().iterator();
  }

  // Inherited.
  @Override
  protected void setupModelForLoop(TemplateModel model, WebMethod method, int index) throws TemplateException {
    super.setupModelForLoop(model, method, index);

    if (var != null) {
      model.setVariable(var, method);
    }
  }

  /**
   * The endpoint interface.
   *
   * @return The endpoint interface.
   */
  public EndpointInterface getEndpointInterface() {
    return endpointInterface;
  }

  /**
   * The endpoint interface.
   *
   * @param endpointInterface The endpoint interface.
   */
  public void setEndpointInterface(EndpointInterface endpointInterface) {
    this.endpointInterface = endpointInterface;
  }

  /**
   * The variable into which to store the current web method.
   *
   * @return The variable into which to store the current web method.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the current web method.
   *
   * @param var The variable into which to store the current web method.
   */
  public void setVar(String var) {
    this.var = var;
  }
}
