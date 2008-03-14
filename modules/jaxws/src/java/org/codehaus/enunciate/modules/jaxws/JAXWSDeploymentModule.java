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

package org.codehaus.enunciate.modules.jaxws;

import freemarker.template.TemplateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.util.ClassDeclarationComparator;
import org.codehaus.enunciate.main.FileArtifact;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.TreeSet;

/**
 * <h1>JAX-WS Module</h1>
 *
 * <p>The JAXWS deployment module is a simple module that generates the request/response/fault beans
 * for doc/lit SOAP operations as specified in the <a href="https://jax-ws.dev.java.net/">JAX-WS specification</a>.</p>
 *
 * <p>The order of the JAXWS deployment module is 0, as it doesn't depend on any artifacts exported
 * by any other module.</p>
 *
 * <ul>
 *   <li><a href="#steps">steps</a></li>
 *   <li><a href="#config">configuration</a></li>
 *   <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <h3>generate</h3>
 *
 * <p>"Generate" is only one significant step in the JAX-WS module.  It generates the stubs according to the JAX-WS spec.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>There are no configuration options for the jaxws deployment module</p>
 * 
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The jaxws deployment module exports its source directory under artifact id "<b>jaxws.src.dir</b>" during the generate step.</p>
 * 
 * @author Ryan Heaton
 * @docFileName module_jaxws.html
 */
public class JAXWSDeploymentModule extends FreemarkerDeploymentModule {

  /**
   * @return "xml"
   */
  @Override
  public String getName() {
    return "jaxws";
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();
    Map<String, WsdlInfo> ns2wsdl = model.getNamespacesToWSDLs();

    URL requestBeanTemplate = JAXWSDeploymentModule.class.getResource("request-bean.fmt");
    URL responseBeanTemplate = JAXWSDeploymentModule.class.getResource("response-bean.fmt");
    URL faultBeanTemplate = JAXWSDeploymentModule.class.getResource("fault-bean.fmt");

    TreeSet<WebFault> visitedFaults = new TreeSet<WebFault>(new ClassDeclarationComparator());
    for (WsdlInfo wsdlInfo : ns2wsdl.values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        for (WebMethod webMethod : ei.getWebMethods()) {
          for (WebMessage webMessage : webMethod.getMessages()) {
            if (webMessage instanceof RequestWrapper) {
              model.put("message", webMessage);
              processTemplate(requestBeanTemplate, model);
            }
            else if (webMessage instanceof ResponseWrapper) {
              model.put("message", webMessage);
              processTemplate(responseBeanTemplate, model);
            }
            else if ((webMessage instanceof WebFault) && ((WebFault) webMessage).isImplicitSchemaElement() && visitedFaults.add((WebFault) webMessage)) {
              model.put("message", webMessage);
              processTemplate(faultBeanTemplate, model);
            }
          }
        }
      }
    }

    File genDir = getGenerateDir();
    getEnunciate().setProperty("jaxws.src.dir", genDir);
    getEnunciate().addArtifact(new FileArtifact(getName(), "jaxws.src.dir", genDir));
  }

  @Override
  public Validator getValidator() {
    return new JAXWSValidator();
  }

}
