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

package org.codehaus.enunciate.template.strategies.jaxrs;

import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;

import java.util.Iterator;

/**
 * Loops through all the resource methods, ordered by path length.
 *
 * @author Ryan Heaton
 */
public class ResourceMethodLoopStrategy extends EnunciateTemplateLoopStrategy<ResourceMethod> {

  private String var = "resourceMethod";

  protected Iterator<ResourceMethod> getLoop(TemplateModel templateModel) throws TemplateException {
    return getModel().getResourceMethods().iterator();
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, ResourceMethod resourceMethod, int index) throws TemplateException {
    super.setupModelForLoop(model, resourceMethod, index);

    if (this.var != null) {
      model.setVariable(this.var, resourceMethod);
    }
  }

  /**
   * The name of the variable into which to put the resource method.
   *
   * @return The name of the variable into which to put the resource method.
   */
  public String getVar() {
    return var;
  }

  /**
   * The name of the variable into which to put the resource method.
   *
   * @param var The name of the variable into which to put the resource method.
   */
  public void setVar(String var) {
    this.var = var;
  }
}
