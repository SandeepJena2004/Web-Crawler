package sandeep.webcrawler;

public class CrawlRequest {
    private String url;
    private int maxPages;
    private int numThreads;
    private int maxDepth;

    // Getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getMaxPages() { return maxPages; }
    public void setMaxPages(int maxPages) { this.maxPages = maxPages; }

    public int getNumThreads() { return numThreads; }
    public void setNumThreads(int numThreads) { this.numThreads = numThreads; }

    public int getMaxDepth() { return maxDepth; }
    public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }
}
