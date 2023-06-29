package pages;

import utils.ATFWebController;

public class AmazonBase extends ATFBasePage {

	public static final String fashion = "//a[@id='nav-hamburger-menu']";
		
    public static final String men = "(//div[normalize-space()=\"Men's Fashion\"])[1]";
    
    public static final String shirts = "(//a[normalize-space()='Shirts'])[1]"; 
    
	public static final String shirt = "(//ul[@class='a-unordered-list a-nostyle a-horizontal octopus-pc-card-list octopus-pc-card-height-v3'])//li[1]//a";
	 
	public static final String quicklook = "(//span[@class='a-button-inner']//span[text()='Quick look'])[position()=1]"; 

    public static final String det = "(//div[@class='a-popover-wrapper']//a[contains(@class, 'a-button-text') and contains(text(), 'See product details')])";
    
    public static final String color = "//li[@id='color_name_0']";
	
	public static final String size = "//select[@id='native_dropdown_selected_size_name']";
	public static final String sizedrp = "(//option[@id='native_size_name_3'])[1]";
	
	public static final String product = "//span[@id='productTitle']";
	
	public static final String quantity = "//select[@name='quantity']";
	
	public static final String  price = " //span[@class='a-price aok-align-center']";
	
	public static final String cart = "//span[text()='Add to Cart']";

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

	    webController.moveToLocatorAndClick(quicklook);

	    do {
	    webController.click(det);
	    }while (webController.isElementPresent(det));
	    
	    
	}
	@SuppressWarnings("unused")
	public void pdp() throws InterruptedException
	{
		
		webController.waitForPageLoad();
		
		for(int i=0; i<10; i++)
		{
			webController.click("//li[@id='color_name_"+ i + "']");
			webController.waitForElementFound(cart, 5);
			
			if (webController.isElementPresent(cart)==false) {
				i++;
			}
			break;
		}
		
	

		
		webController.selectByVisibleText(size, "L");
		System.out.println("THe name of the product is: " +webController.getText(product));
		System.out.println("The size of the product is: " + webController.getText(size));
		System.out.println("The quantity is: "+ webController.getText(quantity));
		System.out.println("The price of the product is: " + webController.getText(price));
		
		 do {
		webController.moveToLocatorAndClick(cart);
		break;
		 }while (webController.isElementPresent(cart));
	}
	
	public void bag()
	{
		webController.waitForPageLoad();
		webController.click(gotocart);
		System.out.println("The no. of items in the cart is: " + webController.getText(bag));
	
	}	
}
