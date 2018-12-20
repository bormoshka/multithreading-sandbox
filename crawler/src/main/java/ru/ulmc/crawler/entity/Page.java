package ru.ulmc.crawler.entity;

import lombok.*;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.time.LocalDateTime.now;

@Getter
@Builder
@ToString
@EqualsAndHashCode(of = {"url", "visitDate"})
public class Page {
    @NonNull
    private final String url;
    @Builder.Default
    @NonNull
    private final LocalDateTime visitDate = now();
    @NonNull
    private final String domain;
    @Builder.Default
    @NonNull
    private final List<String> internalUrls = Collections.emptyList();
    @Builder.Default
    @NonNull
    private final List<String> externalUrls = Collections.emptyList();

    private final Element body;

    public Element getBody() {
        return body.clone();
    }

}
