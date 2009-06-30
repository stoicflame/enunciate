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

package org.codehaus.enunciate.modules.jaxws;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.ReferenceType;
import freemarker.template.TemplateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.util.TypeDeclarationComparator;

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
 * <li><a href="#steps">steps</a></li>
 * <li><a href="#config">configuration</a></li>
 * <li><a href="#artifacts">artifacts</a></li>
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
    File genDir = getGenerateDir();
    if (!isUpToDate(genDir)) {
      EnunciateFreemarkerModel model = getModel();
      Map<String, WsdlInfo> ns2wsdl = model.getNamespacesToWSDLs();

      URL requestBeanTemplate = JAXWSDeploymentModule.class.getResource("request-bean.fmt");
      URL responseBeanTemplate = JAXWSDeploymentModule.class.getResource("response-bean.fmt");
      URL faultBeanTemplate = JAXWSDeploymentModule.class.getResource("fault-bean.fmt");

      TreeSet<WebFault> visitedFaults = new TreeSet<WebFault>(new TypeDeclarationComparator());
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

      //we're going to process the JAX-RS thrown types annotated with @WebFault in case we ever want to serialize the fault beans as XML...
      for (RootResource rootResource : model.getRootResources()) {
        for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
          for (ReferenceType referenceType : resourceMethod.getThrownTypes()) {
            if (!(referenceType instanceof DeclaredType)) {
              throw new ValidationException(resourceMethod.getPosition(), "Method " + resourceMethod + " of " + resourceMethod.getParent().getQualifiedName() + ": thrown type must be a declared type.");
            }

            TypeDeclaration declaration = ((DeclaredType) referenceType).getDeclaration();

            if (declaration == null) {
              throw new ValidationException(resourceMethod.getPosition(), "Method " + resourceMethod + " of " + resourceMethod.getParent().getQualifiedName() + ": unknown declaration for " + referenceType);
            }
            else if (declaration.getAnnotation(javax.xml.ws.WebFault.class) != null) {
              WebFault fault = new WebFault((ClassDeclaration) declaration);
              if (fault.isImplicitSchemaElement() && visitedFaults.add(fault)) {
                model.put("message", fault);
                processTemplate(faultBeanTemplate, model);
              }
            }
          }
        }
      }
    }
    else {
      info("Skipping JAX-WS support generation as everything appears up-to-date...");
    }

    getEnunciate().addArtifact(new FileArtifact(getName(), "jaxws.src.dir", genDir));
    getEnunciate().addAdditionalSourceRoot(genDir);
  }

  /**
   * Whether the generate dir is up-to-date.
   *
   * @param genDir The generate dir.
   * @return Whether the generate dir is up-to-date.
   */
  protected boolean isUpToDate(File genDir) {
    return enunciate.isUpToDateWithSources(genDir);
  }

  @Override
  public Validator getValidator() {
    return new JAXWSValidator();
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty() && getModelInternal().getRootResources().isEmpty()) {
      debug("JAX-WS module is disabled because there are no endpoint interfaces.");
      return true;
    }

    return false;
  }
}
