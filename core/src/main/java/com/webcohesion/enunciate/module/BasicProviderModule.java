package com.webcohesion.enunciate.module;

import java.util.Collections;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public abstract class BasicProviderModule extends BasicEnunicateModule implements ApiRegistryProviderModule {

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Collections.<DependencySpec>singletonList(new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        return module instanceof ContextModifyingModule;
      }

      @Override
      public boolean isFulfilled() {
        return true;
      }
    });
  }
}
