# hybridframework — Naukri Resume Auto-Update

A Selenium + TestNG + Page Object Model framework that logs into naukri.com
and re-uploads your resume on the profile page, driven by Excel test data
(Apache POI) and reported via ChainTest (with a JUnit-XML backup for
Azure DevOps).

This project intentionally runs **once per invocation** — no in-process
polling loop. You schedule the daily run yourself (cron / Task Scheduler),
which is both the safest and the most effective cadence: recruiter search
ranking rewards genuine daily activity, not high-frequency identical
re-uploads, and job portals' terms of service prohibit high-frequency
automated access.

## 1. Prerequisites

- JDK 11+
- Maven 3.6+
- Chrome, Firefox, or Edge installed (driver binaries are auto-managed by
  WebDriverManager — no manual driver downloads needed)

## 2. Credentials — do NOT hardcode them

`testdata/testdata.example.xlsx` (sheet `login`) ships with placeholder cells:

| email                  | password                 |
|------------------------|--------------------------|
| `${naukri.email}`      | `${naukri.password}`     |

`ExcelReader` resolves any `${propertyName}` cell against JVM system
properties first, then environment variables, at run time. Populate them
one of two ways:

**A) Command-line system properties (recommended for scheduled runs):**
```bash
mvn test -Dnaukri.email="you@example.com" -Dnaukri.password="yourRealPassword"
```

**B) Environment variables** (set `NAUKRI_EMAIL` / `NAUKRI_PASSWORD` in your
OS or scheduler and adjust the placeholder names in testdata.xlsx to match,
e.g. `${NAUKRI_EMAIL}`).

Never replace the placeholders with plaintext credentials directly in the
spreadsheet if this repo is ever pushed to source control or shared.

## 3. Configure `config/config.properties`

Set at minimum:
- `browser` — chrome | firefox | edge
- `headless` — `true` for unattended scheduled runs
- `resumeFilePath` — absolute path to the PDF you want uploaded

## 4. Run it once, manually

```bash
mvn clean test
```

- HTML report: `reports/chaintest/NaukriAutomationReport.html`
- JUnit XML backup (for Azure DevOps "Publish Test Results" task):
  `target/surefire-reports/*.xml`
- Failure screenshots: `screenshots/` (also embedded directly in the
  ChainTest HTML report)

## 5. Schedule the daily run

**Linux/macOS (cron)** — runs every day at 9:00 AM:
```
0 9 * * * cd /path/to/hybridframework && /usr/bin/mvn -q test -Dnaukri.email="you@example.com" -Dnaukri.password="yourPassword" >> logs/cron.log 2>&1
```

**Windows (Task Scheduler)**:
- Program/script: `mvn.cmd`
- Arguments: `test -Dnaukri.email=you@example.com -Dnaukri.password=yourPassword`
- Start in: `C:\path\to\hybridframework`
- Trigger: Daily, at your preferred time

Store the credentials in your scheduler's secret/credential store rather
than in plain text in the scheduled task definition where possible
(e.g. Windows Credential Manager, a `.env` file excluded from version
control and sourced by a wrapper script, or a CI secrets store if you run
this from a pipeline instead of a local machine).

## 6. Before relying on this against the live site

`LoginPage` and `ProfilePage` locators are based on naukri.com's commonly
documented DOM structure at the time of writing. Since it's a live
third-party site, **open the login flyout and profile page in your
browser's DevTools and confirm the `By` locators in
`src/main/java/pages/LoginPage.java` and `ProfilePage.java` still match**
before your first scheduled run — a broken locator is far preferable to
discover in a manual dry run than in an unattended 9 AM cron job.

## Project structure

```
hybridframework/
├── config/config.properties
├── testdata/testdata.example.xlsx    (sheet: "login", placeholders only)
├── xmlfiles/testng.xml
├── src/main/java/
│   ├── base/BaseClass.java           (ThreadLocal driver lifecycle)
│   ├── base/BasePage.java            (reusable element actions)
│   ├── factory/BrowserFactory.java   (Chrome/Firefox/Edge launch)
│   ├── pages/LoginPage.java
│   ├── pages/ProfilePage.java
│   ├── helper/ConfigReader.java
│   ├── helper/ExcelReader.java
│   ├── helper/DataProviders.java
│   ├── helper/Utility.java
│   └── listeners/ReportListener.java (ChainTest + screenshot-on-failure)
├── src/test/java/testcases/NaukriAutomationTest.java
├── src/test/resources/chaintest.properties
├── reports/chaintest/                (generated)
├── screenshots/                      (generated)
└── pom.xml
```
