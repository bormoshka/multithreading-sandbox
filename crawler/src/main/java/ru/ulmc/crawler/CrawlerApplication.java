package ru.ulmc.crawler;

import org.apache.commons.lang3.tuple.ImmutablePair;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.ulmc.crawler.client.CrawlerManager;
import ru.ulmc.crawler.client.tools.CrawlingConfig;

@Slf4j
public class CrawlerApplication {

    public static void main(String[] args) {
        CrawlerManager instance = CrawlerManager.getInstance();
        try {
            val path = "C:\\temp\\crawler\\test\\";
            val minDimention = new ImmutablePair<Integer, Integer>(100, 100);
            val snoopConfig = CrawlingConfig.simpleYandex("pencil icon", minDimention, path);
            instance.start(snoopConfig);
        } catch (Exception ex) {
            log.error("unexpected exception", ex);
            instance.stop();
        }
    }

}

