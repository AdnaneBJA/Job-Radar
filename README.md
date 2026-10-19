# Job Radar

A Spring Boot service that watches company career pages and emails a digest when new postings appear.

## How it works

1. A scheduler runs every scraper 30 minutes after the previous run finishes. There is one scraper per company, 67 in all, each behind a common `JobScraper` interface.
2. 20 scrapers call a public API (Workday, Greenhouse, Ashby or a vendor JSON endpoint). The other 47 drive headless Chrome through Selenium.
3. The scrapers run in parallel on a bounded thread pool. Each one has a 180-second timeout, so a hung browser or a failing site can't stall or abort the run.
4. Postings are deduplicated on title, company and location, against the database and across scrapers in the same run. Only new ones are saved to PostgreSQL.
5. The digest email lists every tracked posting by company, with the new ones in a card at the top and their count in the subject line.

## Run it

Needs Java 17, PostgreSQL and an SMTP account (Gmail by default).

```bash
export DB_URL=jdbc:postgresql://localhost:5432/jobradar DB_USERNAME=... DB_PASSWORD=...
export MAIL_USERNAME=... MAIL_PASSWORD=...
./mvnw spring-boot:run
```

Set who receives the digest with `notification.email.to` in `src/main/resources/application.properties`. The thread pool size is set there too (`scraper.thread-pool.*`).

## Tests

```bash
./mvnw test
```

Unit tests use JUnit 5 and Mockito. The application context test boots on an in-memory H2 database, with the scheduler off.
