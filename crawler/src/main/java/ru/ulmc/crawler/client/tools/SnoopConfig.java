package ru.ulmc.crawler.client.tools;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public class SnoopConfig {
    private final String keywords;
    private final Set<String> extensions;
    private final Pair<Integer, Integer> minDemention;
    private final Pair<Integer, Integer> maxDemention;
    private final BlackList blackList;

    public SnoopConfig(String keywords,
                       Set<String> extensions,
                       Pair<Integer, Integer> minDemention,
                       Pair<Integer, Integer> maxDemention,
                       BlackList blackList) {
        this.keywords = keywords;
        this.extensions = unmodifiableSet(extensions);
        this.minDemention = minDemention;
        this.maxDemention = maxDemention;
        this.blackList = blackList;
    }
}
