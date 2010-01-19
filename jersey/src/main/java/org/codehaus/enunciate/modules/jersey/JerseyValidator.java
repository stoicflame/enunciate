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

package org.codehaus.enunciate.modules.jersey;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.*;
import java.util.List;
import java.util.ArrayList;

import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import com.sun.mirror.declaration.*;

/**
 * @author Ryan Heaton
 */
public class JerseyValidator extends BaseValidator {

  private final boolean allowWildcardServlet;

  public JerseyValidator(boolean allowWildcardServlet) {
    this.allowWildcardServlet = allowWildcardServlet;
  }

  @Override
  public ValidationResult validateRootResources(List<RootResource> rootResources) {
    ValidationResult result = new ValidationResult();

    for (RootResource rootResource : rootResources) {

      if (rootResource.getDelegate() instanceof InterfaceDeclaration) {
        result.addError(rootResource, "Jersey doesn't support interfaces as root resources. The @Path parameter will need to be applied to the implementation class.");
      }
      else {
        List<ConstructorDeclaration> candidates = new ArrayList<ConstructorDeclaration>();
        boolean springManaged = rootResource.getAnnotation(SpringManagedLifecycle.class) != null;
        CONSTRUCTOR_LOOP:
        for (ConstructorDeclaration constructor : ((ClassDeclaration) rootResource.getDelegate()).getConstructors()) {
          if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
            for (ParameterDeclaration constructorParam : constructor.getParameters()) {
              if (springManaged) {
                if (isSuppliableByJAXRS(constructorParam)) {
                  result.addWarning(constructorParam, "Constructor parameter will not be supplied by JAX-RS if the lifecycle of this resource is Spring-managed.");
                }
              }
              else if (!isSuppliableByJAXRS(constructorParam)) {
                //none of those annotation are available. not a candidate constructor.
                continue CONSTRUCTOR_LOOP;
              }
            }

            candidates.add(constructor);
          }
        }

        if (candidates.isEmpty() && !springManaged) {
          result.addError(rootResource, "A JAX-RS root resource must have a public constructor for which the JAX-RS runtime can provide all parameter values. " +
            "If the resource lifecycle is to be managed by Spring (which will handle the construction of the bean), then please apply the @" +
            SpringManagedLifecycle.class.getName() + " annotation to the resource.");
        }
        else {
          while (!candidates.isEmpty()) {
            ConstructorDeclaration candidate = candidates.remove(0);
            for (ConstructorDeclaration other : candidates) {
              if (candidate.getParameters().size() == other.getParameters().size()) {
                result.addWarning(rootResource, "Ambiguous JAX-RS constructors (same parameter count).");
              }
            }
          }
        }
      }

      for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
        if ("/*".equals(resourceMethod.getServletPattern())) {
          if (!allowWildcardServlet) {
            result.addError(resourceMethod, "This JAX-RS resource method is designed to catch all requests (including requests to " +
              "Enunciate-generated documentation and other static files). If this is what you want, then please set 'disableWildcardServletError' to 'true'" +
              "in the Enunciate config for the Jersey module.  Otherwise, enable the rest subcontext or adjust the @Path annotation to be more specific.");
          }
          else {
            result.addWarning(resourceMethod, "JAX-RS resource method is designed to catch all requests.");
          }
        }

        for (String producesMime : resourceMethod.getProducesMime()) {
          try {
            MediaType.valueOf(producesMime);
          }
          catch (Exception e) {
            result.addError(resourceMethod, "Invalid produces MIME type: " + producesMime + "(" + e.getMessage() + ").");
          }
        }

        if (resourceMethod.getHttpMethods().size() > 1) {
          result.addError(resourceMethod, "You must not apply multiple HTTP operations to the same method: " + resourceMethod.getHttpMethods());
        }

        for (String method : resourceMethod.getHttpMethods()) {
          if ("GET".equalsIgnoreCase(method) && (resourceMethod.getOutputPayload() == null)) {
            result.addError(resourceMethod, "A resource method that is mapped to HTTP GET must have an output payload. (Does it return void?)");
          }

          if ("GET".equalsIgnoreCase(method) && resourceMethod.getEntityParameter() != null) {
            result.addError(resourceMethod, "A resource method that is mapped to HTTP GET must not specify an entity parameter.");
          }
        }
      }
    }

    return result;
  }

  /**
   * Whether the specified declaration is suppliable by JAX-RS.
   *
   * @param declaration The declaration.
   * @return Whether the specified declaration is suppliable by JAX-RS.
   */
  protected boolean isSuppliableByJAXRS(Declaration declaration) {
    return (declaration.getAnnotation(MatrixParam.class) != null)
        || (declaration.getAnnotation(PathParam.class) != null)
        || (declaration.getAnnotation(QueryParam.class) != null)
        || (declaration.getAnnotation(CookieParam.class) != null)
        || (declaration.getAnnotation(HeaderParam.class) != null)
        || (declaration.getAnnotation(javax.ws.rs.core.Context.class) != null);
  }

}