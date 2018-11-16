package com.sslee.reactive.functional;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import io.netty.channel.nio.NioEventLoopGroup;

@SuppressWarnings("deprecation")
@SpringBootApplication
@EnableAsync
public class AsyncApp {
  
  @Bean
  ThreadPoolTaskExecutor threadPool() {
    ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
    te.setCorePoolSize(1);//초기 
    te.setMaxPoolSize(1);// Queue 에 꽉차면 100까지 증가(즉 200까지 샇이면) 
    te.initialize();
    return te;
  }
  
  
  @Component
  public static class MyService {
    @Async
    public ListenableFuture<String> work(String req)  {
      return new AsyncResult<>(req+"/myService");
    }
  }
  
  @RestController
  public static class MyController {
    
    static final String URL = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";
    AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
    
    @Autowired
    private MyService myService;
    
    @GetMapping("/rest2")
    public DeferredResult<String> rest(int idx) {
      
      DeferredResult<String> dr = new DeferredResult<>();
      Completion.from(rt.getForEntity(URL, String.class, "h"+idx))
                .andApply(en -> rt.getForEntity(URL2,String.class, en.getBody()))
                .andApply(s -> myService.work(s.getBody()))
                .andError(e -> dr.setErrorResult(e.toString()))
                .andAccept(s -> dr.setResult(s));
      
      return dr;
    }
  }
  
  public static class AcceptCompletion<S> extends Completion<S,Void> {
    Consumer<S> con;
    
    public AcceptCompletion(Consumer<S> con) {
      this.con = con;
    }
    
    @Override
    void run(S s) {
      System.out.println("################## AcceptCompletion called next call accept "+ s);
      con.accept(s);
    }
  }
  
  public static class ErrorCompletion<S> extends Completion<S,S> {
    Consumer<Throwable> econ;
    
    ErrorCompletion(Consumer<Throwable> econ) {
      this.econ = econ;
    }
    
    @Override
    void run(S s) {
      if(next != null) {
        System.out.println("################## errorCompletion called next call");
        next.run(s);
      }
    }
    
    @Override
    void error(Throwable e) {
      System.out.println("################## errorCompletion called error=>"+e.getMessage());
      this.econ.accept(e);
    }
    
  }
  
  public static class FunctionCompletion<S,T> extends Completion<S,T> {
    Function<S,ListenableFuture<T>> f;
    
    public FunctionCompletion(Function<S, ListenableFuture<T>> f) {
      this.f = f;
    }
    
    @Override
    void run(S s) {
      ListenableFuture<T> lf = f.apply(s);
      lf.addCallback(t -> complete(t), e -> error(e));
    }
  }
  
  public static class Completion<S,T> {
    
    Completion next;
    public Completion() {}
    
    public static <S,T> Completion<S,T> from(ListenableFuture<T> lf) {
      Completion<S,T> c = new Completion<>();
      lf.addCallback(s -> c.complete(s), e -> c.error(e));
      return c;
    }
    
    public void andAccept(Consumer<T> con) {
      Completion<T,Void> c = new AcceptCompletion<>(con);
      this.next = c;
    }
    
    public <U> Completion<T,U> andApply(Function<T, ListenableFuture<U>> f) {
      Completion<T,U> c = new FunctionCompletion<T,U>(f);
      this.next = c;
      return c;
    }

    void complete(T t) {
      if(next != null) next.run(t);
    }
    
    public Completion<T,T> andError(Consumer<Throwable> econ) {
      Completion<T,T> c = new ErrorCompletion<>(econ);
      this.next = c;
      return c;
    }

    void run(S s) {
    }

    void error(Throwable e) {
      e.printStackTrace();
      if(next != null) next.error(e);
    }
  }
  
  public static void main(String[] args) {
    SpringApplication.run(AsyncApp.class, args);
  }

}
