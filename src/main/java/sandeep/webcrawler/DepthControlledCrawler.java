package sandeep.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DepthControlledCrawler {
    
    private static final int MAX_DEPTH = 2;
    private Set<String> visitedUrls = new HashSet<>();
    
    public void crawl(String url, int depth) {
        if (depth > MAX_DEPTH || visitedUrls.contains(url)) {
            return;
        }
        
        try {
            visitedUrls.add(url);
            System.out.println("[Depth: " + depth + "] " + url);
            
            Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get();
            
            System.out.println("  Title: " + document.title());
            
            Elements links = document.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                crawl(nextUrl, depth + 1);
            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        DepthControlledCrawler crawler = new DepthControlledCrawler();
        crawler.crawl("https://example.com", 0);
    }
}
