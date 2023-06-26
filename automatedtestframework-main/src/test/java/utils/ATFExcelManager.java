package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ATFExcelManager
{
   protected static final Logger log = LoggerFactory.getLogger(ATFExcelManager.class);
   public static final HashMap<String, HashMap<String, String>> pageData = new HashMap<>();
   public static HashMap<String, String> testData = new HashMap<>();
   private static WritableWorkbook wwbCopy;
   private static WritableSheet shSheet;


   public static void GrabTestData(String filePath)
   {
      Workbook workBook = null;
      try
      {
         workBook = Workbook.getWorkbook(new File(filePath));
      } catch (Exception e)
      {
         log.info(e.toString());
      }
      int sheetsCount = workBook.getNumberOfSheets();
      for (int tab = 0; tab < sheetsCount; tab++)
      {
         Sheet sheet = workBook.getSheet(tab);
         HashMap<String, String> sheetData = new HashMap<>();
         int totalNoOfRows = sheet.getRows();
         for (int row = 1; row < totalNoOfRows; row++)
         {
            String key = sheet.getCell(0, row).getContents().toString().trim();
            String value = sheet.getCell(1, row).getContents().toString().trim();
            if ( !StringUtils.isEmpty(key))
            {
               sheetData.put(key, value);
            }
         }
         pageData.put(sheet.getName(), sheetData);
      }
      workBook.close();
   }


   public void exportData(String... bookData)
   {
      File excelFile = new File(ATFConstants.EXCEL_PATH_TESTDATA_OUTPUT);
      if ( !excelFile.exists())
      {
         createTheExcelSheet();
      }
      try
      {
         FileInputStream inputStream = new FileInputStream(excelFile);
         org.apache.poi.ss.usermodel.Workbook workbook = WorkbookFactory.create(inputStream);
         org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("TestDataOutput");
         int rowCount = sheet.getLastRowNum();
         Row row = sheet.createRow(++rowCount);
         int columnCount = 0;
         Cell cell = row.createCell(columnCount);
         cell.setCellValue(rowCount);
         for (String field : bookData)
         {
            cell = row.createCell(++columnCount);
            cell.setCellValue(field);
         }
         inputStream.close();
         FileOutputStream outputStream = new FileOutputStream(excelFile);
         workbook.write(outputStream);
         workbook.close();
         outputStream.close();
      } catch (IOException | EncryptedDocumentException | InvalidFormatException ex)
      {
         ex.printStackTrace();
      }
   }


   private void createTheExcelSheet()
   {
      try
      {
         wwbCopy = Workbook.createWorkbook(new File(ATFConstants.EXCEL_PATH_TESTDATA_OUTPUT));
         shSheet = wwbCopy.createSheet("TestDataOutput", 0);
      } catch (Exception e)
      {
         System.out.println(e);
      }
      WritableCellFormat hedder = GetHedderFormat();
      SetValueIntoCell(0, 0, "SNo.", hedder);
      SetValueIntoCell(1, 0, "Order Number", hedder);
      SetValueIntoCell(2, 0, "Order Created Date", hedder);
      SetValueIntoCell(3, 0, "Order Created Time", hedder);
      try
      {
         wwbCopy.write();
         wwbCopy.close();
      } catch (Exception e)
      {}
   }


   public static HashMap<String, String> GetExcelData(String filePath, String sheetName)
   {
      HashMap<String, String> data = new HashMap<>();
      Workbook wb = null;
      try
      {
         wb = Workbook.getWorkbook(new File(filePath));
         Sheet sh = wb.getSheet(sheetName);
         int totalNoOfColumns = sh.getColumns();
         for (int row = 0; row < totalNoOfColumns; row++)
         {
            String key = sh.getCell(0, row).getContents().toString().trim();
            String value = sh.getCell(1, row).getContents().toString().trim();
            if ( !StringUtils.isEmpty(key))
            {
               data.put(key, value);
               testData.put(key, value);
            }
         }
         wb.close();
      } catch (Exception e)
      {
         System.out.println(e);
         wb.close();
      }
      return data;
   }


   private static void SetValueIntoCell(int iColumnNumber, int iRowNumber, String strData, WritableCellFormat format)
   {
      WritableSheet wshTemp = wwbCopy.getSheet(0);
      Label labTemp = new Label(iColumnNumber, iRowNumber, strData, format);
      try
      {
         wshTemp.addCell(labTemp);
      } catch (Exception e)
      {}
   }


   private static WritableCellFormat GetHedderFormat()
   {
      WritableFont hedderFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.WHITE);
      WritableCellFormat format = new WritableCellFormat(hedderFont);
      try
      {
         format.setAlignment(Alignment.LEFT);
         format.setBackground(jxl.format.Colour.SEA_GREEN);
         format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
      } catch (WriteException e)
      {}
      return format;
   }


   private static WritableCellFormat GetCellFormat()
   {
      WritableFont cellFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.AUTOMATIC);
      WritableCellFormat format = new WritableCellFormat(cellFont);
      try
      {
         format.setAlignment(Alignment.LEFT);
         format.setBackground(jxl.format.Colour.WHITE);
         format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
      } catch (WriteException e)
      {}
      return format;
   }
}