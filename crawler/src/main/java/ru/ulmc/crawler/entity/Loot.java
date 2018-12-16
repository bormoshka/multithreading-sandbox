package ru.ulmc.crawler.entity;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Loot {
    private final Page sourcePage;
    private final String uri;
}
