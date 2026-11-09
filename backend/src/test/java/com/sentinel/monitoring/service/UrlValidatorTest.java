package com.sentinel.monitoring.service;

import com.sentinel.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    private UrlValidator urlValidator;

    @BeforeEach
    void setUp() {
        urlValidator = new UrlValidator();
    }

    @Test
    @DisplayName("Should accept valid public HTTP/HTTPS URLs")
    void testValidUrls() {
        assertDoesNotThrow(() -> urlValidator.validate("https://api.github.com/users"));
        assertDoesNotThrow(() -> urlValidator.validate("http://example.com/api/v1/health"));
        assertDoesNotThrow(() -> urlValidator.validate("https://httpbin.org/status/200"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "ftp://example.com",
            "file:///etc/passwd",
            "gopher://example.com",
            "javascript:alert(1)"
    })
    @DisplayName("Should reject invalid or unsupported protocol URLs")
    void testInvalidProtocols(String url) {
        assertThrows(BadRequestException.class, () -> urlValidator.validate(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost/api",
            "http://localhost:8080/api",
            "http://127.0.0.1/admin",
            "http://127.0.0.1:3306",
            "http://0.0.0.0/test",
            "http://test.localhost/api"
    })
    @DisplayName("Should reject localhost and loopback addresses (SSRF Protection)")
    void testLocalhostBlocked(String url) {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> urlValidator.validate(url));
        assertTrue(ex.getMessage().contains("Localhost") || ex.getMessage().contains("private or local"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://169.254.169.254/latest/meta-data/",
            "http://metadata.google.internal/computeMetadata/v1/",
            "http://instance-data.internal/metadata"
    })
    @DisplayName("Should reject cloud metadata endpoints (SSRF Protection)")
    void testCloudMetadataBlocked(String url) {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> urlValidator.validate(url));
        assertTrue(ex.getMessage().contains("metadata"));
    }
}

