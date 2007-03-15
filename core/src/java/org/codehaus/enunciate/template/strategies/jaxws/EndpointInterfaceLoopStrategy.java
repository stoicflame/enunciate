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

import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
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
      throw new MissingParameterException("wsdl");
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
