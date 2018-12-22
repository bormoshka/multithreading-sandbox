package ru.ulmc.crawler.client.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jsoup.nodes.Element;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageSources {
    private Element body;
    private List<String> links;
}
