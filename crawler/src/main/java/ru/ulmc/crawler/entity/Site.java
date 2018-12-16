package ru.ulmc.crawler.entity;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Site {
    private final String domain;
    private final String enterUri;
    private final AtomicInteger depth = new AtomicInteger();

    public void goingDeeper() {
        depth.incrementAndGet();
    }

    public void isDepthLevelReached(int targetDepth) {
       //todo
    }
}
