package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test/test/CICD")
    private String testCICD() {
        return "testCICD is Working~!!!_ver6";
    }
}
