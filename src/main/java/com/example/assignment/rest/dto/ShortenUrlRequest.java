package com.example.assignment.rest.dto;


public class ShortenUrlRequest {

    private String longUrl;

    // optional: if user wants custom expiration overwrite at creation time
    private Long expirationMinutes;

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(Long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }
}