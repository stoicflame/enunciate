package com.webcohesion.enunciate.io;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.EnunciateModule;
import rx.Observer;

/**
 * @author Ryan Heaton
 */
public class InvokeEnunciateModule implements Observer<EnunciateContext> {

  private final EnunciateModule module;

  public InvokeEnunciateModule(EnunciateModule module) {
    this.module = module;
  }


  @Override
  public void onCompleted() {

  }

  @Override
  public void onError(Throwable throwable) {

  }

  @Override
  public void onNext(EnunciateContext enunciateContext) {
    this.module.call(enunciateContext);
  }
}
