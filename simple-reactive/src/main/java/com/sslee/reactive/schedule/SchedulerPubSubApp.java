package com.sslee.reactive.schedule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class SchedulerPubSubApp {
  
  public static void main(String[] args) {
    
    Publisher<Integer> pub = sub -> {
      sub.onSubscribe(new Subscription() {
        int nb = 0;
        boolean isCancelled = false;

        @Override
        public void request(long n) {
          ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
          es.scheduleAtFixedRate(() -> {
            if(!isCancelled)
              sub.onNext(nb++);
            else {
              sub.onComplete();
              es.shutdown();
              return;
            }
          }, 3, 300, TimeUnit.MILLISECONDS);
          sub.onComplete();
          
        }

        @Override
        public void cancel() {
          isCancelled = true;
        }
        
      });
    };
    
    //Publisher가 느린경우 
    //subscribeOn 사용하라.
    Publisher<Integer> subOnPub = sub -> {
      ExecutorService es = Executors.newSingleThreadExecutor();
      es.execute(() -> pub.subscribe(sub));
    };
    
    //Subscriber가 느린경우
    //publishOn 을 사용
    Publisher<Integer> pubOnPub = sub -> {
      
      pub.subscribe(new Subscriber<Integer>() {
        
        ExecutorService es  = Executors.newSingleThreadExecutor();

        @Override
        public void onSubscribe(Subscription s) {
          sub.onSubscribe(s);
        }

        @Override
        public void onNext(Integer t) {
          es.execute(() -> sub.onNext(t) );
        }

        @Override
        public void onError(Throwable t) {
          es.execute(() -> sub.onError(t) );
          es.shutdown();
        }

        @Override
        public void onComplete() {
          es.execute(() -> sub.onComplete() );
          es.shutdown();
        }
        
      });
    };
    
    Publisher<Integer> takePub = sub -> {
      pub.subscribe(new Subscriber<Integer>() {
        int max = 5;
        Subscription scription;
        
        @Override
        public void onSubscribe(Subscription s) {
          scription = s;
          sub.onSubscribe(s);
        }

        @Override
        public void onNext(Integer t) {
          if(max-- > 0) 
            sub.onNext(t);
          else
            scription.cancel();
        }

        @Override
        public void onError(Throwable t) {
          sub.onError(t);
          
        }

        @Override
        public void onComplete() {
          sub.onComplete();
        }
        
      });
    };
    
    Subscriber<Integer> sub = new Subscriber<Integer>() {

      @Override
      public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Integer t) {
        System.out.println(Thread.currentThread().getName()+" received value: "+t);
      }

      @Override
      public void onError(Throwable t) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void onComplete() {
        System.out.println(Thread.currentThread().getName()+" received message completed");
        
      }
    };
    
    takePub.subscribe(sub);
    System.out.println(Thread.currentThread().getName()+" Exit");
  }

}
