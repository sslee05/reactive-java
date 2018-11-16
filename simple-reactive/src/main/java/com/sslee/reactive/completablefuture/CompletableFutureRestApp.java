package com.sslee.reactive.completablefuture;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import io.netty.channel.nio.NioEventLoopGroup;

@SuppressWarnings("deprecation")
@SpringBootApplication
public class CompletableFutureRestApp {
  
  @RestController
  public static class MyController {
    
    @Autowired private MyService myService;
    AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
    
    @GetMapping("/rest")
    public ListenableFuture<ResponseEntity<String>> hello(String idx) {
      
      //SpringMVC가 ListenableFuture에 대한 알아서 callback 를 return 한다.
      ListenableFuture<ResponseEntity<String>> lFuture = 
          rt.getForEntity("http://localhost:8081/service?req={req}", String.class,"hello"+idx);
      
      return lFuture;
    }
    
    @GetMapping("/rest2")
    public DeferredResult<String> hello2(String idx) {
      
      //setResult 하는 순간 Spring이 client로 응답을 보낸다.
      DeferredResult<String> dr = new DeferredResult<>();
      
      /*
      ListenableFuture<ResponseEntity<String>> lFuture = 
          rt.getForEntity("http://localhost:8081/service?req={req}", String.class,"hello"+idx);
      lFuture.addCallback(s -> {
        ListenableFuture<ResponseEntity<String>> lFuture2 = 
            rt.getForEntity("http://localhost:8081/service2?req={req}", String.class,"hello"+s.getBody());
        
        lFuture2.addCallback(s2 -> dr.setResult(s2.getBody()), e -> dr.setResult(e.getMessage()));
      },e -> dr.setErrorResult(e.getMessage()));
      */
      
      toCf(rt.getForEntity("http://localhost:8081/service?req={req}", String.class,"hello"+idx))
       .thenCompose(re -> toCf(rt.getForEntity("http://localhost:8081/service2?req={req}", String.class,"hello"+re.getBody())))
       .thenApplyAsync(re -> myService.work(re.getBody()))
       .thenAccept(s -> dr.setResult(s))
       .exceptionally(e -> {dr.setErrorResult(e.getMessage()); return (Void)null;});
      
      return dr;
    }
    
    <T> CompletableFuture<T> toCf(ListenableFuture<T> lf) {
      CompletableFuture<T> cf = new CompletableFuture<T>();
      lf.addCallback(s -> cf.complete(s), e -> cf.completeExceptionally(e));
      return cf;
    }
  }
  
  @Service
  static class MyService {
    public String work(String req)  {
      return req + "/myService";
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(CompletableFutureRestApp.class,args);
  }
}
