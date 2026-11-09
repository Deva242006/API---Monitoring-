package com.sentinel.monitoring.service;

import com.sentinel.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * SSRF protection: validates URLs to prevent requests to internal/private networks.
 */
@Component
public class UrlValidator {

    private static final Logger log = LoggerFactory.getLogger(UrlValidator.class);

    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new BadRequestException("URL cannot be empty");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new BadRequestException("Invalid URL format: " + url);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new BadRequestException("Only HTTP and HTTPS protocols are allowed");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new BadRequestException("URL must contain a valid host");
        }

        String lowerHost = host.toLowerCase();
        if (lowerHost.equals("localhost") || lowerHost.equals("127.0.0.1") || lowerHost.equals("::1")
                || lowerHost.equals("0.0.0.0") || lowerHost.endsWith(".localhost")) {
            throw new BadRequestException("Localhost URLs are not allowed");
        }

        if (lowerHost.equals("169.254.169.254") || lowerHost.equals("metadata.google.internal")
                || lowerHost.endsWith(".internal")) {
            throw new BadRequestException("Cloud metadata endpoints are not allowed");
        }

        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isAnyLocalAddress()
                    || address.isMulticastAddress()) {
                throw new BadRequestException("URLs resolving to private or local addresses are not allowed");
            }
        } catch (UnknownHostException e) {
            log.warn("Could not resolve host for SSRF check: {}", host);
        }
    }
}

