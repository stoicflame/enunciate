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
