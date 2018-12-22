package ru.ulmc.crawler;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.CrawlerManager;

import static java.util.Collections.singleton;

@Slf4j
public class CrawlerApplication {

    public static void main(String[] args) {
        CrawlerManager instance = CrawlerManager.getInstance();
        try {
            instance.start("https://www.google.com/search?safe=off&q=", "pencil icon",
                    singleton(".jpg"), "C:\\temp\\crawler\\test\\");
        } catch (Exception ex) {
            log.error("unexpected exception", ex);
            instance.stop();
        }
    }

}

