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

package org.codehaus.enunciate.contract.jaxrs;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;

import javax.ws.rs.Path;
import java.util.*;

/**
 * A JAX-RS root resource.
 * 
 * @author Ryan Heaton
 */
public class RootResource extends Resource {

  private final Path path;

  public RootResource(ClassDeclaration delegate) {
    super(delegate);

    this.path = delegate.getAnnotation(Path.class);
    if (this.path == null) {
      throw new IllegalArgumentException("A JAX-RS root resource must be annotated with @javax.ws.rs.Path.");
    }

  }

  /**
   * The path of the root resource.
   *
   * @return The path of the root resource.
   */
  public String getPath() {
    return this.path.value();
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
   * @return The resource params.
   */
  @Override
  protected List<ResourceParameter> getResourceParameters(TypeDeclaration delegate) {
    List<ResourceParameter> resourceParams = super.getResourceParameters(delegate);

    if (getDelegate() == delegate) {
      //root resources also include constructor params.

      Collection<ConstructorDeclaration> constructors = ((ClassDeclaration) delegate).getConstructors();
      ConstructorDeclaration chosen = null;
      CONSTRUCTOR_LOOP : for (ConstructorDeclaration constructor : constructors) {
        //the one with the most params is the chosen one.
        if (chosen == null || constructor.getParameters().size() > chosen.getParameters().size()) {
          //Has more constructor parameters.  See if they're all Jersey-provided.
          for (ParameterDeclaration param : constructor.getParameters()) {
            if (!ResourceParameter.isResourceParameter(param)) {
              continue CONSTRUCTOR_LOOP;
            }
          }
          chosen = constructor;
        }
      }

      if (chosen != null) {
        for (ParameterDeclaration param : chosen.getParameters()) {
          resourceParams.add(new ResourceParameter(param));
        }
      }
    }

    return resourceParams;
  }

}
