package pages;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.Assert;

import utils.ATFWebController;

public class MMTBase extends ATFBasePage
{
	public static final String closead = "//a[@id='webklipper-publisher-widget-container-notification-close-div']";

	public static final String close = "//span[@class='ic_circularclose_grey']";
	
	public static final String from = "//*[@id=\"root\"]/div/div[2]/div/div/div[2]/div[1]/div[1]/label/p/span"; //relxpath
	
	public static final String blr = "(//li[@id='react-autowhatever-1-section-0-item-0'])[1]";
	
	public static final String to = "//*[@id=\"root\"]/div/div[2]/div/div/div[2]/div[1]/div[2]/label"; //relxpath
	public static final String tosearch = "(//input[@placeholder='To'])[1]";
	
	public static final String fromsearch = "//input[@placeholder='From']";
	public static final String hyd = "//*[@id=\"react-autowhatever-1-section-0-item-0\"]"; 
	
    public static final String depart = "//*[@id=\"root\"]/div/div[2]/div/div/div[2]/div[1]/div[3]/label/p[1]";

    public static final String travel = "(//span[@class='lbl_input appendBottom5'])[1]";

    public static final String adult = "//*[@id=\"root\"]/div/div[2]/div/div/div[2]/div[1]/div[5]/div[1]/div[1]/ul[1]/li[2]";
    public static final String child =	"//*[@id=\"root\"]/div/div[2]/div/div/div[2]/div[1]/div[5]/div[1]/div[1]/div/div[1]/ul/li[4]";
	public static final String infant = "//*[@id=\"root\"]/div/div[2]/div/div/div[2]/div[1]/div[5]/div[1]/div[1]/div/div[2]/ul/li[2]";
	public static final String buss = "(//li[normalize-space()='Business'])[1]";
	public static final String apply = "//button[normalize-space()='APPLY']";
	
	public static final String search = "(//a[normalize-space()='Search'])[1]";
	
	public static final String close2 = "//*[@id=\"root\"]/div/div[2]/div[2]/div[2]/div/span";
	
	public static final String air = "//*[@id=\"root\"]/div/div[2]/div[2]/div/div[1]/div[2]/div[1]/div/div[4]/label";
	
	public static final String aircode = "//p[@class='fliCode']";
	public static final String dur = "//div[@class='stop-info flexOne']";	
	public static final String price = "//div[@class='blackText fontSize18 blackFont white-space-no-wrap']";
			
			
	public MMTBase(ATFWebController atfWebController) {
		super(atfWebController);
	}
	
	public void home() throws ParseException
	{
		webController.waitForPageLoad();
		
		webController.click(from);
		webController.click(fromsearch);
		webController.sendKeys(fromsearch, "blr");
		webController.waitForElementFound(blr, FIVE);
		webController.click(blr);
		
		webController.click(to);
		webController.click(tosearch);
		webController.sendKeys(tosearch, "hyderabad");
		webController.waitForElementFound(hyd, FIVE);
		webController.click(hyd);
		
		

		
		String date = webController.getText(depart);
		System.out.println(date);
		
		DateFormat formatter, formatter2 ; 
		Date dates ; 
		Calendar cal=Calendar.getInstance();
		Calendar cal2= (Calendar)Calendar.getInstance();
		
		  formatter = new SimpleDateFormat("dd MMMyy");
		  formatter2 = new SimpleDateFormat("EEE MMM dd yyyy");
		  dates = (Date)formatter.parse(date); 
		 
		 cal.setTime(dates);
		 cal.add(Calendar.DAY_OF_MONTH, 30);
		 Date dt = cal.getTime();
		 cal2.setTime(dt);
		 
		 String str = formatter2.format(dt);  
		
			
			  System.out.println("The modified" + " Date:" + str);
			 
		       try {
				 webController.click("//div[@aria-label='" + str + "']", 10);
		       }
		       catch (Exception e)
		       {
		    	   webController.click("//div[@aria-label='" + str + "']//p[1]");

		       }
		webController.click(travel);
		webController.click(adult);
		webController.click(child);
		webController.click(infant);
		webController.click(buss);
		
		webController.click(apply);
		webController.click(search);
		
		
	}	
	public void ads()
	{
		
	    webController.waitForPageLoad();
        
	    if (webController.isElementPresent("//*[@id=\"fullpage-error\"]/div/div/div/button"))
	    {
	    	webController.click("//*[@id=\"fullpage-error\"]/div/div/div/button");
	    }
		if (webController.isElementPresent("//iframe[@id='webklipper-publisher-widget-container-notification-frame']"))
				{
			webController.switchToFrame("//iframe[@id='webklipper-publisher-widget-container-notification-frame']");
			webController.click(closead);
				}
	}	
	 public void page2()
	 {
		 webController.waitForPageLoad();
		if (webController.isElementPresent(close2))
		{
		webController.click(close2);
		}
	
		 webController.click(air);
		 
		 List<WebElement> li = webController.findElements(aircode);
		 System.out.println("The name of flights are: ");
		 
		 for (int i =0; i <li.size(); i++)
		 {
			 String list = li.get(i).getText();
			 
			 System.out.println(list);
			 
		 }
		 
		 List<WebElement> li2 = webController.findElements(dur);
		 System.out.println("The Durations are: ");
		 
		 for (int i =0; i <li2.size(); i++)
		 {
			 String list2 = li2.get(i).getText();
			 
			 System.out.println(list2);
			 
		 }
		 List<WebElement> li3 = webController.findElements(price);
		 System.out.println("The Prices are: ");
		 
		 for (int i =0; i <li3.size(); i++)
		 {
			 String list3 = li3.get(i).getText();
			 
			 System.out.println(list3);
			 
		 }
		 
		 int s =li.size();
		 
		 Assert.assertTrue(s>0);
	 }
		
	
}
