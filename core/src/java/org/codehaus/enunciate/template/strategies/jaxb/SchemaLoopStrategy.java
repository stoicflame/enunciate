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

package org.codehaus.enunciate.template.strategies.jaxb;

import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;

import java.util.Iterator;

/**
 * Strategy that loops through each schema.
 *
 * @author Ryan Heaton
 */
public class SchemaLoopStrategy extends EnunciateTemplateLoopStrategy<SchemaInfo> {

  private String var = "schema";
  private SchemaInfo currentSchema;

  /**
   * Get the loop through the schemas.
   *
   * @param model The model to work with.
   * @return The loop through the schemas.
   */
  protected Iterator<SchemaInfo> getLoop(TemplateModel model) throws TemplateException {
    return getNamespacesToSchemas().values().iterator();
  }

  // Inherited.
  @Override
  protected void setupModelForLoop(TemplateModel model, SchemaInfo schemaInfo, int index) throws TemplateException {
    super.setupModelForLoop(model, schemaInfo, index);

    if (var != null) {
      getModel().setVariable(var, schemaInfo);
    }

    this.currentSchema = schemaInfo;
  }

  /**
   * The variable to which to assign the current schema in the loop.
   *
   * @return The variable to which to assign the current schema in the loop.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable to which to assign the current schema in the loop.
   *
   * @param var The variable to which to assign the current schema in the loop.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * The current schema in the loop.
   *
   * @return The current schema in the loop.
   */
  public SchemaInfo getCurrentSchema() {
    return currentSchema;
  }
}
