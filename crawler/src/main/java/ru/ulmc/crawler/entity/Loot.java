package ru.ulmc.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
@AllArgsConstructor
public class Loot {
    private final StaticPage sourcePage;
    private final String uri;
}
