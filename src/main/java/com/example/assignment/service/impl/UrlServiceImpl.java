package com.example.assignment.service.impl;

import com.example.assignment.persistence.entity.Url;
import com.example.assignment.persistence.repository.UrlRepository;
import com.example.assignment.rest.dto.ShortenUrlRequest;
import com.example.assignment.rest.dto.ShortenUrlResponse;
import com.example.assignment.rest.dto.UrlStatsResponse;
import com.example.assignment.service.UrlService;
import com.example.assignment.tools.CodeGenerator;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UrlServiceImpl implements UrlService {

    private final UrlRepository repository;
    private final CodeGenerator generator;

    private final long defaultExpirationMinutes;

    public UrlServiceImpl(
            UrlRepository repository,
            CodeGenerator generator,
            @Value("${app.url.expiration-minutes:5}")
            long defaultExpirationMinutes) {
        this.repository = repository;
        this.generator = generator;
        this.defaultExpirationMinutes = defaultExpirationMinutes;
    }

    @Override
    public ShortenUrlResponse shorten(ShortenUrlRequest request, String baseUrl) {
        String longUrl = normalizeUrl(request.getLongUrl());

        long minutes =
                (request.getExpirationMinutes() != null) ? request.getExpirationMinutes() : defaultExpirationMinutes;

        if (minutes <= 0) {
            throw new IllegalArgumentException("expirationMinutes must be > 0");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpiry = now.plusMinutes(minutes);

        var existingOpt = repository.findFirstByLongUrlOrderByExpiresAtDesc(longUrl);

        if (existingOpt.isPresent()) {
            Url existing = existingOpt.get();

            if (existing.getExpiresAt().isAfter(now)) {
                existing.setExpiresAt(newExpiry);
                return toResponse(existing, baseUrl);
            }
        }

        Url created = new Url();
        created.setLongUrl(longUrl);
        created.setExpiresAt(newExpiry);

        created.setShortCode(generateUniqueCode());

        repository.save(created);

        return toResponse(created, baseUrl);
    }

    @Override
    public String resolveAndCount(String shortCode) {
        LocalDateTime now = LocalDateTime.now();

        Url mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        if (!mapping.getExpiresAt().isAfter(now)) {
            throw new RuntimeException("Short URL expired");
        }

        mapping.setClickCount(mapping.getClickCount() + 1);

        return mapping.getLongUrl();
    }

    @Override
    public UrlStatsResponse getStats(String shortCode) {
        Url mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        UrlStatsResponse resp = new UrlStatsResponse();
        resp.setLongUrl(mapping.getLongUrl());
        resp.setShortCode(mapping.getShortCode());
        resp.setClickCount(mapping.getClickCount());
        resp.setExpiresAt(mapping.getExpiresAt());
        return resp;
    }

    @Override
    public UrlStatsResponse overwriteExpiration(String shortCode, long expirationMinutes) {
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("expirationMinutes must be > 0");
        }

        Url mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        mapping.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));

        UrlStatsResponse resp = new UrlStatsResponse();
        resp.setLongUrl(mapping.getLongUrl());
        resp.setShortCode(mapping.getShortCode());
        resp.setClickCount(mapping.getClickCount());
        resp.setExpiresAt(mapping.getExpiresAt());
        return resp;
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 10; i++) {
            String code = generator.generate();
            if (!repository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique short code");
    }

    private ShortenUrlResponse toResponse(Url mapping, String baseUrl) {
        ShortenUrlResponse resp = new ShortenUrlResponse();
        resp.setLongUrl(mapping.getLongUrl());
        resp.setShortCode(mapping.getShortCode());
        resp.setShortUrl(baseUrl + "/" + mapping.getShortCode());
        resp.setExpiresAt(mapping.getExpiresAt());
        return resp;
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("longUrl is required");
        }

        String trimmed = url.trim();

        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }

        try {
            URI.create(trimmed);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        return trimmed;
    }
}