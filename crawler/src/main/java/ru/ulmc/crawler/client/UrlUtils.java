package ru.ulmc.crawler.client;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UrlUtils {
    private static final String URI_REGEX = "(www\\.)?[-a-zA-Z0-9%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
    private static final String URI_REGEX_FULL = "^(https?:\\/\\/" + URI_REGEX + ")$";
    private static final Pattern patternFull = Pattern.compile(URI_REGEX_FULL);
    private static final Pattern patternShort = Pattern.compile(URI_REGEX);
    private static final Pattern patternImage = Pattern.compile("(?i)\\\\.(png|jpe?g|gif)([?].+)?$");

    public static boolean isImageUri(String uri) {
        return patternImage.matcher(uri).matches();
    }

    public static Optional<String> getFileExtension(final URL url) {
        if (url == null) {
            return Optional.empty();
        }
        final String file = url.toString();
        final String ext = file.substring(file.lastIndexOf('.'));
        return Optional.of(ext);
    }

    public static Optional<URL> toCorrectUrl(String uri, String testUri) {
        try {
            Matcher matcher = patternFull.matcher(testUri);
            if (matcher.find()) {
                return Optional.of(new URL(matcher.group(1)));
            } else if (patternShort.matcher(testUri).matches()) {
                return Optional.of(new URL(new URL(uri), testUri));
            }
            return Optional.empty();
        } catch (MalformedURLException e) {
            log.warn("Malformed uri {} testUri {}", uri, testUri, e);
            return Optional.empty();
        }
    }
}
