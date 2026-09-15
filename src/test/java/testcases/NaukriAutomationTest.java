package testcases;

import base.BaseClass;
import helper.ConfigReader;
import helper.DataProviders;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.ProfilePage;

import listeners.ReportListener;

import java.time.Instant;

/**
 * Daily resume-refresh automation.
 *
 * Intended usage: trigger ONE run of this suite per day via an external
 * scheduler (cron on Linux/macOS, or Windows Task Scheduler) invoking:
 *
 *     mvn test -Dnaukri.email=you@example.com -Dnaukri.password=yourPassword
 *
 * or by populating testdata/testdata.xlsx directly. This class performs a
 * single login -> navigate to profile -> re-upload resume -> verify
 * sequence and then lets @AfterClass tear the browser down. It does NOT
 * loop or poll internally - repeated execution is entirely the scheduler's
 * responsibility, at whatever interval you configure there (daily is the
 * recommended cadence for actual profile-visibility benefit).
 */
@Listeners(ReportListener.class)
public class NaukriAutomationTest extends BaseClass {

    @Test(dataProvider = "loginData", dataProviderClass = DataProviders.class,
          description = "Login to Naukri and re-upload resume on the profile page")
    public void updateResumeOnNaukri(String email, String password) {

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(email, password);

        Assert.assertTrue(loginPage.isLoginSuccessful(),
                "Login did not succeed for user: " + email);

        ProfilePage profilePage = new ProfilePage(getDriver());
        profilePage.goToProfile();

        String resumePath = ConfigReader.get("resumeFilePath");
        profilePage.uploadResume(resumePath);

        Assert.assertTrue(profilePage.isUploadSuccessful(),
                "Resume upload confirmation was not detected after uploading: " + resumePath);

        System.out.println("Resume upload verified successfully at: " + Instant.now());
    }
}
