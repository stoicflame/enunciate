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
    FilterBuilder excludeFilter = new FilterBuilder();
    Set<String> includes = enunciate.getIncludePatterns();
    if (includes != null && !includes.isEmpty()) {
      includeFilter = new FilterBuilder();
      for (String include : includes) {
        if (AntPatternMatcher.isValidPattern(include)) {
          includeFilter = includeFilter.add(new AntPatternInclude(include));
          excludeFilter = excludeFilter.add(new AntPatternInclude(include));
        }
        else {
          includeFilter = includeFilter.add(new StringEqualsInclude(include));
          excludeFilter = excludeFilter.add(new StringEqualsInclude(include));
        }
      }
    }

    Set<String> excludes = enunciate.getExcludePatterns();
    if (excludes != null) {
      for (String exclude : excludes) {
        if (AntPatternMatcher.isValidPattern(exclude)) {
          excludeFilter = excludeFilter.add(new AntPatternExclude(exclude));
        }
        else {
          excludeFilter = excludeFilter.add(new StringEqualsExclude(exclude));
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
      boolean filteredOut = !this.excludeFilter.apply(className);
      if (accepted && !filteredOut) {
        getStore().put(className, className);
      }
    }
  }
}
