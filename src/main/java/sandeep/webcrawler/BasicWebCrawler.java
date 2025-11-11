package sandeep.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BasicWebCrawler {
    
    private Set<String> visitedUrls = new HashSet<>();
    
    public void crawl(String url) {
        if (visitedUrls.contains(url)) {
            return;
        }
        
        try {
            visitedUrls.add(url);
            System.out.println("Crawling: " + url);
            
            Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get();
            
            Elements links = document.select("a[href]");
            System.out.println("Found " + links.size() + " links");
            
            for (Element link : links) {
                String absoluteUrl = link.absUrl("href");
                System.out.println("  - " + absoluteUrl);
            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        BasicWebCrawler crawler = new BasicWebCrawler();
        crawler.crawl("https://example.com");
    }
}
