package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.CiscoScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

class CiscoScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://careers.cisco.com/global/en/search-results");
            Thread.sleep(8000);

            // Check Country or Region accordion
            List<WebElement> countryAccordion = driver.findElements(By.id("CountryorRegionAccordion"));
            System.out.println("=== #CountryorRegionAccordion found: " + countryAccordion.size() + " ===");
            if (!countryAccordion.isEmpty()) {
                String html = countryAccordion.get(0).getAttribute("innerHTML");
                if (html.length() > 1500) html = html.substring(0, 1500);
                System.out.println("innerHTML: " + html.replace("\n", " "));
            }

            // Check the span selector from the task
            List<WebElement> countrySpan = driver.findElements(
                    By.cssSelector("#CountryorRegionAccordion > span.phw-mb-0.phw-g-i-cPM5As"));
            System.out.println("\n=== Country span found: " + countrySpan.size() + " ===");
            if (!countrySpan.isEmpty()) {
                System.out.println("Text: " + countrySpan.get(0).getText());
            }

            // Check Experience Level accordion
            List<WebElement> expAccordion = driver.findElements(By.id("ExperienceLevelAccordion"));
            System.out.println("\n=== #ExperienceLevelAccordion found: " + expAccordion.size() + " ===");
            if (!expAccordion.isEmpty()) {
                String html = expAccordion.get(0).getAttribute("innerHTML");
                if (html.length() > 1500) html = html.substring(0, 1500);
                System.out.println("innerHTML: " + html.replace("\n", " "));
            }

            // Check all checkboxes or inputs on the page
            List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
            System.out.println("\n=== Checkboxes: " + checkboxes.size() + " ===");
            for (int i = 0; i < Math.min(30, checkboxes.size()); i++) {
                String id = checkboxes.get(i).getAttribute("id");
                String name = checkboxes.get(i).getAttribute("name");
                String value = checkboxes.get(i).getAttribute("value");
                String ariaLabel = checkboxes.get(i).getAttribute("aria-label");
                System.out.printf("  checkbox[%d] id='%s' name='%s' value='%s' aria-label='%s'%n",
                        i, id, name, value, ariaLabel);
            }

            // Look for clickable filter items (labels, spans, buttons inside accordions)
            List<WebElement> filterLabels = driver.findElements(
                    By.cssSelector("[id*='Accordion'] label, [id*='Accordion'] button, [id*='Accordion'] span[role='checkbox']"));
            System.out.println("\n=== Filter labels/buttons inside accordions: " + filterLabels.size() + " ===");
            for (int i = 0; i < Math.min(20, filterLabels.size()); i++) {
                String tag = filterLabels.get(i).getTagName();
                String text = filterLabels.get(i).getText().trim();
                String cls = filterLabels.get(i).getAttribute("class");
                String role = filterLabels.get(i).getAttribute("role");
                if (!text.isEmpty()) {
                    System.out.printf("  [%d] tag='%s' role='%s' text='%s' class='%s'%n", i, tag, role, text, cls);
                }
            }

            // Look for job result cards
            List<WebElement> jobCards = driver.findElements(By.cssSelector("[data-ph-at-id='jobs-list'] li, .jobs-list-item, [class*='job-tile'], [data-ph-at-id='job-link']"));
            System.out.println("\n=== Job cards found: " + jobCards.size() + " ===");
            for (int i = 0; i < Math.min(3, jobCards.size()); i++) {
                String text = jobCards.get(i).getText().replace("\n", " | ");
                if (text.length() > 200) text = text.substring(0, 200);
                System.out.println("  [" + i + "] " + text);
            }

            // Alternative: look for any list of results
            List<WebElement> searchResults = driver.findElements(By.cssSelector("[class*='search-result'], [class*='job-list'], [class*='JobList']"));
            System.out.println("\n=== Search result containers: " + searchResults.size() + " ===");

            // Try phenom-specific selectors
            List<WebElement> phenomJobs = driver.findElements(By.cssSelector("ph-job-card, [ph-teaser], a[data-ph-at-id='job-link']"));
            System.out.println("\n=== Phenom job elements: " + phenomJobs.size() + " ===");

            // Dump all <a> tags that might be job links
            List<WebElement> allLinks = driver.findElements(By.cssSelector("a[href*='/global/en/job/']"));
            System.out.println("\n=== Job links (a[href*='/global/en/job/']): " + allLinks.size() + " ===");
            for (int i = 0; i < Math.min(5, allLinks.size()); i++) {
                String href = allLinks.get(i).getAttribute("href");
                String text = allLinks.get(i).getText().replace("\n", " | ").trim();
                if (text.length() > 200) text = text.substring(0, 200);
                System.out.printf("  a[%d] href='%s' text='%s'%n", i, href, text);
            }

            // Check for "Show more" or pagination
            List<WebElement> showMore = driver.findElements(By.cssSelector("[class*='show-more'], [class*='ShowMore'], [ph-teaser*='show-more'], button[class*='more']"));
            System.out.println("\n=== Show more buttons: " + showMore.size() + " ===");

            List<WebElement> pagination = driver.findElements(By.cssSelector("[class*='paginat'], [class*='Paginat'], nav[aria-label*='page']"));
            System.out.println("=== Pagination elements: " + pagination.size() + " ===");

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        CiscoScraper scraper = new CiscoScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Cisco jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
