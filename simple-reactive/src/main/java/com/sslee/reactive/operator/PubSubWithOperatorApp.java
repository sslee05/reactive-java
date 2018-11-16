package com.sslee.reactive.operator;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Operator 는 scala akka-stream 의 flow 처럼 data flow에 있어 변환 역할 
 * @author sslee
 *
 */
public class PubSubWithOperatorApp {
  
  public static void main(String[] args) {
    
    Iterator<Integer> iter = Stream.iterate(1, a -> a + 1).limit(10).iterator();
    Publisher<Integer> source = iterPub(iter);
    Publisher<Integer> foldLeftFlow = foldLeftFlow(source,0, (Integer b, Integer a) -> b + a);
    Publisher<String> mapFlow = mapFlow(foldLeftFlow,(Integer i) -> "["+ i+"]");
    Subscriber<String> sink = new SubscribeAdaptor<String>();
    mapFlow.subscribe(sink);
  }
  
  private static <A,B> Publisher<B> mapFlow(Publisher<A> pub, Function<A,B> f) {
    return (Subscriber<? super B> sub) -> { 
      pub.subscribe(new SubscribeAdaptor<A>() {
        @Override
        public void onNext(A t) {
          sub.onNext(f.apply(t));
        }
      });
    };
  }
  
  private static <A,B> Publisher<B> foldLeftFlow(Publisher<A> pub,B z, BiFunction<B,A,B> f) {
    return (Subscriber<? super B> sub) -> {
      pub.subscribe(new SubscribeAdaptor<A>() {
        B result = z;
        @Override 
        public void onNext(A a) {
          result = f.apply(result, a);
        }
        @Override public void onComplete() {
          sub.onNext(result);
          sub.onComplete();
        }
      });
    };
  }

  private static Publisher<Integer> iterPub(Iterator<Integer> iter) {
    return new Publisher<Integer>() {

      @Override
      public void subscribe(Subscriber<? super Integer> sub) {
        
        sub.onSubscribe(new Subscription() {

          @Override
          public void request(long n) {
            try {
              iter.forEachRemaining(a -> sub.onNext(a));
              sub.onComplete();
            }catch(Throwable t) {
              sub.onError(t);
            }
          }

          @Override
          public void cancel() {
          }
        });
      }
    };
  }

}
