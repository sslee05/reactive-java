package com.sslee.reactive.nonblocking;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

public class LoaderApp {
  
  static AtomicInteger counter = new AtomicInteger();
  
  public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
    ExecutorService es = Executors.newFixedThreadPool(100);
    
    RestTemplate rt = new RestTemplate();
    String url = "http://localhost:8080/rest2?idx= {idx}";
    StopWatch mainStopWatch = new StopWatch();
    mainStopWatch.start();
    
    CyclicBarrier barrier = new CyclicBarrier(100);
    
    
    IntStream.range(0, 100).forEach(i -> {
      es.submit(() -> {
        int idx = counter.getAndAdd(1);
        
        barrier.await();
        
        StopWatch sw = new StopWatch();
        sw.start();
        
        String result = rt.getForObject(url, String.class,idx);
        sw.stop();
        
        System.out.println(Thread.currentThread().getName()+"Elapsed:"+ idx + " "+result+" "+sw.getTotalTimeSeconds());
        return null;
      });
    });
    
    //barrier.await();
    
    
    es.shutdown();
    es.awaitTermination(100, TimeUnit.SECONDS);
    
    mainStopWatch.stop();
    System.out.println("Total:"+mainStopWatch.getTotalTimeSeconds());
  }

}
