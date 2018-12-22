package ru.ulmc.crawler.client.tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BlackList {
    private final Set<String> exclusions;

    private BlackList(Collection<String> exclusions) {
        this.exclusions = Collections.unmodifiableSet(new HashSet<>(exclusions));
    }

    public static BlackList loadDefaults() {
        URL resource = BlackList.class.getClassLoader().getResource("exclusions.txt");
        try {
            List<String> excl = FileUtils.readLines(new File(resource.toURI()), "UTF-8");
            return new BlackList(excl);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Misconfiguration!", e);
        }

    }

    public boolean inBlackList(String host) {
        return exclusions.contains(host);
    }
}
