package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.DependingModuleAware;
import com.webcohesion.enunciate.module.EnunciateModule;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class EnunciateJaxbModule implements EnunciateModule, DependingModuleAware {

  private Set<String> dependingModules = null;
  private EnunciateContext context;

  @Override
  public String getName() {
    return "jaxb";
  }

  @Override
  public List<DependencySpec> getDependencies() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEnabled() {
    return !this.context.getConfiguration().getSource().getBoolean("enunciate.modules.jaxb[@disabled]")
      && (this.dependingModules == null || !this.dependingModules.isEmpty());
  }

  @Override
  public void init(EnunciateContext context) {
    this.context = context;
  }

  @Override
  public void call(EnunciateContext context) {

  }

  @Override
  public void acknowledgeDependingModules(Set<String> dependingModules) {
    this.dependingModules = dependingModules;
  }
}
