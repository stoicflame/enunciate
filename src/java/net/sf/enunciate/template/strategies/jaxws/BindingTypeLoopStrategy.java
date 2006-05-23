package net.sf.enunciate.template.strategies.jaxws;

import net.sf.enunciate.contract.jaxws.BindingType;
import net.sf.enunciate.contract.jaxws.EndpointImplementation;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Loop through the binding types of a given endpoint interface.
 *
 * @author Ryan Heaton
 */
public class BindingTypeLoopStrategy extends EnunciateTemplateLoopStrategy<BindingType> {

  private String var = "bindingType";
  private EndpointInterface endpointInterface;

  //Inherited.
  protected Iterator<BindingType> getLoop(TemplateModel model) throws TemplateException {
    EndpointInterface endpointInterface = this.endpointInterface;

    if (endpointInterface == null) {
      endpointInterface = (EndpointInterface) model.getVariable("endpointInterface");

      if (endpointInterface == null) {
        throw new MissingParameterException("endpointInterface");
      }
    }

    Collection<BindingType> bindingTypes = new ArrayList<BindingType>();
    Collection<EndpointImplementation> impls = endpointInterface.getEndpointImplementations();
    for (EndpointImplementation implementation : impls) {
      String bindingType = implementation.getBindingType();
      if (bindingType != null) {
        bindingTypes.add(BindingType.fromNamespace(bindingType));
      }
    }

    if (bindingTypes.isEmpty()) {
      //spec says if no bindings are present, use SOAP 1.1
      bindingTypes.add(BindingType.SOAP_1_1);
    }

    return bindingTypes.iterator();
  }

  //Inherited.
  @Override
  protected void setupModelForLoop(TemplateModel model, BindingType bindingType, int index) throws TemplateException {
    super.setupModelForLoop(model, bindingType, index);

    if (var != null) {
      model.setVariable(var, bindingType);
    }
  }

  /**
   * The variable into which to store the binding type.
   *
   * @return The variable into which to store the binding type.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the binding type.
   *
   * @param var The variable into which to store the binding type.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * The endpoint interface for which to iterate over each binding type.
   *
   * @return The endpoint interface for which to iterate over each binding type.
   */
  public EndpointInterface getEndpointInterface() {
    return endpointInterface;
  }

  /**
   * The endpoint interface for which to iterate over each binding type.
   *
   * @param endpointInterface The endpoint interface for which to iterate over each binding type.
   */
  public void setEndpointInterface(EndpointInterface endpointInterface) {
    this.endpointInterface = endpointInterface;
  }

}
