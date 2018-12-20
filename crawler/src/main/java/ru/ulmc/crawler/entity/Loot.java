package ru.ulmc.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Loot {
    private final Page sourcePage;
    private final String uri;
}
