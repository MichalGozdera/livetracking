package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
public class SimpleController {

    Location location;
    History history;
    boolean close;
    File locationLog;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
    ZoneId polishZone = ZoneId.of("Europe/Warsaw");


    @RequestMapping(value = "/location", method = RequestMethod.POST)
    public String home(@RequestParam(value = "lat", required = false) double lat,
                       @RequestParam(value = "lon", required = false) double lon,
                       @RequestParam(value = "cl", required = false) String cl
    ) throws IOException {
        if (locationLog == null) {
            createFile();
        }
        location = new Location(dtf.format(LocalDateTime.now(polishZone)), lon, lat);
        close = cl.equals("close");
        appendToFile();
        insertHistory();
        history.addLocation(location);
        System.out.println(location.latitude + " " + location.longitude + " " + close);
        return "home";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        if (locationLog == null) {
            createFile();
        }
        if (!isFileFromToday()) {
            clearFile();
        }
        insertHistory();

        ObjectMapper Obj = new ObjectMapper();
        String historyString;
        if (location == null) {
            if(!history.locations.isEmpty()){
              location=history.locations.get(history.locations.size()-1);
            }

        }
        try {
            historyString = Obj.writeValueAsString(history);
        } catch (Exception e) {
            e.printStackTrace();
            historyString = null;
        }

        model.addAttribute("history", historyString);
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

    private void insertHistory() {
        if (history == null) {
            history = new History();

            try (Scanner scanner = new Scanner(locationLog)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parsedLine = line.split(";");
                    String dateString = parsedLine[0];
                    String longitude = parsedLine[1];
                    String latitude = parsedLine[2];
                    Location location = new Location(dateString, Double.parseDouble(longitude), Double.parseDouble(latitude));
                    history.addLocation(location);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isFileFromToday() {
        LocalDate loggedDate = checkLoggedDate();
        if(loggedDate==null){
            return true;        }
        return  loggedDate.equals(LocalDate.now(polishZone));
    }

    private void appendToFile() {
        if (!isFileFromToday()) {
            clearFile();
        }
        insertContentToFile();

    }

    private void createFile() {
        try {
            locationLog = new File("dailyLog.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertContentToFile() {
        try (FileWriter fw = new FileWriter(locationLog, true)) {
            fw.append(location.locationDate + ";" + location.longitude + ";" + location.latitude+System.lineSeparator());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalDate checkLoggedDate() {
        LocalDate loggedDate = null;
        try (Scanner scanner = new Scanner(locationLog)) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parsedLine = line.split(";");
                String dateString = parsedLine[0];
                loggedDate = LocalDateTime.parse(dateString, dtf).toLocalDate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return loggedDate;
    }

    private void clearFile() {
        try (FileWriter fw = new FileWriter(locationLog)) {
            System.out.println("file cleared");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}