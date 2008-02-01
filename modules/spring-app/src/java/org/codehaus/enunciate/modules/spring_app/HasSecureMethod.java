/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.spring_app;

import com.sun.mirror.declaration.MethodDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.Collection;
import java.util.List;

import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.rest.RESTEndpoint;

import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;

/**
 * Gets the qualified package name for a package or type.
 *
 * @author Ryan Heaton
 */
public class HasSecureMethod implements TemplateMethodModelEx {

  /**
   * Gets the client-side package for the type, type declaration, package, or their string values.
   *
   * @param list The arguments.
   * @return The string value of the client-side package.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The hasSecureMethod method must have an endpoint as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object object = BeansWrapper.getDefaultInstance().unwrap(from);
    Collection<? extends MethodDeclaration> methods;
    if (object instanceof EndpointInterface) {
      methods = ((EndpointInterface) object).getWebMethods();
    }
    else if (object instanceof RESTEndpoint) {
      methods = ((RESTEndpoint) object).getRESTMethods();
    }
    else {
      throw new TemplateModelException("The hasSecureMethod method must be either an EndpointInterface or a RESTEndpoint.  Not " + object.getClass().getName());
    }

    for (MethodDeclaration method : methods) {
      if ((method.getAnnotation(RolesAllowed.class) != null) ||
        (method.getAnnotation(PermitAll.class) != null) ||
        (method.getAnnotation(DenyAll.class) != null)) {
        return true;
      }
    }

    return false;
  }

}
