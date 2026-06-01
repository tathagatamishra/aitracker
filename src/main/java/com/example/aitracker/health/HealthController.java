package com.example.aitracker.health;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public String getMethodName() {
        return new String("Ok");
    }
    
}
