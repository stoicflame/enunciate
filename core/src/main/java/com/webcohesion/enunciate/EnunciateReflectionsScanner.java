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
import com.webcohesion.enunciate.module.TypeDetectingModule;
import com.webcohesion.enunciate.util.AntPatternInclude;
import com.webcohesion.enunciate.util.AntPatternMatcher;
import com.webcohesion.enunciate.util.StringEqualsInclude;
import javassist.bytecode.ClassFile;
import org.jetbrains.annotations.Nullable;
import org.reflections.scanners.Scanner;
import org.reflections.util.FilterBuilder;
import org.reflections.vfs.Vfs;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class EnunciateReflectionsScanner implements Scanner {

  static final String INDEX = EnunciateReflectionsScanner.class.getName();

  private final FilterBuilder includeFilter;
  private final FilterBuilder excludeFilter;
  private final List<TypeDetectingModule> detectingModules;

  public EnunciateReflectionsScanner(Enunciate enunciate, List<EnunciateModule> modules) {
    this.detectingModules = new ArrayList<>();
    for (EnunciateModule module : modules) {
      if (module instanceof TypeDetectingModule) {
        this.detectingModules.add((TypeDetectingModule) module);
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

  @Override
  public String index() {
    return INDEX;
  }

  public boolean acceptsInput(String file) {
    return Scanner.super.acceptsInput(file) || file.endsWith(".java");
  }

  @Nullable
  @Override
  public List<Map.Entry<String, String>> scan(Vfs.File file) {
    if (file.getName().endsWith(".java")) {
      return Collections.singletonList(entry(file.getRelativePath(), file.getRelativePath()));
    }
    else {
      return Scanner.super.scan(file);
    }
  }

  @Override
  public List<Map.Entry<String, String>> scan(ClassFile classFile) {
    boolean detected = false;

    for (TypeDetectingModule detectingModule : this.detectingModules) {
      if (detectingModule.internal(classFile)) {
        //internal types should be marked as NOT detected by any module.
        detected = false;
        break;
      }

      if (detectingModule.typeDetected(classFile)) {
        detected = true;
        //do not break: type detecting modules may need to be aware of non-detected types or that are detected by other modules.
      }
    }

    String className = classFile.getName();

    ArrayList<Map.Entry<String, String>> entries = new ArrayList<>();
    boolean filteredIn = this.includeFilter != null && this.includeFilter.test(className);
    if (filteredIn) {
      //if it's explicitly included, add it.
      entries.add(entry(className, className));
    }
    else {
      boolean filteredOut = this.excludeFilter != null && this.excludeFilter.test(className);
      if (detected && !filteredOut) {
        //else if it's detected and not explicitly excluded, add it.
        entries.add(entry(className, className));
      }
    }

    return entries;
  }
}
