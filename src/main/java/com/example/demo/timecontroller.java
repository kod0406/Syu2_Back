package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@RestController
public class timecontroller {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test/time")
    public Map<String, Object> testTime() {
        Map<String, Object> result = new HashMap<>();
        result.put("now", LocalDateTime.now());
        result.put("instant", Instant.now());
        result.put("dbTime", jdbcTemplate.queryForObject("SELECT NOW()", LocalDateTime.class));
        result.put("systemTimezone", TimeZone.getDefault().getID());
        result.put("zoneId", ZoneId.systemDefault().toString());
        return result;
    }
}