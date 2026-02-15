package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.MetaScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class MetaScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.metacareers.com/jobsearch?roles[0]=Internship&teams[0]=Software%20Engineering&teams[1]=Product%20Management&teams[2]=Enterprise%20Engineering&teams[3]=Internship%20-%20Business&teams[4]=Infrastructure");
            Thread.sleep(8000);

            // Check role=list elements
            List<WebElement> lists = driver.findElements(By.cssSelector("[role='list']"));
            System.out.println("=== role='list' elements: " + lists.size() + " ===");
            for (int i = 0; i < lists.size(); i++) {
                List<WebElement> children = lists.get(i).findElements(By.cssSelector(":scope > *"));
                String text = lists.get(i).getText().replace("\n", " | ");
                if (text.length() > 150) text = text.substring(0, 150) + "...";
                System.out.println("[" + i + "] children=" + children.size() + " text=" + text);
            }

            // Check all <a> tags
            List<WebElement> allLinks = driver.findElements(By.tagName("a"));
            System.out.println("\n=== All <a> tags: " + allLinks.size() + " ===");
            for (int i = 0; i < Math.min(20, allLinks.size()); i++) {
                String href = allLinks.get(i).getAttribute("href");
                String text = allLinks.get(i).getText().replace("\n", " | ").trim();
                if (!text.isEmpty() || (href != null && href.contains("job"))) {
                    System.out.println("  a[" + i + "] href=" + href + " text=" + text);
                }
            }

            // Check role=listitem
            List<WebElement> listItems = driver.findElements(By.cssSelector("[role='listitem']"));
            System.out.println("\n=== role='listitem': " + listItems.size() + " ===");

            // Check for divs that contain job title text
            List<WebElement> asicDivs = driver.findElements(By.xpath("//*[contains(text(), 'ASIC Engineer')]"));
            System.out.println("\n=== Elements containing 'ASIC Engineer': " + asicDivs.size() + " ===");
            for (int i = 0; i < Math.min(3, asicDivs.size()); i++) {
                WebElement el = asicDivs.get(i);
                System.out.println("  tag=" + el.getTagName() + " class=" + el.getAttribute("class"));
                // Get parent info
                WebElement p = el.findElement(By.xpath("./.."));
                WebElement gp = p.findElement(By.xpath("./.."));
                System.out.println("    parent: tag=" + p.getTagName() + " class=" + p.getAttribute("class"));
                System.out.println("    grandparent: tag=" + gp.getTagName() + " class=" + gp.getAttribute("class"));
                String gpHtml = gp.getAttribute("outerHTML");
                if (gpHtml.length() > 800) gpHtml = gpHtml.substring(0, 800);
                System.out.println("    gpHTML: " + gpHtml.replace("\n", " "));
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        MetaScraper scraper = new MetaScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Meta jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
