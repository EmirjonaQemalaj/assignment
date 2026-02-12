package com.example.assignment.service.impl;

import com.example.assignment.exceptions.InvalidInputException;
import com.example.assignment.persistence.entity.Url;
import com.example.assignment.persistence.repository.UrlRepository;
import com.example.assignment.service.UrlService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UrlServiceImpl implements UrlService {

    private static final String URL_NOT_FOUND = "Short URL not found";

    private final UrlRepository urlRepository;

    @Value("${url.expiration.minutes:5}")
    private long expirationMinutes;

    public UrlServiceImpl(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String processUrl(String inputUrl) {

//        urlRepository.deleteByExpirationTimeBefore(LocalDateTime.now());

        boolean isShort;
        try {
            new java.net.URL(inputUrl);
            isShort = false;
        } catch (Exception e) {
            isShort = true;
        }

        if (isShort) {
            Optional<Url> optional = urlRepository.findByShortCode(inputUrl);
            if (optional.isEmpty()) {
                throw new InvalidInputException(URL_NOT_FOUND);
            }

            Url mapping = optional.get();
            if (mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
                urlRepository.delete(mapping);
                throw new InvalidInputException(URL_NOT_FOUND);
            }

            mapping.setClickCount(mapping.getClickCount() + 1);
            urlRepository.save(mapping);
            return mapping.getLongUrl();
        }

        Optional<Url> existing = urlRepository.findByLongUrl(inputUrl);

        if (existing.isPresent()) {
            Url mapping = existing.get();

            if (mapping.getExpiresAt().isAfter(LocalDateTime.now())) {
                mapping.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
                urlRepository.save(mapping);
                return mapping.getShortCode();
            }
            urlRepository.delete(mapping);
        }

        String shortUrl = generateUniqueShortUrl();

        Url newMapping = new Url();
        newMapping.setLongUrl(inputUrl);
        newMapping.setShortCode(shortUrl);
        newMapping.setClickCount(0L);
        newMapping.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));

        urlRepository.save(newMapping);

        return shortUrl;
    }

    private String generateUniqueShortUrl() {
        String shortUrl;
        do {
            shortUrl = UUID.randomUUID().toString().substring(0, 8);
        } while (urlRepository.findByShortCode(shortUrl).isPresent());
        return shortUrl;
    }
}