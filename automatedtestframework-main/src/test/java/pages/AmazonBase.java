package pages;

import utils.ATFWebController;

public class AmazonBase extends ATFBasePage {

	public static final String fashion = "//a[@id='nav-hamburger-menu']";
		
    public static final String men = "(//div[normalize-space()=\"Men's Fashion\"])[1]";
    
    public static final String shirts = "(//a[normalize-space()='Shirts'])[1]"; 
    
	public static final String shirt = "/html/body/div[1]/div[2]/div[2]/div[3]/div[3]/div/div[2]/div[2]/ul/li[1]/span/div/a";
	 
	public static final String quicklookcss = "#centerCol"; //relcss
	public static final String quickxp = "/html/body/div[1]/div[2]/div[2]/div[3]/div[3]/div/div[2]/div[2]/ul/li[1]/span/div/span/span/span/input";
	public static final String quicklookindex = "(//input[@type='submit'])[16]";
	
    public static final String detcss = "#centerCol";
    public static final String detindex = "(//a[@class='a-button-text'][normalize-space()='See product details'])[4]";
    public static final String detxp = "/html/body/div[4]/div/div/div/div/div[1]/div[1]/div/div/span[3]/span/span/a";
    
    public static final String color = "(//img[@alt='Brown'])[1]";
	
	public static final String size = "//select[@id='native_dropdown_selected_size_name']";
	public static final String sizedrp = "(//option[@id='native_size_name_3'])[1]";
	public static final String product = "//span[@id='productTitle']";
	
	public static final String quantity = "//*[@id=\"quantity\"]";
	
	public static final String  price = "//*[@id=\"corePrice_feature_div\"]/div/span[1]/span[2]/span[2]";
	
	public static final String cart = "/html/body/div[2]/div[2]/div[5]/div[1]/div[1]/div[2]/div[2]/div/div/div[2]/div[3]/div/div[1]/div/div/div/form/div/div/div/div/div[3]/div/div[32]/div[1]/span/span/span/input";

	public static final String gotocart = "//a[@href='/cart?ref_=sw_gtc']";

	public static final String bag = "//span[@id='nav-cart-count']";
	
	public AmazonBase(ATFWebController atfWebController) {
		super(atfWebController);
		// TODO Auto-generated constructor stub
		
		
	}
	public void Homeclick()
	{
		
		webController.click(fashion);
		webController.click(men);
		webController.clickUsingJavascript(shirts);
	}

	public void shirts() throws InterruptedException
	
	{
		webController.waitForPageLoad();

	    webController.moveToLocator(shirt);

	    
	    webController.click(quickxp);
	    webController.waitForPageLoad();
	    
	    webController.moveToLocatorAndClick(detxp);
	   
        
	}
	public void pdp()
	{
		
		webController.waitForPageLoad();
		webController.click(color);
		webController.selectByVisibleText(size, "L");
		System.out.println("THe name of the product is: " +webController.getText(product));
		System.out.println("The size of the product is: " + webController.getText(size));
		System.out.println("The quantity is: "+ webController.getText(quantity));
		System.out.println("The price of the product is: " + webController.getText(price));
		webController.findClickableElement(cart);
		webController.click(cart);
	}
	
	public void bag()
	{
		webController.click(gotocart);
		System.out.println("The no. of items in the cart is: " + webController.getText(bag));
	
	}	
}
