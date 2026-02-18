package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.DatadogScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class DatadogScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://careers.datadoghq.com/all-jobs/?location_Americas%5B0%5D=Canada");
            Thread.sleep(8000);

            System.out.println("=== Page title: " + driver.getTitle() + " ===");

            // Check for job links
            List<WebElement> jobLinks = driver.findElements(By.cssSelector("a[href*='/detail/']"));
            System.out.println("\n=== a[href*='/detail/']: " + jobLinks.size() + " ===");
            for (int i = 0; i < Math.min(5, jobLinks.size()); i++) {
                String href = jobLinks.get(i).getAttribute("href");
                String text = jobLinks.get(i).getText().trim().replace("\n", " | ");
                String cls = jobLinks.get(i).getAttribute("class");
                if (text.length() > 200) text = text.substring(0, 200);
                System.out.printf("  a[%d] class='%s' href='%s' text='%s'%n", i, cls, href, text);
            }

            // Try broader selectors
            List<WebElement> allLinks = driver.findElements(By.cssSelector("a[href*='job'], a[href*='career'], a[href*='position']"));
            System.out.println("\n=== Job-related links: " + allLinks.size() + " ===");
            for (int i = 0; i < Math.min(5, allLinks.size()); i++) {
                String href = allLinks.get(i).getAttribute("href");
                String text = allLinks.get(i).getText().trim();
                if (!text.isEmpty() && text.length() < 200) {
                    System.out.printf("  a[%d] href='%s' text='%s'%n", i, href, text);
                }
            }

            // Check headings
            List<WebElement> headings = driver.findElements(By.cssSelector("h2, h3, h4, h5"));
            System.out.println("\n=== Headings: " + headings.size() + " ===");
            for (int i = 0; i < Math.min(15, headings.size()); i++) {
                String text = headings.get(i).getText().trim();
                if (!text.isEmpty()) {
                    System.out.printf("  %s text='%s'%n", headings.get(i).getTagName(), text);
                }
            }

            // Check for any list/card structure
            List<WebElement> cards = driver.findElements(By.cssSelector("[class*='card'], [class*='Card'], [class*='job'], [class*='Job'], [class*='listing'], [class*='Listing']"));
            System.out.println("\n=== Card/job/listing elements: " + cards.size() + " ===");
            for (int i = 0; i < Math.min(5, cards.size()); i++) {
                String tag = cards.get(i).getTagName();
                String cls = cards.get(i).getAttribute("class");
                String text = cards.get(i).getText().trim().replace("\n", " | ");
                if (text.length() > 200) text = text.substring(0, 200);
                System.out.printf("  %s[%d] class='%s' text='%s'%n", tag, i, cls, text);
            }

            // Body text snippet
            String bodyText = driver.findElement(By.tagName("body")).getText();
            if (bodyText.length() > 3000) bodyText = bodyText.substring(0, 3000);
            System.out.println("\n=== Body text (first 3000 chars): ===\n" + bodyText);

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        DatadogScraper scraper = new DatadogScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Datadog jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
