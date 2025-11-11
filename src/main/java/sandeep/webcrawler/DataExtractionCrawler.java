package sandeep.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class DataExtractionCrawler {
    
    private Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
    private List<PageData> extractedData = Collections.synchronizedList(new ArrayList<>());
    private ExecutorService executorService;
    private String seedDomain;
    private static final int MAX_PAGES = 50;
    
    public DataExtractionCrawler(String seedUrl, int numThreads) {
        this.executorService = Executors.newFixedThreadPool(numThreads);
        try {
            this.seedDomain = new java.net.URI(seedUrl).getHost();
        } catch (Exception e) {
            this.seedDomain = "";
        }
    }
    
    public void crawl(String startUrl) {
        Queue<String> urlQueue = new ConcurrentLinkedQueue<>();
        urlQueue.offer(startUrl);
        visitedUrls.add(startUrl);
        
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            futures.add(executorService.submit(() -> processUrls(urlQueue)));
        }
        
        try {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void processUrls(Queue<String> urlQueue) {
        while (visitedUrls.size() < MAX_PAGES) {
            String url = urlQueue.poll();
            if (url == null) break;
            
            try {
                Document document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(5000)
                        .get();
                
                PageData data = new PageData();
                data.url = url;
                data.title = document.title();
                data.description = extractMetaDescription(document);
                data.headings = extractHeadings(document);
                data.links = extractLinks(document);
                
                extractedData.add(data);
                System.out.println("Extracted: " + url);
                
                Elements allLinks = document.select("a[href]");
                for (Element link : allLinks) {
                    String absUrl = link.absUrl("href");
                    if (!visitedUrls.contains(absUrl) && visitedUrls.size() < MAX_PAGES) {
                        visitedUrls.add(absUrl);
                        urlQueue.offer(absUrl);
                    }
                }
                
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    private String extractMetaDescription(Document doc) {
        Element metaDesc = doc.selectFirst("meta[name=description]");
        return metaDesc != null ? metaDesc.attr("content") : "N/A";
    }
    
    private List<String> extractHeadings(Document doc) {
        List<String> headings = new ArrayList<>();
        doc.select("h1, h2, h3").forEach(h -> headings.add(h.text()));
        return headings;
    }
    
    private List<String> extractLinks(Document doc) {
        List<String> links = new ArrayList<>();
        doc.select("a[href]").forEach(link -> links.add(link.absUrl("href")));
        return links;
    }
    
    public void exportToCSV(String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("URL,Title,Description,Headings\n");
            
            for (PageData data : extractedData) {
                String headingsStr = String.join("|", data.headings);
                String row = String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escape(data.url),
                        escape(data.title),
                        escape(data.description),
                        escape(headingsStr));
                writer.write(row);
            }
        }
        System.out.println("Data exported to " + filename);
    }
    
    private String escape(String str) {
        return str.replace("\"", "\\\"").replace("\n", "\\n");
    }
    
    private static class PageData {
        String url;
        String title;
        String description;
        List<String> headings;
        List<String> links;
    }
    
    public static void main(String[] args) throws IOException {
        DataExtractionCrawler crawler = new DataExtractionCrawler("https://example.com", 4);
        crawler.crawl("https://example.com");
        crawler.exportToCSV("crawled_data.csv");
    }
}
