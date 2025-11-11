package sandeep.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ConcurrentWebCrawler {

    private final int maxDepth;
    private final int numThreads;
    private final int maxPages;
    private static final int CRAWL_DELAY_MS = 500;
    private static final int CONNECTION_TIMEOUT_MS = 10000;

    private final Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
    private final Queue<CrawlTask> taskQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService;
    private final String seedDomain;
    private final AtomicInteger processingCount = new AtomicInteger(0);
    private volatile boolean shouldStop = false;

    // Listener to report crawled URLs externally
    private Consumer<String> resultListener;

    public ConcurrentWebCrawler(String seedUrl, int maxPages, int numThreads, int maxDepth) {
        this.maxDepth = maxDepth;
        this.numThreads = numThreads;
        this.maxPages = maxPages;
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.seedDomain = extractDomain(seedUrl);
    }

    public void setResultListener(Consumer<String> listener) {
        this.resultListener = listener;
    }

    public void stop() {
        shouldStop = true;
        executorService.shutdownNow();
    }

    private String extractDomain(String url) {
        try {
            String host = new URI(url).getHost();
            return host != null ? host.toLowerCase() : "";
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private boolean isSameDomain(String url) {
        try {
            String domain = extractDomain(url);
            if (domain.isEmpty() || seedDomain.isEmpty()) {
                return false;
            }
            return domain.contains(seedDomain) || seedDomain.contains(domain);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        url = url.toLowerCase();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }
        if (url.contains("#") || url.contains(".pdf") || url.contains(".jpg") ||
                url.contains(".png") || url.contains(".zip")) {
            return false;
        }
        return true;
    }

    public void crawl(String startUrl) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("WEB CRAWLER STARTED");
        System.out.println("=".repeat(70));
        System.out.println("Target URL: " + startUrl);
        System.out.println("Seed Domain: " + seedDomain);
        System.out.println("Max Pages: " + maxPages);
        System.out.println("Max Depth: " + maxDepth);
        System.out.println("Threads: " + numThreads);
        System.out.println("=".repeat(70) + "\n");

        taskQueue.offer(new CrawlTask(startUrl, 0));
        visitedUrls.add(startUrl);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            futures.add(executorService.submit(this::crawlWorker));
        }

        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                System.out.println("\n⚠️  Timeout reached - shutting down...");
                shouldStop = true;
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Crawler interrupted: " + e.getMessage());
            shouldStop = true;
            executorService.shutdownNow();
        }

        printResults();
    }

    private void printResults() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CRAWLING COMPLETE");
        System.out.println("=".repeat(70));
        System.out.println("Total URLs discovered: " + visitedUrls.size());
        System.out.println("URLs remaining in queue: " + taskQueue.size());
        System.out.println("=".repeat(70) + "\n");
    }

    private void crawlWorker() {
        while (!shouldStop && visitedUrls.size() < maxPages) {
            if (taskQueue.isEmpty()) {
                if (processingCount.get() == 0) {
                    shouldStop = true;
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            if (visitedUrls.size() >= maxPages) {
                shouldStop = true;
                break;
            }

            CrawlTask task = taskQueue.poll();

            if (task == null) continue;

            processingCount.incrementAndGet();
            try {
                crawlUrl(task);
                try {
                    Thread.sleep(CRAWL_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } finally {
                processingCount.decrementAndGet();
            }
        }
    }

    private void crawlUrl(CrawlTask task) {
        if (task.depth > maxDepth || visitedUrls.size() >= maxPages) {
            return;
        }

        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Crawling (" + task.depth + "): " + task.url);

        try {
            Document document = Jsoup.connect(task.url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(CONNECTION_TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();

            String title = document.title();
            if (title.isEmpty()) {
                title = "[No Title]";
            }
            System.out.println("  ✓ Title: " + title);

            // Report this URL externally if listener is set
            if (resultListener != null) {
                resultListener.accept(task.url);
            }

            if (task.depth < maxDepth && visitedUrls.size() < maxPages) {
                Elements links = document.select("a[href]");
                int newLinksAdded = 0;

                for (Element link : links) {
                    String absoluteUrl = link.absUrl("href");

                    if (isValidUrl(absoluteUrl) &&
                            !visitedUrls.contains(absoluteUrl) &&
                            isSameDomain(absoluteUrl) &&
                            visitedUrls.size() < maxPages) {

                        visitedUrls.add(absoluteUrl);
                        taskQueue.offer(new CrawlTask(absoluteUrl, task.depth + 1));
                        newLinksAdded++;
                    }
                }

                System.out.println("  ✓ Found " + links.size() + " links, added " + newLinksAdded + " to queue");
            }
        } catch (IOException e) {
            System.err.println("  ✗ Error: " + e.getClass().getSimpleName() + " - " + task.url);
        }
    }

    private static class CrawlTask {
        String url;
        int depth;

        CrawlTask(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}
