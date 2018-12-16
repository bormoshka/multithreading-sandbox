package ru.ulmc.crawler.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UrlUtils {


    public static Optional<String> getFileExtension(@NonNull final URL url) {
        final String file = url.getFile();
        if (file.contains(".")) {
            final String sub = file.substring(file.lastIndexOf('.') + 1);
            if (sub.length() == 0) {
                return Optional.empty();
            }
            if (sub.contains("?")) {
                return Optional.of(sub.substring(0, sub.indexOf('?')));
            }
            return Optional.of(sub);
        }
        return Optional.empty();
    }

    public static URL toAbsoluteUrl(String uri, String testUri) {
        try {
            if (testUri.startsWith("http")) {
                return new URL(testUri);
            } else {
                return new URL(new URL(uri), testUri);
            }
        } catch (MalformedURLException e) {
            log.warn("Malformed uri {}", uri, e);
            return null;
        }
    }
}
