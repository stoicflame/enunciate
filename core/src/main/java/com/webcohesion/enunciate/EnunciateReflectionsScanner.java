package com.webcohesion.enunciate;

import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.module.TypeFilteringModule;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.scanners.AbstractScanner;
import org.reflections.vfs.Vfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateReflectionsScanner extends AbstractScanner {

  private final List<TypeFilteringModule> filteringModules;

  public EnunciateReflectionsScanner(List<EnunciateModule> modules) {
    this.filteringModules = new ArrayList<TypeFilteringModule>();
    for (EnunciateModule module : modules) {
      if (module instanceof TypeFilteringModule) {
        this.filteringModules.add((TypeFilteringModule) module);
      }
    }
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

  private static String sourcePathToFqn(String relativePath) {
    return relativePath.replace('/', '.').replace('\\', '.').substring(0, relativePath.length() - 5);
  }

  public void scan(Object type) {
    for (TypeFilteringModule filteringModule : this.filteringModules) {
      MetadataAdapter metadata = getMetadataAdapter();
      if (filteringModule.acceptType(type, metadata)) {
        String className = metadata.getClassName(type);
        getStore().put(className, className);
      }
    }
  }
}
