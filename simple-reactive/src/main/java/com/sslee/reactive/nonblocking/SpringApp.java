package com.sslee.reactive.nonblocking;

import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@SpringBootApplication
@EnableAsync
public class SpringApp {
  
  @Component
  public static class MyService {
    @Async
    public Future<String> hello02() throws InterruptedException {
      System.out.println(Thread.currentThread().getName()+"-hello()");
      Thread.sleep(2000L);
      return new AsyncResult<>("hellow");
    }
    
    //callback 형태로 사용가능 
    //default로 SimpleAsyncTaskExecutor를 사용하며 이는 cacheing 작업 없이
    //무조건 만들기만 함. 절대 production 환경에서는 사용X
    //@Async(value="otherPool")// ThreadPool이 여러개일 경우 
    @Async
    public ListenableFuture<String> hello() throws InterruptedException {
      System.out.println(Thread.currentThread().getName()+"-hello()");
      Thread.sleep(2000L);
      return new AsyncResult<>("hellow");
    }
  }
  
  @Bean
  ThreadPoolTaskExecutor threadPool() {
    ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
    te.setCorePoolSize(10);//초기 
    te.setMaxPoolSize(100);// Queue 에 꽉차면 100까지 증가(즉 200까지 샇이면) 
    te.setQueueCapacity(200);
    te.setThreadNamePrefix("myThread");
    te.initialize();
    //te.setTaskDecorator(taskDecorator); log기록시 
    
    return te;
  }
  
  public static void main(String[] args) {
    //try(ConfigurableApplicationContext c = SpringApplication.run(SpringApp.class, args)){};
    SpringApplication.run(SpringApp.class, args);
  }
  
  @Autowired
  MyService myservice;
  
  @Bean
  ApplicationRunner run() {
    return args -> {
      System.out.println(Thread.currentThread().getName()+"-run()");
      
      ListenableFuture<String> lFuture = myservice.hello();
      lFuture.addCallback(s -> System.out.println(Thread.currentThread().getName()+"-result:"+ s), 
          t -> System.out.println(Thread.currentThread().getName()+"-"+t.getMessage()));
      //Future<String> future = myservice.hello02();
      System.out.println(Thread.currentThread().getName()+"-exit:");
      //System.out.println(Thread.currentThread().getName()+"-result:"+ future.get());
      
    };
  }

}
