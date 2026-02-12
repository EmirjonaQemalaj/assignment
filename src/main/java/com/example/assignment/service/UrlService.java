package com.example.assignment.service;

import com.example.assignment.rest.dto.ShortenUrlRequest;
import com.example.assignment.rest.dto.ShortenUrlResponse;
import com.example.assignment.rest.dto.UrlStatsResponse;
import java.util.List;

public interface UrlService {

    ShortenUrlResponse shorten(ShortenUrlRequest request, String baseUrl);

    String resolveAndCount(String shortCode);

    UrlStatsResponse getStats(String shortCode);

    UrlStatsResponse overwriteExpiration(String shortCode, long expirationMinutes);
}
