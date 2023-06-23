package utils;

public class ATFConstants
{
   /**
    * Project capabilities
    */
   public static final String PROJECT_NAME = System.getProperty("user.dir").split("/")[System.getProperty("user.dir").split("/").length-1];
   public static final String PROJECT_RELEASE = "-R2";
   public static final String AUTOMATION_OUTPUT_PATH = System.getProperty("user.home") + "/Desktop/Automation/" + PROJECT_NAME;
   public static final String AUTOMATION_RESOURCES_PATH = System.getProperty("user.dir") + "/src/test/resources/";
   public static final String AUTOMATION_REPORTS_PATH = AUTOMATION_OUTPUT_PATH + "/Reports/";
   public static final String AUTOMATION_SCREENSHOT_PATH = AUTOMATION_OUTPUT_PATH + "/Screenshots";
   public static final String EXCEL_PATH_TESTDATA_OUTPUT = AUTOMATION_OUTPUT_PATH + "/TestDataOutput.xls";
   public static final String AUTOMATION_PROPERTIES_PATH = AUTOMATION_RESOURCES_PATH + "app.properties";
   public static final String AUTOMATION_LOG4J_PATH = AUTOMATION_RESOURCES_PATH + "log4j.properties";
   public static final String EXCEL_PATH = AUTOMATION_RESOURCES_PATH + "AutomationTestData.xls";
   public static final String AUTOMATION_TESTDATA_INPUT = "testDataFile";
   public static final String BROWSER = "browser";
   public static final String URL = "url";
   public static final String SAUCELABS = "sauceLabs";
   public static final String PLATFORM_NAME  = "platformName";
   public static final String BROWSER_VERSION = "browserVersion";
   public static final String USERNAME = "USER_NAME";
   public static final String ACCESS_KEY = "ACCESS_KEY";
   public static final String TEST_SCRIPT_NAME = "testScriptName";
   public static final String SYSTEM = "system";
   public static final String SCREENSHOT = "capturePassScreenshot";
   public static final String AUTOMATION_EXCEL_OUTPUT = System.getProperty("user.dir") + "/Results/AutomationTestOutput.xls";
   public static final String OUTPUT_EXCEL_SHEET_NAME = "Output";
   /**
    * Project Data
    *
    */
}
