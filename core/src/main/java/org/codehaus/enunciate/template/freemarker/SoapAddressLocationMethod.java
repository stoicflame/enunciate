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

package org.codehaus.enunciate.template.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;

import java.util.List;

/**
 * Gets the soap address location of a specified endoint interface.
 *
 * @author Ryan Heaton
 */
public class SoapAddressLocationMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The soapAddressPath method must have an endpoint as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object object = BeansWrapper.getDefaultInstance().unwrap(from);
    EndpointInterface endpointInterface;
    if (object instanceof EndpointInterface) {
      endpointInterface = ((EndpointInterface) object);
    }
    else {
      throw new TemplateModelException("The soapAddressPath method must be an EndpointInterface.  Not " + object.getClass().getName());
    }

    EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
    EnunciateConfiguration config = model.getEnunciateConfig();
    String path = config.getDefaultSoapSubcontext() + endpointInterface.getServiceName();
    if (config.getSoapServices2Paths().containsKey(endpointInterface.getServiceName())) {
      path = config.getSoapServices2Paths().get(endpointInterface.getServiceName());
    }

    return model.getBaseDeploymentAddress() != null ? (model.getBaseDeploymentAddress() + path) : path;
  }

}