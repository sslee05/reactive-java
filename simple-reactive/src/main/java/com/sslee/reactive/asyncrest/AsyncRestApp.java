package com.sslee.reactive.asyncrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import io.netty.channel.nio.NioEventLoopGroup;

@SuppressWarnings("deprecation")
@SpringBootApplication
public class AsyncRestApp {
  
  @RestController
  public static class MyController {
    
    //RestTemplate rt = new RestTemplate();
    //AsyncRestTemplate은 getForEntity 때 spring이 thread 를 만든다.
    //따라서 이렇게 하면 뒤에 work thread 가 많이 생긴다.
    //AsyncRestTemplate rt = new AsyncRestTemplate();
    
    //new NioEventLoopGroup(1)를 HttpClient lib를 Netty를 이용 Nio 를 이용하여 worker thread를 1개로 이용 
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
      
      //SpringMVC가 ListenableFuture에 대한 알아서 callback 를 return 한다.
      ListenableFuture<ResponseEntity<String>> lFuture = 
          rt.getForEntity("http://localhost:8081/service?req={req}", String.class,"hello"+idx);
      
      lFuture.addCallback(s -> {
        ListenableFuture<ResponseEntity<String>> lFuture2 = 
            rt.getForEntity("http://localhost:8081/service2?req={req}", String.class,"hello"+s.getBody());
        
        lFuture2.addCallback(s2 -> dr.setResult(s2.getBody()), e -> dr.setResult(e.getMessage()));
      },e -> dr.setErrorResult(e.getMessage()));
      
      return dr;
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(AsyncRestApp.class,args);
  }
}
