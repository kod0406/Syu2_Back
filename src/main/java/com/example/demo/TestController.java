package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    @GetMapping("/test/test/CICD")
    private String testCICD() {
        return "testCICD";
    }
}
