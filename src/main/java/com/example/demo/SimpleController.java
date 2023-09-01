package com.example.demo;

import com.example.demo.websocket.LocationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class SimpleController {

   public Location location;
    public boolean close;
    HistoryHandler handler=HistoryHandler.getInstance();

@Autowired
    private AnnotationConfigServletWebServerApplicationContext context ;

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
        try {
            sendLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "home";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        handler.createFile();
      handler.clearFile();
        handler.insertHistory();

        return "home";
    }


    private void sendLocation() throws Exception {
        LocationHandler locationHandler=(LocationHandler)context.getBean("locationHandler");
        if (!close && location != null) {
            TextMessage tm = new TextMessage(location.locationDate + ";" + location.longitude + ";" + location.latitude+";"+handler.history.getDistance());
            locationHandler.handleMessage(null,tm);
        }
    }



}