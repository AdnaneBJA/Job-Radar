package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class GoogleScraperTest {

    @Test
    void debugCardStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.google.com/about/careers/applications/jobs/results?hl=en-US&q=Software&target_level=INTERN_AND_APPRENTICE");
            Thread.sleep(5000);

            List<WebElement> cards = driver.findElements(By.cssSelector("li.lLd3Je"));
            if (!cards.isEmpty()) {
                // Print inner HTML of first card
                String html = cards.get(0).getAttribute("innerHTML");
                System.out.println("=== First card innerHTML ===");
                System.out.println(html);

                System.out.println("\n=== Spans in first card ===");
                List<WebElement> spans = cards.get(0).findElements(By.cssSelector("span"));
                for (int i = 0; i < spans.size(); i++) {
                    String cls = spans.get(i).getAttribute("class");
                    String txt = spans.get(i).getText().trim();
                    if (!txt.isEmpty()) {
                        System.out.printf("  span[%d] class='%s' text='%s'%n", i, cls, txt);
                    }
                }
            }
        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        GoogleScraper scraper = new GoogleScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Google jobs (PhD excluded) ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
