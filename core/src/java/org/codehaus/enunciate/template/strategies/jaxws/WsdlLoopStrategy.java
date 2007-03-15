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
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
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
