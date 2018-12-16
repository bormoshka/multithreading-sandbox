package ru.ulmc.crawler;

import java.io.IOException;
import java.util.Collections;

import ru.ulmc.crawler.client.CrawlerManager;

public class CrawlerApplication {

    public static void main(String[] args) throws IOException {
        CrawlerManager crawler = new CrawlerManager();
        crawler.start("happy", Collections.singleton(".jpg"));
    }

}

