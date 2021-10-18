package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
public class SimpleController {

    Location location;
    boolean close;
 HistoryHandler handler=HistoryHandler.getInstance();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
    ZoneId polishZone = ZoneId.of("Europe/Warsaw");



    @RequestMapping(value = "/location", method = RequestMethod.POST)
    public String home(@RequestParam(value = "lat", required = false) double lat,
                       @RequestParam(value = "lon", required = false) double lon,
                       @RequestParam(value = "cl", required = false) String cl
    ) throws IOException {
       handler.createFile();
        location = new Location(dtf.format(LocalDateTime.now(polishZone)), lon, lat);
        close = cl.equals("close");
        handler.appendToFile(location);
        handler.insertHistory();

        handler.history.addLocation(location);
        System.out.println(location.latitude + " " + location.longitude + " " + close);
        return "home";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        handler.createFile();
      handler.clearFile();
        handler.insertHistory();

        return "home";
    }



    private ExecutorService nonBlockingService = Executors
            .newCachedThreadPool();


    @GetMapping("/sse")
    public SseEmitter handleSse() throws InterruptedException {
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.awaitTermination(5L, TimeUnit.SECONDS);
        if (!close && location != null) {
            nonBlockingService.execute(() -> {
                try {
                    emitter.send(location.locationDate + ";" + location.longitude + ";" + location.latitude);
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