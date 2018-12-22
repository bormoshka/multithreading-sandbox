package ru.ulmc.crawler.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.UrlEncoded;
import ru.ulmc.crawler.client.loot.ImageSnooper;
import ru.ulmc.crawler.client.tasks.DownloadTask;
import ru.ulmc.crawler.client.tasks.MonitoringTask;
import ru.ulmc.crawler.client.tasks.PageProcessingTask;
import ru.ulmc.crawler.client.tasks.UriExtractingTask;
import ru.ulmc.crawler.client.tools.BlackList;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.StaticPage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static ru.ulmc.crawler.client.TaskType.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CrawlerManager {
    private static final CrawlerManager instance = new CrawlerManager();
    private static final int URI_TASK_PARALLELISM = 2;
    private static final int PAGE_TASK_PARALLELISM = 2;
    private static final int DOWNLOAD_TASK_PARALLELISM = 6;

    private final Map<TaskType, List<Future<?>>> futureMap = new ConcurrentHashMap<>();
    private final BlockingQueue<StaticPage> pageBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Loot> lootBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> uriBlockingQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = newFixedThreadPool(
            URI_TASK_PARALLELISM
                    + PAGE_TASK_PARALLELISM
                    + DOWNLOAD_TASK_PARALLELISM);
    @Getter
    private volatile String keyword;

    public static CrawlerManager getInstance() {
        return instance;
    }

    public void askForStop(TaskType... stopTasks) {
        for (TaskType stopTask : stopTasks) {
            futureMap.get(stopTask).forEach(future -> future.cancel(true));
        }
    }

    public void stop() {
        CrawlerManager.getInstance().askForStop(TaskType.values());
        executor.shutdownNow();
    }

    public void start(String entryPoint, String keyword, Set<String> extensions, String path)
            throws IOException, URISyntaxException {
        BlackList blackList = new BlackList();
        blackList.load();
        this.keyword = keyword;
        startTask(1, MONITORING, getMonitoringTask());
        startTask(URI_TASK_PARALLELISM, EXTRACT, getUriTask(blackList));
        startTask(PAGE_TASK_PARALLELISM, PROCESS, getPageTask(extensions));
        startTask(DOWNLOAD_TASK_PARALLELISM, DOWNLOAD, getDownloadTask(path));

        uriBlockingQueue.offer(entryPoint + UrlEncoded.encodeString(keyword));
        log.trace("Put uri to queue");
    }

    private MonitoringTask getMonitoringTask() {
        return new MonitoringTask(pageBlockingQueue, lootBlockingQueue, uriBlockingQueue);
    }

    private void startTask(int parallelism, TaskType taskType, Runnable task) {
        for (int i = 0; i < parallelism; i++) {
            futureMap.computeIfAbsent(taskType, k -> new ArrayList<>())
                    .add(executor.submit(task));
        }
    }

    private DownloadTask getDownloadTask(String path) {
        return new DownloadTask(lootBlockingQueue, path, 1000);
    }

    private UriExtractingTask getUriTask(BlackList blackList) {
        return new UriExtractingTask(uriBlockingQueue, pageBlockingQueue, blackList);
    }

    private PageProcessingTask getPageTask(Set<String> extensions) {
        return new PageProcessingTask(pageBlockingQueue, lootBlockingQueue, singleton(new ImageSnooper(extensions)));
    }
}
