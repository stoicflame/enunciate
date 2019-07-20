/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.CompletionFailureException;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jaxb.JaxbModule;
import com.webcohesion.enunciate.modules.jaxb.model.ImplicitChildElement;
import com.webcohesion.enunciate.modules.jaxws.model.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.reflections.adapters.MetadataAdapter;

import javax.jws.WebService;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlTransient;
import java.io.File;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class JaxwsModule extends BasicProviderModule implements TypeDetectingModule, ApiRegistryProviderModule, ApiFeatureProviderModule, WebInfAwareModule {

  private JaxbModule jaxbModule;
  private DataTypeDetectionStrategy defaultDataTypeDetectionStrategy;
  private EnunciateJaxwsContext jaxwsContext;
  private File webInfDir;

  @Override
  public String getName() {
    return "jaxws";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Collections.singletonList(new JaxbDependencySpec());
  }

  public EnunciateJaxwsContext getJaxwsContext() {
    return jaxwsContext;
  }

  @Override
  public ApiRegistry getApiRegistry() {
    return new JaxwsApiRegistry(this.jaxwsContext);
  }

  public DataTypeDetectionStrategy getDataTypeDetectionStrategy() {
    String dataTypeDetection = this.config.getString("[@datatype-detection]", null);

    if (dataTypeDetection != null) {
      try {
        return DataTypeDetectionStrategy.valueOf(dataTypeDetection);
      }
      catch (IllegalArgumentException e) {
        //fall through...
      }
    }

    if (this.defaultDataTypeDetectionStrategy != null) {
      return this.defaultDataTypeDetectionStrategy;
    }

    if (this.enunciate.getIncludePatterns().isEmpty()) {
      //if there are no configured include patterns, then we'll just stick with "local" detection so we don't include too much.
      return DataTypeDetectionStrategy.local;
    }
    else {
      //otherwise, we'll assume the user knows what (s)he's doing and aggressively include everything.
      return DataTypeDetectionStrategy.aggressive;
    }
  }

  public void setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy strategy) {
    this.defaultDataTypeDetectionStrategy = strategy;
  }

  private boolean isUseSourceParameterNames() {
    return this.config.getBoolean("[@useSourceParameterNames]", false);
  }

  private boolean isAggressiveWebMethodExcludePolicy() {
    return this.config.getBoolean("[@aggressiveWebMethodExcludePolicy]", false);
  }

  @Override
  public void setWebInfDir(File webInfDir) {
    this.webInfDir = webInfDir;
  }

  public File getSunJaxwsXmlFile() {
    File sunJaxwsXmlFile = null;

    String configuredSunJaxwsXmlFile = this.config.getString("[@sun-jaxws-xml-file]", null);
    if (configuredSunJaxwsXmlFile != null) {
      sunJaxwsXmlFile = resolveFile(configuredSunJaxwsXmlFile);
    }
    else if (this.webInfDir != null) {
      sunJaxwsXmlFile = new File(this.webInfDir, "sun-jaxws.xml");
    }

    if (sunJaxwsXmlFile != null && sunJaxwsXmlFile.exists()) {
      return sunJaxwsXmlFile;
    }

    return null;
  }

  @Override
  public void call(EnunciateContext context) {
    jaxwsContext = new EnunciateJaxwsContext(this.jaxbModule.getJaxbContext(), isUseSourceParameterNames());
    boolean aggressiveWebMethodExcludePolicy = isAggressiveWebMethodExcludePolicy();

    Map<String, String> eiPaths = new HashMap<>();
    File sunJaxwsXmlFile = getSunJaxwsXmlFile();
    if (sunJaxwsXmlFile != null) {
      XMLConfiguration config;
      try {
        config = new XMLConfiguration(sunJaxwsXmlFile);
      }
      catch (ConfigurationException e) {
        throw new EnunciateException(e);
      }

      List<HierarchicalConfiguration> endpoints = config.configurationsAt("endpoint");
      for (HierarchicalConfiguration endpoint : endpoints) {
        String impl = endpoint.getString("[@implementation]", null);
        String urlPattern = endpoint.getString("[@url-pattern]", null);
        if (impl != null && urlPattern != null) {
          eiPaths.put(impl, urlPattern);
        }
      }
    }

    DataTypeDetectionStrategy detectionStrategy = getDataTypeDetectionStrategy();
    if (detectionStrategy != DataTypeDetectionStrategy.passive) {
      Set<? extends Element> elements = detectionStrategy == DataTypeDetectionStrategy.local ? context.getLocalApiElements() : context.getApiElements();
      for (Element declaration : elements) {
        try {
          if (declaration instanceof TypeElement) {
            TypeElement element = (TypeElement) declaration;

            XmlRegistry registryMetadata = declaration.getAnnotation(XmlRegistry.class);
            if (registryMetadata != null) {
              this.jaxbModule.addPotentialJaxbElement(element, new LinkedList<>());
            }

            if (isEndpointInterface(element)) {
              EndpointInterface ei = new EndpointInterface(element, elements, aggressiveWebMethodExcludePolicy, jaxwsContext);
              for (EndpointImplementation implementation : ei.getEndpointImplementations()) {
                String urlPattern = eiPaths.get(implementation.getQualifiedName().toString());
                if (urlPattern != null) {
                  if (!urlPattern.startsWith("/")) {
                    urlPattern = "/" + urlPattern;
                  }

                  if (urlPattern.endsWith("/*")) {
                    urlPattern = urlPattern.substring(0, urlPattern.length() - 2) + ei.getServiceName();
                  }

                  implementation.setPath(urlPattern);
                }
              }
              jaxwsContext.add(ei);
              addReferencedDataTypeDefinitions(ei);
            }
          }
        }
        catch (RuntimeException e) {
          if (e.getClass().getName().endsWith("CompletionFailure")) {
            throw new CompletionFailureException(Collections.singletonList(declaration), e);
          }

          throw e;
        }
      }
    }
  }

  protected void addReferencedDataTypeDefinitions(EndpointInterface ei) {
    LinkedList<Element> contextStack = new LinkedList<>();
    contextStack.push(ei);
    try {
      for (WebMethod webMethod : ei.getWebMethods()) {
        addReferencedTypeDefinitions(webMethod, contextStack);
      }
    }
    catch (RuntimeException e) {
      if (e.getClass().getName().endsWith("CompletionFailure")) {
        throw new CompletionFailureException(contextStack, e);
      }

      throw e;
    }
    finally {
      contextStack.pop();
    }
  }

  protected void addReferencedTypeDefinitions(WebMethod webMethod, LinkedList<Element> contextStack) {
    contextStack.push(webMethod);
    try {
      WebResult result = webMethod.getWebResult();
      this.jaxbModule.getJaxbContext().addReferencedTypeDefinitions(result.isAdapted() ? result.getAdapterType() : result.getType(), contextStack);
      for (WebParam webParam : webMethod.getWebParameters()) {
        this.jaxbModule.getJaxbContext().addReferencedTypeDefinitions(webParam.isAdapted() ? webParam.getAdapterType() : webParam.getType(), contextStack);
      }
      for (WebFault webFault : webMethod.getWebFaults()) {
        addReferencedTypeDefinitions(webFault, contextStack);
      }
    }
    catch (RuntimeException e) {
      if (e.getClass().getName().endsWith("CompletionFailure")) {
        throw new CompletionFailureException(contextStack, e);
      }

      throw e;
    }
    finally {
      contextStack.pop();
    }
  }

  protected void addReferencedTypeDefinitions(WebFault webFault, LinkedList<Element> contextStack) {
    contextStack.push(webFault);
    try {
      if (webFault.isImplicitSchemaElement()) {
        for (ImplicitChildElement childElement : webFault.getChildElements()) {
          WebFault.FaultBeanChildElement fbce = (WebFault.FaultBeanChildElement) childElement;
          this.jaxbModule.getJaxbContext().addReferencedTypeDefinitions(fbce.isAdapted() ? fbce.getAdapterType() : fbce.getType(), contextStack);
        }
      }
      else {
        DeclaredType faultBeanType = webFault.getExplicitFaultBeanType();
        if (faultBeanType != null) {
          this.jaxbModule.getJaxbContext().addReferencedTypeDefinitions(faultBeanType, contextStack);
        }
      }
    }
    catch (RuntimeException e) {
      if (e.getClass().getName().endsWith("CompletionFailure")) {
        throw new CompletionFailureException(contextStack, e);
      }

      throw e;
    }
    finally {
      contextStack.pop();
    }
  }

  @Override
  public boolean internal(Object type, MetadataAdapter metadata) {
    String classname = metadata.getClassName(type);
    return classname.startsWith("com.sun.xml.ws");
  }

  @Override
  public boolean typeDetected(Object type, MetadataAdapter metadata) {
    List<String> classAnnotations = metadata.getClassAnnotationNames(type);
    if (classAnnotations != null) {
      for (String classAnnotation : classAnnotations) {
        if (WebService.class.getName().equals(classAnnotation)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * A quick check to see if a declaration is an endpoint interface.
   */
  public boolean isEndpointInterface(TypeElement declaration) {
    WebService ws = declaration.getAnnotation(WebService.class);
    return declaration.getAnnotation(XmlTransient.class) == null
      && ws != null
      && ((declaration.getKind() == ElementKind.INTERFACE)
      //if this is a class declaration, then it has an "implicit" endpoint interface if it doesn't reference another.
      || (ws.endpointInterface() == null) || ("".equals(ws.endpointInterface())));
  }

  public class JaxbDependencySpec implements DependencySpec {

    @Override
    public boolean accept(EnunciateModule module) {
      if (module instanceof JaxbModule) {
        jaxbModule = ((JaxbModule) module);
        jaxbModule.setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy.passive);
        return true;
      }

      return false;
    }

    @Override
    public boolean isFulfilled() {
      return jaxbModule != null;
    }


    @Override
    public String toString() {
      return "jaxb";
    }
  }
}
