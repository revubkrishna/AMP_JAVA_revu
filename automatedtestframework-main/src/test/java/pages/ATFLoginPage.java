package pages;


import org.openqa.selenium.Keys;

import utils.ATFWebController;

public class ATFLoginPage extends ATFBasePage {
    public static final String SEARCH_INPUT = "//textarea[@name='q']";
    protected static final String SEARCH_BUTTON = "//input[@value='Google Search']";
    public ATFLoginPage(ATFWebController getWebController) {
        super(getWebController);
    }

    public void doSearch(String input) {
        ReportInfo("Inputting " + input + " to search field");
        webController.type(SEARCH_INPUT, input);
        webController.sendKeys(SEARCH_INPUT, Keys.ENTER);
        ReportPass("Setting the input as " + input);
        IsLocatorVisible(SEARCH_BUTTON, "Google Search button");
        VerifyIntegerValues(1, 2, "integer");
        VerifyStringValues("a", "a", "string");
        String text = webController.getAttribute(SEARCH_BUTTON, "aria-label");
        VerifyLocatorText(text, "Google Search");
    }
}
