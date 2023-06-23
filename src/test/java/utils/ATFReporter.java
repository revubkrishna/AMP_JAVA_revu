package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ATFReporter {
    private static final Logger log = LoggerFactory.getLogger(ATFReporter.class);
    private final static HashMap<String, String> testDetails = new HashMap<>();
    private final static HashMap<String, String> videoDetails = new HashMap<>();
    private final static HashMap<String, String> testScriptNames = new HashMap<>();
    private static final String TAB_ICON = "https://www2.deloitte.com/content/dam/resources/deloitte/icons/favicon.ico";
    private static final String LOGO_IMG_DELOITTE = "https://www.tangentia.com/wp-content/uploads/2018/10/Deloitte-Logo-1024x274.png";
    public static int testFailCount = 0;
    public static String clientImage;
    private static ExtentReports extentReporter;
    private static ExtentTest extentTest;
    private static String resultsFolderPath, displayName, browserName, testCaseName, testGroupName, testCaseFolderPath, screenshotsFolderPath, executionType, recordedVideosFolderPath, recordedVideosFileName;
    private static int testPassCount = 0, testSkipCount = 0, passTestCount = 0, failTestCount = 0, skipTestCount = 0, testCasesCount = 0, testStepCount;
    private static long startTime = 0, endTime = 0, duration = 0, totalDuration = 0;

    private static ExtentReports GetInstance() {
        executionType = GetProperty(ATFConstants.SYSTEM);
        if (extentReporter == null) {
            String homePath = System.getProperty("user.dir");
            String os = System.getProperty("os.name").toLowerCase();
            int splitString;
            if (os.contains("mac") || (os.contains("nux") || os.contains("nix"))) {
                splitString = homePath.lastIndexOf('/');
            } else {
                splitString = homePath.lastIndexOf('\\');
            }
            String reportName = homePath.substring(splitString + 1);
            resultsFolderPath = homePath + "/Results/" + executionType;
            testCaseFolderPath = resultsFolderPath + "/TestScripts";
            recordedVideosFolderPath = resultsFolderPath + "/RecordedVideos";
            screenshotsFolderPath = testCaseFolderPath + "/CapturedScreenshots";
            try {
                File e = new File(resultsFolderPath);
                if (e.exists()) {
                    FileUtils.deleteDirectory(new File(resultsFolderPath));
                }
                File f = new File(screenshotsFolderPath);
                if (!f.exists()) {
                    f.mkdirs();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            extentReporter = new ExtentReports();
            ExtentHtmlReporter reporter = new ExtentHtmlReporter(resultsFolderPath + "/ExtentReport.html");
            reporter.config().setTheme(Theme.DARK);
            reporter.config().setDocumentTitle(ATFConstants.PROJECT_NAME);
            reporter.config().setReportName(reportName);
            extentReporter.attachReporter(reporter);
        }
        return extentReporter;
    }


    public static void ReportPass(String message, ATFWebController webController) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy_hh-mm-ss_a");
        String date = sdf.format(new Date());
        message = "Passed: --> " + message.trim();
        String path = "";
        if (GetProperty(ATFConstants.SCREENSHOT).equalsIgnoreCase("true")) {
            String fileName = date + ".png";
            path = webController.takeScreenshot(screenshotsFolderPath, fileName);
            try {
                extentTest.pass(message + extentTest.addScreenCaptureFromPath(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            path = path.split(executionType + "/")[1];
        } else {
            extentTest.log(Status.PASS, message);
        }
        log.info(message);
        LogTestCaseHTML("Pass", path, message);
        testPassCount++;
    }


    public static void ReportFail(String message, ATFWebController webController) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy_hh-mm-ss_a");
        String date = sdf.format(new Date());
        message = "Failed: --> " + message.trim();
        String fileName = date + ".png";
        String path = webController.takeScreenshot(screenshotsFolderPath, fileName);
        try {
            extentTest.fail(message + extentTest.addScreenCaptureFromPath(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(message);
        path = path.split(executionType + "/")[1];
        LogTestCaseHTML("Fail", path, message);
        testFailCount++;
    }


    public static void ReportInfo(String message) {
        message = message.trim();
        extentTest.log(Status.INFO, message);
        log.info("" + message);
        LogTestCaseHTML("Info", "", message);
    }


    public static void ReportSkip(String message) {
        message = message.trim();
        extentTest.log(Status.SKIP, message);
        log.info("" + message);
        LogTestCaseHTML("Skip", "", message);
        testSkipCount++;
    }


    public static boolean IsLocatorVisible(String locator, String message, ATFWebController webController) {
        boolean isVisible = false;
        if (webController.isElementVisible(locator)) {
            ReportPass(message.trim() + " is displaying as Expected", webController);
            isVisible = true;
        } else {
            ReportFail(message.trim() + " was not displaying as Expected", webController);
        }
        return isVisible;
    }


    public static boolean IsLocatorNotVisible(String locator, String message, ATFWebController webController) {
        boolean isVisible = false;
        if (!webController.isElementVisible(locator)) {
            ReportPass(message + " is hidden on the page, as Expected", webController);
            isVisible = true;
        } else {
            ReportFail(message + ": '" + locator + "' is not hidden on the page, Not Expected", webController);
        }
        return isVisible;
    }


    public static void VerifyStringValues(String expectedValue, String actualValue, String message, ATFWebController webController) {
        String passMessage = "Expected " + message + " { " + expectedValue + " } Matches Actual " + message + " { " + actualValue + " } ";
        String failMessage = "Expected " + message + " { " + expectedValue + " } Not Matches Actual " + message + " { " + actualValue + " } ";
        if (!expectedValue.equals(actualValue)) {
            ReportFail(failMessage, webController);
        } else {
            ReportPass(passMessage, webController);
        }
    }


    public static void VerifyIntegerValues(int expectedValue, int actualValue, String message, ATFWebController webController) {
        String passMessage = "Expected " + message + " { " + expectedValue + " } Matches Actual " + message + " { " + actualValue + " } ";
        String failMessage = "Expected " + message + " { " + expectedValue + " } Not Matches Actual " + message + " { " + actualValue + " } ";
        if (expectedValue != actualValue) {
            ReportFail(failMessage, webController);
        } else {
            ReportPass(passMessage, webController);
        }
    }


    public static void VerifyLocatorText(String locator, String verificationMessage, ATFWebController webController) {
        ReportInfo("Verifying '" + verificationMessage + "' text on the Page");
        if (webController.isElementVisible(locator)) {
            String actualMessage = webController.getText(locator);
            String passMessage = "Expected { " + verificationMessage + " } Matches Actual --> { " + actualMessage + " }";
            String failMessage = "Expected { " + verificationMessage + " } Not Matches Actual --> { " + actualMessage + " }";
            if (!verificationMessage.equals(actualMessage)) {
                ReportFail(failMessage, webController);
            } else {
                ReportPass(passMessage, webController);
            }
        } else {
            ReportFail(verificationMessage + ": '" + locator + "' was not displaying on the page", webController);
        }
    }


    public static void StartTest(String[] testNameDescription) {
        testStepCount = 0;
        testFailCount = 0;
        testPassCount = 0;
        testSkipCount = 0;
        Calendar cal = Calendar.getInstance();
        startTime = cal.getTimeInMillis();
        testCaseName = testNameDescription[0];
        displayName = testNameDescription[3];
        browserName = testNameDescription[4];
        testGroupName = ToTitleCase(testNameDescription[5]);
        extentReporter = GetInstance();
        extentTest = extentReporter.createTest(displayName);
        SimpleDateFormat sdf = new SimpleDateFormat("hhmmssa");
        recordedVideosFileName = testCaseName + "_" + sdf.format(new Date());
        StartScreenRecording();
        CreateTestCaseHTML();
        ReportInfo(testNameDescription[2] + ", Test Script: {" + testCaseName + "} with Description: {" + testNameDescription[1] + "}");
        testCasesCount++;
    }


    public static void EndTest() {
        Calendar cal2 = Calendar.getInstance();
        endTime = cal2.getTimeInMillis();
        duration = endTime - startTime;
        totalDuration = totalDuration + duration;
        extentTest.log(Status.INFO, "Execution Time: " + ReturnTime(duration));
        log.info("" + "Execution Time: " + ReturnTime(duration));
        CloseTestCaseHTML();
        extentReporter.flush();
        StopScreenRecording();
        videoDetails.put(testCaseName, recordedVideosFileName + ATFScreenRecorder.fileSavedFormat);
        if (testFailCount == 0) {
            if (testSkipCount == 1) {
                testDetails.put(testCaseName, "SKIP");
                testScriptNames.put(testCaseName, displayName);
                skipTestCount++;
            } else {
                testDetails.put(testCaseName, "PASS");
                testScriptNames.put(testCaseName, displayName);
                passTestCount++;
            }
        } else {
            testDetails.put(testCaseName, "FAIL");
            testScriptNames.put(testCaseName, displayName);
            failTestCount++;
        }
    }


    public static void PublishTestExecutionSummary() {
        String summaryHTMLPath = resultsFolderPath + "/SummaryReport.htm";
        try {
            File f = new File(summaryHTMLPath);
            FileWriter bw;
            try {
                String newData = "";
                int i = 0;
                for (String a : testDetails.keySet()) {
                    newData = newData + "<tr> <td align=center style='width:5%'> " + ++i + "</td><td align=left style='width:60%'>" + testScriptNames.get(a).trim() + "</td>";
                    if (testDetails.get(a).trim().equalsIgnoreCase("FAIL")) {
                        newData = newData + "<td style='width:15%' ><b><center><font color=red>FAIL</a></td></tr>";
                    } else if (testDetails.get(a).trim().equalsIgnoreCase("PASS")) {
                        newData = newData + "<td style='width:15%' ><b><center><font color=green>PASS</td></tr>";
                    } else if (testDetails.get(a).trim().equalsIgnoreCase("SKIP")) {
                        newData = newData + "<td style='width:15%' ><b><center><font color=black>SKIP</td></tr>";
                    }
                }
                bw = new FileWriter(f, true);
                String text = "";
                text = "<html> <head> <title>Execution Summary</title>";
                text = text + "<link rel='shortcut icon' type='image/x-icon' media='all' href='" + TAB_ICON + "'>";
                text = text + "<style> #tabNames { font-family: Trebuchet MS, Arial, Helvetica, sans-serif; border-collapse: collapse; width: 80%; margin: auto; text-align: center; } #tabNames td, #tabNames th { border: 1px solid #ABB2B9; padding: 8px;   padding-top: 12px; padding-bottom: 12px;   width: auto; } #tabNames th { padding-top: 12px; padding-bottom: 12px; text-align: center; color: white; background-color: #333; }   #TestScriptDetails { font-family: Trebuchet MS, Arial, Helvetica, sans-serif; border-collapse: collapse; width: 80%; margin: auto; text-align: center; }   #TestScriptDetails td, #TestScriptDetails th { border: 1px solid #ABB2B9; padding: 10px;   padding-top: 6px; padding-bottom: 6px;   width: 50%; } #TestScriptDetails tr:nth-child(odd){background-color: #ffffff;} #TestScriptDetails tr:nth-child(even){background-color: #FFFFFF;} #TestScriptDetails tr:hover {background-color: #333; color: white;} #TestScriptDetails th { padding-top: 7px; padding-bottom: 7px; color: white; background-color: #333; }  </style> </head><body>";
                if (!StringUtils.isEmpty(clientImage)) {
                    text = text + "<img src='" + clientImage + "' style='width: 200px; margin-right: 9.5%; float: left; margin-top:25px; margin-bottom: 10px;'>";
                }
                text = text + "<img src='" + LOGO_IMG_DELOITTE + "' style='width: 200px; margin-right: 9.5%; float: right; margin-top:25px; margin-bottom: 10px;'><br> </br><br> </br>";
                text = text + "<br> </br><table id=tabNames> <tr> <th>Test Execution Summary</th> </tr> </table>";
                text = text + "<table id=tabNames> <tr> <th>Suite Type</th> <th>Browser</th> <th>Test Scripts Executed</th> <th>Test Scripts Passed</th> <th>Test Scripts Failed</th> <th>Test Scripts Skipped</th> <th>Execution Time</th> </tr>";
                text = text + "<tr> <td>" + testGroupName + "</td> <td>" + ToTitleCase(browserName) + "</td> <td>" + testCasesCount + "</td> <td>" + passTestCount + "</td> <td>" + failTestCount + "</td> <td>" + skipTestCount + "</td> <td>" + ReturnTime(totalDuration) + "</td> </tr> </table> ";
                text = text + "<br> </br><table id=TestScriptDetails> <tr> <th>Test Script Details</th> </tr> </table> <table id=TestScriptDetails> <tr> <th align=center style='width:5%'>SNo.</th> <th align=left style='width:60%'>Test Script Name</th><th align=center style='width:15%'>Test Script Status</th></tr> </table> <table id=TestScriptDetails> " + newData + " </table><br> </br><br> </br></body></html>";
                bw.write(text);
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AddRowToScenarioHTML();
        ExportReport();
    }


    private static void CreateTestCaseHTML() {
        String testCaseHTML = testCaseFolderPath + "/" + testCaseName + ".htm";
        File f = new File(testCaseHTML);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BufferedWriter bw;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM yyyy");
        try {
            bw = new BufferedWriter(new FileWriter(f));
            String text = "";
            text = "<html> <head> <title>Execution Summary</title>";
            text = text + "<link rel='shortcut icon' type='image/x-icon' media='all' href='" + TAB_ICON + "'>";
            text = text + "<style> #TestScriptDetails { font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif; border-collapse: collapse; width: 80%; margin: auto; text-align: center; clear: left;} #TestScriptDetails tr:hover {background-color: #333; color: white;} #TestScriptDetails td, #TestScriptDetails th { border: 1px solid #ABB2B9; padding: 10px; padding-top: 6px; padding-bottom: 6px; width: auto; } #TestScriptDetails th { padding-top: 7px; padding-bottom: 7px; color: white; background-color: #333; width: auto; } #Transform { text-transform: uppercase; } </style> </head> <body> ";
            if (!StringUtils.isEmpty(clientImage)) {
                text = text + "<img src='" + clientImage + "' style='width: 200px; margin-right: 9.5%; float: left; margin-top:25px; margin-bottom: 10px;'>";
            }
            text = text + "<img src='" + LOGO_IMG_DELOITTE + "' style='width: 200px; margin-right: 9.5%; float: right; margin-top:25px; margin-bottom: 10px;'> <br> </br><br> </br><br> </br>";
            text = text + "<table id=TestScriptDetails> <tr> <th><font size='5'>Test Execution Summary</font></th> </tr> </table>";
            text = text + "<table id=TestScriptDetails> <tr> <th align='left'> Test Script: " + displayName + "</th> <th align='right' style='width:16%'> " + sdf.format(cal.getTime()) + " </th> </tr></table>";
            text = text + "<table id=TestScriptDetails> <tr> <th style='width:8%'>Step No</th> <th >Step Description</th><th style='width:11%'>Step Status</th><th style='width:16%'>Time of Execution</th> </table></body></html>";
            bw.write(text);
            bw.close();
        } catch (Exception ignored) {
        }
    }


    private static void LogTestCaseHTML(String status, String path, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy_hh-mm-ss_a");
        String testCaseHTML = testCaseFolderPath + "/" + testCaseName + ".htm";
        String text = "";
        try {
            File f = new File(testCaseHTML);
            FileWriter bw;
            try {
                bw = new FileWriter(f, true);
                text = "<style> #TestScriptDetails { font-family: Trebuchet MS, Arial, Helvetica, sans-serif; border-collapse: collapse; width: 80%; margin: auto; text-align: center; }   #TestScriptDetails td, #TestScriptDetails th { border: 1px solid #ABB2B9; padding: 10px;   padding-top: 6px; padding-bottom: 6px;   width: 50%; } #TestScriptDetails tr:nth-child(odd){background-color: #ffffff;} #TestScriptDetails tr:nth-child(even){background-color: #FFFFFF;} #TestScriptDetails tr:hover {background-color: #333; color: white;} #TestScriptDetails th { padding-top: 7px; padding-bottom: 7px; color: white; background-color: #333; }  #Transform {text-transform: uppercase;}</style> </head><body>";
                text = text + "<table id='TestScriptDetails'> <tr> <td style='width:8%'>" + ++testStepCount + "</td> <td align='left' > " + message.toString() + "  </td>";
                if (status.equals("Pass")) {
                    if (GetProperty(ATFConstants.SCREENSHOT).equalsIgnoreCase("true")) {
                        text = text + "<td style='width:11%' ><b><center><a href=../" + path + "><font color=green>PASS</a></td>";
                    } else {
                        text = text + "<td style='width:11%' ><color=green><b><font color=green>PASS</td>";
                    }
                } else if (status.equals("Fail")) {
                    text = text + "<td style='width:11%' ><b><center><a href=../" + path + "><font color=red><b>FAIL</a></td>";
                } else if (status.equals("Info")) {
                    text = text + "<td style='width:11%' ><color=black><center>INFO</td>";
                } else if (status.equals("Skip")) {
                    text = text + "<td style='width:11%' ><color=black><center>SKIP</td>";
                }
                Calendar cal = Calendar.getInstance();
                sdf = new SimpleDateFormat("hh:mm:ss a");
                text = text + "<td style='width:16%'>" + sdf.format(cal.getTime()) + "</td></tr></table>";
                bw.write(text);
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void CloseTestCaseHTML() {
        String testCaseHTML = testCaseFolderPath + "/" + testCaseName + ".htm";
        File f = new File(testCaseHTML);
        FileWriter bw;
        try {
            bw = new FileWriter(f, true);
            String text = "<style> #TestScriptDetails { font-family: Trebuchet MS, Arial, Helvetica, sans-serif; border-collapse: collapse; width: 80%; margin: auto; text-align: center; }   #TestScriptDetails td, #TestScriptDetails th { border: 1px solid #ABB2B9; padding: 10px;   padding-top: 6px; padding-bottom: 6px;   width: auto; } #TestScriptDetails tr:nth-child(odd){background-color: #ffffff;} #TestScriptDetails tr:nth-child(even){background-color: #FFFFFF;} #TestScriptDetails tr:hover {background-color: #333; color: white;} #TestScriptDetails th { padding-top: 7px; padding-bottom: 7px; color: white; background-color: #333; }  #Transform {text-transform: uppercase;}</style> </head><body>";
            text = text + "<br> </br><table id='TestScriptDetails'>";
            text = text + "<tr><th align='left' style='width:auto'><font size=2>Total Steps: " + testStepCount + "</font></th>";
            text = text + "<th align='left' style='width:auto'><font size=2>Verifications Passed: " + testPassCount + "</font></th>";
            text = text + "<th align='left' style='width:auto'><font size=2>Steps Failed: " + testFailCount + "</font></th>";
            text = text + "<th align='left' style='width:auto'><font size=2>Steps Skipped: " + testSkipCount + "</font></th>";
            text = text + "<th align='left' style='width:auto'><font size=2>Execution Time: " + ReturnTime(duration) + "</font> </th></tr></table><br> </br><br> </br><br> </br>";
            bw.write(text);
            String filterCode = "";
            bw.write(filterCode);
            bw.close();
        } catch (Exception e) {
        }
    }


    private static void AddRowToScenarioHTML() {
        String path = testCaseFolderPath.split("Results/")[1];
        String path1 = recordedVideosFolderPath.split("Results/")[1];
        String summaryHTMLPath = resultsFolderPath + "/" + testGroupName.trim() + "SuiteExecutionReport.htm";
        File f = new File(summaryHTMLPath);
        FileWriter bw;
        try {
            bw = new FileWriter(f, true);
            String text = "";
            text = "<html> <head> <title>Execution Summary</title>";
            text = text + "<link rel='shortcut icon' type='image/x-icon' media='all' href='" + TAB_ICON + "'>";
            text = text + "<style> #tabNames { font-family: Trebuchet MS, Arial, Helvetica, sans-serif; border-collapse: collapse; width: 80%; margin: auto; text-align: center; } #tabNames td, #tabNames th { border: 1px solid #ABB2B9; padding: 8px;   padding-top: 12px; padding-bottom: 12px;   width: auto; } #tabNames th { padding-top: 12px; padding-bottom: 12px; text-align: center; color: white; background-color: #333; }   #TestScriptDetails { font-family: Trebuchet MS, Arial, Helvetica, sans-serif; border-collapse: collapse; width: 80%; margin: auto; text-align: center; clear: left; }   #TestScriptDetails td, #TestScriptDetails th { border: 1px solid #ABB2B9; padding: 10px;   padding-top: 6px; padding-bottom: 6px;   width: 50%; } #TestScriptDetails tr:nth-child(odd){background-color: #ffffff;} #TestScriptDetails tr:nth-child(even){background-color: #FFFFFF;} #TestScriptDetails th { padding-top: 7px; padding-bottom: 7px; color: white; background-color: #333; }  </style> </head><body>";
            if (!StringUtils.isEmpty(clientImage)) {
                text = text + "<img src='" + clientImage + "' style='width: 200px; margin-right: 9.5%; float: right; margin-top:25px; margin-bottom: 10px;'>";
            }
            text = text + "<img src='" + LOGO_IMG_DELOITTE + "' style='width: 200px; margin-right: 9.5%; float: right; margin-top:25px; margin-bottom: 10px;'> <br> </br><br> </br> ";
            String newData = "";
            int i = 0;
            for (String a : testDetails.keySet()) {
                newData = newData + "<tr> <td align=center style='width:5%'> " + ++i + "</td><td align=left style='width:52%'>" + testScriptNames.get(a).trim() + "</a></td>";
                if (testDetails.get(a).trim().equalsIgnoreCase("FAIL")) {
                    newData = newData + "<td style='width:13%' ><b><center><a href=../" + path + "/" + a.trim() + ".htm><font color=red>FAIL</a></td>";
                } else if (testDetails.get(a).trim().equalsIgnoreCase("PASS")) {
                    newData = newData + "<td style='width:13%' ><b><center><a href=../" + path + "/" + a.trim() + ".htm><font color=green>PASS</td>";
                } else if (testDetails.get(a).trim().equalsIgnoreCase("SKIP")) {
                    newData = newData + "<td style='width:13%' ><b><center><a href=../" + path + "/" + a.trim() + ".htm><font color=black>SKIP</td>";
                }
                if (GetProperty("captureVideo").equalsIgnoreCase("true")) {
                    String tempValue = ATFScreenRecorder.fileSavedFormat.replace(".", "");
                    if (!StringUtils.isEmpty(tempValue)) {
                        newData = newData + "<td style='width:14%' ><b><center><a href=../" + path1 + "/" + videoDetails.get(a) + "><font color=green>Click Here</td>";
                    } else {
                        newData = newData + "<td style='width:14%' ><center><font color=black>Video Not Available</td>";
                    }
                } else {
                    newData = newData + "<td style='width:14%' ><center><font color=black>Video Not Available</td>";
                }
            }
            text = text + "<br> </br> <table id=TestScriptDetails> <tr> <th><font size='5'>" + testGroupName + " Test Suite Summary</font></th> </tr> </table>";
            text = text + "<table id=TestScriptDetails> <tr> <th align=center style='width:5%'>SNo.</th> <th align=left style='width:52%'>Test Script Name</th><th align=center style='width:13%'>Test Script Status</th> <th align=center style='width:14%'>Video Link</th></tr> </table> <table id=TestScriptDetails> " + newData + " </table>";
            text = text + "<br> </br><table id='TestScriptDetails'>";
            text = text + "<tr><th align='left' style='width:auto'><font size=2>Test Scripts Executed: " + testCasesCount + "</font></th>";
            text = text + "<th align='left' style='width:auto'><font size=2>Test Scripts Passed: " + passTestCount + "</font></th>";
            text = text + "<th align='left' style='width:auto'><font size=2>Test Scripts Failed: " + failTestCount + "</font></th>";
            text = text + "<th align='left' style='width:auto'><font size=2>Test Scripts Skipped: " + skipTestCount + "</font></th>";
            text = text + "<th align='left' style='width:auto'><font size=2>Total Execution Time: " + ReturnTime(totalDuration) + "</font></th></tr></table><br> </br><br> </br><br> </br></body></html>";
            bw.write(text);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String ToTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }


    private static String ReturnTime(Long duration) {
        String format = String.format("%%0%dd", 2);
        duration = duration / 1000;
        String seconds = String.format(format, duration % 60);
        String minutes = String.format(format, (duration % 3600) / 60);
        String hours = String.format(format, duration / 3600);
        return hours + ":" + minutes + ":" + seconds;
    }


    private static void StartScreenRecording() {
        if (GetProperty("captureVideo").equalsIgnoreCase("true")) {
            ATFScreenRecorder.StartRecording(recordedVideosFolderPath, recordedVideosFileName);
        }
    }


    private static void StopScreenRecording() {
        if (GetProperty("captureVideo").equalsIgnoreCase("true")) {
            ATFScreenRecorder.StopRecording();
        }
    }


    private static String GetProperty(String parameter) {
        return ATFProperties.GetProperty(ATFConstants.AUTOMATION_PROPERTIES_PATH, parameter);
    }


    private static void ExportReport() {
        String endFolder = System.getProperty("user.dir") + "/Results/" + GetProperty(ATFConstants.SYSTEM);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy_hh-mm-ss_a");
        String date = sdf.format(new Date());
        File trDir = new File(ATFConstants.AUTOMATION_REPORTS_PATH + date + "/" + GetProperty(ATFConstants.SYSTEM));
        if (!trDir.exists()) {
            trDir.mkdirs();
        }
        File source = new File(endFolder);
        File dest = new File(trDir.toString());
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}