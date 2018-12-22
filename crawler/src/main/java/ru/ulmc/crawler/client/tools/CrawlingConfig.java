package ru.ulmc.crawler.client.tools;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.util.UrlEncoded;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import ru.ulmc.crawler.client.loot.ImageSnooper;
import ru.ulmc.crawler.client.loot.LootSnooper;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

public class CrawlingConfig {
    private static final BlackList defaultBlackList = BlackList.loadDefaults();
    @Getter
    private final String keywords;
    @Getter
    private final String entryUri;
    @Getter
    private final String exportPath;
    @Getter
    private final int maxDownloads;
    @Getter
    private final int uriExtractingTimeout;
    @Getter
    private final Set<LootSnooper> snoopers;
    @Getter
    private final SnoopConfig snoopConfig;
    @Getter
    private final BlackList blackList;


    @Builder
    private CrawlingConfig(String keywords,
                           String entryUri,
                           String exportPath,
                           Collection<String> extensions,
                           ImmutablePair<Integer, Integer> minDimention,
                           ImmutablePair<Integer, Integer> maxDimention,
                           BlackList blackList,
                           int maxDownloads, int uriExtractingTimeout) {
        this.keywords = keywords;
        this.entryUri = entryUri;
        this.exportPath = exportPath;
        this.maxDownloads = maxDownloads;
        this.blackList = blackList;
        this.uriExtractingTimeout = uriExtractingTimeout;
        this.snoopConfig = SnoopConfig.builder()
                .extensions(unmodifiableSet(new HashSet<>(extensions)))
                .minDimention(minDimention)
                .maxDimention(maxDimention)
                .build();
        this.snoopers = unmodifiableSet(singleton(new ImageSnooper(snoopConfig)));
    }

    public static CrawlingConfig simpleGoogle(String keywords,
                                              ImmutablePair<Integer, Integer> minDimention,
                                              String exportPath) {
        return CrawlingConfig.builder()
                .blackList(defaultBlackList)
                .keywords(keywords)
                .entryUri("https://www.google.com/search?safe=off&q=" + UrlEncoded.encodeString(keywords))
                .extensions(asList("jpg", "jpeg", "png", "gif"))
                .minDimention(minDimention)
                .maxDownloads(10000)
                .uriExtractingTimeout(100)
                .exportPath(exportPath).build();
    }

    public static CrawlingConfig simpleYandex(String keywords,
                                              ImmutablePair<Integer, Integer> minDimention,
                                              String exportPath) {
        return CrawlingConfig.builder()
                .blackList(defaultBlackList)
                .keywords(keywords)
                .entryUri("https://yandex.ru/search/?text=" + UrlEncoded.encodeString(keywords))
                .extensions(asList("jpg", "jpeg", "png", "gif"))
                .minDimention(minDimention)
                .maxDownloads(10000)
                .uriExtractingTimeout(100)
                .exportPath(exportPath).build();
    }


    @Builder
    public static class SnoopConfig {
        @Getter
        private final Set<String> extensions;
        private final ImmutablePair<Integer, Integer> minDimention;
        private final ImmutablePair<Integer, Integer> maxDimention;

        public Optional<Pair<Integer, Integer>> getMinimumDimention() {
            return Optional.ofNullable(minDimention);
        }

        public Optional<Pair<Integer, Integer>> getMaximumDimention() {
            return Optional.ofNullable(maxDimention);
        }
    }
}
