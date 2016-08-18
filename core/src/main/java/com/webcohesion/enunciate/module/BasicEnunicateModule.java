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
package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateContext;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public abstract class BasicEnunicateModule implements EnunciateModule, DependingModuleAwareModule {

  protected Set<String> dependingModules = null;
  protected Enunciate enunciate;
  protected EnunciateContext context;
  protected HierarchicalConfiguration config;

  @Override
  public void init(Enunciate engine) {
    this.enunciate = engine;
    this.config = (HierarchicalConfiguration) this.enunciate.getConfiguration().getSource().subset("modules." + getName());
  }

  @Override
  public void init(EnunciateContext context) {
    this.context = context;
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEnabled() {
    return !this.config.getBoolean("[@disabled]", false);
  }

  public File resolveFile(String filePath) {
    return this.context.getConfiguration().resolveFile(filePath);
  }

  @Override
  public void acknowledgeDependingModules(Set<String> dependingModules) {
    this.dependingModules = dependingModules;
  }

  protected void debug(String message, Object... formatArgs) {
    this.enunciate.getLogger().debug(message, formatArgs);
  }

  protected void info(String message, Object... formatArgs) {
    this.enunciate.getLogger().info(message, formatArgs);
  }

  protected void warn(String message, Object... formatArgs) {
    this.enunciate.getLogger().warn(message, formatArgs);
  }

  protected void error(String message, Object... formatArgs) {
    this.enunciate.getLogger().error(message, formatArgs);
  }

  protected String positionOf(javax.lang.model.element.Element element) {
    StringBuilder position = new StringBuilder(descriptionOf(element));

    javax.lang.model.element.Element enclosing = element.getEnclosingElement();
    while (enclosing != null && !(enclosing instanceof PackageElement)) {
      position.append(" of ").append(enclosing.getSimpleName().toString());
      enclosing = enclosing.getEnclosingElement();
    }

    //capitalize it.
    return capitalize(position.toString());
  }

  protected String capitalize(String position) {
    return Character.toUpperCase(position.charAt(0)) + position.substring(1);
  }

  protected String descriptionOf(javax.lang.model.element.Element element) {
    StringBuilder description = new StringBuilder();
    ElementKind kind = element.getKind();
    if (kind != null) {
      description.append(kind.name()).append(' ');
    }

    Name name = element.getSimpleName();
    if (element instanceof TypeElement) {
      name = ((TypeElement) element).getQualifiedName();
    }
    description.append(name.toString());
    return description.toString();
  }
}
