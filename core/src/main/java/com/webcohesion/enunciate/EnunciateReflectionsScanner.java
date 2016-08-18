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
package com.webcohesion.enunciate;

import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.module.TypeFilteringModule;
import com.webcohesion.enunciate.util.*;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.scanners.AbstractScanner;
import org.reflections.util.FilterBuilder;
import org.reflections.vfs.Vfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateReflectionsScanner extends AbstractScanner {

  private final FilterBuilder includeFilter;
  private final FilterBuilder excludeFilter;
  private final List<TypeFilteringModule> filteringModules;

  public EnunciateReflectionsScanner(Enunciate enunciate, List<EnunciateModule> modules) {
    this.filteringModules = new ArrayList<TypeFilteringModule>();
    for (EnunciateModule module : modules) {
      if (module instanceof TypeFilteringModule) {
        this.filteringModules.add((TypeFilteringModule) module);
      }
    }

    FilterBuilder includeFilter = null;
    Set<String> includes = enunciate.getIncludePatterns();
    if (includes != null && !includes.isEmpty()) {
      includeFilter = new FilterBuilder();
      for (String include : includes) {
        if (AntPatternMatcher.isValidPattern(include)) {
          includeFilter = includeFilter.add(new AntPatternInclude(include));
        }
        else {
          includeFilter = includeFilter.add(new StringEqualsInclude(include));
        }
      }
    }

    FilterBuilder excludeFilter = null;
    Set<String> excludes = enunciate.getExcludePatterns();
    if (excludes != null && !excludes.isEmpty()) {
      excludeFilter = new FilterBuilder();
      for (String exclude : excludes) {
        if (AntPatternMatcher.isValidPattern(exclude)) {
          excludeFilter = excludeFilter.add(new AntPatternInclude(exclude));
        }
        else {
          excludeFilter = excludeFilter.add(new StringEqualsInclude(exclude));
        }
      }
    }

    this.includeFilter = includeFilter;
    this.excludeFilter = excludeFilter;
  }

  public boolean acceptsInput(String file) {
    return super.acceptsInput(file) || file.endsWith(".java");
  }

  @Override
  public Object scan(Vfs.File file, Object classObject) {
    if (file.getName().endsWith(".java")) {
      getStore().put(file.getRelativePath(), file.getRelativePath());
      return classObject;
    }
    else {
      return super.scan(file, classObject);
    }
  }

  public void scan(Object type) {
    boolean accepted = false;

    MetadataAdapter metadata = getMetadataAdapter();

    for (TypeFilteringModule filteringModule : this.filteringModules) {
      if (filteringModule.acceptType(type, metadata)) {
        accepted = true;
        //do not break: type filtering modules may need to be aware of types that are not accepted, or that are accepted by other modules.
      }
    }

    String className = metadata.getClassName(type);

    boolean filteredIn = this.includeFilter != null && this.includeFilter.apply(className);
    if (filteredIn) {
      //if it's explicitly included, add it.
      getStore().put(className, className);
    }
    else {
      boolean filteredOut = this.excludeFilter != null && this.excludeFilter.apply(className);
      if (accepted && !filteredOut) {
        //else if it's accepted and not explicitly excluded, add it.
        getStore().put(className, className);
      }
    }
  }
}
