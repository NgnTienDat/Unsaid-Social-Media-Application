package com.ntd.unsaid.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/users")
public class UserController {

    
    @GetMapping("/health")
    public String healthCheck() {
        return "User Service is up and running!";
    }
}
