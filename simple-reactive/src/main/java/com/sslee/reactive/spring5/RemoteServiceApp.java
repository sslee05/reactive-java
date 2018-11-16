package com.sslee.reactive.spring5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class RemoteServiceApp {
  
  //pom.xml 에 starter-tomcat 주석 풀고 해야 함.
  @Bean
  TomcatReactiveWebServerFactory tomcatReactiveWebServerFactory() {
    return new TomcatReactiveWebServerFactory();
  }
  
  @RestController
  public static class MyController {
    
    @GetMapping("/service")
    public Mono<String> service(String req) throws InterruptedException {
      Thread.sleep(2000L);
      //throw new RuntimeException("testError");
      return Mono.just(req + "/service");
    }
    
    @GetMapping("/service2")
    public Mono<String> service2(String req) throws InterruptedException {
      Thread.sleep(2000L);
      return Mono.just(req + "/service2");
    }
  }
  
  public static void main(String[] args) {
    System.setProperty("server.port","8081");
    System.setProperty("server.tomcat.max-threads", "100");
    SpringApplication.run(RemoteServiceApp.class, args);
  }

}
