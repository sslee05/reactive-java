package com.sslee.reactive;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@SpringBootApplication
public class SimpleReactiveApplication {

  @RestController
  public static class Controller {
    
    @RequestMapping("/hello")
    public Publisher<String> hellow(String name) {
      return (Subscriber<? super String> sub) -> {
        sub.onSubscribe(new Subscription() {

          @Override
          public void request(long n) {
            sub.onNext("hellow first reactive MVC in springframework "+ name);
            sub.onComplete();
          }
          @Override
          public void cancel() {
          }
        });
      }; 
    }
  }
  
	public static void main(String[] args) {
		SpringApplication.run(SimpleReactiveApplication.class, args);
	}
}
