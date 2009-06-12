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

import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Loop through the web faults of a specified wsdl.
 *
 * @author Ryan Heaton
 */
public class WebFaultLoopStrategy extends EnunciateTemplateLoopStrategy<WebFault> {

  private String var = "webFault";
  private WsdlInfo wsdl;

  protected Iterator<WebFault> getLoop(TemplateModel model) throws TemplateException {
    Collection<WsdlInfo> wsdls;
    if (this.wsdl != null) {
      wsdls = Arrays.asList(wsdl);
    }
    else {
      wsdls = getNamespacesToWSDLs().values();
    }

    HashMap<String, WebFault> declaredFaults = new HashMap<String, WebFault>();
    for (WsdlInfo wsdl : wsdls) {
      for (EndpointInterface ei : wsdl.getEndpointInterfaces()) {
        Collection<WebMethod> webMethods = ei.getWebMethods();
        for (WebMethod webMethod : webMethods) {
          for (WebFault webFault : webMethod.getWebFaults()) {
            declaredFaults.put(webFault.getQualifiedName(), webFault);
          }
        }
      }
    }

    return declaredFaults.values().iterator();
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, WebFault webFault, int index) throws TemplateException {
    super.setupModelForLoop(model, webFault, index);

    if (var != null) {
      model.setVariable(var, webFault);
    }
  }

  /**
   * The variable into which to store the web fault.
   *
   * @return The variable into which to store the web fault.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the web fault.
   *
   * @param var The variable into which to store the web fault.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * The WSDL from which to list the web faults.
   *
   * @return The WSDL from which to list the web faults.
   */
  public WsdlInfo getWsdl() {
    return wsdl;
  }

  /**
   * The WSDL from which to list the web faults.
   *
   * @param wsdl The WSDL from which to list the web faults.
   */
  public void setWsdl(WsdlInfo wsdl) {
    this.wsdl = wsdl;
  }


}
