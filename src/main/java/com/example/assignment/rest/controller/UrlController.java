package com.example.assignment.rest.controller;

import com.example.assignment.rest.dto.ShortenUrlRequest;
import com.example.assignment.rest.dto.ShortenUrlResponse;
import com.example.assignment.rest.dto.UrlStatsResponse;
import com.example.assignment.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(path = "/api/urls")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @PostMapping()
    public ResponseEntity<ShortenUrlResponse> shorten(@RequestBody ShortenUrlRequest request,
            HttpServletRequest httpRequest) {
        String baseUrl = getBaseUrl(httpRequest);
        return ResponseEntity.ok(service.shorten(request, baseUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = service.resolveAndCount(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
    }

    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<UrlStatsResponse> stats(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getStats(shortCode));
    }

    @PatchMapping("/{shortCode}/expiration")
    public ResponseEntity<UrlStatsResponse> overwriteExpiration(@PathVariable String shortCode,
            @RequestParam long minutes) {
        return ResponseEntity.ok(service.overwriteExpiration(shortCode, minutes));
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        boolean defaultPort = (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
        return defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
    }
}
