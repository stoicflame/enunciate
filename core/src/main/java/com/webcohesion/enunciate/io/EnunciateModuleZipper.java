package com.webcohesion.enunciate.io;

import com.webcohesion.enunciate.EnunciateOutput;
import com.webcohesion.enunciate.module.EnunciateModule;
import rx.functions.FuncN;

/**
 * @author Ryan Heaton
 */
public class EnunciateModuleZipper implements FuncN<EnunciateOutput> {

  private final EnunciateModule module;

  public EnunciateModuleZipper(EnunciateModule module) {
    this.module = module;
  }

  @Override
  public EnunciateOutput call(Object... args) {
    EnunciateOutput output = (EnunciateOutput) args[0]; //todo: ?. Is there a better way to do this?
    if (this.module != null) {
      module.call(output);
    }
    return output;
  }
}
