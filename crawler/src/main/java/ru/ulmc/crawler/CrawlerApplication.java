package ru.ulmc.crawler;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.CrawlerManager;

import java.io.IOException;

import static java.util.Collections.singleton;
@Slf4j
public class CrawlerApplication {

    public static void main(String[] args) throws IOException {
        CrawlerManager instance = CrawlerManager.getInstance();
        try {
            instance.start("happy", singleton(".jpg"), "C:\\temp\\crawler\\");
        } catch (Exception ex) {
            log.error("unexpected exception", ex);
            instance.stop();
        }
    }

}

