package ru.ulmc.crawler.client.tools;

import lombok.Builder;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.ulmc.crawler.client.loot.AnchorSnooper;
import ru.ulmc.crawler.client.loot.ImageSnooper;
import ru.ulmc.crawler.client.loot.LootSnooper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;
import static org.eclipse.jetty.util.UrlEncoded.encodeString;

public class CrawlingConfig {
    private static final BlackList defaultBlackList = BlackList.loadDefaults();
    @Getter
    private final String keywords;
    @Getter
    private final Set<String> entryUris;
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
                           Set<String> entryUris,
                           String exportPath,
                           Collection<String> extensions,
                           ImmutablePair<Integer, Integer> minDimention,
                           ImmutablePair<Integer, Integer> maxDimention,
                           BlackList blackList,
                           int maxDownloads, int uriExtractingTimeout) {
        this.keywords = keywords;
        this.entryUris = unmodifiableSet(new HashSet<>(entryUris));
        this.exportPath = exportPath;
        this.maxDownloads = maxDownloads;
        this.blackList = blackList;
        this.uriExtractingTimeout = uriExtractingTimeout;
        this.snoopConfig = SnoopConfig.builder()
                .extensions(unmodifiableSet(new HashSet<>(extensions)))
                .minDimension(minDimention)
                .maxDimension(maxDimention)
                .build();
        this.snoopers = unmodifiableSet(new HashSet<>(
                asList(new ImageSnooper(snoopConfig),
                        new AnchorSnooper(snoopConfig))));
    }

    public static CrawlingConfig simpleGoogle(String keywords,
                                              ImmutablePair<Integer, Integer> minDimension,
                                              String exportPath) {
        return getDefault(keywords, minDimension, exportPath, 100, singleton(getGoogle(keywords)));
    }

    private static String getGoogle(String keywords) {
        return "https://www.google.com/search?safe=off&q=" + encodeString(keywords);
    }

    public static CrawlingConfig simpleYandex(String keywords,
                                              ImmutablePair<Integer, Integer> minDimension,
                                              String exportPath) {
        return getDefault(keywords, minDimension, exportPath, 300, singleton(getYandex(keywords)));
    }


    public static CrawlingConfig complex(String keywords,
                                         ImmutablePair<Integer, Integer> minDimension,
                                         String exportPath) {
        val entryPoints = new HashSet<>(asList(getYandex(keywords), getGoogle(keywords)));
        return getDefault(keywords, minDimension, exportPath, 300, entryPoints);
    }

    private static CrawlingConfig getDefault(String keywords,
                                             ImmutablePair<Integer, Integer> minDimension,
                                             String exportPath,
                                             int timeout,
                                             Set<String> entryPoints) {
        return CrawlingConfig.builder()
                .blackList(defaultBlackList)
                .keywords(keywords)
                .entryUris(entryPoints)
                .extensions(asList("jpg", "jpeg", "png", "gif"))
                .minDimention(minDimension)
                .maxDownloads(10000)
                .uriExtractingTimeout(timeout)
                .exportPath(exportPath + encodeString(keywords))
                .build();
    }


    private static String getYandex(String keywords) {
        return "https://yandex.ru/search/?text=" + encodeString(keywords);
    }

    @Builder
    public static class SnoopConfig {
        @Getter
        private final Set<String> extensions;
        private final ImmutablePair<Integer, Integer> minDimension;
        private final ImmutablePair<Integer, Integer> maxDimension;

        public Optional<Pair<Integer, Integer>> getMinimumDimension() {
            return Optional.ofNullable(minDimension);
        }

        public Optional<Pair<Integer, Integer>> getMaximumDimension() {
            return Optional.ofNullable(maxDimension);
        }
    }
}
