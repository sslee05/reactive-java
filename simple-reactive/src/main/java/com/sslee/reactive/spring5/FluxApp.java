package com.sslee.reactive.spring5;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
@EnableAsync
public class FluxApp {
  
  @RestController
  public static class MyController {
    
    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";
    
    @Autowired MyService myService;
    
    //AsyncTemplate 비슷한 역할 
    WebClient client = WebClient.create();
    
    @GetMapping("/rest2")
    public Mono<String> rest(String idx) {
      
      //ClientResponse 는 ResponseEntity역할 
      //실제 실행되지 않는다. 왜냐하면 Mono는 publisher이다. subsciber가 구독하지 않는면 실제 발행되지 안는다.
      //왜냐면 subscriber가 back-pressure를 해야 하기 때문이다.
      //Mono return 될시 Spring 이 받아서 client를 subscriber로 등록되어 publisher를 통해 subscribe를 호출시  이때 구독 발생 이때 실행
      //Mono<ClientResponse> res =  client.get().uri(URL1,idx).exchange();
      //return res.flatMap(cr -> cr.bodyToMono(String.class));
      
      return client.get().uri(URL1,idx).exchange()
          .flatMap(cr -> cr.bodyToMono(String.class))
          .flatMap(s -> client.get().uri(URL2,s).exchange())
          .flatMap(cr -> cr.bodyToMono(String.class))
          .doOnNext(s -> System.out.println(Thread.currentThread().getName()+":"+s))
          .flatMap(s -> Mono.fromCompletionStage(myService.work2(s)))
          .doOnNext(s -> System.out.println(Thread.currentThread().getName()+":"+s));
          
    }
    
  }
  
  @Service
  static class MyService {
    public String work(String req)  {
      return req + "/myService";
    }
    
    @Async
    public CompletableFuture<String> work2(String req)  {
      return CompletableFuture.completedFuture(req + "/myService");
    }
  }

  public static void main(String[] args) {
    System.setProperty("reactor.ipc.netty.workerCount", "1");
    System.setProperty("reactor.ipc.netty.maxConnections", "2000");
    SpringApplication.run(FluxApp.class,args);
  }
}
