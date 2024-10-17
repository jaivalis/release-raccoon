package com.raccoon.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedController {

    @GetMapping("/api/protected")
    public String protectedEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return "This is a protected endpoint. User ID: " + jwt.getSubject();
    }
}
