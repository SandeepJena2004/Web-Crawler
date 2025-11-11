package sandeep.webcrawler;

import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/crawl")
@CrossOrigin(origins = "http://localhost:3000") // allow React dev server
public class CrawlerController {

    private ConcurrentWebCrawler crawler;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    // Thread-safe status and result tracking
    private volatile String currentStatus = "Idle";
    private final List<String> crawledUrls = Collections.synchronizedList(new ArrayList<>());

    @PostMapping("/start")
    public String startCrawl(@RequestBody CrawlRequest request) {
        if (crawler != null) {
            return "Crawler already running!";
        }

        currentStatus = "Crawling";
        crawledUrls.clear();

        // Pass parameters to crawler - you must modify ConcurrentWebCrawler to accept them
        crawler = new ConcurrentWebCrawler(request.getUrl(), request.getMaxPages(), request.getNumThreads(), request.getMaxDepth());

        executor.submit(() -> {
            // Example: You need to update your ConcurrentWebCrawler to publish results back here
            crawler.setResultListener(url -> {
                crawledUrls.add(url);
            });

            crawler.crawl(request.getUrl());

            currentStatus = "Completed";
            crawler = null;
        });

        return "Crawling started for " + request.getUrl();
    }

    @PostMapping("/stop")
    public String stopCrawl() {
        if (crawler == null) {
            return "No crawler running!";
        }
        // Implement a stop mechanism in your crawler and call here
        crawler.stop();  // example; implement stop() in ConcurrentWebCrawler
        crawler = null;
        currentStatus = "Idle";
        return "Crawler stopped.";
    }

    @GetMapping("/status")
    public CrawlStatusResponse getStatus() {
        // Return current crawl status and cloned result list
        return new CrawlStatusResponse(currentStatus, new ArrayList<>(crawledUrls));
    }
}
