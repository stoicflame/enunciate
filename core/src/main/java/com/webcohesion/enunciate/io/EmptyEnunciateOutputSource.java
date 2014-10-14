package com.webcohesion.enunciate.io;

import com.webcohesion.enunciate.EnunciateOutput;
import rx.Observable;
import rx.Subscriber;

/**
 * @author Ryan Heaton
 */
public class EmptyEnunciateOutputSource implements Observable.OnSubscribe<EnunciateOutput> {

  @Override
  public void call(Subscriber<? super EnunciateOutput> subscriber) {
    if (!subscriber.isUnsubscribed()) {
      subscriber.onNext(new EnunciateOutput());
      subscriber.onCompleted();
    }
  }
}
