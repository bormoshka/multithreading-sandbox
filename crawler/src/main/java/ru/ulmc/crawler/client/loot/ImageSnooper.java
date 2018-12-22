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
        Elements links = page.getBody().getAllElements()
                .select("a[href~=.+(.jpe?g|.png)]");
        Set<String> urls = new HashSet<>();
        imgs.forEach(element -> filterImagesBySize(urls, element));
        links.forEach(element -> getUrlFromAnchor(urls, element));
        //return urls.stream()
        //        .map(s -> toCorrectUrl(page.getUrl(), s))
        //        .filter(Optional::isPresent)
        //        .map(Optional::get)
        //        .filter(this::matchesExtensions)
        //        .map(URL::toString)
        //        .collect(Collectors.toSet());
        return urls;
    }

    private boolean getUrlFromAnchor(Set<String> urls, Element element) {
        String href = element.absUrl("href");
        if (href.trim().isEmpty()) {
            return false;
        }
        return urls.add(href);
    }

    private void filterImagesBySize(Set<String> urls, Element element) {
        val dimentionOptional = snoopConfig.getMinimumDimension();
        if (!dimentionOptional.isPresent()) {
            return;
        }
        val dimention = dimentionOptional.get();
        String width = element.attr("width");
        if (width.isEmpty()) {
            return;
        }
        int widthInt = Integer.parseInt(width.replaceAll("\\D", ""));
        if (widthInt > dimention.getRight()) {
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
