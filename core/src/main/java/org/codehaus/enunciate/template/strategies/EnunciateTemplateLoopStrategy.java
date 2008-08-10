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

package org.codehaus.enunciate.template.strategies;

import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateAnnotationProcessorFactory;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.rest.RESTEndpoint;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerTemplateBlock;
import net.sf.jelly.apt.strategies.TemplateLoopStrategy;
import net.sf.jelly.apt.Context;

import java.util.Map;
import java.util.Collection;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

/**
 * Basic class for enunciate loops.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciateTemplateLoopStrategy<L> extends TemplateLoopStrategy<L, FreemarkerTemplateBlock> {

  /**
   * Convenience method to lookup a namespace prefix given a namespace.
   *
   * @param namespace The namespace for which to lookup the prefix.
   * @return The namespace prefix.
   */
  protected String lookupPrefix(String namespace) {
    return getNamespacesToPrefixes().get(namespace);
  }

  /**
   * Convenience method to lookup a namespace schema given a namespace.
   *
   * @param namespace The namespace for which to lookup the schema.
   * @return The schema info.
   */
  protected SchemaInfo lookupSchema(String namespace) {
    return getNamespacesToSchemas().get(namespace);
  }

  /**
   * Convenience method to lookup a namespace wsdl given a namespace.
   *
   * @param namespace The namespace for which to lookup the wsdl.
   * @return The wsdl info.
   */
  protected WsdlInfo lookupWSDL(String namespace) {
    return getNamespacesToWSDLs().get(namespace);
  }

  /**
   * The namespace to prefix map.
   *
   * @return The namespace to prefix map.
   */
  protected Map<String, String> getNamespacesToPrefixes() {
    return getModel().getNamespacesToPrefixes();
  }

  /**
   * The namespace to schema map.
   *
   * @return The namespace to schema map.
   */
  protected Map<String, SchemaInfo> getNamespacesToSchemas() {
    return getModel().getNamespacesToSchemas();
  }

  /**
   * The namespace to wsdl map.
   *
   * @return The namespace to wsdl map.
   */
  protected Map<String, WsdlInfo> getNamespacesToWSDLs() {
    return getModel().getNamespacesToWSDLs();
  }

  /**
   * The REST endpoints in the model.
   *
   * @return The REST endpoints in the model.
   */
  protected Collection<RESTEndpoint> getRESTEndpoints() {
    return getModel().getRESTEndpoints();
  }

  /**
   * Get the current root model.
   *
   * @return The current root model.
   */
  protected EnunciateFreemarkerModel getModel() {
    return ((EnunciateFreemarkerModel) FreemarkerModel.get());
  }

  /**
   * The current annotation processing environment.
   *
   * @return The current annotation processing environment.
   */
  protected AnnotationProcessorEnvironment getAnnotationProcessorEnvironment() {
    return Context.getCurrentEnvironment();
  }

  /**
   * Whether processing should be verbose.
   *
   * @return Whether processing should be verbose.
   */
  protected boolean isVerbose() {
    return getAnnotationProcessorEnvironment().getOptions().containsKey(EnunciateAnnotationProcessorFactory.VERBOSE_OPTION);
  }

}
