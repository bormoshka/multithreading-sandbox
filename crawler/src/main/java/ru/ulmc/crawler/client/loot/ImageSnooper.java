package ru.ulmc.crawler.client.loot;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ulmc.crawler.entity.StaticPage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ru.ulmc.crawler.client.UrlUtils.getFileExtension;

@Slf4j
public class ImageSnooper implements LootSnooper {
    private final Set<String> searchExtensions;

    public ImageSnooper(Set<String> extensions) {
        searchExtensions = extensions;
    }

    @Override
    public Collection<String> sniffOut(StaticPage page) {
        Elements imgs = page.getBody().getAllElements()
                .select("img[src]");
        Elements links = page.getBody().getAllElements()
                .select("a[href~=.+(.jpe?g|.png)]");
        List<String> urls = new ArrayList<>();
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

    private boolean getUrlFromAnchor(List<String> urls, Element element) {
        String href = element.absUrl("href");
        if (href.isEmpty()) {
            return false;
        }
        return urls.add(href);
    }

    private void filterImagesBySize(List<String> urls, Element element) {
        String width = element.attr("width");
        if (width.isEmpty()) {
            return;
        }
        int widthInt = Integer.parseInt(width.replace("\\w", ""));
        if (widthInt > 500) {
            urls.add(element.attr("abs:src"));
        } else {
            log.trace("Skipping small image");
        }
    }

    private boolean matchesExtensions(URL url) {
        String ext = getFileExtension(url).orElse("").toLowerCase();
        return searchExtensions.contains(ext);
    }
}
