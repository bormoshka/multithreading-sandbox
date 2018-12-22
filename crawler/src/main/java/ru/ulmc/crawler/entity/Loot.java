package ru.ulmc.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.eclipse.jetty.util.security.Credential;


@Getter
@ToString
@AllArgsConstructor
public class Loot {
    private final StaticPage sourcePage;
    private final String uri;

    public String getLootName() {
        String extension = uri.substring(uri.lastIndexOf('.'));
        return sourcePage.getDomain()
                + "_"
                + Credential.MD5.digest(uri).replace(':','_')
                + extension;
    }
}
