package com.sslee.reactive.intro;

import java.util.Iterator;

/**
 * Iterable 을 구현하면 for-each를 사용할 수 있다.
 * Iterable 은 순회할 수 있음을 나타 낸다.
 * @author sslee
 *
 */
public class IterableApp {
  
  // Iterable   <-----> Obserable  (duality)
  // pull               push
  
  static Iterable<Integer> getIterable() {
    return () -> new Iterator<Integer>() {
      int i = 0;
      static final int MAX = 10;
      public boolean hasNext() {
        return i < MAX;
      }
      public Integer next() {
        return ++i;
      }
    };
  }
  
  public static void main(String[] args) {
    Iterable<Integer> iter = getIterable();
    
    for(int i : iter) { // if(iter.hashNext()) iter.next(); // pull 방식 
      System.out.println(i);
    }
  }

}
