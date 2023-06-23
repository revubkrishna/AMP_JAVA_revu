package utils;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ATFWebController {
    protected static final Logger log = LoggerFactory.getLogger(ATFWebController.class);
    private static final String CSS_PREFIX = "css=", VALUE = "value";
    private static final int FIVE = 5, TEN = 10;
    private static final int DEFAULT_KERNEL_RADIUS = 2, BYTE_MASK = 0x000000FF;
    private static final float PIXEL_ERROR_LIMIT = 1000;
    private static final double RED_RATIO = 0.2126f, BLUE_RATIO = 0.0722f, GREEN_RATIO = 0.7152f, PIXEL_ERROR_THRESHOLD_RATIO = 0.2;
    private static final double PIXEL_THRESHOLD_CUBE_LUM_DIFF = PIXEL_ERROR_THRESHOLD_RATIO * PIXEL_ERROR_LIMIT;
    @SuppressWarnings("unused")
	private static String sysEnv, PATH;
    private final WebDriver webDriver;
   
    
    
   
   
    
    public ATFWebController(WebDriver webDriver) {
        this.webDriver = webDriver;
    }


    /**
     * This method build and return a webDriver instance by which one can use to control the automation of a specified
     * web browser and platform or Operating System.
     *
     * @param url - main test url
     *            \
     * @return - Instance of WebBrowser
     */
    public static ATFWebController buildWebDriver(String url, String browserName, String[] testDetails) {
        System.gc();
        log.info("");
        log.info("=========================================================");
        log.info("Test: {}", testDetails[0]);
        log.info("Test Description: {}", testDetails[1]);
        log.info("=========================================================");
        WebDriver wd = BuildWebDriver(browserName, testDetails[6]);
        wd.get(url);
        return new ATFWebController(wd);
    }


    /**
     * This method build and return a webDriver instance by which one can use to control the automation of a specified
     * web browser and platform or Operating System.
     *
     * @param url - main test url
     * @return - Instance of WebBrowser
     */
    public static ATFWebController buildRemoteWebDriver(String url, String browserName, String gridURL, String[] details) {
        System.gc();
        log.info("");
        log.info("=========================================================");
        log.info("Test: {}", details[0]);
        log.info("Test Description: {}", details[1]);
        log.info("=========================================================");
        WebDriver wd = buildRemoteWebDriver(browserName, gridURL);
        wd.get(url);
        return new ATFWebController(wd);
    }

    public static String findFileName(String path, String name, FileSearchType fileSearchType) {
        String systemSeperator = System.getProperty("file.separator");
        log.debug("Starting search for file: {}{}{}", path, systemSeperator, name);
        String myPath = setFileDelimiter(path);
        File file = new File(myPath);
        if (!file.isDirectory()) {
            log.debug("Search path for file is not a directory: {}", path);
            return "";
        }
        if (name == null || name.isEmpty()) {
            log.error("the file name for the search is empty: {}", name);
            return "";
        }
        File[] files = file.listFiles();
        try {
            assert files != null;
            for (File subFile : files) {
                String findPath;
                if ((fileSearchType == FileSearchType.Both || fileSearchType == FileSearchType.Directory && subFile.isDirectory() || fileSearchType == FileSearchType.File && subFile.isFile()) && subFile.getName().toLowerCase().matches(name.toLowerCase())) {
                    log.debug("Search ok, " + subFile.getCanonicalPath());
                    return subFile.getCanonicalPath();
                }
                if (!subFile.isDirectory() || (findPath = findFileName(subFile.getCanonicalPath(), name, fileSearchType)).isEmpty()) {
                    continue;
                }
                return findPath;
            }
        } catch (IOException e) {
            log.error("Can't find file: {},{}", path, name);
            log.info("Can't find file " + path + name);
        }
        return "";
    }

    private static String setFileDelimiter(String aPath) {
        String systemFileSeparator = System.getProperty("file.separator");
        String wrongFileSeparator = systemFileSeparator.equals("/") ? "\\" : "/";
        char s = '\\';
        if (aPath.contains(wrongFileSeparator) || wrongFileSeparator.contains(String.valueOf(s)) && aPath.contains(String.valueOf(s))) {
            log.debug("replacing (wrong fileSeparator) " + wrongFileSeparator + " with SystemFileSeparator " + systemFileSeparator + " in " + aPath);
            return aPath.replace(wrongFileSeparator, systemFileSeparator);
        }
        return aPath;
    }

    /**
     * Compares two images, returning true if they are similar enough to constitute a match.
     *
     * @param errorsPerThousandThreshold The acceptable errors per thousand (pixels); i.e. haw many mismatched
     *                                   pixels are there allowed to be before the images are considered different. The lower the
     *                                   number the more strict the comparison will be
     * @return
     * @throws IOException
     */
    public static boolean compareImages(String image1, String image2, long errorsPerThousandThreshold) {
        try {
            BufferedImage img1 = ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(image1)));
            BufferedImage img2 = ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(image2)));
            if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                log.trace("The two images are different sizes, automatically returning false");
                return false;
            }
            long ept = compareRGB24Images(img1, img2, 0, 0, img1.getWidth(), img1.getHeight(), DEFAULT_KERNEL_RADIUS);
            boolean match = ept <= errorsPerThousandThreshold;
            log.info("Match:[{}]  Err/1K:[{}]  Limit:[{}]  PixErrLim:[{}]", match, ept, errorsPerThousandThreshold, PIXEL_ERROR_LIMIT);
            return match;
        } catch (Exception ex) {
            log.error("Error", ex);
        }
        return false;
    }

    /**
     * Compares a given region of two RGB24 type buffered image objects, returning the errors per thousand
     * (pixels) between the two.
     *
     * @param image1       The first image to compare
     * @param image2       The second image to compare
     * @param xOffset      The starting x coordinate of the region that will be compared
     * @param yOffset      The starting y coordinate of the region that will be compared
     * @param width        The width of the comparison region
     * @param height       The height of the comparison region
     * @param kernelRadius The radius used in the local average filter, the larger the number, the fuzzier the
     *                     comparison will be
     * @return The errors per thousand (pixels) that was found between the two images
     */
    private static long compareRGB24Images(BufferedImage image1, BufferedImage image2, int xOffset, int yOffset, int width, int height, int kernelRadius) {
        double[] luminance1 = computeLuminance(image1, xOffset, yOffset, width, height);
        double[] luminance2 = computeLuminance(image2, xOffset, yOffset, width, height);
        applyLuminanceAdjustment(luminance1, luminance2);
        luminance1 = performLocalAvgFilter(luminance1, width, height, kernelRadius);
        luminance2 = performLocalAvgFilter(luminance2, width, height, kernelRadius);
        double lumDiffSumCubes = 0;
        int pixelMismatchCntr = 0;
        for (int i = 0; i < luminance1.length && i < luminance2.length; i++) {
            double lumDiff3 = Math.pow(Math.abs(luminance2[i] - luminance1[i]), 3);
            lumDiffSumCubes += lumDiff3;
            if (lumDiff3 > PIXEL_THRESHOLD_CUBE_LUM_DIFF) {
                pixelMismatchCntr++;
            }
        }
        int npixels = width * height;
        int errorsPerThou = pixelMismatchCntr * (int) PIXEL_ERROR_LIMIT / npixels;
        double avgCubeLumDiff = lumDiffSumCubes / npixels;
        log.trace("Mismatched Pixels:[{}]  TotalPixels:[{}]  Errors Per Thousand:[{}]  " + "Total Difference Lum^3:[{}]  Average Difference Lum^3:[{}]  Pixel Error Threshol:[{}]", pixelMismatchCntr, npixels, errorsPerThou, lumDiffSumCubes, avgCubeLumDiff, PIXEL_ERROR_LIMIT);
        return errorsPerThou;
    }

    private static double[] computeLuminance(BufferedImage image, int xStart, int yStart, int width, int height) {
        checkBounds(image, xStart, yStart, width, height);
        double[] luminance = new double[width * height];
        for (int y = yStart; y < height; y++) {
            for (int x = xStart; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & BYTE_MASK;
                int green = (pixel >> 8) & BYTE_MASK;
                int blue = (pixel) & BYTE_MASK;
                luminance[x * y] = RED_RATIO * red + GREEN_RATIO * green + BLUE_RATIO * blue;
            }
        }
        return luminance;
    }

    private static void checkBounds(BufferedImage image, int xStart, int yStart, int width, int height) {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        if (xStart > imgWidth || width > imgWidth || (xStart + width) > imgWidth) {
            throw new RuntimeException("image does not fully contain the comparison region");
        }
        if (yStart > imgHeight || height > imgHeight || (yStart + height) > imgHeight) {
            throw new RuntimeException("image does not fully contain the comparison region");
        }
    }

    private static void applyLuminanceAdjustment(double[] luminance1, double[] luminance2) {
        double avgLum1 = averageLuminance(luminance1);
        double avgLum2 = averageLuminance(luminance2);
        double diffAvgLum = avgLum1 - avgLum2;
        for (int i = 0; i < luminance2.length; i++) {
            luminance2[i] += diffAvgLum;
        }
    }

    private static double averageLuminance(double[] luminance) {
        double avgLum = 0;
        for (double element : luminance) {
            avgLum += element;
        }
        avgLum = avgLum / luminance.length;
        return avgLum;
    }

    protected static double[] performLocalAvgFilter(double[] data, int width, int height, int kernelRadius) {
        double[] filteredData = new double[data.length];
        for (int ii = 0; ii < data.length; ii++) {
            int c = 0;
            for (int i = -kernelRadius; i <= kernelRadius; i++) {
                for (int j = -kernelRadius; j <= kernelRadius; j++) {
                    int pixIndex = ii + (i * width) + j;
                    if (pixIndex >= 0 && pixIndex < data.length) {
                        filteredData[ii] += data[pixIndex];
                        c++;
                    }
                }
            }
            filteredData[ii] = filteredData[ii] / c;
        }
        return filteredData;
    }

    /**
     * This method build a webDriver based on the passed in browser request
     *
     * @return WebDriver
     * @throws MalformedURLException
     */
    private static WebDriver BuildWebDriver(String browserName, String sauceLabs) {
        BrowserType browserType = BrowserType.getBrowserTypeFromString(browserName);
        if (Boolean.parseBoolean(sauceLabs)) {
            Map<String, Object> sauceOptions = new HashMap<>();
            sauceOptions.put("username", System.getProperty(ATFConstants.USERNAME));
            sauceOptions.put("accessKey", System.getProperty(ATFConstants.ACCESS_KEY));
            sauceOptions.put("testScriptName", System.getProperty(ATFConstants.TEST_SCRIPT_NAME));
            switch (browserType) {
                case FIREFOX_DRIVER:
                case MARIONETTE:
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    firefoxOptions.setCapability(ATFConstants.PLATFORM_NAME, System.getProperty(ATFConstants.PLATFORM_NAME));
                    firefoxOptions.setCapability(ATFConstants.BROWSER_VERSION, System.getProperty(ATFConstants.BROWSER_VERSION));
                    firefoxOptions.setCapability("sauce:options", sauceOptions);
                    return new RemoteWebDriver(firefoxOptions);
                case CHROME_DRIVER:
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.setCapability(ATFConstants.PLATFORM_NAME, System.getProperty(ATFConstants.PLATFORM_NAME));
                    chromeOptions.setCapability(ATFConstants.BROWSER_VERSION, System.getProperty(ATFConstants.BROWSER_VERSION));
                    chromeOptions.setCapability("sauce:options", sauceOptions);
                    return new RemoteWebDriver(chromeOptions);
                case SAFARI:
                    SafariOptions safariOptions = new SafariOptions();
                    safariOptions.setCapability(ATFConstants.PLATFORM_NAME, System.getProperty(ATFConstants.PLATFORM_NAME));
                    safariOptions.setCapability(ATFConstants.BROWSER_VERSION, System.getProperty(ATFConstants.BROWSER_VERSION));
                    safariOptions.setCapability("sauce:options", sauceOptions);
                    return new RemoteWebDriver(safariOptions);
                case EDGE:
                    EdgeOptions edgeOptions = new EdgeOptions();
                    edgeOptions.setCapability(ATFConstants.PLATFORM_NAME, System.getProperty(ATFConstants.PLATFORM_NAME));
                    edgeOptions.setCapability(ATFConstants.BROWSER_VERSION, System.getProperty(ATFConstants.BROWSER_VERSION));
                    edgeOptions.setCapability("sauce:options", sauceOptions);
                    return new RemoteWebDriver(edgeOptions);
                default:
                    log.info("Current support is there for Chrome, Firefox, Firefox Marionette, Internet Explorer, Edge & Safari. Support is not there for " + browserName);
                    firefoxOptions = new FirefoxOptions();
                    firefoxOptions.setCapability(ATFConstants.PLATFORM_NAME, System.getProperty(ATFConstants.PLATFORM_NAME));
                    firefoxOptions.setCapability(ATFConstants.BROWSER_VERSION, System.getProperty(ATFConstants.BROWSER_VERSION));
                    firefoxOptions.setCapability("sauce:options", sauceOptions);
                    return new RemoteWebDriver(firefoxOptions);
            }
        } else {
            switch (browserType) {
                case FIREFOX_DRIVER:
                case MARIONETTE:
                    WebDriverManager.firefoxdriver().setup();
                    new FirefoxDriver();
                case CHROME_DRIVER:
                    WebDriverManager.chromedriver().setup();
                    return new ChromeDriver();
                case SAFARI:
                    return new SafariDriver();
                case EDGE:
                    WebDriverManager.edgedriver().setup();
                    return new EdgeDriver();
                default:
                    log.info("Current support is there for Chrome, Firefox, Firefox Marionette, Internet Explorer, Edge & Safari. Support is not there for " + browserName);
                    WebDriverManager.chromedriver().setup();
                    return new ChromeDriver();
            }
        }
    }

    /**
     * This method build a RemoteWebDriver based on the passed in browser request
     *
     * @return RemoteWebDriver
     */
    private static RemoteWebDriver buildRemoteWebDriver(String browserName, String gridURL) {
        DesiredCapabilities capabillities = null;
        BrowserType browserType = BrowserType.getBrowserTypeFromString(browserName);
        switch (browserType) {
            case MARIONETTE:
            case FIREFOX_DRIVER:
                capabillities = DesiredCapabilities.firefox();
                capabillities.setCapability("os", System.getProperty("os"));
                capabillities.setCapability("os_version", System.getProperty("os_version"));
                capabillities.setCapability("browser", System.getProperty("browser_name"));
                capabillities.setCapability("browser_version", System.getProperty("browser_version"));
                capabillities.setCapability("acceptSslCerts", "true");
                try {
                    return new RemoteWebDriver(new URL(gridURL), capabillities);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            case CHROME_DRIVER:
                capabillities = DesiredCapabilities.chrome();
                capabillities.setCapability("os", System.getProperty("os"));
                capabillities.setCapability("os_version", System.getProperty("os_version"));
                capabillities.setCapability("browser", System.getProperty("browser_name"));
                capabillities.setCapability("browser_version", System.getProperty("browser_version"));
                capabillities.setCapability("acceptSslCerts", "true");
                try {
                    return new RemoteWebDriver(new URL(gridURL), capabillities);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            case SAFARI:
                capabillities = DesiredCapabilities.safari();
                capabillities.setCapability("os", System.getProperty("os"));
                capabillities.setCapability("os_version", System.getProperty("os_version"));
                capabillities.setCapability("browser", System.getProperty("browser_name"));
                capabillities.setCapability("browser_version", System.getProperty("browser_version"));
                capabillities.setCapability("acceptSslCerts", "true");
                try {
                    return new RemoteWebDriver(new URL(gridURL), capabillities);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            case EDGE:
                capabillities = DesiredCapabilities.edge();
                capabillities.setCapability("os", System.getProperty("os"));
                capabillities.setCapability("os_version", System.getProperty("os_version"));
                capabillities.setCapability("browser", System.getProperty("browser_name"));
                capabillities.setCapability("browser_version", System.getProperty("browser_version"));
                capabillities.setCapability("acceptSslCerts", "true");
                try {
                    return new RemoteWebDriver(new URL(gridURL), capabillities);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            default:
                log.info("Current support is there for Chrome, Firefox, Firefox Marionette, Internet Explorer, Edge & Safari. Support is not there for " + browserName);
                capabillities = DesiredCapabilities.chrome();
                capabillities.setCapability("os", System.getProperty("os"));
                capabillities.setCapability("os_version", System.getProperty("os_version"));
                capabillities.setCapability("browser", System.getProperty("browser_name"));
                capabillities.setCapability("browser_version", System.getProperty("browser_version"));
                capabillities.setCapability("acceptSslCerts", "true");
                try {
                    return new RemoteWebDriver(new URL(gridURL), capabillities);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    public static String GetProperty(String parameter) {
        return ATFProperties.GetProperty(parameter);
    }

    public void acceptAlert() {
        try {
            log.info("Accepting Alert ...");
            Alert a = webDriver.switchTo().alert();
            String text = a.getText();
            a.accept();
            log.info("Alert Text: " + text);
        } catch (Exception e) {
            log.info(("We are hiding an exception here: " + e.getMessage()));
        }
    }

    public void addSelection(String locator, String optionLocator) {
        Select sel = new Select(findElement(locator));
        if (sel.isMultiple()) {
            sel.selectByVisibleText(optionLocator);
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#check (java.lang.String) */
    public void check(String locator) {
        WebElement e = findElement(locator);
        if (!e.isSelected()) {
            e.click();
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#clearField (java.lang.String) */
    public void clearField(String locator) {
        WebElement j2c = findElement(locator);
        j2c.clear();
    }

    /* This method will perform the right click action
     *
     * @param element - WebElement */
    public void rightClick(String locator) {
        WebElement e1 = elementToBeClickable(locator);
        Actions action = new Actions(webDriver);
        action.contextClick(e1).build().perform();
        delay(FIVE);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#click (java.lang.String) */
    public void click(String locator) 
    {
        click(locator, 10);
    }
    

    /* (non-Javadoc)
     *
     * @see WebBrowserController#click (java.lang.String) */
    public void click(String locator, int timeoutInSeconds) {
        log.debug("Clicking: " + locator);
        findClickableElement(locator, timeoutInSeconds).click();
    }

    public void clickByAction(String locator) {
        Actions action = new Actions(webDriver);
        action.click(findClickableElement(locator));
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#clickUsingJavascript (java.lang.String) */
    public void clickUsingJavascript(String locator) {
        WebElement we = findClickableElement(locator);
        String event = "arguments[0].click()";
        JavascriptExecutor executor = (JavascriptExecutor) webDriver;
        try {
            we.click();
        } catch (Exception e) {
            executor.executeScript(event, we);
        }
    }

    public void quit() {
        if (webDriver != null) {
            System.gc();
            webDriver.quit();
        }
    }

    public void closeSlowly() {
        if (webDriver != null) {
            webDriver.close();
            delay(5);
        }
        if (webDriver != null && !webDriver.toString().contains("null")) {
            log.warn("Web driver is still active or still has windows open...calling quit");
            quit();
        }
    }

    public void closeAllChildWindows() {
        switchToParentWindow();
        Set<String> winHandles = webDriver.getWindowHandles();
        String handle = null;
        for (int i = winHandles.size(); i > 1; i--) {
            handle = (String) winHandles.toArray()[winHandles.size() - 1];
            webDriver.switchTo().window(handle).close();
        }
        switchToParentWindow();
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#delay (long) */
    public void delay(int timeoutInSeconds) {
        try {
            TimeUnit.SECONDS.sleep(timeoutInSeconds);
        } catch (InterruptedException e) {
        }
    }

    public void dismissAlert() {
        try {
            Alert a = webDriver.switchTo().alert();
            String text = a.getText();
            a.dismiss();
            log.info("Dismissing alert: " + text);
        } catch (Exception e) {
            log.info(("We are hiding an exception here: " + e.getMessage()));
        }
    }

    /* (non-Javadoc)
     *
     * #doubleClick(java.lang.String) */
    public void doubleClick(String locator) {
        WebElement element = elementToBeClickable(locator);
        Actions action = new Actions(webDriver);
        try {
            action.doubleClick(element);
            action.perform();
        } catch (Exception e) {
            log.warn("Could not double click : " + e);
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#isVisible (java.lang.String) */
    public WebElement elementToBeClickable(String locator) {
        WebDriverWait wait = new WebDriverWait(webDriver, FIVE);
        return wait.until(ExpectedConditions.elementToBeClickable(getSelector(locator)));
    }

    /* (non-Javadoc)
     *
     * #enterMultiLineText(java.lang.String,
     * java.util.List) */
    public void enterMultiLineText(String locator, List<String> values) {
        StringBuilder dataEnter = new StringBuilder();
        for (String value : values) {
            dataEnter.append(value);
            if (values.indexOf(value) < values.size() - 1) {
                dataEnter.append(System.getProperty("line.separator"));
            }
        }
        type(locator, dataEnter.toString());
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#findElement (java.lang.String) */
    public WebElement findElementcss(String locator) 
    {   
        return webDriver.findElement(By.cssSelector(locator));
    }

    public WebElement findElement(String locator) {
        return findElement(locator, FIVE);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#findElement (java.lang.String) */
    public WebElement findElement(String locator, int timeoutInSeconds) {
        log.debug("debug: Looking for element: " + locator);
        return (new WebDriverWait(webDriver, timeoutInSeconds)).until(ExpectedConditions.presenceOfElementLocated(getSelector(locator)));
    }

    public WebElement findClickableElement(String locator) {
        return findClickableElement(locator, FIVE);
    }

    public WebElement findClickableElement(String locator, int timeoutInSeconds) {
        log.debug("debug: Looking for element: {}", locator);
        return (new WebDriverWait(webDriver, timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable(getSelector(locator)));
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#findElements (java.lang.String) */
    public List<WebElement> findElements(String locator) {
        return webDriver.findElements(getSelector(locator));
    }

    /* (non-Javadoc)
     *
     * #findTextElements(java.lang.String) */
    public List<String> findTextElements(String locator) {
        List<String> toReturn = new ArrayList<>();
        for (WebElement w : webDriver.findElements(getSelector(locator))) {
            toReturn.add(w.getText());
        }
        return toReturn;
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#fireJsEvent (java.lang.String, java.lang.String) */
    public void fireJsEvent(String script) {
        ((JavascriptExecutor) webDriver).executeScript(script);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#fireJsEvent (java.lang.String, java.lang.String) */
    public void fireJsEvent(String locator, String event) {
        String newLocator = locator.replace(CSS_PREFIX, "");
        String script = "$('" + newLocator + "')." + event + "();";
        log.debug(script);
        ((JavascriptExecutor) webDriver).executeScript(script);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#fireJsEvent (org.openqa.selenium.WebElement , java.lang.String) */
    public void fireJsEvent(WebElement locator, String event) {
        String exec = event;
        if (getBrowserName().toLowerCase().contains("internet")) {
            exec = "on" + event;
        }
        ((JavascriptExecutor) webDriver).executeScript(exec, locator);
    }

    /**
     * KEG notes works in chrome for add user
     * doesn't work for create bundle - drop down
     */
    public void focus(String locator) {
        findElement(locator).sendKeys(Keys.TAB);
    }

    public String getAlert() {
        return webDriver.switchTo().alert().getText();
    }

    public List<String> getAll(String locator) {
        ArrayList<String> ret = new ArrayList<>();
        String attrName = "name";
        int row = 1;
        while (true) {
            try {
                String mod = String.format(locator, row++);
                try {
                    String attr = webDriver.findElement(By.xpath(mod)).getAttribute(attrName);
                    ret.add(attr);
                } catch (Exception e) {
                    String text = getText(mod);
                    ret.add(text);
                }
            } catch (Exception e) {
                log.debug("" + e);
                break;
            }
        }
        return ret;
    }

    public List<String> getAllText(String locator) {
        List<String> textList = new ArrayList<>();
        List<WebElement> elements = webDriver.findElements(getSelector(locator));
        for (WebElement element : elements) {
            try {
                textList.add(element.getText());
            } catch (Exception e) {
            }
        }
        return textList;
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#getAttribute (java.lang.String, java.lang.String) */
    public String getAttribute(String locator, String attribute) {
        return findElement(locator).getAttribute(attribute);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#getAttribute (java.lang.String, java.lang.String) */
    public String getCSSValue(String locator, String attribute) {
        return findElement(locator).getCssValue(attribute);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#getBodyText () */
    public String getBodyText() {
        return webDriver.findElement(By.tagName("body")).getText();
    }

    public String getBrowserVersion() {
        Capabilities capabilities = ((RemoteWebDriver) webDriver).getCapabilities();
        return capabilities.getVersion().toUpperCase();
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController# getBrowserName() */
    public String getBrowserName() {
        Capabilities capabilities = ((RemoteWebDriver) webDriver).getCapabilities();
        return capabilities.getBrowserName().toUpperCase();
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController# getBrowserName() */
    public String getBrowserInfo() {
        String browserInfo = null;
        browserInfo = (String) ((JavascriptExecutor) webDriver).executeScript("return navigator.platform");
        browserInfo += ", ";
        browserInfo += (String) ((JavascriptExecutor) webDriver).executeScript("return navigator.userAgent");
        return browserInfo;
    }

    public String getAlertText() {
        return webDriver.switchTo().alert().getText();
    }

    public String getSelectedLabel(String locator) {
        return new Select(findElement(locator)).getFirstSelectedOption().getText();
    }

    public String[] getSelectedLabels(String locator) {
        List<WebElement> options = new Select(findElement(locator)).getAllSelectedOptions();
        String[] retArr = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            retArr[i] = options.get(i).getText();
        }
        return retArr;
    }

    public List<WebElement> getSelectOptions(String locator) {
        Select select = new Select(findElement(locator, TEN));
        return select.getOptions();
    }

    protected By getSelector(String locator) {
        String[] prefix = locator.split("=", 2);
        if (prefix[0].equals("css")) {
            return By.cssSelector(prefix[1]);
        } else if (prefix[0].equals("id")) {
            return By.id(prefix[1]);
        } else if (prefix[0].equals("class")) {
            return By.className(prefix[1]);
        } else if (prefix[0].equals("xpath")) {
            return By.xpath(prefix[1]);
        } else if (prefix[0].equals("link")) {
            return By.linkText(prefix[1]);
        } else if (prefix[0].equals("name")) {
            return By.name(prefix[1]);
        } else {
            return By.xpath(locator);
        }
    }

    /**
     * doesn't work for MBO-UI screens as written. Use methods in MBOTableUtils
     */
    public String getTable(String locator) {
        log.debug("Getting table ...");
        String[] tableLocation = locator.split("\\.", 3);
        if (tableLocation.length != 3) {
            throw new RuntimeException("Incorrect table locator used");
        }
        WebElement table = findElement(tableLocation[0]);
        List<WebElement> allRows = table.findElements(By.tagName("tr"));
        List<WebElement> cells = allRows.get(Integer.parseInt(tableLocation[1])).findElements(By.tagName("td"));
        return cells.get(Integer.parseInt(tableLocation[2])).getText();
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#getText (java.lang.String) */
    public String getText(String locator) {
        try {
            return findElement(locator).getText();
        } catch (StaleElementReferenceException e) {
            return findElement(locator).getText();
        }
    }

    public void retryAndClick(String locator) {
        int attempts = 0;
        while (attempts < 2) {
            try {
                findClickableElement(locator).click();
                delay(1);
                break;
            } catch (StaleElementReferenceException e) {
                waitForPageLoad();
                delay(1);
            }
            attempts++;
        }
    }

    public String getTitle() {
        return webDriver.getTitle();
    }

    public String getValue(String locator) {
        return findElement(locator).getAttribute(VALUE);
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    /**
     * Return the current url
     *
     * @return string with current url
     */
    public String getCurrentUrl() {
        return webDriver.getCurrentUrl();
    }

    public int getElementsCount(String locator) {
        Number num = webDriver.findElements(getSelector(locator)).size();
        return num.intValue();
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#goBack() */
    public void goBack() {
        webDriver.navigate().back();
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#goToTop() */
    public void goToTop() {
        scrollBy(0, -250);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#hideElement (java.lang.String) */
    public void hideElement(String locator) {
        String newlocator = locator.replace(CSS_PREFIX, "");
        String script = "$('" + newlocator + "').style.visibility = 'hidden'";
        ((JavascriptExecutor) webDriver).executeScript(script);
    }

    public boolean isChecked(String locator) {
        return findElement(locator, FIVE).isSelected();
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController# isElementPresent(java.lang.String) */
    public boolean isElementPresent(String locator) {
        return (findElements(locator).size() > 0);
    }

    /* (non-Javadoc)
     *
     * #isEnabled(java.lang.String) */
    public boolean isElementPresent(String locator, int timeoutInSeconds) {
        for (int i = 0; i < timeoutInSeconds; i++) {
            if (isElementPresent(locator)) {
                return true;
            }
            delay(1);
        }
        return false;
    }

    public boolean isEnabled(String locator) {
        return findElement(locator, FIVE).isEnabled();
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#isTextPresent (java.lang.String) */
    public boolean isTextPresent(String pattern) {
        String bodyText = getBodyText();
        return bodyText.contains(pattern);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#isVisible (java.lang.String) */
    public boolean isElementVisible(String locator) {
        return isElementVisible(locator, FIVE);
    }

    /**
     * {@inheritDoc} <br/>
     * Similar to sendKeys
     */
    public void keyPress(String locator, String key) {
        findElement(locator).sendKeys(key);
    }

    public void makeElementVisible(String locator) {
        WebElement elem = findElement(locator);
        String js = "arguments[0].style.height='1'; arguments[0].style.visibility='visible'; arguments[0].style.display='block';";
        ((JavascriptExecutor) webDriver).executeScript(js, elem);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#mouseOut (java.lang.String) */
    public void mouseOut(String locator) {
        String newLocator = locator.replace(CSS_PREFIX, "");
        ((JavascriptExecutor) webDriver).executeScript("$('" + newLocator + "').mouseout();");
    }

    public Actions mouseOverByActions(String locator) {
        Actions action = new Actions(webDriver);
        action.moveToElement(findElement(locator));
        return action;
    }

    public void dragAndDropAnElement(WebElement sourceLocator, WebElement destinationLocator) {
        new Actions(webDriver);
        Actions dragdrop = new Actions(webDriver);
        try {
            dragdrop.clickAndHold(sourceLocator).moveToElement(destinationLocator).release(destinationLocator).build().perform();
        } catch (StaleElementReferenceException se) {
            log.error("Stale element:", se);
        } catch (NoSuchElementException ne) {
            log.error("no such element: source: {}, destination:{}", sourceLocator, destinationLocator);
            log.error("message: {}", ne);
        } catch (Exception e) {
            log.error("unexpected error attempting to drag and drop", e);
        }
    }

    /**
     * This method uses the webDriver object to drag and drop the elements from source to destination
     */
    public void dragAndDropAnElement(String sourceLocator, String destinationLocator, int timeoutInSeconds) {
        Actions builder = new Actions(getWebDriver());
        WebElement sElement = findElement(sourceLocator, timeoutInSeconds);
        WebElement dElement = findElement(destinationLocator, timeoutInSeconds);
        builder.clickAndHold(sElement).moveToElement(dElement).release(dElement).build().perform();
    }

    /**
     * This method uses the webDriver object to drag and drop the elements from source to destination using javascript
     */
    public void dragAndDropAnElementUsingJavascript(String sourceLocator, String destinationLocator, int timeoutInSeconds) {
        Actions builder = new Actions(getWebDriver());
        WebElement src = findElement(sourceLocator, timeoutInSeconds);
        WebElement des = findElement(destinationLocator, timeoutInSeconds);
        String xto = Integer.toString(src.getLocation().x);
        String yto = Integer.toString(des.getLocation().y);
        JavascriptExecutor executor = (JavascriptExecutor) webDriver;
        String event = "function simulate(f,c,d,e){var b,a=null;for(b in eventMatchers)if(eventMatchers[b].test(c)){a=b;break}if(!a)return!1;document.createEvent?(b=document.createEvent(a),a==\"HTMLEvents\"?b.initEvent(c,!0,!0):b.initMouseEvent(c,!0,!0,document.defaultView,0,d,e,d,e,!1,!1,!1,!1,0,null),f.dispatchEvent(b)):(a=document.createEventObject(),a.detail=0,a.screenX=d,a.screenY=e,a.clientX=d,a.clientY=e,a.ctrlKey=!1,a.altKey=!1,a.shiftKey=!1,a.metaKey=!1,a.button=1,f.fireEvent(\"on\"+c,a));return!0} var eventMatchers={HTMLEvents:/^(?:load|unload|abort|error|select|change|submit|reset|focus|blur|resize|scroll)$/,MouseEvents:/^(?:click|dblclick|mouse(?:down|up|over|move|out))$/}; " + "simulate(arguments[0],\"mousedown\",0,0); simulate(arguments[0],\"mousemove\",arguments[1],arguments[2]); simulate(arguments[0],\"mouseup\",arguments[1],arguments[2]); ";
        try {
            src.click();
            des.click();
            builder.dragAndDrop(src, des);
        } catch (Exception e) {
            executor.executeScript(event, src, xto, yto);
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#mouseOver (java.lang.String) */
    public void mouseOverByJavascript(String locator) {
        String newLocator = locator.replace(CSS_PREFIX, "");
        ((JavascriptExecutor) webDriver).executeScript("$('" + newLocator + "').mouseover();");
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#mouseOver (org.openqa.selenium.WebElement ) */
    public void mouseOverByJavascript(WebElement locator) {
        ((JavascriptExecutor) webDriver).executeScript("mouseover()", locator);
    }

    /**
     * This method uses the webDriver object to move over the desired child element and click using action class
     *
     * @param locator
     */
    public void moveToLocatorAndClick(String... locator) {
        Actions action = new Actions(webDriver);
        String localLocator = null, bLocator = null;
        for (String b : locator) {
            localLocator = b;
            if (!isElementVisible(localLocator)) {
                action.moveToElement(findElement(bLocator)).build().perform();
                delay(1);
            }
            action.moveToElement(findElement(localLocator, TEN)).build().perform();
            bLocator = localLocator;
        }
        try {
            action.moveToElement(findElement(localLocator)).click().build().perform();
        } catch (Exception e) {
            click(localLocator);
        }
    }

    /**
     * This method uses the webDriver object to move over the desired child element and click using action class
     *
     * @param locator
     */
    public void moveToLocator(String... locator) {
        Actions action = new Actions(webDriver);
        String localLocator = null, bLocator = null;
        for (String b : locator) {
            localLocator = b;
            if (!isElementVisible(localLocator)) {
                action.moveToElement(findElement(bLocator)).build().perform();
                delay(1);
            }
            action.moveToElement(findElement(localLocator, TEN)).build().perform();
            bLocator = localLocator;
        }
    }

    protected void scrollToLocatorAndClick(String locator) {
        scrollToLocator(locator);
        click(locator);
    }

    protected void scrollToLocator(String locator) {
        waitForElementVisible(locator, TEN);
        scrollToLocatorJS(locator);
        scrollBy(0, -10);
        delay(1);
    }

    public void scrollToLocatorJS(String locator) {
        WebElement element = findElement(locator);
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
        delay(1);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#open( java.lang.String) */
    public void open(String url) {
        webDriver.get(url);
    }

    /**
     * This method overrides the IE Browser certificate error
     */
    public void overrideIECertificateError() {
        if (getBrowserName().equalsIgnoreCase("InternetExplorer")) {
            String CONTINUE = "css=body.securityError";
            if (isElementVisible(CONTINUE, TEN)) {
                try {
                    log.info("Security Warning Present ...");
                    webDriver.get("javascript:document.getElementById('overridelink').click()");
                    log.info("Skipping Certificate Warning !!! ...");
                } catch (WebDriverException e) {
                    webDriver.navigate().to("javascript:document.getElementById('overridelink').click()");
                    log.info("Skipping Certificate Warning !!! ...");
                }
            }
        }
    }

    /**
     * This method overrides the Edge Browser certificate error
     */
    public void overrideEdgeCertificateError() {
        if (getBrowserName().equals("MICROSOFTEDGE")) {
            String CONTINUE = "css=id#invalidcert_continue";
            String DETAIS = "css=span[id='moreInformationDropdownSpan']";
            String LINK = "//a[@id='overridelink']/id";
            if (isElementVisible(CONTINUE, TEN)) {
                try {
                    log.info("Security Warning Present ...");
                    click(CONTINUE);
                    log.info("Skipping Certificate Warning !!! ...");
                } catch (Exception e) {
                    log.info("Certificate Warning not Present");
                }
            } else if (isElementVisible(DETAIS)) {
                clickUsingJavascript(DETAIS);
                clickUsingJavascript(LINK);
                waitForPageLoad();
            }
        }
    }

    /**
     * Determines if the specified element is visible. An element can be rendered invisible by setting the CSS
     * "visibility" property to "hidden", or the "display" property to "none", either for the element itself or
     * one if its ancestors. This method will fail if the element is not present.
     *
     * @param locator          an <a href="#locators">element locator</a>
     * @param timeoutInSeconds int time value
     * @return true if the specified element is visible, false otherwise
     */
    public boolean isElementVisible(String locator, int timeoutInSeconds) {
        try {
            (new WebDriverWait(webDriver, timeoutInSeconds)).until(ExpectedConditions.visibilityOfElementLocated(getSelector(locator)));
            return true;
        } catch (Exception e1) {
            return false;
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#reload() */
    public void reload() {
        webDriver.navigate().refresh();
    }

    public void scrollToEnd() {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("window.scrollBy(0, 250)", "");
    }

    public void scrollToTop() {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("window.scrollBy(0, -250)", "");
    }

    public void scrollBy(int xlocation, int ylocation) {
        String jscript = "window.scrollBy(" + xlocation + ", " + ylocation + ");";
        for (int i = 1; i <= 10; i++) {
            ((JavascriptExecutor) webDriver).executeScript(jscript);
        }
    }

    public void selectByVisibleText(String dropdown, String visibleText) {
        Select select = new Select(findElement(dropdown));
        select.selectByVisibleText(visibleText);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#select (java.lang.String, java.lang.String) */
    public void select(String dropdown, String optionLocator) {
        Select select = new Select(findElement(dropdown));
        List<WebElement> options = select.getOptions();
        log.debug("options found " + options.size() + " elements");
        String[] locator = optionLocator.split("=");
        log.debug("Locator has " + locator.length + " elements");
        String find;
        if (locator.length > 1) {
            find = locator[1];
        } else {
            find = locator[0];
        }
        log.debug(" find is " + find);
        if (locator[0].contains(VALUE)) {
            log.debug("checking value");
            for (WebElement we : options) {
                log.debug("Printing found options: " + we.getAttribute(VALUE));
                if (we.getAttribute(VALUE).equals(find)) {
                    we.click();
                    break;
                }
            }
        } else if (locator[0].contains("id")) {
            log.debug("checking id");
            for (WebElement we : options) {
                log.debug("Printing found options: " + we.getAttribute("id"));
                if (we.getAttribute("id").equals(find)) {
                    we.click();
                    break;
                }
            }
        } else if (locator[0].contains("index")) {
            log.debug("checking index");
            for (WebElement we : options) {
                log.debug("Printing found options: " + we.getAttribute("index"));
                if (we.getAttribute("index").equals(find)) {
                    we.click();
                    break;
                }
            }
        } else {
            boolean flag = false;
            log.debug("checking text or label, default option");
            for (WebElement we : options) {
                log.debug("Printing found options: " + we.getText());
                if (we.getText().equals(find)) {
                    we.click();
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                click(optionLocator);
            }
        }
    }

    /* (non-Javadoc)
     *
     * #isSelectOptionDisable(java.lang.String,
     * java.lang.String) */
    public boolean isSelectOptionEnable(String selectLocator, String optionLocator) {
        Select select = new Select(findElement(selectLocator));
        List<WebElement> options = select.getOptions();
        log.debug("options found " + options.size() + " elements");
        String[] locator = optionLocator.split("=");
        log.debug("Locator has " + locator.length + " elements");
        String find;
        if (locator.length > 1) {
            find = locator[1];
        } else {
            find = locator[0];
        }
        log.debug(" find is " + find);
        if (locator[0].contains(VALUE)) {
            log.debug("checking value");
            for (WebElement we : options) {
                log.debug("Printing found options: " + we.getAttribute(VALUE));
                if (we.getAttribute(VALUE).equals(find)) {
                    return we.isEnabled();
                }
            }
        } else if (locator[0].contains("id")) {
            log.debug("checking id");
            for (WebElement we : options) {
                log.debug("Printing found options: " + we.getAttribute("id"));
                if (we.getAttribute("id").equals(find)) {
                    return we.isEnabled();
                }
            }
        } else if (locator[0].contains("index")) {
            log.debug("checking index");
            for (WebElement we : options) {
                log.debug("Printing found options: " + we.getAttribute("index"));
                if (we.getAttribute("index").equals(find)) {
                    return we.isEnabled();
                }
            }
        } else {
            log.debug("checking text or label, default option");
            for (WebElement we : options) {
                log.debug("Printing found options: " + we.getText());
                if (we.getText().equals(find)) {
                    return we.isEnabled();
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#select (java.lang.String, java.lang.String) */
    public void selectAndTab(String selectLocator, String optionLocator) {
        select(selectLocator, optionLocator);
        focus(selectLocator);
    }

    public void selectFrame(String locator) {
        webDriver.switchTo().frame(locator);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#selectWindow (java.lang.String) */
    public void switchToWindow(String windowID) {
        webDriver.switchTo().window(windowID);
    }

    public String getWindowHandle() {
        return webDriver.getWindowHandle();
    }

    public void sendKeys(String locator, Keys k) {
        waitForElementFound(locator, FIVE);
        WebElement we = findElement(locator);
        we.sendKeys(k);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#sendKeys( java.lang.String, java.lang.String) */
    public void sendKeys(String locator, String value) {
        try {
            waitForElementFound(locator, FIVE);
            WebElement we = findElement(locator);
            we.sendKeys(value);
        } catch (ElementNotVisibleException e) {
            throw new RuntimeException("Unable to send keys to element: " + locator, e);
        }
    }

    /* (non-Javadoc)
     *
     * #sendOpenFile(java.lang.String,
     * java.lang.String) */
    public void sendOpenFile(String locator, String filePath) {
        findElement(locator).sendKeys(filePath);
    }

    /* (non-Javadoc)
     *
     * #sendSaveFile(java.lang.String,
     * java.lang.String) */
    public void upLoadFile(String locator, String filePath) {
        delay(2);
        findElement(locator).sendKeys(filePath);
        delay(2);
    }

    public void setCheckBoxState(String locator, boolean check) {
        waitForElementFound(locator, TEN);
        if (isChecked(locator) != check) {
            click(locator);
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#submit (java.lang.String) */
    public void submit(String locator) {
        findElement(locator).submit();
    }

    public TargetLocator switchTo() {
        return webDriver.switchTo();
    }

    public void switchToAlert() {
        try {
            webDriver.switchTo().alert();
        } catch (Exception e) {
            log.info(("Not able to swith to Alert " + e.getMessage()));
        }
    }

    public void switchToFrame(int index) {
        webDriver.switchTo().frame(index);
    }

    public void switchToFrame(String frameLocator) {
        webDriver.switchTo().frame(findElement(frameLocator));
    }

    public void switchToNewlyOpenedWindow() {
        Set<String> winHandles = webDriver.getWindowHandles();
        String handle = (String) winHandles.toArray()[winHandles.size() - 1];
        webDriver.switchTo().window(handle);
    }

    public void switchToParentWindow() {
        Set<String> winHandles = webDriver.getWindowHandles();
        String handle = (String) winHandles.toArray()[0];
        webDriver.switchTo().window(handle);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#type( java.lang.String, java.lang.String) */
    public void type(String locator, String value) {
        WebElement we = findElement(locator);
        try {
            we.click();
        } catch (Exception e) {
        }
        we.clear();
        we.sendKeys(value);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#typeWithoutClearing( java.lang.String, java.lang.String) */
    public void typeWithoutClearing(String locator, String value) {
        WebElement we = findElement(locator);
        // try to click otherwise ignore if it fails
        try {
            we.click();
        } catch (Exception e) {
        }
        we.sendKeys(value);
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#typeUsingJavascript( java.lang.String, java.lang.String) */
    public void typeUsingJavascript(String locator, String value) {
        WebElement we = findElement(locator);
        String event = "arguments[0].value=\"" + value + "\";";
        JavascriptExecutor executor = (JavascriptExecutor) webDriver;
        // Try to send keys the normal way but if it it fails, type using javascript
        try {
            we.sendKeys(value);
        } catch (Exception e) {
            executor.executeScript(event, we);
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#typeUsingRobot( java.lang.String, java.lang.String) */
    public void typeUsingRobot(String locator, String value) {
        WebElement we = findElement(locator);
        // try to click otherwise ignore if it fails
        try {
            we.click();
        } catch (Exception e) {
        }
        ClipboardOwner clipboardOwner = new ClipboardOwner() {
            @Override
            public void lostOwnership(Clipboard clipboard, Transferable contents) {
            }
        };
        Robot robot;
        try {
            robot = new Robot();
            try {
                we.sendKeys(value);
            } catch (Exception e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection stringSelection = new StringSelection(value);
                clipboard.setContents(stringSelection, clipboardOwner);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
            }
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController#uncheck (java.lang.String) */
    public void uncheck(String locator) {
        WebElement e = findElement(locator);
        if (e.isSelected()) {
            e.click();
        }
    }

    /* @ param timeout in milliseconds
     *
     * @see WebBrowserController#waitForAlert (int) */
    public void waitForAlert(int timeoutInSeconds) {
        for (int i = 0; i < timeoutInSeconds; i++) {
            try {
                String alertText = getAlert();
                if (!alertText.isEmpty()) {
                    log.debug("Alert: " + alertText);
                    return;
                }
            } catch (Exception e) {
            }
            delay(1);
        }
        throw new RuntimeException("Alert not found");
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController# waitForElementFound(java.lang.String, int) */
    public void waitForElementFound(String locator, int timeoutInSeconds) {
        String msg = "Wait %ss for [%s] on page [%s]";
        log.debug(String.format(msg, timeoutInSeconds, locator, this.getClass()));
        try {
            WebElement e = (new WebDriverWait(webDriver, timeoutInSeconds)).until(ExpectedConditions.presenceOfElementLocated(getSelector(locator)));
            log.debug("DEBUG: Element found");
            if (e != null) {
                return;
            } else {
                throw new ElementNotVisibleException(locator + " did not load.");
            }
        } catch (TimeoutException toe) {
            throw new ElementNotVisibleException(locator + " did not load with TimeoutException " + toe.getMessage());
        }
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController# waitForElementHidden(java.lang.String, int) */
    public void waitForElementHidden(String locator, int timeoutInSeconds) {
        String msg = "Wait %ss for [%s] on page [%s]";
        log.debug(String.format(msg, timeoutInSeconds, locator, this.getClass().getSimpleName()));
        boolean visible = true;
        for (int i = 0; i < timeoutInSeconds; i++) {
            visible = isElementVisible(locator);
            if (!visible) {
                return;
            }
            delay(1);
        }
        throw new ElementNotVisibleException(locator + " is still visible on the Web Page. ");
    }

    /* (non-Javadoc)
     *
     * @see WebBrowserController# waitForElementVisible(java.lang.String, int) */
    public void waitForElementVisible(String locator, int timeoutInSeconds) {
        String msg = "Wait %ss for [%s] on page [%s]";
        log.debug("waitForElementVisible should time out in " + timeoutInSeconds);
        log.debug(String.format(msg, timeoutInSeconds, locator, this.getClass().getSimpleName()));
        WebElement e = (new WebDriverWait(webDriver, timeoutInSeconds)).until(ExpectedConditions.visibilityOfElementLocated(getSelector(locator)));
        log.debug("Element found");
        if (e != null) {
            return;
        } else {
            throw new ElementNotVisibleException(locator + " did not become visible.");
        }
    }

    public void waitForText(String text, int timeoutInSeconds) {
        for (int i = 0; i < timeoutInSeconds; i++) {
            try {
                if (isTextPresent(text)) {
                    log.debug("Text " + text + " found.");
                }
            } catch (Exception e) {
            }
            delay(1);
        }
    }

    public void windowMaximize() {
        webDriver.manage().window().maximize();
    }

    public int getNumberOfOpenWindows() {
        int windows = 0;
        try {
            windows = webDriver.getWindowHandles().size();
        } catch (Exception e) {
            log.warn("All browser sessions appear to be closed: {}", e.getMessage());
        }
        return windows;
    }

    public boolean isAlertPresent() {
        try {
            webDriver.switchTo().alert();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForElementClickable(String locator, int timeoutInSeconds) {
        WebElement e = null;
        log.debug("Wait for Element Visible should time out in " + timeoutInSeconds);
        e = (new WebDriverWait(webDriver, timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable(getSelector(locator)));
        log.debug("end wait for Element Visible");
        if (e != null) {
            return;
        } else {
            throw new ElementNotVisibleException(locator + " did not become visible.");
        }
    }

    public void waitForPageLoad() {
        ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };
        WebDriverWait wait = new WebDriverWait(webDriver, 70);
        wait.until(pageLoadCondition);
    }

    public String getMainFrame() {
        String nameOrId = null;
        List<WebElement> iframes = webDriver.findElements(By.tagName("iframe"));
        if (iframes.size() != 0) {
            nameOrId = iframes.get(0).getAttribute("id");
            if (nameOrId == null) {
                nameOrId = iframes.get(0).getAttribute("name");
                return nameOrId;
            }
        }
        return nameOrId;
    }

    public void switchToDefaultContent() {
        try {
            (webDriver).switchTo().defaultContent();
        } catch (WebDriverException e) {
            log.error("unable to switch to default content", e);
        }
    }

    public void openNewTab() {
        JavascriptExecutor jse = (JavascriptExecutor) getWebDriver();
        jse.executeScript("window.open()");
        switchToNewlyOpenedWindow();
    }

    /**
     * selects a random element in the dropdown list
     *
     * @param locator - identified dropdown list
     */
    public void selectRandomItemFromDropDown(String locator) {
        waitForDropdownPopulated(locator);
        List<WebElement> options = getSelectOptions(locator);
        Random r = new Random();
        select(locator, options.get(r.nextInt(options.size())).getText());
    }

    /**
     * selects an item from a dropdown list that contains the parameter stringLike
     *
     * @param locator    - identifies the dropdown list
     * @param stringLike - string to match in the elements of the dropdown list
     * @return the text name of the "matching" item selected in the dropdown; empty string if no match found
     */
    public String selectLikeItemFromDropDown(String locator, String stringLike) {
        String opt = null;
        waitForDropdownPopulated(locator);
        List<WebElement> options = getSelectOptions(locator);
        for (WebElement el : options) {
            opt = el.getText();
            if (opt.toLowerCase().contains(stringLike.toLowerCase())) {
                selectItemFromDropDown(locator, opt);
                log.debug("value as selected in dropdown is " + opt);
                return opt;
            }
        }
        log.warn("Returning empty string, No Service name found in dropdown matching " + stringLike);
        return opt;
    }

    /**
     * selects the correct item from the given select item in the GUI. If the value is not set (null or "")
     * this method does nothing, else it will try to select the item.<br>
     * <br>
     * If the value does not contain 'label=' it is added automatically.
     */
    public void selectItemFromDropDown(String locator, String value) {
        delay(1);
        waitForDropdownPopulated(locator);
        if (value.startsWith("label=")) {
            value = value.replace("label=", "");
        }
        select(locator, value);
    }

    /**
     * if dropdown is a required field, will select from the list, depending on content of parameter item if
     * item is empty, selects random element from the dropdown list if item has content, selects it from the
     * dropdown list if dropdown isn't required field, does nothing
     *
     * @param locator    - identifies the dropdown list
     * @param item       - element to select on the dropdown list
     * @param isRequired - is this a required field
     */
    public void selectItemFromDropDown(String locator, String item, boolean isRequired) {
        if (item == null || item.equals("")) {
            if (isRequired) {
                selectRandomItemFromDropDown(locator);
            }
            return;
        }
        waitForDropdownPopulated(locator);
        try {
            select(locator, item);
        } catch (Exception e) {
            log.warn(item + " not found in the select, finding a good match");
            List<WebElement> options = getSelectOptions(locator);
            for (WebElement el : options) {
                String opt = el.getText();
                if (opt.contains(item)) {
                    select(locator, opt);
                    return;
                }
            }
            selectItemFromDropDown(locator, item);
        }
    }

    private void waitForDropdownPopulated(String locator) {
        List<WebElement> options = getSelectOptions(locator);
        for (int l = 1; l <= 30; l++) {
            options = getSelectOptions(locator);
            if (options.size() >= 2) {
                delay(2);
                break;
            } else {
                delay(1);
            }
        }
    }

    /**
     * selects an item from a dropdown list that starts with the parameter stringStartsWith
     *
     * @param locator    - identifies the dropdown list
     * @param startsWith - string to match starts with in elements of the dropdown list
     */
    public void selectItemStartWithFromDropDown(String locator, String startsWith) {
        waitForDropdownPopulated(locator);
        List<WebElement> options = getSelectOptions(locator);
        for (WebElement el : options) {
            String opt = el.getText();
            if (opt.toLowerCase().startsWith(startsWith.toLowerCase())) {
                selectItemFromDropDown(locator, opt);
            }
        }
    }

    /**
     * This method tries to retrieve the system platform and return if it fails, it logs the exception and returns null
     *
     * @return the system platform, or null
     */
    public String getPlatformName() {
        String version = System.getProperty("os.version");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "Windows " + version;
        } else if (os.contains("nux") || os.contains("nix")) {
            return "Linux " + version;
        } else if (os.contains("mac")) {
            return "Mac " + version;
        } else {
            return "Other " + version;
        }
    }

    public void close()
            throws Exception {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    public String takeScreenshot(String screenshotPath, String fileName) {
        File directory = new File(screenshotPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        screenshotPath = screenshotPath + "/" + fileName;
        File scrFile = ((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.FILE);
        try {
            FileHandler.copy(scrFile, new File(screenshotPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return screenshotPath;
    }

    /**
     * This method will return the current system time in user defined format.
     */
    public String getSystemTime(String hmma) {
        // SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        SimpleDateFormat sdf = new SimpleDateFormat(hmma);
        return sdf.format(new Date());
    }

    /**
     * This method will return the current system date in user defined format.
     */
    public String getSystemDate(String dd_M_yyyy) {
        // SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
        SimpleDateFormat sdf = new SimpleDateFormat(dd_M_yyyy);
        return sdf.format(new Date());
    }


    public enum FileSearchType {
        Directory,
        File,
        Both;

        FileSearchType() {
        }
    }


    /**
     * This method holds the data in order to configure proper testing environment
     */
    private enum BrowserType {
        MARIONETTE("firefoxm"),
        FIREFOX_DRIVER("firefox"),
        CHROME_DRIVER("chrome"),
        SAFARI("safari"),
        SAUSLABS("sauslabs"),
        EDGE("edge");

        BrowserType(String stringName) {
            this.setBrowserName(stringName);
        }


        private static BrowserType getBrowserTypeFromString(String stringName) {
            String a = stringName.toLowerCase().replaceAll(" ", "").trim();
            if ((a.equals("ff")) || (a.equals("firefox")) || (a.startsWith("firefoxdriver"))) {
                return BrowserType.FIREFOX_DRIVER;
            } else if ((a.equals("ffm")) || (a.equals("firefoxm")) || (a.contains("marionette")) || (a.equals("firefoxmarionette"))) {
                return BrowserType.MARIONETTE;
            } else if ((a.equals("chrome")) || (a.equals("chromedriver")) || (a.equals("googlechrome"))) {
                return BrowserType.CHROME_DRIVER;
            } else if ((a.equals("internetexplorer")) || (a.equals("ie")) || (a.equals("internet_explorer")) || (a.startsWith("ie"))) {
                return BrowserType.SAUSLABS;
            } else if (a.equals("safari")) {
                return BrowserType.SAFARI;
            } else if (a.equals("edge") || (a.contains("microsoftedge"))) {
                return BrowserType.EDGE;
            } else {
                return BrowserType.FIREFOX_DRIVER;
            }
        }


        private void setBrowserName(String stringName) {
        }
    }
}