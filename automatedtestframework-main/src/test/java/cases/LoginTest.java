package cases;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pages.ATFLoginPage;
import utils.ATFTestDetails;


public class LoginTest extends ATFBaseTest {
    @BeforeMethod(alwaysRun = true)
    public void beforeTest(Method method) {
        launchBrowser(method);
    }


    @ATFTestDetails(user = "RAJU", date = "10/23/2019", displayName = "Login Into Application")
    @Test(groups = {REGRESSION}, description = "Test login into application")
    public void testLoginIntoApplication() {
        atfLoginPage = navigateToPage(ATFLoginPage.class);
        atfLoginPage.doSearch("LoginPage.USERNAME");
        AssertElementVisible(GetWebController(), ATFLoginPage.SEARCH_INPUT, "Input box");
    }
}