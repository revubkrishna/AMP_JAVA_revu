package pages;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ATFReporter;
import utils.ATFWebController;


public class ATFBasePage
{
   protected static final Logger log = LoggerFactory.getLogger(ATFBasePage.class);
   protected static ATFWebController webController;
   public static final int ZERO = 0, FIVE = 5, TEN = 10, THIRTY = 30, SIXTY = 60, NINTY = 90;


   public ATFBasePage(ATFWebController atfWebController)
   {
      ATFBasePage.webController = atfWebController;
   }


   public static ATFWebController GetWebController()
   {
      return webController;
   }


   public <T extends ATFBasePage> T navigateToPage(Class<T> pageType)
   {
      T page = null;
      try
      {
         Constructor<T> constructor = pageType.getConstructor(ATFWebController.class);
         page = constructor.newInstance(GetWebController());
         log.info("Navigated to '" + pageType.getSimpleName() + "'");
      } catch (Exception e)
      {}
      return page;
   }


   public static void ReportPass(String message)
   {
      ATFReporter.ReportPass(message, GetWebController());
   }


   public static void ReportFail(String message)
   {
      ATFReporter.ReportFail(message, GetWebController());
   }


   public static void ReportInfo(String message)
   {
      ATFReporter.ReportInfo(message);
   }


   public static void ReportSkip(String message)
   {
      ATFReporter.ReportSkip(message);
   }


   public static void VerifyIntegerValues(int expectedValue, int actualValue, String message)
   {
      ATFReporter.VerifyIntegerValues(expectedValue, actualValue, message, GetWebController());
   }


   public static void VerifyStringValues(String expectedValue, String actualValue, String message)
   {
      ATFReporter.VerifyStringValues(expectedValue, actualValue, message, GetWebController());
   }


   public static void VerifyLocatorText(String locator, String verificationMessage)
   {
      ATFReporter.VerifyLocatorText(locator, verificationMessage, GetWebController());
   }


   public static void IsLocatorVisible(String locator, String message)
   {
      ATFReporter.IsLocatorVisible(locator, message, GetWebController());
   }


   public static void IsLocatorNotVisible(String locator, String message)
   {
      ATFReporter.IsLocatorNotVisible(locator, message, GetWebController());
   }
}
