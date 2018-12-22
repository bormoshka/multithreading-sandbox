package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.CrawlerManager;
import ru.ulmc.crawler.client.TaskType;
import ru.ulmc.crawler.client.tools.CrawlingConfig;
import ru.ulmc.crawler.entity.Loot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DownloadTask implements Runnable {
    private final BlockingQueue<Loot> lootBlockingQueue;
    private final File dir;
    private int maxDownloads;
    private static final AtomicInteger filesDownloaded = new AtomicInteger();

    public DownloadTask(BlockingQueue<Loot> outputQueue,
                        CrawlingConfig config) {
        this.lootBlockingQueue = outputQueue;
        dir = new File(config.getExportPath());
        this.maxDownloads = config.getMaxDownloads();
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

    private void process(Loot loot) throws InterruptedException, IOException {
        log.trace("Looting {}", loot);
        URL url = new URL(loot.getUri());
        String filename = dir.getAbsolutePath() + "\\" + loot.getLootName();
        if (exists(filename)) {
            log.debug("File exists: {}", filename);
            return;
        }
        try (ReadableByteChannel channel = Channels.newChannel(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(filename);
             FileChannel fileChannel = fileOutputStream.getChannel()) {

            fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
        } catch (IOException ex) {
            log.trace("File not found or other errors {} {}", url, ex.getMessage());
        }
    }

    private boolean exists(String filename) {
        return new File(filename).exists();
    }


    @Override
    public void run() {
     //   Thread.currentThread().setName("DownloadTask");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tryToStop();
                Loot loot = lootBlockingQueue.take();
                process(loot);
            } catch (InterruptedException e) {
                log.info("DownloadTask was interrupted");
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error("DownloadTask stopped with exception", e);
                CrawlerManager.getInstance().stop();
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof InterruptedException) {
                    log.info("DownloadTask was interrupted");
                    Thread.currentThread().interrupt();
                    return;
                }
                CrawlerManager.getInstance().stop();
                log.error("DownloadTask stopped with unknown exception", e);
                throw e;
            }
        }
    }

    private void tryToStop() {
        if (filesDownloaded.incrementAndGet() >= maxDownloads) {
            CrawlerManager.getInstance().askForStop(TaskType.EXTRACT, TaskType.PROCESS, TaskType.DOWNLOAD);
        }
    }
}
