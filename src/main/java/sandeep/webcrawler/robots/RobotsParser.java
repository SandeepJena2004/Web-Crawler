package sandeep.webcrawler.robots;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RobotsParser {
    
    private Map<String, RobotRules> robotsCache = new HashMap<>();
    
    public boolean isAllowed(String url) throws Exception {
        URL parsedUrl = new URL(url);
        String domain = parsedUrl.getHost();
        String path = parsedUrl.getPath();
        
        if (!robotsCache.containsKey(domain)) {
            fetchRobotsTxt(domain);
        }
        
        RobotRules rules = robotsCache.get(domain);
        return rules == null || rules.isAllowed(path);
    }
    
    private void fetchRobotsTxt(String domain) {
        try {
            String robotsUrl = "https://" + domain + "/robots.txt";
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new URL(robotsUrl).openStream()));
            
            RobotRules rules = new RobotRules();
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("Disallow:")) {
                    String path = line.substring(9).trim();
                    rules.addDisallowedPath(path);
                } else if (line.startsWith("Crawl-delay:")) {
                    String delay = line.substring(12).trim();
                    try {
                        rules.setCrawlDelay(Integer.parseInt(delay) * 1000);
                    } catch (NumberFormatException e) {
                        // Ignore invalid values
                    }
                }
            }
            
            reader.close();
            robotsCache.put(domain, rules);
            
        } catch (Exception e) {
            System.out.println("Could not fetch robots.txt for " + domain);
            robotsCache.put(domain, null);
        }
    }
    
    private static class RobotRules {
        private Set<String> disallowedPaths = new HashSet<>();
        private int crawlDelay = 0;
        
        void addDisallowedPath(String path) {
            disallowedPaths.add(path);
        }
        
        void setCrawlDelay(int delay) {
            this.crawlDelay = delay;
        }
        
        boolean isAllowed(String path) {
            for (String disallowed : disallowedPaths) {
                if (path.startsWith(disallowed)) {
                    return false;
                }
            }
            return true;
        }
    }
}
