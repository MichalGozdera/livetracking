package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    History history;
    File locationLog;
    ZoneId polishZone = ZoneId.of("Europe/Warsaw");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");




    @RequestMapping(path = "/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public History historyJson() {
        insertHistory();
        return history;
    }

    private void insertHistory() {
        if (history == null) {
            history = new History();
            if (locationLog == null) {
                createFile();
            }
            if (!isFileFromToday()) {
                clearFile();
            }

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

    private void createFile() {
        try {
            locationLog = new File("dailyLog.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFileFromToday() {
        LocalDate loggedDate = checkLoggedDate();
        if(loggedDate==null){
            return true;        }
        return  loggedDate.equals(LocalDate.now(polishZone));
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
