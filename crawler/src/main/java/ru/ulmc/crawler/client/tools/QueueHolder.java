package ru.ulmc.crawler.client.tools;

import lombok.Getter;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.StaticPage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class QueueHolder {
    private final BlockingQueue<StaticPage> pageBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Loot> lootBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> uriBlockingQueue = new LinkedBlockingQueue<>();
}
