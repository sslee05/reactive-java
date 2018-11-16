package com.sslee.reactive.pubsub;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * 
 * Publisher
 * Subscriber
 * Subscription ( back-pressure 역할 )
 * Processor
 * 
 * Subscribe method list
 * onSubscribe onNext* (onError | onComplete)?
 * 
 * 
 * @author sslee
 *
 */
public class PubSub {
  
  public static void main(String[] args) throws InterruptedException  {
    //Publisher   <-- Obserable
    //Subscriber  <-- Observer
    
    ExecutorService es = Executors.newSingleThreadExecutor();
    Iterable<Integer> iter = Arrays.asList(1,2,3,4,5);
    Publisher<Integer> pub = new Publisher<Integer>() {

      @Override
      public void subscribe(Subscriber<? super Integer> sub) {
        
        Iterator<Integer> it = iter.iterator();
        
        sub.onSubscribe(new Subscription() {
          @Override
          public void request(long n) {
            es.execute(() -> { try {
              int i = 0;
              while(i++ < n ) {
                if(it.hasNext())
                  sub.onNext(it.next());
                else {
                  sub.onComplete();
                }
              }
            }catch (RuntimeException e) {
              sub.onError(e);
            }});
          }

          @Override
          public void cancel() {
            // TODO Auto-generated method stub
          }
          
        });
      }
    };
    
    Subscriber<Integer> sub = new Subscriber<Integer>() {

      Subscription subscription = null;
      @Override
      public void onSubscribe(Subscription s) {
        this.subscription = s;
        System.out.println("onSubscribe");
        s.request(1);
      }

      @Override
      public void onNext(Integer t) {// publisher에 발생시 처리하는 method 
        // TODO Auto-generated method stub
        System.out.println("onNext "+ t);
        subscription.request(1);
      }

      @Override
      public void onError(Throwable t) { // publisher 가 exception 시 subscriber인 나에게 줘 
        // TODO Auto-generated method stub
        System.out.println("onError "+ t);
      }

      @Override
      public void onComplete() { //publisher 가 줄 data완료 
        // TODO Auto-generated method stub
        System.out.println("onComplete ");
      }
      
    };
    
    pub.subscribe(sub);
    es.awaitTermination(3, TimeUnit.SECONDS);
  }
  
 

}
