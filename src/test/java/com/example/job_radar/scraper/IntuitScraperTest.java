package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.IntuitScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class IntuitScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://jobs.intuit.com/search-jobs?glat=45.5285&glon=-73.6104");
            Thread.sleep(5000);

            // Check filter sections
            List<WebElement> filterButtons = driver.findElements(By.cssSelector("button.expandable-parent"));
            System.out.println("=== Filter sections: " + filterButtons.size() + " ===");
            for (int i = 0; i < filterButtons.size(); i++) {
                String text = filterButtons.get(i).getText().trim();
                String expanded = filterButtons.get(i).getAttribute("aria-expanded");
                System.out.printf("  button[%d] text='%s' expanded='%s'%n", i, text, expanded);
            }

            // Check filter checkboxes
            List<WebElement> checkboxes = driver.findElements(By.cssSelector("input.filter-checkbox"));
            System.out.println("\n=== Filter checkboxes: " + checkboxes.size() + " ===");
            for (int i = 0; i < Math.min(20, checkboxes.size()); i++) {
                String display = checkboxes.get(i).getAttribute("data-display");
                String count = checkboxes.get(i).getAttribute("data-count");
                System.out.printf("  checkbox[%d] data-display='%s' data-count='%s'%n", i, display, count);
            }

            // Check search results list
            List<WebElement> resultsList = driver.findElements(By.id("search-results-list"));
            System.out.println("\n=== #search-results-list found: " + resultsList.size() + " ===");

            if (!resultsList.isEmpty()) {
                List<WebElement> jobItems = resultsList.get(0).findElements(By.cssSelector("li"));
                System.out.println("=== Job items (li): " + jobItems.size() + " ===");
                for (int i = 0; i < Math.min(3, jobItems.size()); i++) {
                    String html = jobItems.get(i).getAttribute("innerHTML");
                    if (html.length() > 600) html = html.substring(0, 600);
                    System.out.println("\n[" + i + "] innerHTML: " + html.replace("\n", " "));

                    List<WebElement> links = jobItems.get(i).findElements(By.cssSelector("a"));
                    for (int j = 0; j < links.size(); j++) {
                        String href = links.get(j).getAttribute("href");
                        String text = links.get(j).getText().trim();
                        System.out.printf("  a[%d] href='%s' text='%s'%n", j, href, text);
                    }

                    List<WebElement> spans = jobItems.get(i).findElements(By.cssSelector("span"));
                    for (int j = 0; j < spans.size(); j++) {
                        String cls = spans.get(j).getAttribute("class");
                        String text = spans.get(j).getText().trim();
                        if (!text.isEmpty()) {
                            System.out.printf("  span[%d] class='%s' text='%s'%n", j, cls, text);
                        }
                    }
                }
            }

            // Check pagination
            List<WebElement> nextLinks = driver.findElements(By.cssSelector("a.next"));
            System.out.println("\n=== Pagination a.next found: " + nextLinks.size() + " ===");
            List<WebElement> paginationLinks = driver.findElements(By.cssSelector(".pagination a"));
            System.out.println("=== .pagination a found: " + paginationLinks.size() + " ===");
            for (int i = 0; i < paginationLinks.size(); i++) {
                String text = paginationLinks.get(i).getText().trim();
                String href = paginationLinks.get(i).getAttribute("href");
                String cls = paginationLinks.get(i).getAttribute("class");
                System.out.printf("  pagination a[%d] class='%s' text='%s' href='%s'%n", i, cls, text, href);
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        IntuitScraper scraper = new IntuitScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Intuit jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
