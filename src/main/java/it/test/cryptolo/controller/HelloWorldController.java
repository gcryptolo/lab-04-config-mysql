package it.test.cryptolo.controller;

import it.test.cryptolo.entity.TestData;
import it.test.cryptolo.service.TestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test-data")
public class HelloWorldController {

    @Autowired
    private TestDataService service;

    @GetMapping("/")
    public @ResponseBody List<TestData> getAll() {
        var res = service.findAll();
        return res;
    }
}