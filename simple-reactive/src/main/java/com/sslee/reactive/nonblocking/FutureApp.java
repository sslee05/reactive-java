package com.sslee.reactive.nonblocking;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 결과를 가져오는 방법 
 * 1. Future get //blocking
 * 2. Callback 를 이용 
 * @author sslee
 *
 */
public class FutureApp {
  
  interface SuccessCallback {
    void onSuccess(String result);
  }
  
  interface ExceptionCallback {
    void onError(Throwable t);
  }
  
  public static class CallbackFutureTask extends FutureTask<String> {
    SuccessCallback successCallback;
    ExceptionCallback exceptionCallback;
    public CallbackFutureTask(Callable<String> callable, SuccessCallback successCallback,ExceptionCallback errorCallback) {
      super(callable);
      this.successCallback = Objects.requireNonNull(successCallback);
      this.exceptionCallback = Objects.requireNonNull(errorCallback);
    }
    
    protected void done() {
        try {
          this.successCallback.onSuccess(get());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          this.exceptionCallback.onError(e.getCause());
        }
    }
  }
  
  public static void main(String[] args) {
    ExecutorService es = Executors.newCachedThreadPool();
    
    CallbackFutureTask callback = new CallbackFutureTask(() -> {
      Thread.sleep(2000L);
      //if(1 == 1) throw new RuntimeException("Async ERROR!");
      System.out.println(Thread.currentThread().getName()+" asyn");
      return "hellow";
    },result -> System.out.println(Thread.currentThread().getName()+"-"+result),
      t -> System.out.println(Thread.currentThread().getName()+"-"+t.getMessage())) ;
    
    es.execute(callback);
    System.out.println(Thread.currentThread().getName()+"Exit");
    
    es.shutdown();
    
  }
  
  //callback by used isDone method
  public static void main_FutureTask(String[] args) throws InterruptedException, ExecutionException {
    FutureTask<String> future = new FutureTask<String>(() -> {
        Thread.sleep(2000L);
        System.out.println(Thread.currentThread().getName()+" asyn");
        return "hellow";
    }) {
      @Override
      protected void done() {
        try {
          System.out.println(Thread.currentThread().getName()+"-"+get());
        } catch (InterruptedException e) {} catch (ExecutionException e) {}
      }
    };
    
    ExecutorService es = Executors.newCachedThreadPool();
    es.execute(future);
    
    System.out.println(Thread.currentThread().getName()+"future is done "+ future.isDone());//nonblocking
    System.out.println(Thread.currentThread().getName()+"Exit");
    
    //future.get() blocking
    //System.out.println(Thread.currentThread().getName()+"-"+future.get());//blocking
    
  }
  
  public static void main_(String[] args) throws InterruptedException, ExecutionException {
    FutureTask<String> future = new FutureTask<String>(() -> {
      try {
        Thread.sleep(2000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println(Thread.currentThread().getName()+" asyn");
      return "hellow";
    }) ;
    
    ExecutorService es = Executors.newCachedThreadPool();
    es.execute(future);
    
    System.out.println(Thread.currentThread().getName()+"future is done "+ future.isDone());//nonblocking
    System.out.println(Thread.currentThread().getName()+"Exit");
    
    //future.get() blocking
    System.out.println(Thread.currentThread().getName()+"-"+future.get());//blocking
    
  }
  
  //Future get 
  public static void main_future_get(String[] args) throws InterruptedException, ExecutionException {
    ExecutorService es = Executors.newCachedThreadPool();
    
    Future<String> future = es.submit(() -> {
      try {
        Thread.sleep(2000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println(Thread.currentThread().getName()+" asyn");
      return "hellow";
    });
    
    System.out.println(Thread.currentThread().getName()+"future is done "+ future.isDone());//nonblocking
    System.out.println(Thread.currentThread().getName()+"Exit");
    
    //future.get() blocking
    System.out.println(Thread.currentThread().getName()+"-"+future.get());//blocking
  }
  
  //Runnable 
  public static void main_void(String[] args) {
    ExecutorService es = Executors.newCachedThreadPool();
    
    es.execute(() -> {
      try {
        Thread.sleep(2000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println(Thread.currentThread().getName()+" asyn");
    });
    
    System.out.println(Thread.currentThread().getName()+"Exit");
  }

}
