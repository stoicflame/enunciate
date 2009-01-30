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

package org.codehaus.enunciate.modules.csharp;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.ReferenceType;
import freemarker.template.TemplateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.csharp.CSharpValidator;
import org.codehaus.enunciate.util.TypeDeclarationComparator;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.template.freemarker.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.beans.Introspector;

/**
 * <h1>C# Module</h1>
 *
 * <p>The C# module generates C# client code for accessing the SOAP endpoints and compiles the code for .NET.</p>
 *
 * <p>The order of the JAXWS deployment module is 0, as it doesn't depend on any artifacts exported
 * by any other module.</p>
 *
 * <ul>
 * <li><a href="#config">configuration</a></li>
 * </ul>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>There are no configuration options for the C# deployment module</p>
 *
 * @author Ryan Heaton
 * @docFileName module_csharp.html
 */
public class CSharpDeploymentModule extends FreemarkerDeploymentModule {

  private String jarName = null;
  private final Map<String, String> packageToNamespaceConversions = new HashMap<String, String>();

  public CSharpDeploymentModule() {
  }

  /**
   * @return "csharp"
   */
  @Override
  public String getName() {
    return "csharp";
  }

  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
      TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new TypeDeclarationComparator());
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          String pckg = ei.getPackage().getQualifiedName();
          if (!this.packageToNamespaceConversions.containsKey(pckg)) {
            this.packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
          }
          for (WebMethod webMethod : ei.getWebMethods()) {
            for (WebFault webFault : webMethod.getWebFaults()) {
              allFaults.add(webFault);
            }
          }
        }
      }

      for (WebFault webFault : allFaults) {
        String pckg = webFault.getPackage().getQualifiedName();
        if (!this.packageToNamespaceConversions.containsKey(pckg)) {
          this.packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
        }
      }
      
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          String pckg = typeDefinition.getPackage().getQualifiedName();
          if (!this.packageToNamespaceConversions.containsKey(pckg)) {
            this.packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
          }
        }
      }
    }
  }

  protected String packageToNamespace(String pckg) {
    if (pckg == null) {
      return null;
    }
    else {
      StringBuilder ns = new StringBuilder();
      for (StringTokenizer toks = new StringTokenizer(pckg, "."); toks.hasMoreTokens();) {
        String tok = toks.nextToken();
        ns.append(Character.toString(tok.charAt(0)).toUpperCase());
        if (tok.length() > 1) {
          ns.append(tok.substring(1));
        }
        if (toks.hasMoreTokens()) {
          ns.append('.');
        }
      }
      return ns.toString();
    }
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled()) {
      //todo: validate the C# compiler exists...
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    File genDir = getGenerateDir();
    if (!enunciate.isUpToDateWithSources(genDir)) {
      EnunciateFreemarkerModel model = getModel();
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(this.packageToNamespaceConversions);
      ComponentTypeForMethod componentTypeFor = new ComponentTypeForMethod(this.packageToNamespaceConversions);
      CollectionTypeForMethod collectionTypeFor = new CollectionTypeForMethod(this.packageToNamespaceConversions);
      classnameFor.setJdk15(true);
      componentTypeFor.setJdk15(true);
      collectionTypeFor.setJdk15(true);
      model.put("packageFor", new ClientPackageForMethod(this.packageToNamespaceConversions));
      model.put("classnameFor", classnameFor);
      model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));
      model.put("componentTypeFor", componentTypeFor);
      model.put("collectionTypeFor", collectionTypeFor);

      info("Generating the JAX-WS client classes...");
      model.setFileOutputDirectory(genDir);
      
    }
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
    return new CSharpValidator();
  }

}
