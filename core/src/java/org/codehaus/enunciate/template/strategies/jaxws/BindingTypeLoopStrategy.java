/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.template.strategies.jaxws;

import org.codehaus.enunciate.contract.jaxws.BindingType;
import org.codehaus.enunciate.contract.jaxws.EndpointImplementation;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
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
      bindingTypes.add(implementation.getBindingType());
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
