package com.example.assignment;

import com.example.assignment.exceptions.InvalidInputException;
import com.example.assignment.persistence.entity.Url;
import com.example.assignment.persistence.repository.UrlRepository;
import com.example.assignment.service.impl.UrlServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlServiceImplTest {

    @Mock
    private UrlRepository repository;

    private UrlServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new UrlServiceImpl(repository);
    }

    @Test
    void shouldCreateNewShortUrlWhenNotExisting() {

        when(repository.findByLongUrl("https://google.com"))
                .thenReturn(Optional.empty());

        when(repository.findByShortCode(any()))
                .thenReturn(Optional.empty());

        String result = service.processUrl("https://google.com");

        assertNotNull(result);
        verify(repository).save(any());
    }

    @Test
    void shouldReuseExistingNonExpiredUrlAndResetExpiration() {

        Url existing = new Url();
        existing.setLongUrl("https://google.com");
        existing.setShortCode("existing");
        existing.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        when(repository.findByLongUrl("https://google.com"))
                .thenReturn(Optional.of(existing));

        String result = service.processUrl("https://google.com");

        assertEquals("existing", result);
        verify(repository).save(existing); // expiration reset
    }

    @Test
    void shouldCreateNewIfExistingExpired() {

        Url expired = new Url();
        expired.setLongUrl("https://google.com");
        expired.setShortCode("old");
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(repository.findByLongUrl("https://google.com"))
                .thenReturn(Optional.of(expired));

        when(repository.findByShortCode(any()))
                .thenReturn(Optional.empty());

        String result = service.processUrl("https://google.com");

        assertNotEquals("old", result);
        verify(repository).delete(expired);
        verify(repository).save(any());
    }


    @Test
    void resolveShouldIncrementClickCount() {

        Url mapping = new Url();
        mapping.setShortCode("abc");
        mapping.setLongUrl("https://google.com");
        mapping.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        mapping.setClickCount(0L);

        when(repository.findByShortCode("abc"))
                .thenReturn(Optional.of(mapping));

        String result = service.processUrl("abc");

        assertEquals("https://google.com", result);
        assertEquals(1L, mapping.getClickCount());
        verify(repository).save(mapping);
    }

    @Test
    void resolveShouldThrowIfExpired() {

        Url mapping = new Url();
        mapping.setShortCode("abc");
        mapping.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(repository.findByShortCode("abc"))
                .thenReturn(Optional.of(mapping));

        assertThrows(InvalidInputException.class,
                () -> service.processUrl("abc"));

        verify(repository).delete(mapping);
    }

    @Test
    void resolveShouldThrowIfNotFound() {

        when(repository.findByShortCode("abc"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidInputException.class,
                () -> service.processUrl("abc"));
    }
}
