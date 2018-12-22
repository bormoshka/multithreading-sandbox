package ru.ulmc.crawler.client.loot;

import ru.ulmc.crawler.entity.StaticPage;

import java.util.Collection;

public interface LootSnooper {

    Collection<String> sniffOut(StaticPage elements);
}
