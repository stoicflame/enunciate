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
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

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
        if (!isExternallyManagedLifecycle(rootResource)) {
          result.addWarning(rootResource, "The Jersey runtime doesn't support interfaces as root resources. The @Path parameter will need to be applied to the " +
            "implementation class. If the lifecycle of this root resource is to be managed externally (e.g. by Spring or something), then let Enunciate know by " +
            "annotating this class with @" + ExternallyManagedLifecycle.class.getName() + ".");
        }
      }
      else {
        List<ConstructorDeclaration> candidates = new ArrayList<ConstructorDeclaration>();
        boolean externallyManagedLifecycle = isExternallyManagedLifecycle(rootResource);
        CONSTRUCTOR_LOOP:
        for (ConstructorDeclaration constructor : ((ClassDeclaration) rootResource.getDelegate()).getConstructors()) {
          if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
            for (ParameterDeclaration constructorParam : constructor.getParameters()) {
              if (!externallyManagedLifecycle && !isSuppliableByJAXRS(constructorParam)) {
                //none of those annotation are available. not a candidate constructor.
                continue CONSTRUCTOR_LOOP;
              }
            }

            candidates.add(constructor);
          }
        }

        if (candidates.isEmpty() && !externallyManagedLifecycle) {
          result.addWarning(rootResource, "A JAX-RS root resource must have a public constructor for which the JAX-RS runtime can provide all parameter values. " +
            "If the resource lifecycle is to be managed externally (e.g. by Spring or something), then please let Enunciate know by applying the @" +
            ExternallyManagedLifecycle.class.getName() + " annotation to the resource.");
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
          if ("GET".equalsIgnoreCase(method) && (resourceMethod.getRepresentationMetadata() == null)) {
            result.addWarning(resourceMethod, "A resource method that is mapped to HTTP GET should probably have an output payload. (Does it return void?)");
          }

          if ("GET".equalsIgnoreCase(method) && resourceMethod.getEntityParameter() != null) {
            result.addError(resourceMethod, "A resource method that is mapped to HTTP GET must not specify an entity parameter.");
          }
        }
      }
    }

    return result;
  }

  private boolean isExternallyManagedLifecycle(RootResource rootResource) {
    return rootResource.getAnnotation(SpringManagedLifecycle.class) != null && rootResource.getAnnotation(ExternallyManagedLifecycle.class) != null;
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