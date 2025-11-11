package sandeep.webcrawler;

import java.util.List;

public class CrawlStatusResponse {
    private String status;        // Possible values: "Crawling", "Completed", "Idle"
    private List<String> results; // List of URLs crawled so far

    // No-argument constructor for frameworks like Jackson
    public CrawlStatusResponse() {}

    // Parameterized constructor
    public CrawlStatusResponse(String status, List<String> results) {
        this.status = status;
        this.results = results;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Setter for status
    public void setStatus(String status) {
        this.status = status;
    }

    // Getter for results
    public List<String> getResults() {
        return results;
    }

    // Setter for results
    public void setResults(List<String> results) {
        this.results = results;
    }
}
