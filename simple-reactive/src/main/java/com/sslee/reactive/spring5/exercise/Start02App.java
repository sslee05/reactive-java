package com.sslee.reactive.spring5.exercise;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class Start02App {
  
  @GetMapping("/event/{id}")
  Mono<Event> event(@PathVariable long id) {
    return Mono.just(new Event(id,"event"+id));
  }
  
  @GetMapping("/events")
  Flux<Event> events() {
    return Flux.just(new Event(1,"event1"),new Event(2,"event2"));
  }
  
  
  /**
   * response 값이 이렇게 즉 stream으로 back-pressure 만큼 
   data:{"id":1,"value":"event1"}
   data:{"id":2,"value":"event2"}
   *
   * 참고: Mono<List<Event>> 경우 stream으로 back-pressure 가 무한 값이라고 생각 해도 즉 한번에 
   * [
       data:{"id":1,"value":"event1"},
       data:{"id":2,"value":"event2"}
   * ]
   * @return
   */
  @GetMapping(value="/events2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<Event> events2() {
    List<Event> xs = Arrays.asList(new Event(1,"event1"),new Event(2,"event2"));
    return Flux.fromIterable(xs);
  }

  @GetMapping(value="/events3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<Event> event3() {
    //return Flux.fromStream(Stream.generate(() -> new Event(System.currentTimeMillis(), "value"))).take(10);
    return Flux
        .fromStream(Stream.generate(() -> new Event(System.currentTimeMillis(), "value")))
        .delayElements(Duration.ofSeconds(1))
        .take(10);
  }
  
  @GetMapping(value="/events4", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<Event> event4() {
    return Flux
        .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(),"value"))).take(10);
  }
  
  @GetMapping(value="/events5", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<Event> event5() {
    return Flux
        .<Event,Long>generate(() -> 1L, (i,sink) -> {
          sink.next(new Event(i, "value"+i));
          return i;
         })
        .take(10);
  }
  
  @GetMapping(value="/events6", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<Event> event6() {
    Flux<Event> fl =  Flux
        .<Event,Long>generate(() -> 1L, (i,sink) -> {
          sink.next(new Event(i, "value"+i));
          return i;
         })
        .take(10);
    
    Flux<Long> intervalFl = Flux.interval(Duration.ofSeconds(1));
    
    return Flux.zip(fl, intervalFl).map(t -> new Event(t.getT2(), t.getT1().getValue()) );
    
  }
  
  @Data @AllArgsConstructor
  public static class Event {
    long id;
    String value;
  }
  
  public static void main(String[] args) {
    SpringApplication.run(Start02App.class, args);
  }

}
