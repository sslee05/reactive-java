package com.sslee.reactive.intro;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Iterable <-----> Obserable (duality)
 * pull             push
 * 
 * @author sslee
 *
 */
public class OberableApp {
  
  static class Publisher extends Observable implements Runnable {
    @Override
    public void run() {
      for(int i= 0; i < 10; ) {
        setChanged();
        notifyObservers(Thread.currentThread().getName()+":" + ++i);//push
      }
    }
  }
  
  public static void main(String[] args) {
    Observer subscriber = (Observable o, Object arg) -> System.out.println(arg);
    Publisher pub = new Publisher();
    pub.addObserver(subscriber);
    
    ExecutorService ec = Executors.newSingleThreadExecutor();
    ec.submit(pub);
    System.out.println(Thread.currentThread().getName()+":EXIT");
    ec.shutdown();
  }

}
