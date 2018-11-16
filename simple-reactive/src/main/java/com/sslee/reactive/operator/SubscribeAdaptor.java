package com.sslee.reactive.operator;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class SubscribeAdaptor<T> implements Subscriber<T> {

  @Override
  public void onSubscribe(Subscription subscription) {
    System.out.println("I am Subscriber and called onSubscribe");
    subscription.request(Long.MAX_VALUE);
    
  }

  @Override
  public void onNext(T t) {
    System.out.println(t);
    
  }

  @Override
  public void onError(Throwable t) {
    System.out.println("I am Subscriber and called onError");
    t.printStackTrace();
    
  }

  @Override
  public void onComplete() {
    System.out.println("I am Subscriber and called onCompleted");
  }

}
