package ru.ulmc.crawler.client.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ulmc.crawler.entity.PageSources;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.ulmc.crawler.client.tools.UrlUtils.isImageUri;

@Slf4j
public class StaticHtmlBodyParser {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
    private CrawlingConfig config;

    public StaticHtmlBodyParser(CrawlingConfig config) {
        this.config = config;
    }

    public Optional<PageSources> preparePageSources(URI uri) throws IOException, URISyntaxException {
        Element body;
        try {
            body = fetchBody(uri);
        } catch (Exception ex) {
            log.error("Something went wrong with {}", uri, ex);
            return Optional.empty();
        }
        List<Elements> links = findLinks(body);

        List<String> list = new ArrayList<>();
        List<String> listLocal = new ArrayList<>();
        links.forEach(elements -> extractHrefs(uri, list, listLocal, elements));
        listLocal.addAll(list);
        log.trace("links {}", list);
        return Optional.of(new PageSources(body, listLocal));
    }

    private void extractHrefs(URI uri, List<String> list, List<String> listLocal, Elements elements) {
        for (String href : elements.eachAttr("abs:href")) {
            if (isImageUri(href)) {
                continue;
            }
            UrlUtils.toCorrectUrl(uri.toString(), href).ifPresent(url -> {
                if (url.getHost().equals(uri.getHost())) {
                    listLocal.add(url.toString());
                } else {
                    list.add(url.toString());
                }
            });
        }
    }

    private List<Elements> findLinks(Element body) {
        List<Elements> elementsList = new ArrayList<>(128);
        body.getAllElements().forEach(element -> {
            Elements a = element.getElementsContainingText(config.getKeywords())
                    .select("a");
            if (a != null && !a.isEmpty()) {
                elementsList.add(a);
            }
        });
        return elementsList;
    }

    private Element fetchBody(URI uri) throws IOException {
        Element body;
        Connection connect = Jsoup.connect(uri.toString())
                .userAgent(USER_AGENT)
                .followRedirects(true)
                //.ignoreHttpErrors(true)
                .ignoreContentType(true);

        body = connect.execute().parse().body();
        return body;
    }

}
