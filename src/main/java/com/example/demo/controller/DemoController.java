package com.example.demo.controller;

import com.example.demo.Autometrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/")
    @Autometrics
    public String index() {
        return "Hello, World!";
    }
}
