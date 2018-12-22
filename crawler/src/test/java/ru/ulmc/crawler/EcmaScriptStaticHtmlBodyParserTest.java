package ru.ulmc.crawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.DefaultJavaScriptErrorListener;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore
public class EcmaScriptStaticHtmlBodyParserTest {
    @Test
    public void test() throws IOException {
        try (final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)) {
            webClient.setCssErrorHandler(new SilentCssErrorHandler());
            webClient.setJavaScriptErrorListener(new DefaultJavaScriptErrorListener());
            final HtmlPage page = webClient.getPage("https://duckduckgo.com/?q=test&t=h_&ia=images&iax=images");
            final String pageAsXml = page.asXml();

            final String pageAsText = page.asText();
            System.out.println(pageAsXml);
            System.out.println(pageAsText);

        }
    }
}
