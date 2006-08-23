package net.sf.enunciate.template.strategies.jaxws;

import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.Iterator;

/**
 * Loop through each endpoint interface of a given WSDL.
 *
 * @author Ryan Heaton
 */
public class EndpointInterfaceLoopStrategy extends EnunciateTemplateLoopStrategy<EndpointInterface> {

  private WsdlInfo wsdl;
  private String var = "endpointInterface";

  //Inherited.
  protected Iterator<EndpointInterface> getLoop(TemplateModel model) throws TemplateException {
    WsdlInfo wsdl = this.wsdl;
    if (wsdl == null) {
      wsdl = (WsdlInfo) model.getVariable("wsdl");

      if (wsdl == null) {
        throw new MissingParameterException("wsdl");
      }
    }

    return wsdl.getEndpointInterfaces().iterator();
  }

  //Inherited.
  @Override
  protected void setupModelForLoop(TemplateModel model, EndpointInterface endpointInterface, int index) throws TemplateException {
    super.setupModelForLoop(model, endpointInterface, index);

    if (var != null) {
      model.setVariable(var, endpointInterface);
    }
  }

  /**
   * The WSDL from which to iterate over each endpoint interface.
   *
   * @return The WSDL from which to iterate over each endpoint interface.
   */
  public WsdlInfo getWsdl() {
    return wsdl;
  }

  /**
   * The WSDL from which to iterate over each endpoint interface.
   *
   * @param wsdl The WSDL from which to iterate over each endpoint interface.
   */
  public void setWsdl(WsdlInfo wsdl) {
    this.wsdl = wsdl;
  }

  /**
   * The variable to which to assing the current endpoint interface.
   *
   * @return The variable to which to assing the current endpoint interface.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable to which to assing the current endpoint interface.
   *
   * @param var The variable to which to assing the current endpoint interface.
   */
  public void setVar(String var) {
    this.var = var;
  }
}
