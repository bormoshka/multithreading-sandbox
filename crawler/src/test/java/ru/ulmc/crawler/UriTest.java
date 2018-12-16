package ru.ulmc.crawler;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UriTest {

    @Test
    public void simple() throws URISyntaxException, MalformedURLException {
        URI uri = new URI("http://www.google.com/test/test.html?q=1");
        System.out.println(uri);
        String s = "/sesd/qwe.html?eq=2";
        URL url = new URL(new URL(uri.toString()), s);
        System.out.println(url);
        url = new URL(new URL(uri.toString()), "./sda");
        System.out.println(url);

    }
}
