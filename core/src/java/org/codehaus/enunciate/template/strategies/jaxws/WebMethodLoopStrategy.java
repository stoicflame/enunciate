/*
 * Copyright 2006-2008 Web Cohesion
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

import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
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
