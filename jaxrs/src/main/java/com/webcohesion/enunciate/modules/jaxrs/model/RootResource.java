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
package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.ws.rs.Path;
import java.util.*;

/**
 * A JAX-RS root resource.
 * 
 * @author Ryan Heaton
 */
public class RootResource extends Resource {

  public RootResource(TypeElement delegate, EnunciateJaxrsContext context) {
    super(delegate, loadPath(delegate), context);
  }

  private static String loadPath(TypeElement delegate) {
    Path path = delegate.getAnnotation(Path.class);
    if (path == null) {
      throw new IllegalArgumentException("A JAX-RS root resource must be annotated with @javax.ws.rs.Path.");
    }
    return path.value();
  }

  /**
   * @return null
   */
  public Resource getParent() {
    return null;
  }

  /**
   * The resource parameters for a root resource include the constructor params.
   *
   * @param delegate The declaration.
   * @param context The context.
   * @return The resource params.
   */
  @Override
  protected Set<ResourceParameter> getResourceParameters(TypeElement delegate, EnunciateJaxrsContext context) {
    Set<ResourceParameter> resourceParams = super.getResourceParameters(delegate, context);

    //root resources also include constructor params.
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(delegate.getEnclosedElements());
    ExecutableElement chosen = null;
    CONSTRUCTOR_LOOP : for (ExecutableElement constructor : constructors) {
      //the one with the most params is the chosen one.
      if (chosen == null || constructor.getParameters().size() > chosen.getParameters().size()) {
        //Has more constructor parameters.  See if they're all Jersey-provided.
        for (VariableElement param : constructor.getParameters()) {
          if (!ResourceParameter.isResourceParameter(param, context)) {
            continue CONSTRUCTOR_LOOP;
          }
        }
        chosen = constructor;
      }
    }

    if (chosen != null) {
      for (VariableElement param : chosen.getParameters()) {
        resourceParams.add(new ResourceParameter(param, this));
      }
    }

    return resourceParams;
  }

}
