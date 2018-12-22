package ru.ulmc.crawler.client.tools;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.security.Credential;
import ru.ulmc.crawler.entity.Loot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.Charset.defaultCharset;

@Slf4j
public class Exporter {
    private final static Pattern fileExtension = Pattern.compile("^(\\.[a-zA-Z0-9]{2,4}).*$");
    private final CrawlingConfig config;
    private final File dir;

    public Exporter(CrawlingConfig config) {
        this.config = config;
        dir = new File(config.getExportPath());
        checkDir(dir.getAbsolutePath());
    }

    private void checkDir(String downloadPath) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Cannot create output dirs with path: " + downloadPath);
        }
        if (!dir.isDirectory() || !dir.canWrite()) {
            throw new RuntimeException("Cannot write to this path: " + downloadPath);
        }
    }

    public void export(Loot loot) throws MalformedURLException {
        URL url = new URL(loot.getUri());
        String filename = dir.getAbsolutePath() + "\\" + getLootName(loot);
        File file = new File(filename);
        if (file.exists()) {
            log.debug("File exists: {}", filename);
            return;
        }

        try (InputStream inputStream = url.openStream()) {
            FileUtils.copyInputStreamToFile(inputStream, file);
            FileUtils.write(new File(filename + ".txt"), loot.getUri(), defaultCharset());
        } catch (IOException ex) {
            log.trace("File not found or other errors {} {}", url, ex.getMessage());
        }
    }

    public String getLootName(Loot loot) {
        String extension = getFileExtension(loot);
        return loot.getSourcePage().getDomain()
                + "_"
                + Credential.MD5.digest(loot.getUri()).replace(':', '_')
                + extension;
    }

    private String getFileExtension(Loot loot) {
        String uri = loot.getUri();
        String extension = uri.substring(uri.lastIndexOf('.'));
        Matcher matcher = fileExtension.matcher(extension);
        if (matcher.find()) {
            extension = matcher.group(1);
        }
        return postProcessExtension(extension);
    }

    private String postProcessExtension(String ext) {
        switch (ext) {
            case ".tiff":
                return ".tiff";
            case ".jpeg":
            case ".jpg":
                return ".jpg";
            case ".png":
                return ".png";
            case ".gif":
                return ".gif";
            default:
                return ".JPEG";
        }
    }
}
