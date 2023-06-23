package utils;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import cases.ATFBaseTest;
import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ATFTestMatrix
{
   static Workbook wbook;
   static WritableWorkbook wwbCopy;
   static String ExecutedTestCasesSheet;
   static WritableSheet shSheet;
   static String sheetName;
   static String sheetNameCreated, excelPath, excelName;
   private static final Logger log = LoggerFactory.getLogger(ATFTestMatrix.class);


   @Test
   public void getTestMethodDetails()
   {
      Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()).setScanners(new MethodAnnotationsScanner()));
      log.debug("class path is : {}", ClasspathHelper.forJavaClassPath().toString());
      Set<Method> methods = reflections.getMethodsAnnotatedWith(Test.class);
      List<String> methodNames = new ArrayList<>();
      for (Method method : methods)
      {
         try
         {
            String userId = method.getAnnotation(ATFTestDetails.class).user();
            String annotationDate = method.getAnnotation(ATFTestDetails.class).date();
            String methodName = method.getName();
            String className = method.getDeclaringClass().getSimpleName().toString();
            String dispalyName = method.getAnnotation(ATFTestDetails.class).displayName();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
            DateTime date = formatter.parseDateTime(annotationDate);
            String group = "";
            for (String g : method.getDeclaredAnnotation(Test.class).groups())
            {
               group = group + g + " ";
            }
            group = group.trim();
            String description = method.getDeclaredAnnotation(Test.class).description().trim();
            String data = "'" + className + "#" + methodName + "': '" + dispalyName + "',";
            if (group.contains(ATFBaseTest.REGRESSION))
            {
               methodNames.add(description + "|" + methodName + "|" + toTitleCase(ATFBaseTest.REGRESSION) + "|" + userId + "|" + date.toString(formatter) + "|" + className + "|" + dispalyName + "|" + data);
            }
            else if (group.contains(ATFBaseTest.SMOKE))
            {
               methodNames.add(description + "|" + methodName + "|" + toTitleCase(ATFBaseTest.SMOKE) + "|" + userId + "|" + date.toString(formatter) + "|" + className + "|" + dispalyName + "|" + data);
            }
         } catch (Exception e)
         {
            // System.out.println(e.getMessage());
         }
      }
      Collections.sort(methodNames);
      System.out.println("Description|TestScriptName|Group|TestCreatedBy|TestCreatedOn|TestClassName");
      methodNames.stream().sorted().forEach(System.out::println);
      setExcelSheet();
      setDataToExcel(methodNames);
      closeFiles();
   }


   @Test
   private void countTestMethods()
   {
      Set<String> smokeCount = new HashSet<>();
      Set<String> reggCount = new HashSet<>();
      Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()).setScanners(new MethodAnnotationsScanner()));
      Set<Method> methods = reflections.getMethodsAnnotatedWith(Test.class);
      for (Method method : methods)
      {
         try
         {
            String methodName = method.getName();
            String group = "";
            for (String g : method.getDeclaredAnnotation(Test.class).groups())
            {
               group = group + g + " ";
            }
            group = group.trim();
            if (group.contains(ATFBaseTest.REGRESSION))
            {
               reggCount.add(methodName);
            }
            else if (group.contains(ATFBaseTest.SMOKE))
            {
               smokeCount.add(methodName);
            }
         } catch (Exception e)
         {
            System.out.println(e.getMessage());
         }
      }
      log.info("'{}' Somke Test cases", smokeCount.size());
      log.info("'{}' Regression Test cases", reggCount.size());
      log.info("'{}' Total test cases", smokeCount.size() + reggCount.size());
   }


   private void setDataToExcel(List<String> methodNames)
   {
      WritableCellFormat hedder = getHedderFormat();
      WritableCellFormat white = getCellFormat();
      setValueIntoCell(0, 0, "SNo", hedder);
      setValueIntoCell(1, 0, "Description", hedder);
      setValueIntoCell(2, 0, "TestMethodName", hedder);
      setValueIntoCell(3, 0, "Group", hedder);
      setValueIntoCell(4, 0, "TestCreatedBy", hedder);
      setValueIntoCell(5, 0, "TestCreatedOn", hedder);
      setValueIntoCell(6, 0, "TestClassName", hedder);
      setValueIntoCell(7, 0, "TestDisplayName", hedder);
      setValueIntoCell(8, 0, "JenkinsData", hedder);
      int row = 1;
      for (String e : methodNames)
      {
         int column = 1;
         String d[] = e.split("[|]");
         setValueIntoCell(0, row, String.valueOf(row), white);
         for (String data : d)
         {
            setValueIntoCell(column++, row, data, white);
         }
         row++;
      }
      sheetAutoFitColumns();
   }


   private void sheetAutoFitColumns()
   {
      WritableSheet sheet = wwbCopy.getSheet(0);
      for (int i = 0; i < sheet.getColumns(); i++)
      {
         Cell[] cells = sheet.getColumn(i);
         int longestStrLen = -1;
         if (cells.length == 0)
         {
            continue;
         }
         for (Cell cell : cells)
         {
            if (cell.getContents().length() > longestStrLen)
            {
               String str = cell.getContents();
               if (str == null || str.isEmpty())
               {
                  continue;
               }
               longestStrLen = str.trim().length();
            }
         }
         if (longestStrLen == -1)
         {
            continue;
         }
         if (longestStrLen > 150)
         {
            longestStrLen = 150;
         }
         CellView cv = sheet.getColumnView(i);
         if ((longestStrLen * 156 + 100) >= 40000)
         {
            cv.setSize(40000);
         }
         else
         {
            cv.setSize(longestStrLen * 156 + 100);
         }
         sheet.setColumnView(i, cv);
         sheet.setColumnView(0, 5);
      }
   }


   private WritableCellFormat getHedderFormat()
   {
      WritableFont hedderFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE);
      WritableCellFormat format = new WritableCellFormat(hedderFont);
      try
      {
         format.setAlignment(Alignment.LEFT);
         format.setBackground(Colour.SEA_GREEN);
         format.setBorder(Border.ALL, BorderLineStyle.THIN);
      } catch (WriteException e)
      {}
      return format;
   }


   private WritableCellFormat getCellFormat()
   {
      WritableFont cellFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.AUTOMATIC);
      WritableCellFormat format = new WritableCellFormat(cellFont);
      try
      {
         format.setAlignment(Alignment.LEFT);
         format.setBackground(Colour.WHITE);
         format.setBorder(Border.ALL, BorderLineStyle.THIN);
      } catch (WriteException e)
      {}
      return format;
   }


   private void setValueIntoCell(int iColumnNumber, int iRowNumber, String strData, WritableCellFormat format)
   {
      WritableSheet wshTemp = wwbCopy.getSheet(0);
      Label labTemp = new Label(iColumnNumber, iRowNumber, strData, format);
      try
      {
         wshTemp.addCell(labTemp);
      } catch (Exception e)
      {}
   }


   public void setExcelSheet()
   {
      sheetName = "Automated Tests";
      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy h-mm a");
      excelName = ATFConstants.PROJECT_NAME + " Automated test cases on " + sdf.format(new Date());
      excelPath = ATFConstants.AUTOMATION_REPORTS_PATH.replaceAll("/Reports/", "/") + excelName + ".xls".toString();
      try
      {
         wwbCopy = Workbook.createWorkbook(new File(excelPath));
         shSheet = wwbCopy.createSheet(sheetName, 0);
      } catch (Exception e)
      {
         System.out.println(e);
      }
   }


   public void closeFiles()
   {
      try
      {
         wwbCopy.write();
         wwbCopy.close();
      } catch (Exception e)
      {}
   }


   public int getTestCount(String id, String dateFrom, String dateTo)
   {
      String datePattern = "MM/dd/yyyy";
      log.info("Getting system-controllers test count for {} from {} to {}", id, dateFrom, dateTo);
      Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()).setScanners(new MethodAnnotationsScanner()));
      DateTimeFormatter formatter = DateTimeFormat.forPattern(datePattern);
      DateTime jDateFrom = formatter.parseDateTime(dateFrom);
      DateTime jDateTo = dateTo == null ? null : formatter.parseDateTime(dateTo);
      Set<Method> methods = reflections.getMethodsAnnotatedWith(ATFTestDetails.class);
      List<String> methodNames = new ArrayList<>();
      int count = 0;
      log.info("Found {} methods {}", methods.size(), methods.toString());
      Set<String> ids = new HashSet<>();
      for (Method method : methods)
      {
         String userId = method.getAnnotation(ATFTestDetails.class).user();
         ids.add(userId);
         String annotationDate = method.getAnnotation(ATFTestDetails.class).date();
         try
         {
            DateTime dtAnnotation = formatter.parseDateTime(annotationDate);
            if (jDateTo != null)
            {
               if (userId.equals(id) && (dtAnnotation.isAfter(jDateFrom) || dtAnnotation.isEqual(jDateFrom)) && (dtAnnotation.isBefore(jDateTo) || dtAnnotation.isEqual(jDateTo)))
               {
                  methodNames.add(method.getName());
                  count++;
               }
            }
            else
            {
               if (userId.equals(id) && (dtAnnotation.isAfter(jDateFrom) || dtAnnotation.isEqual(jDateFrom)))
               {
                  methodNames.add(method.getName());
                  count++;
               }
            }
         } catch (IllegalArgumentException iae)
         {
            log.error("Error in Date Annotation in method {}, user {}, {}, should be {}", method.toString(), userId, iae.getMessage(), datePattern);
         }
      }
      log.info("Found user ids : {}", ids.toString());
      log.info("\nId: " + id + " \nFrom: " + dateFrom + " \nTo: " + dateTo + " \nCount: " + count + " \nMethod Names: " + methodNames);
      return count;
   }


   private String toTitleCase(String input)
   {
      StringBuilder titleCase = new StringBuilder();
      boolean nextTitleCase = true;
      for (char c : input.toCharArray())
      {
         if (Character.isSpaceChar(c))
         {
            nextTitleCase = true;
         }
         else if (nextTitleCase)
         {
            c = Character.toTitleCase(c);
            nextTitleCase = false;
         }
         titleCase.append(c);
      }
      return titleCase.toString();
   }
}