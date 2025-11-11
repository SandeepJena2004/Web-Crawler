package sandeep.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class DomainRestrictedCrawler {
    
    private static final int MAX_DEPTH = 2;
    private Set<String> visitedUrls = new HashSet<>();
    private String seedDomain;
    
    public DomainRestrictedCrawler(String seedUrl) throws URISyntaxException {
        this.seedDomain = extractDomain(seedUrl);
    }
    
    private String extractDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String host = uri.getHost();
        return host.startsWith("www.") ? host.substring(4) : host;
    }
    
    private boolean isSameDomain(String url) {
        try {
            String domain = extractDomain(url);
            return domain.contains(seedDomain) || seedDomain.contains(domain);
        } catch (URISyntaxException e) {
            return false;
        }
    }
    
    public void crawl(String url, int depth) {
        if (depth > MAX_DEPTH || visitedUrls.contains(url) || !isSameDomain(url)) {
            return;
        }
        
        try {
            visitedUrls.add(url);
            System.out.println("[Depth: " + depth + "] " + url);
            
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(5000)
                    .get();
            
            Elements links = document.select("a[href]");
            for (Element link : links) {
                String absoluteUrl = link.absUrl("href");
                crawl(absoluteUrl, depth + 1);
            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) throws URISyntaxException {
        DomainRestrictedCrawler crawler = new DomainRestrictedCrawler("https://example.com");
        crawler.crawl("https://example.com", 0);
    }
}
