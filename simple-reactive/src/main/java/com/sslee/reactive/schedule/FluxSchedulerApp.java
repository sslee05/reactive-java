package com.sslee.reactive.schedule;

import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class FluxSchedulerApp {
  
  public static void main(String[] args) {
    Flux.range(0, 10)
        .publishOn(Schedulers.newSingle("pub"))
        .log()
        .subscribeOn(Schedulers.newSingle("sub"))
        .log()
        .subscribe(i -> System.out.println(Thread.currentThread().getName()+":"+i));
        
    System.out.println(Thread.currentThread().getName()+" Exit");
    
    Flux.interval(Duration.ofMillis(1000)).take(10).subscribe(System.out::println);
    
  }

}
