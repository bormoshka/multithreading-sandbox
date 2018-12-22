package ru.ulmc.crawler.client.tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BlackList {
    private final Set<String> exclusions = new CopyOnWriteArraySet<>();

    public void load() throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource("exclusions.txt");
        List<String> excl = FileUtils.readLines(new File(resource.toURI()), "UTF-8");
        exclusions.addAll(excl);
    }

    public boolean inBlackList(String host) {
        return exclusions.contains(host);
    }
}
