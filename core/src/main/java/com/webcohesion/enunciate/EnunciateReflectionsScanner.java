package com.webcohesion.enunciate;

import org.reflections.scanners.AbstractScanner;
import org.reflections.util.FilterBuilder;
import org.reflections.vfs.Vfs;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class EnunciateReflectionsScanner extends AbstractScanner {

  public EnunciateReflectionsScanner(Set<String> includes, Set<String> excludes) {
    FilterBuilder filterBuilder = new FilterBuilder();
    if (includes != null) {
      for (String include : includes) {
        filterBuilder = filterBuilder.includePackage(include);
      }
    }
    if (excludes != null) {
      for (String exclude : excludes) {
        filterBuilder = filterBuilder.excludePackage(exclude);
      }
    }
    filterResultsBy(filterBuilder);
  }

  public boolean acceptsInput(String file) {
    return super.acceptsInput(file) || file.endsWith(".java");
  }

  @Override public Object scan(Vfs.File file, Object classObject) {
    if (file.getName().endsWith(".java")) {
      getStore().put("/" + file.getRelativePath(), "/" + file.getRelativePath());
      return classObject;
    }
    else {
      return super.scan(file, classObject);
    }
  }

  public void scan(Object cls) {
    String className = getMetadataAdapter().getClassName(cls);
    getStore().put(className, className);
  }
}
