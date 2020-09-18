package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.net.SocketException;
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
    FTPClient ftpClient;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
    ZoneId polishZone = ZoneId.of("Europe/Warsaw");
    String fileName = "dailyLog.txt";
    File downloadedFile;
    boolean connected;


    @RequestMapping(value = "/location", method = RequestMethod.POST)
    public String home(@RequestParam(value = "lat", required = false) double lat,
                       @RequestParam(value = "lon", required = false) double lon,
                       @RequestParam(value = "cl", required = false) String cl
    ) throws IOException {
        connectToFTP();
        location = new Location(dtf.format(LocalDateTime.now(polishZone)), lon, lat);
        close = cl.equals("close");
        if (close) {
            clearFile();
        }
        appendToFile();
        insertHistory();
        history.addLocation(location);
        System.out.println(location.latitude + " " + location.longitude + " " + close);
        return "home";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {

        connectToFTP();
        if (!isFileFromToday()) {
            clearFile();
        }
        insertHistory();

        ObjectMapper Obj = new ObjectMapper();
        String historyString;
        if (location == null) {
            if (!history.locations.isEmpty()) {
                location = history.locations.get(history.locations.size() - 1);
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
            downloadedFile = new File(fileName);
            try {
                OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadedFile));
                ftpClient.retrieveFile(fileName, outputStream1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (Scanner scanner = new Scanner(downloadedFile)) {
                while(scanner.hasNextLine())                {
                    String line = scanner.nextLine();
                    String[] parsedLine = line.split(";");
                    String dateString = parsedLine[0];
                    String longitude = parsedLine[1];
                    String latitude = parsedLine[2];
                    Location location = new Location(dateString, Double.parseDouble(longitude), Double.parseDouble(latitude));
                    history.addLocation(location);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isFileFromToday() {
        LocalDate loggedDate = checkLoggedDate();
        if (loggedDate == null) {
            return true;
        }
        return loggedDate.equals(LocalDate.now(polishZone));
    }

    private void appendToFile() throws IOException {
        if (!isFileFromToday()) {
            clearFile();
        }
        insertContentToFile();

    }


    private void connectToFTP() {
        try {
            if (ftpClient == null || !connected) {
                ftpClient = new FTPClient();
                ftpClient.connect(System.getenv("FTP_HOST"));
                ftpClient.login(System.getenv("FTP_LOGIN"), System.getenv("FTP_PASS"));
                if(ftpClient.getReply()==200){
                    connected=true;
                }
            }
            System.out.println(ftpClient.getReplyString());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void insertContentToFile() {
        connectToFTP();
        String textToAppend = location.locationDate + ";" + location.longitude + ";" + location.latitude + System.lineSeparator();
        try (ByteArrayInputStream local = new ByteArrayInputStream(textToAppend.getBytes("UTF-8"))) {
            ftpClient.appendFile(fileName, local);
            System.out.println(ftpClient.getReply());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private LocalDate checkLoggedDate() {
        LocalDate loggedDate = null;
        downloadedFile = new File(fileName);
        try {
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadedFile));
            ftpClient.retrieveFile(fileName, outputStream1);
            System.out.println(ftpClient.getReply());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Scanner scanner = new Scanner(downloadedFile)) {
            if(scanner.hasNextLine())

            {
                String line = scanner.nextLine();
                String[] parsedLine = line.split(";");
                String dateString = parsedLine[0];
                loggedDate = LocalDateTime.parse(dateString, dtf).toLocalDate();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return loggedDate;
    }

    private void clearFile() {
        try (ByteArrayInputStream local = new ByteArrayInputStream("".getBytes("UTF-8"))) {
            ftpClient.storeFile(fileName, local);
            System.out.println(ftpClient.getReply());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}