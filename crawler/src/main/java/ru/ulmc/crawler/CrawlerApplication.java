package ru.ulmc.crawler;

import org.apache.commons.lang3.tuple.ImmutablePair;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.ulmc.crawler.client.Crawler;
import ru.ulmc.crawler.client.tools.CrawlingConfig;

@Slf4j
public class CrawlerApplication {

    public static void main(String[] args) {
        Crawler instance = Crawler.getInstance();
        try {
            val path = "C:\\temp\\crawler\\test\\";
            val minDimension = new ImmutablePair<Integer, Integer>(200, 200);
            String keywords;
            if(args.length > 0) {
                keywords = args[0];
            } else {
                keywords = "pencil icon";
            }
            val snoopConfig = CrawlingConfig.complex(keywords, minDimension, path);
            instance.start(snoopConfig);
        } catch (Exception ex) {
            log.error("unexpected exception", ex);
            instance.stop();
        }
    }

}

