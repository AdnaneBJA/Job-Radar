package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.RobinHoodScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class RobinHoodScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get("https://careers.robinhood.com/");
            Thread.sleep(5000);

            // Check category dropdown
            List<WebElement> categoryDropdown = driver.findElements(By.cssSelector("ul.dropdown-menu.category"));
            System.out.println("=== Category dropdown found: " + categoryDropdown.size() + " ===");

            // Check the dropdown trigger button
            List<WebElement> dropdownBtns = driver.findElements(By.cssSelector("button.dropdown-toggle, .dropdown-toggle, [data-toggle='dropdown']"));
            System.out.println("=== Dropdown toggle buttons: " + dropdownBtns.size() + " ===");
            for (int i = 0; i < Math.min(5, dropdownBtns.size()); i++) {
                String tag = dropdownBtns.get(i).getTagName();
                String text = dropdownBtns.get(i).getText().trim();
                String cls = dropdownBtns.get(i).getAttribute("class");
                System.out.printf("  [%d] tag='%s' class='%s' text='%s'%n", i, tag, cls, text);
            }

            // Click the category filter for EARLY TALENT AND INTERNSHIPS
            WebElement earlyTalent = driver.findElement(
                    By.cssSelector("li[data-filter='EARLYTALENTANDINTERNSHIPS']"));
            js.executeScript("arguments[0].click();", earlyTalent);
            System.out.println("\nClicked EARLY TALENT AND INTERNSHIPS");
            Thread.sleep(3000);

            // Click Toronto, Canada location
            WebElement toronto = driver.findElement(
                    By.cssSelector("li[data-location='Toronto, Canada']"));
            js.executeScript("arguments[0].click();", toronto);
            System.out.println("Clicked Toronto, Canada");
            Thread.sleep(3000);

            // Look at parent elements of job links
            List<WebElement> jobLinks = driver.findElements(By.cssSelector("a[href*='greenhouse.io']"));
            System.out.println("\n=== Greenhouse links: " + jobLinks.size() + " ===");
            for (int i = 0; i < Math.min(3, jobLinks.size()); i++) {
                WebElement link = jobLinks.get(i);
                WebElement parent = link.findElement(By.xpath("./.."));
                WebElement grandparent = parent.findElement(By.xpath("./.."));
                String gpHtml = grandparent.getAttribute("outerHTML");
                if (gpHtml.length() > 1000) gpHtml = gpHtml.substring(0, 1000);
                System.out.println("\n[" + i + "] grandparent outerHTML: " + gpHtml.replace("\n", " "));
                System.out.println("  parent tag=" + parent.getTagName() + " class=" + parent.getAttribute("class"));
                System.out.println("  parent text: " + parent.getText().replace("\n", " | "));
            }

            // Check if filtering actually showed/hid elements
            List<WebElement> visibleItems = driver.findElements(By.cssSelector("a[href*='greenhouse.io']"));
            int visibleCount = 0;
            for (WebElement el : visibleItems) {
                if (el.isDisplayed()) visibleCount++;
            }
            System.out.println("\n=== Visible greenhouse links: " + visibleCount + " / " + visibleItems.size() + " ===");

            // Show first 5 visible ones with parent text
            int shown = 0;
            for (WebElement el : visibleItems) {
                if (el.isDisplayed() && shown < 5) {
                    WebElement p = el.findElement(By.xpath("./.."));
                    System.out.println("  visible[" + shown + "] parent text: " + p.getText().replace("\n", " | "));
                    shown++;
                }
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        RobinHoodScraper scraper = new RobinHoodScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " RobinHood jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
