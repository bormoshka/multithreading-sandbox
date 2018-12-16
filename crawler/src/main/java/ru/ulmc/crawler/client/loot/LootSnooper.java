package ru.ulmc.crawler.client.loot;

import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.List;

import ru.ulmc.crawler.entity.Page;

public interface LootSnooper {

    Collection<String> sniffOut(Page elements);
}
