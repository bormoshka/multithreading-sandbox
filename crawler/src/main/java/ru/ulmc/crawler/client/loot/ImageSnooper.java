package ru.ulmc.crawler.client.loot;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ulmc.crawler.client.tools.CrawlingConfig;
import ru.ulmc.crawler.entity.StaticPage;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ru.ulmc.crawler.client.tools.UrlUtils.getFileExtension;

@Slf4j
public class ImageSnooper implements LootSnooper {
    private final Set<String> searchExtensions;
    private CrawlingConfig.SnoopConfig snoopConfig;

    public ImageSnooper(CrawlingConfig.SnoopConfig snoopConfig) {
        this.snoopConfig = snoopConfig;
        searchExtensions = snoopConfig.getExtensions();
    }

    @Override
    public Collection<String> sniffOut(StaticPage page) {
        Elements imgs = page.getBody().getAllElements()
                .select("img[src]");
        Set<String> urls = new HashSet<>();
        imgs.forEach(element -> filterImagesBySize(urls, element));
        return urls;
    }

    private void filterImagesBySize(Set<String> urls, Element element) {
        val dimensionOptional = snoopConfig.getMinimumDimension();
        if (!dimensionOptional.isPresent()) {
            return;
        }
        val dimension = dimensionOptional.get();
        String width = element.attr("width");
        if (width.isEmpty()) {
            return;
        }
        int widthInt = Integer.parseInt(width.replaceAll("\\D", ""));
        if (widthInt > dimension.getRight()) {
            String attr = element.attr("abs:src");
            if (!attr.trim().isEmpty()) {
                urls.add(attr);
            }
        } else {
            log.trace("Skipping small image");
        }
    }

    private boolean matchesExtensions(URL url) {
        String ext = getFileExtension(url).orElse("").toLowerCase();
        return searchExtensions.contains(ext);
    }
}
