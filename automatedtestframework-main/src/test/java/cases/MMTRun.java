package cases;

import java.lang.reflect.Method;
import java.text.ParseException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pages.MMTBase;
import utils.ATFTestDetails;

public class MMTRun  extends ATFBaseTest 
{
	 @BeforeMethod(alwaysRun = true)
	    public void beforeTest(Method method) {
	        launchBrowser(method);
}

	    @ATFTestDetails(user = "Revu", date = "18/06/2023", displayName = "MakeMyTrip Website")
	    @Test(groups = {REGRESSION}, description = "")
	    public void mmt() throws InterruptedException, ParseException
	    {
	    	mmtBase = navigateToPage(MMTBase.class);
	    
	    	mmtBase.ads();
	    	mmtBase.home();
	    	mmtBase.page2();
	    
	    }

}