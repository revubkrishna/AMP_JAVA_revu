package cases;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pages.AmazonBase;
import utils.ATFTestDetails;

public class AmazonRun  extends ATFBaseTest 
{
	 @BeforeMethod(alwaysRun = true)
	    public void beforeTest(Method method) {
	        launchBrowser(method);
}

	    @ATFTestDetails(user = "Revu", date = "16/06/2023", displayName = "Amazon Website")
	    @Test(groups = {REGRESSION}, description = "Test Cart adding")
	    public void amazon() throws InterruptedException
	    {
	    	amazonBase = navigateToPage(AmazonBase.class);
	    	
	    	amazonBase.Homeclick();
	    	amazonBase.shirts();
	    	amazonBase.pdp();
	    	amazonBase.bag();
	    }

}
