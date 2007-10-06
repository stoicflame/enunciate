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

package org.codehaus.enunciate.modules.xfire;

import freemarker.template.TemplateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.EnunciateException;

import java.io.IOException;
import java.net.URL;

/**
 * <h1>XFire Module</h1>
 *
 * <p>The XFire deployment module is a simple module that generates the request/response beans
 * for rpc/lit SOAP operations.</p>
 *
 * <p>The XFire module <i>used</i> to be the primary module for assembing the app.  As of release 1.5,
 * this functionality has been separated into the spring-app module.</i>
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
 * <p>The "generate" step generates the source beans.  It is the only significant step in the XFire module.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>There are no additional configuration elements for the XFire module.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The XFire deployment module exports the following artifacts:</p>
 *
 * <ul>
 *   <li>The "xfire-server.src.dir" artifact is the directory where the beans are generated.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public class XFireDeploymentModule extends FreemarkerDeploymentModule {

  /**
   * @return "xfire"
   */
  @Override
  public String getName() {
    return "xfire";
  }

  /**
   * @return The URL to "rpc-request-bean.fmt"
   */
  protected URL getRPCRequestBeanTemplateURL() {
    return XFireDeploymentModule.class.getResource("rpc-request-bean.fmt");
  }

  /**
   * @return The URL to "rpc-response-bean.fmt"
   */
  protected URL getRPCResponseBeanTemplateURL() {
    return XFireDeploymentModule.class.getResource("rpc-response-bean.fmt");
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!enunciate.isModuleEnabled("jaxws")) {
      throw new EnunciateException("The XFire module requires an enabled JAXWS module.");
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //generate the rpc request/response beans.
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        for (WebMethod webMethod : ei.getWebMethods()) {
          for (WebMessage webMessage : webMethod.getMessages()) {
            if (webMessage instanceof RPCInputMessage) {
              model.put("message", webMessage);
              processTemplate(getRPCRequestBeanTemplateURL(), model);
            }
            else if (webMessage instanceof RPCOutputMessage) {
              model.put("message", webMessage);
              processTemplate(getRPCResponseBeanTemplateURL(), model);
            }
          }
        }
      }
    }

    getEnunciate().setProperty("xfire-server.src.dir", getGenerateDir());
    getEnunciate().addArtifact(new FileArtifact(getName(), "xfire-server.src.dir", getGenerateDir()));
  }

  @Override
  public Validator getValidator() {
    return new XFireValidator();
  }

}
