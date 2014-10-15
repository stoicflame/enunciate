package com.webcohesion.enunciate.io;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.EnunciateModule;
import rx.functions.FuncN;

/**
 * @author Ryan Heaton
 */
public class EnunciateModuleZipper implements FuncN<EnunciateContext> {

  private final EnunciateModule module;

  public EnunciateModuleZipper(EnunciateModule module) {
    this.module = module;
  }

  @Override
  public EnunciateContext call(Object... args) {
    EnunciateContext output = (EnunciateContext) args[0]; //todo: ?. Is there a better way to do this?
    if (this.module != null) {
      module.call(output);
    }
    return output;
  }
}
