package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.SalesforceScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class SalesforceScraperTest {

    @Test
    void debugFilterStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://careers.salesforce.com/en/jobs/");
            Thread.sleep(5000);

            // Find all form elements (select, input, checkbox, etc.)
            List<WebElement> selects = driver.findElements(By.cssSelector("select"));
            System.out.println("=== SELECT elements: " + selects.size() + " ===");
            for (int i = 0; i < selects.size(); i++) {
                String id = selects.get(i).getAttribute("id");
                String name = selects.get(i).getAttribute("name");
                String cls = selects.get(i).getAttribute("class");
                System.out.printf("  select[%d] id='%s' name='%s' class='%s'%n", i, id, name, cls);

                // Show options
                List<WebElement> opts = selects.get(i).findElements(By.cssSelector("option"));
                for (int j = 0; j < Math.min(10, opts.size()); j++) {
                    String val = opts.get(j).getAttribute("value");
                    String text = opts.get(j).getText().trim();
                    System.out.printf("    option[%d] value='%s' text='%s'%n", j, val, text);
                }
            }

            // Find filter labels
            List<WebElement> labels = driver.findElements(By.cssSelector("label"));
            System.out.println("\n=== LABEL elements: " + labels.size() + " ===");
            for (int i = 0; i < Math.min(20, labels.size()); i++) {
                String text = labels.get(i).getText().trim();
                String forAttr = labels.get(i).getAttribute("for");
                if (!text.isEmpty()) {
                    System.out.printf("  label[%d] for='%s' text='%s'%n", i, forAttr, text);
                }
            }

            // Find forms
            List<WebElement> forms = driver.findElements(By.cssSelector("form"));
            System.out.println("\n=== FORM elements: " + forms.size() + " ===");
            for (int i = 0; i < forms.size(); i++) {
                String action = forms.get(i).getAttribute("action");
                String id = forms.get(i).getAttribute("id");
                String cls = forms.get(i).getAttribute("class");
                System.out.printf("  form[%d] id='%s' action='%s' class='%s'%n", i, id, action, cls);
            }

            // Find inputs
            List<WebElement> inputs = driver.findElements(By.cssSelector("input"));
            System.out.println("\n=== INPUT elements: " + inputs.size() + " ===");
            for (int i = 0; i < Math.min(20, inputs.size()); i++) {
                String type = inputs.get(i).getAttribute("type");
                String name = inputs.get(i).getAttribute("name");
                String id = inputs.get(i).getAttribute("id");
                String placeholder = inputs.get(i).getAttribute("placeholder");
                System.out.printf("  input[%d] type='%s' name='%s' id='%s' placeholder='%s'%n",
                        i, type, name, id, placeholder);
            }

            // Find buttons
            List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
            System.out.println("\n=== BUTTON elements: " + buttons.size() + " ===");
            for (int i = 0; i < Math.min(15, buttons.size()); i++) {
                String text = buttons.get(i).getText().trim();
                String type = buttons.get(i).getAttribute("type");
                String cls = buttons.get(i).getAttribute("class");
                if (!text.isEmpty()) {
                    System.out.printf("  button[%d] type='%s' class='%s' text='%s'%n", i, type, cls, text);
                }
            }

            // Check total job count shown on page
            String bodyText = driver.findElement(By.tagName("body")).getText();
            // Look for "Displaying X to Y of Z" text
            int displayIdx = bodyText.indexOf("Displaying");
            if (displayIdx >= 0) {
                System.out.println("\n=== " + bodyText.substring(displayIdx, Math.min(displayIdx + 60, bodyText.length())) + " ===");
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://careers.salesforce.com/en/jobs/?search=&country=Canada&jobtype=Intern&pagesize=20#results");
            Thread.sleep(8000);

            // Check for card elements
            List<WebElement> cards = driver.findElements(By.cssSelector("div.card"));
            System.out.println("=== div.card found: " + cards.size() + " ===");
            for (int i = 0; i < Math.min(3, cards.size()); i++) {
                String html = cards.get(i).getAttribute("innerHTML");
                if (html.length() > 800) html = html.substring(0, 800);
                System.out.println("\n[card " + i + "] innerHTML: " + html.replace("\n", " "));
            }

            // Check location-related elements
            List<WebElement> locationEls = driver.findElements(
                    By.cssSelector("[class*='location'], [class*='Location']"));
            System.out.println("\n=== Location elements: " + locationEls.size() + " ===");
            for (int i = 0; i < Math.min(5, locationEls.size()); i++) {
                String text = locationEls.get(i).getText().trim();
                String cls = locationEls.get(i).getAttribute("class");
                String tag = locationEls.get(i).getTagName();
                if (!text.isEmpty()) {
                    System.out.printf("  %s[%d] class='%s' text='%s'%n", tag, i, cls, text);
                }
            }

            // Check pagination
            List<WebElement> pagination = driver.findElements(By.cssSelector("ul.pagination li"));
            System.out.println("\n=== Pagination items: " + pagination.size() + " ===");
            for (int i = 0; i < pagination.size(); i++) {
                String text = pagination.get(i).getText().trim();
                String cls = pagination.get(i).getAttribute("class");
                System.out.printf("  li[%d] class='%s' text='%s'%n", i, cls, text);
            }

            // Count and display result count
            String bodyText = driver.findElement(By.tagName("body")).getText();
            int displayIdx = bodyText.indexOf("Displaying");
            if (displayIdx >= 0) {
                System.out.println("\n=== " + bodyText.substring(displayIdx, Math.min(displayIdx + 60, bodyText.length())) + " ===");
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        SalesforceScraper scraper = new SalesforceScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Salesforce jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
