package com.example.assignment.scheduler;

import com.example.assignment.persistence.repository.UrlRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ExpirationScheduler {

    private final UrlRepository repository;

    public ExpirationScheduler(UrlRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void deleteExpired() {
        repository.deleteExpired(LocalDateTime.now());
    }
}