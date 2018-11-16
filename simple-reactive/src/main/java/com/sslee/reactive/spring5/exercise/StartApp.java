package com.sslee.reactive.spring5.exercise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@SpringBootApplication
@RestController
@Slf4j
public class StartApp {
  
  @GetMapping("/")
  Mono<String> hello() {
    log.info("pos1");
    //Publisher -> (Publisher) -> (Publisher) -> Subscriber
    //return Mono.just("Hello Webflux").log();
    //method는 call-by-value 다.
    //Mono<String> m = Mono.just(genMsg()).doOnNext(s -> log.info("in doOnNext {}",s)).log();
    
    Mono<String> m = Mono.fromSupplier(() -> genMsg()).doOnNext(s -> log.info("in doOnNext {}",s)).log();
    //String msg = m.block();// subscriber 가 값를 꺼냄. 끝나면 아래 코드 진행  
    
    //Source 는 Cold Source(Publisher) 이다. 호출마다 처음부터 data stream 를 전송  
    //참고: Hot Source(Publisher) 는 외부 API 호출등을 통한 Source로, 여러 Subscriber가 가각의 각각의 구독시점에 각각의 구독시점부터의 data 를 전송 
    //m.subscribe();
    log.info("pos2");
    
    return m;
  }
  
  public String genMsg() {
    log.info("called genMsg");
    return "Hello Webflux";
  }

  public static void main(String[] args) {
    SpringApplication.run(StartApp.class, args);
  }
}
