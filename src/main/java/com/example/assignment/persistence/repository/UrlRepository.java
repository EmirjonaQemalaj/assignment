package com.example.assignment.persistence.repository;

import com.example.assignment.persistence.entity.Url;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {


    Optional<Url> findFirstByLongUrlOrderByExpiresAtDesc(String longUrl);

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying
    @Query("DELETE FROM Url u WHERE u.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}