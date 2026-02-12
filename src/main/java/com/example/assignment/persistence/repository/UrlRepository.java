package com.example.assignment.persistence.repository;

import com.example.assignment.persistence.entity.Url;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByLongUrl(String longUrl);

    Optional<Url> findByShortCode(String shortUrl);

    void deleteByExpiresAtBefore(LocalDateTime now);
}