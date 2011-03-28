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

package org.codehaus.enunciate.template.freemarker;

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.doc.DocumentationGroup;

import java.util.*;

/**
 * List the groups for a given documentation component.
 *
 * @author Ryan Heaton
 */
public class GetGroupsMethod implements TemplateMethodModelEx {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The getGroups method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    Set<String> groups = new TreeSet<String>();
    if (SchemaInfo.class.isInstance(unwrapped)) {
      SchemaInfo info = (SchemaInfo) unwrapped;
      for (TypeDefinition typeDef : info.getTypeDefinitions()) {
        gatherGroups(typeDef, groups);
      }
      for (RootElementDeclaration element : info.getGlobalElements()) {
        gatherGroups(element, groups);
      }
    }
    else if (WsdlInfo.class.isInstance(unwrapped)) {
      WsdlInfo wsdl = (WsdlInfo) unwrapped;
      for (EndpointInterface ei : wsdl.getEndpointInterfaces()) {
        gatherGroups(ei, groups);
      }
    }
    else if ("rest".equals(unwrapped)) {
      for (RootResource rootResource : getModel().getRootResources()) {
        for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
          gatherGroups(resourceMethod, groups);
        }
      }
    }
    else if (Collection.class.isInstance(unwrapped)) {
      //if it's a collection, we'll assume it's a collection of resource methods.
      //we may have to create a special class to hold a collection of resource methods some day...
      Collection<ResourceMethod> resources = (Collection<ResourceMethod>) unwrapped;
      for (ResourceMethod resource : resources) {
        gatherGroups(resource, groups);
      }
    }
    else if (TypeDeclaration.class.isInstance(unwrapped)) {
      gatherGroups((TypeDeclaration) unwrapped, groups);
    }

    else {
      throw new TemplateModelException("Don't know how to gather groups for: " + unwrapped + ".");
    }

    return groups;
  }

  private void gatherGroups(MethodDeclaration decl, Set<String> groups) {
    if (decl != null) {
      DocumentationGroup documentationGroup = decl.getAnnotation(DocumentationGroup.class);
      if (documentationGroup != null) {
        groups.addAll(Arrays.asList(documentationGroup.value()));
      }
      else {
        gatherGroups(decl.getDeclaringType(), groups);
      }
    }
  }

  private void gatherGroups(TypeDeclaration decl, Set<String> groups) {
    if (decl != null) {
      DocumentationGroup documentationGroup = decl.getAnnotation(DocumentationGroup.class);
      if (documentationGroup != null) {
        groups.addAll(Arrays.asList(documentationGroup.value()));
      }
      else {
        PackageDeclaration pkg = decl.getPackage();
        if (pkg != null) {
          documentationGroup = pkg.getAnnotation(DocumentationGroup.class);
          if (documentationGroup != null) {
            groups.addAll(Arrays.asList(documentationGroup.value()));
          }
          else {
            groups.add(decl.getSimpleName());
          }
        }
      }
    }
  }

  /**
   * Get the current root model.
   *
   * @return The current root model.
   */
  protected static EnunciateFreemarkerModel getModel() {
    return ((EnunciateFreemarkerModel) FreemarkerModel.get());
  }

}