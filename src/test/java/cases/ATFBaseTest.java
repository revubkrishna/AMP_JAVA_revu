package cases;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import pages.ATFBasePage;
import pages.ATFLoginPage;
import pages.AmazonBase;
import pages.MMTBase;
import utils.ATFConstants;
import utils.ATFProperties;
import utils.ATFReporter;
import utils.ATFTestDetails;
import utils.ATFWebController;


public class ATFBaseTest {

    public static final String REGRESSION = "regression";
    public static final String SMOKE = "smoke";
    protected static final Logger log = LoggerFactory.getLogger(ATFBaseTest.class);
    private static ATFWebController webController;
    protected String url, testScriptName, testScriptDescription;
    protected ATFLoginPage atfLoginPage;
    public AmazonBase amazonBase;
    public MMTBase mmtBase;

    protected void launchBrowser(Method method) {
        String displayName = method.getAnnotation(ATFTestDetails.class).displayName().trim();
        testScriptName = method.getName();
        String testGroup = method.getDeclaredAnnotation(Test.class).groups()[0];
        testScriptDescription = method.getDeclaredAnnotation(Test.class).description();
        String browser = getProperty(ATFConstants.BROWSER);
        String sauceLabs = getProperty(ATFConstants.SAUCELABS);
        if (Boolean.valueOf(sauceLabs)) {
            System.setProperty(ATFConstants.USERNAME, getProperty(ATFConstants.USERNAME));
            System.setProperty(ATFConstants.ACCESS_KEY, getProperty(ATFConstants.ACCESS_KEY));
            System.setProperty(ATFConstants.TEST_SCRIPT_NAME, testScriptName);
            System.setProperty(ATFConstants.PLATFORM_NAME, getProperty(ATFConstants.PLATFORM_NAME));
            System.setProperty(ATFConstants.BROWSER_VERSION, getProperty(ATFConstants.BROWSER_VERSION));
        }
        String[] testNameDescription = {testScriptName, testScriptDescription, getClass().getSimpleName(), displayName, browser, testGroup, sauceLabs};
        ATFReporter.StartTest(testNameDescription);
        url = getProperty(ATFConstants.URL);
        webController = ATFWebController.buildWebDriver(url, browser, testNameDescription);
        webController.windowMaximize();
    }


    @BeforeSuite(alwaysRun = true)
    protected void beforeSuite() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(ATFConstants.AUTOMATION_LOG4J_PATH));
        } catch (IOException e) {
            System.out.println("Error: Cannot laod configuration file");
        }

    }


    @AfterMethod(alwaysRun = true)
    protected void quitBrowser(ITestResult result) {
        if ((result.getStatus() == ITestResult.FAILURE) || ATFReporter.testFailCount > 0) {
            if (!(result.getThrowable() == null)) {
                ReportFail("" + result.getThrowable());
            }
        } else if (result.getStatus() == ITestResult.SKIP) {
            ReportSkip("Test Script: '" + testScriptName + "' with Description: '" + testScriptDescription + "' Skipped");
        }
        if (webController != null) {
            webController.quit();
        }
        ATFReporter.EndTest();
        System.out.println("");
    }


    @AfterSuite
    protected void afterSuite() {
        ATFReporter.PublishTestExecutionSummary();
    }


    public void skip(String message) {
        throw new SkipException(message);
    }


    protected static ATFWebController GetWebController() {
        return webController;
    }


    public static void AssertTrue(String expectedValue, String actualValue, String message) {
        String passMessage = "Expected " + message + " { " + expectedValue + " } Matches Actual " + message + " { " + actualValue + " } ";
        String failMessage = "Expected " + message + " { " + expectedValue + " } Not Matches Actual " + message + " { " + actualValue + " } ";
        if (!(expectedValue == actualValue)) {
            ReportFail(failMessage);
        } else {
            ReportPass(passMessage);
        }
        Assert.assertTrue(expectedValue.trim().equals(actualValue.trim()), "Expected Value: " + expectedValue + " Actual Value: " + actualValue);
    }


    public static void AssertSame(int expectedValue, int actualValue, String message) {
        String passMessage = "Expected " + message + " { " + expectedValue + " } Matches Actual " + message + " { " + actualValue + " } ";
        String failMessage = "Expected " + message + " { " + expectedValue + " } Not Matches Actual " + message + " { " + actualValue + " } ";
        if (!(expectedValue == actualValue)) {
            ReportFail(failMessage);
        } else {
            ReportPass(passMessage);
        }
        Assert.assertSame(expectedValue, actualValue, message + " Expected Value: " + expectedValue + " Actual Value: " + actualValue);
    }


    public static void AssertEquals(String expectedValue, String actualValue, String message) {
        String passMessage = "Expected " + message + " { " + expectedValue + " } Matches Actual " + message + " { " + actualValue + " } ";
        String failMessage = "Expected " + message + " { " + expectedValue + " } Not Matches Actual " + message + " { " + actualValue + " } ";
        if (!(expectedValue.equals(actualValue))) {
            ReportFail(failMessage);
        } else {
            ReportPass(passMessage);
        }
        Assert.assertEquals(expectedValue.trim(), actualValue.trim(), "Expected Value: " + expectedValue + " Actual Value: " + actualValue);
    }


    public static void AssertElementVisible(ATFWebController webController, String locator, String message) {
        ATFBaseTest.webController = webController;
        if (!webController.isElementVisible(locator)) {
            ReportFail("Could not locate " + message + " on the page");
        } else {
            ReportPass(message + " Present on the page");
        }
        Assert.assertTrue(webController.isElementVisible(locator), "Could not locate " + message + " on the page");
    }


    protected <T extends ATFBasePage> T navigateToPage(Class<T> clazz) {
        T page = null;
        try {
            Constructor<T> constructor = clazz.getConstructor(ATFWebController.class);
            page = constructor.newInstance(webController);
            log.info("Navigated to '" + clazz.getSimpleName() + "'");
        } catch (Exception e) {
            throw new RuntimeException("Failure to open " + clazz.getName() + " page", e);
        }
        return page;
    }


    protected String getProperty(String parameter) {
        return ATFProperties.GetProperty(ATFConstants.AUTOMATION_PROPERTIES_PATH, parameter);
    }


    protected static void ReportPass(String message) {
        ATFReporter.ReportPass(message, GetWebController());
    }


    protected static void ReportFail(String message) {
        ATFReporter.ReportFail(message, GetWebController());
    }


    protected static void ReportInfo(String message) {
        ATFReporter.ReportInfo(message);
    }


    protected static void ReportSkip(String message) {
        ATFReporter.ReportSkip(message);
    }
}
