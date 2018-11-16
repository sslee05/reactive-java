package com.sslee.reactive.nonblocking;

import java.util.concurrent.Callable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Servlet => HttpServletRequest =>  InputStream(Blocking)
 * Servlet => HttpServletResponse => OutputStream(Blocking)
 *  
 * Tomcat NIO
 * nonBlockingIO 
 * 1       ServletThread1
 * 2  NIO  ServletThread2
 * 3       ServletThread3
 *
 * Servlet 3.0 => InputStream이 다 끝나야 비동기 Servlet
 *   => 비동기 servlet
 * Servlet 3.1 => callback 방식 
 * @author sslee
 *
 */
@SpringBootApplication
@EnableAsync
public class SpringMVCApp {
  
  @RestController
  public static class MyController {
    
    @GetMapping("/async")
    public Callable<String> async() throws InterruptedException {
      System.out.println("callable");
      return () -> {
        System.out.println(Thread.currentThread().getName()+"-async");
        Thread.sleep(2000L);
        return "hellow";
      };
    }
  }
  
  public static void main(String[] args) {
    SpringApplication.run(SpringMVCApp.class, args);
  }

}
