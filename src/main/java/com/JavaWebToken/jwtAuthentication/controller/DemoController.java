package com.JavaWebToken.jwtAuthentication.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint - no authentication required";
    }

    @GetMapping("/public/message")
    public String publicMessage() {
        return "This is a public endpoint - no authentication required";
    }

    @GetMapping("/private/message")
    public String privateMessage() {
        return "This is a private endpoint - authentication required";
    }

    @GetMapping("/admin/message")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminMessage() {
        return "This is an admin endpoint - ADMIN role required";
    }
}
