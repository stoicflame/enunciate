package com.webcohesion.enunciate.io;

import com.webcohesion.enunciate.EnunciateOutput;
import com.webcohesion.enunciate.module.EnunciateModule;
import rx.Observable;
import rx.Subscriber;

/**
 * @author Ryan Heaton
 */
public class EnunciateModuleOperator implements Observable.Operator<EnunciateOutput, EnunciateOutput> {

  private final EnunciateModule module;

  public EnunciateModuleOperator(EnunciateModule module) {
    if (module == null) {
      throw new NullPointerException();
    }

    this.module = module;
  }

  @Override
  public Subscriber<? super EnunciateOutput> call(final Subscriber<? super EnunciateOutput> subscriber) {
    return new Subscriber<EnunciateOutput>() {
      @Override
      public void onCompleted() {
        subscriber.onCompleted();
      }

      @Override
      public void onError(Throwable e) {
        subscriber.onError(e);
      }

      @Override
      public void onNext(EnunciateOutput enunciateOutput) {
        if (!subscriber.isUnsubscribed()) {
          try {
            module.call(enunciateOutput);
            subscriber.onNext(enunciateOutput);
          }
          catch (Exception e) {
            subscriber.onError(e);
          }
        }
      }
    };
  }
}
