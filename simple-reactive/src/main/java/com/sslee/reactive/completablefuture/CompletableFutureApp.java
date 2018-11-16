package com.sslee.reactive.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class CompletableFutureApp {
  
  public static void main(String[] args) throws InterruptedException, ExecutionException {
    //CompletableFuture<Integer> future = CompletableFuture.completedFuture(1);
    //CompletableFuture<Integer> future = new CompletableFuture<>();
    //future.complete(1);
    //future.completeExceptionally(new RuntimeException("test exception"));
    //System.out.println(future.get());
    
    ExecutorService es = Executors.newFixedThreadPool(10);
    
    CompletableFuture
      .supplyAsync(() -> {
        System.out.println(Thread.currentThread().getName()+": supplyAsync");
        //if(1 == 1) throw new RuntimeException("testExcep");
        return 1;
      })
      .thenApply(i -> {
        System.out.println(Thread.currentThread().getName()+": thenAppy:"+i);
        return (i + 2);
      })
      .thenComposeAsync(i -> { //flatMap
        System.out.println(Thread.currentThread().getName()+": thenAppy:"+i);
        return CompletableFuture.completedFuture(i * 2);
      })
      .exceptionally(e -> -10)
      //supplyAsync처리 thread 와 다른 thread 를 받아 처리시 Sync ExecutorService 를 명시적으로  
      .thenAcceptAsync(i -> System.out.println(Thread.currentThread().getName()+": thenAccept:"+i),es );
    
    System.out.println(Thread.currentThread().getName()+"Exit");
    
    es.shutdown();
    ForkJoinPool.commonPool().shutdown();//java 7 이후 default Pool CompletableFuture의 실행에 이용 
    ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    
  }

}
