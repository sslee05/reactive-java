package com.sslee.reactive.nonblocking;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@SpringBootApplication
public class SpringDeferredResultApp {
  
  @RestController
  public static class MyController {
    Queue<DeferredResult<String>> queue = new ConcurrentLinkedQueue<>();
    
    @GetMapping("/dr")
    public DeferredResult<String> callable() {
      System.out.println(Thread.currentThread().getName()+":dr");
      DeferredResult<String> dr = new DeferredResult<>(60000000L);
      queue.add(dr);
      return dr;
    }
    
    @GetMapping("/dr/count")
    public String drcount() {
      return String.valueOf(queue.size());
    }
    
    @GetMapping("/dr/event")
    public String drevent(String msg) {
      queue.stream().forEach(dr -> {
        dr.setResult("Hellow "+ msg);
        queue.remove(dr);
      });
      
      return "OK";
    }
    
    /* 
    @GetMapping("/emitter")
    public ResponseBodyEmitter emitter() {
      System.out.println(Thread.currentThread().getName()+":dr");
      ResponseBodyEmitter emitter = new ResponseBodyEmitter();
      
      Executors.newSingleThreadExecutor().submit(() -> {
        IntStream.range(0, 51).forEach(i -> {
          
          try {
            emitter.send("<p>Stream"+ i+"</p>");
            Thread.sleep(100L);
          } catch (InterruptedException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        });
      });
      
      return emitter;
    }
    */
  }
  
  static AtomicInteger counter = new AtomicInteger();
  
  //@Bean
  ApplicationRunner run() {
    return args -> {
      ExecutorService es = Executors.newFixedThreadPool(100);
      
      RestTemplate rt = new RestTemplate();
      String url = "http://localhost:8080/dr";
      
      StopWatch mainStopWatch = new StopWatch();
      mainStopWatch.start();
      
      IntStream.range(0, 100).forEach(i -> {
        es.execute(() -> {
          int idx = counter.getAndAdd(1);
          System.out.println(Thread.currentThread().getName()+":"+ idx);
          
          StopWatch sw = new StopWatch();
          sw.start();
          
          rt.getForObject(url, String.class);
          sw.stop();
          
          System.out.println(Thread.currentThread().getName()+":"+ idx + " "+sw.getTotalTimeSeconds());
        });
      });
      
      
    };
  }
  
  public static void main(String[] args) {
    SpringApplication.run(SpringDeferredResultApp.class, args);
  }

}
