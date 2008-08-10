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

package org.codehaus.enunciate.modules.amf;

import flex.messaging.FactoryInstance;
import flex.messaging.factories.JavaFactory;
import org.codehaus.enunciate.webapp.ComponentPostProcessor;

import javax.servlet.ServletContext;

/**
 * Flex factory for Enunciate components.  Handles any post-processing specified
 * in the web application configuration.
 *
 * @author Ryan Heaton
 */
public class EnunciateFlexFactory extends JavaFactory {

  @Override
  public Object lookup(FactoryInstance inst) {
    Object instance = super.lookup(inst);

    ServletContext servletContext = flex.messaging.FlexContext.getServletConfig().getServletContext();
    ComponentPostProcessor postProcessor = (ComponentPostProcessor) servletContext.getAttribute(ComponentPostProcessor.class.getName());
    if (postProcessor != null) {
      postProcessor.postProcess(instance);  
    }

    return instance;
  }
}