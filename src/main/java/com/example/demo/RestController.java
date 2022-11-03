package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @RequestMapping(path = "/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public History historyJson() {
        HistoryHandler handler = HistoryHandler.getInstance();
        handler.insertHistory();
        return handler.history;
    }

}
