package ru.ulmc.crawler.client.loot;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ulmc.crawler.client.tools.CrawlingConfig;
import ru.ulmc.crawler.entity.StaticPage;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.ulmc.crawler.client.tools.UrlUtils.getFileExtension;

@Slf4j
public class AnchorSnooper implements LootSnooper {
    private final Set<String> searchExtensions;
    private final String extensionRegex = "\\.(jpe?g|png|gif)";
    private final String subLinkRegex = ".*(https?://.+" + extensionRegex + ").*";
    private final Pattern subLinkPattern = Pattern.compile(subLinkRegex);

    private CrawlingConfig.SnoopConfig snoopConfig;
    private final String query = "a[href~=.+" + extensionRegex + "]";

    public AnchorSnooper(CrawlingConfig.SnoopConfig snoopConfig) {
        this.snoopConfig = snoopConfig;
        searchExtensions = snoopConfig.getExtensions();
    }

    @Override
    public Collection<String> sniffOut(StaticPage page) {
        Elements links = page.getBody().getAllElements().select(query);
        Set<String> urls = new HashSet<>();
        links.forEach(element -> getUrlFromAnchor(urls, element));
        return urls;
    }

    private boolean getUrlFromAnchor(Set<String> urls, Element element) {
        String href = null;
        try {
            href = URLDecoder.decode(element.absUrl("href"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        if (href.trim().isEmpty()) {
            return false;
        }
        Matcher matcher = subLinkPattern.matcher(href);
        if (matcher.find()) {
            String sublink = matcher.group(1);
            return urls.add(sublink);
        }
        return urls.add(href);
    }

    private boolean matchesExtensions(URL url) {
        String ext = getFileExtension(url).orElse("").toLowerCase();
        return searchExtensions.contains(ext);
    }
}
