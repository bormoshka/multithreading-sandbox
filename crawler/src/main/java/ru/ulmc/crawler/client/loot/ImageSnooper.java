package ru.ulmc.crawler.client.loot;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ru.ulmc.crawler.entity.Page;

import static ru.ulmc.crawler.client.UrlUtils.getFileExtension;
import static ru.ulmc.crawler.client.UrlUtils.toAbsoluteUrl;

public class ImageSnooper implements LootSnooper {
    private final Set<String> searchExtensions;

    public ImageSnooper(Set<String> extensions) {
        searchExtensions = extensions;
    }

    @Override
    public Collection<String> sniffOut(Page page) {
        List<String> strings = page.getBody().getAllElements()
                .select("img")
                .eachAttr("src");

        return strings.stream()
                .map(s -> toAbsoluteUrl(page.getUrl(), s))
                .filter(this::matchesExtensions)
                .filter(Objects::nonNull)
                .map(URL::toString)
                .collect(Collectors.toSet());
    }

    private boolean matchesExtensions(URL url) {
        String ext = getFileExtension(url).orElse("").toLowerCase();
        return searchExtensions.contains(ext);
    }
}
