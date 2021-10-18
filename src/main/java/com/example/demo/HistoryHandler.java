package com.example.demo;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class HistoryHandler {

    public History history;
    File locationLog;
    ZoneId polishZone = ZoneId.of("Europe/Warsaw");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
 private static HistoryHandler instance;

    public static HistoryHandler getInstance() {
        if(instance==null){
            instance=new HistoryHandler();
        }
        return instance;
    }


    public void insertHistory() {
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

    public void createFile() {
        if(locationLog == null) {
            try {
                locationLog = new File("dailyLog.txt");
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

    public void clearFile() {
        if(!isFileFromToday()) {
            try (FileWriter fw = new FileWriter(locationLog)) {
                System.out.println("file cleared");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void insertContentToFile(Location location) {
        try (FileWriter fw = new FileWriter(locationLog, true)) {
            fw.append(location.locationDate + ";" + location.longitude + ";" + location.latitude+System.lineSeparator());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendToFile(Location location) {
        if (!isFileFromToday()) {
            clearFile();
        }
        insertContentToFile(location);

    }
}
