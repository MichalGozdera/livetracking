package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Controller
public class SimpleController {

double latitude;
double longitude;
boolean close;




    @RequestMapping(value = "/location", method = RequestMethod.POST )
    public String home(@RequestParam(value = "lat", required = false) double lat,
                           @RequestParam(value = "lon", required = false) double lon,
                       @RequestParam(value = "cl", required = false) String cl
                          ) throws IOException {
      latitude=lat;
      longitude=lon;
      close=cl.equals("close");
      System.out.println(latitude+" "+longitude+" "+close);
      return "home";
    }
    @RequestMapping(value = "/", method = RequestMethod.GET )
    public String home (Model model){
        model.addAttribute("lon",longitude);
        model.addAttribute("lat",latitude);
        return "home";
    }

    private ExecutorService nonBlockingService = Executors
            .newCachedThreadPool();


    @GetMapping("/sse")
    public SseEmitter handleSse() throws InterruptedException {
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.awaitTermination(5L, TimeUnit.SECONDS);
       if(!close){
           nonBlockingService.execute(() -> {
               try {
                   emitter.send(latitude+";"+longitude);
                   // we could send more events
                   emitter.complete();
               } catch (Exception ex) {
                   emitter.completeWithError(ex);
               }
           });
       }
        return emitter;
    }





}