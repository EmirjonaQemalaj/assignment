package com.example.assignment;


import com.example.assignment.persistence.entity.Url;
import com.example.assignment.persistence.repository.UrlRepository;
import com.example.assignment.rest.dto.ShortenUrlRequest;
import com.example.assignment.service.impl.UrlServiceImpl;
import com.example.assignment.tools.CodeGenerator;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlServiceImplTest {

    @Mock
    private UrlRepository repository;

    @Mock
    private CodeGenerator generator;

    private UrlServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new UrlServiceImpl(repository, generator, 5L);
    }

    @Test
    void shouldCreateNewShortUrlWhenNotExisting() {
        ShortenUrlRequest req = new ShortenUrlRequest();
        req.setLongUrl("https://google.com");

        when(repository.findFirstByLongUrlOrderByExpiresAtDesc(any())).thenReturn(Optional.empty());

        when(generator.generate()).thenReturn("abc1234");
        when(repository.existsByShortCode("abc1234")).thenReturn(false);

        var response = service.shorten(req, "http://localhost");

        assertEquals("abc1234", response.getShortCode());
        verify(repository).save(any());
    }

    @Test
    void shouldReuseExistingNonExpiredUrlAndResetExpiration() {
        Url existing = new Url();
        existing.setLongUrl("https://google.com");
        existing.setShortCode("existing");
        existing.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        when(repository.findFirstByLongUrlOrderByExpiresAtDesc(any())).thenReturn(Optional.of(existing));

        ShortenUrlRequest req = new ShortenUrlRequest();
        req.setLongUrl("https://google.com");

        var response = service.shorten(req, "http://localhost");

        assertEquals("existing", response.getShortCode());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCreateNewIfExistingExpired() {
        Url expired = new Url();
        expired.setLongUrl("https://google.com");
        expired.setShortCode("old");
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(repository.findFirstByLongUrlOrderByExpiresAtDesc(any())).thenReturn(Optional.of(expired));

        when(generator.generate()).thenReturn("newcode");
        when(repository.existsByShortCode("newcode")).thenReturn(false);

        ShortenUrlRequest req = new ShortenUrlRequest();
        req.setLongUrl("https://google.com");

        var response = service.shorten(req, "http://localhost");

        assertEquals("newcode", response.getShortCode());
    }

    @Test
    void resolveShouldIncrementClickCount() {
        Url mapping = new Url();
        mapping.setShortCode("abc");
        mapping.setLongUrl("https://google.com");
        mapping.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        mapping.setClickCount(0);

        when(repository.findByShortCode("abc")).thenReturn(Optional.of(mapping));

        String url = service.resolveAndCount("abc");

        assertEquals("https://google.com", url);
        assertEquals(1, mapping.getClickCount());
    }

    @Test
    void resolveShouldThrowIfExpired() {
        Url mapping = new Url();
        mapping.setShortCode("abc");
        mapping.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(repository.findByShortCode("abc")).thenReturn(Optional.of(mapping));

        assertThrows(RuntimeException.class, () -> service.resolveAndCount("abc"));
    }

    @Test
    void overwriteExpirationShouldUpdateTime() {
        Url mapping = new Url();
        mapping.setShortCode("abc");

        when(repository.findByShortCode("abc")).thenReturn(Optional.of(mapping));

        var response = service.overwriteExpiration("abc", 10);

        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now()));
    }
}
