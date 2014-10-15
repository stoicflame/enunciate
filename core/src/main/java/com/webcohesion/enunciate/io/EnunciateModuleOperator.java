package com.webcohesion.enunciate.io;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.EnunciateModule;
import rx.Observable;
import rx.Subscriber;

/**
 * @author Ryan Heaton
 */
public class EnunciateModuleOperator implements Observable.Operator<EnunciateContext, EnunciateContext> {

  private final EnunciateModule module;

  public EnunciateModuleOperator(EnunciateModule module) {
    if (module == null) {
      throw new NullPointerException();
    }

    this.module = module;
  }

  @Override
  public Subscriber<? super EnunciateContext> call(final Subscriber<? super EnunciateContext> subscriber) {
    return new Subscriber<EnunciateContext>() {
      @Override
      public void onCompleted() {
        subscriber.onCompleted();
      }

      @Override
      public void onError(Throwable e) {
        subscriber.onError(e);
      }

      @Override
      public void onNext(EnunciateContext enunciateContext) {
        if (!subscriber.isUnsubscribed()) {
          try {
            module.call(enunciateContext);
            subscriber.onNext(enunciateContext);
          }
          catch (Exception e) {
            subscriber.onError(e);
          }
        }
      }
    };
  }
}
