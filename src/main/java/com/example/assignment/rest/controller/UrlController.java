package com.example.assignment.rest.controller;

import com.example.assignment.rest.dto.UrlRequest;
import com.example.assignment.service.UrlService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/urls")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @PostMapping
    public String process(@RequestBody UrlRequest request) {
        return service.processUrl(request.getUrl());
    }
}
