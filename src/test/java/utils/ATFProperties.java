package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ATFProperties
{
   protected static final Logger log = LoggerFactory.getLogger(ATFProperties.class);
   protected InputStream inputStream;
   protected Properties pro = new Properties();


   /**
    * Returns the runtime value from defined properties file, parameter value
    *
    * @return String, value
    */
   public static String GetProperty(String propertiesFileName, String parameter)
   {
      String value = System.getProperty(parameter);
      if (StringUtils.isEmpty(value))
      {
         FileInputStream file;
         try
         {
            file = new FileInputStream(propertiesFileName);
            Properties prop = new Properties();
            prop.load(file);
            value = prop.getProperty(parameter);
         } catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      return value;
   }


   public static String GetProperty(String parameter)
   {
      return GetProperty(ATFConstants.AUTOMATION_PROPERTIES_PATH, parameter);
   }


   public static void LoadProperties(String filePath)
   {
      Properties props = new Properties();
      try
      {
         props.load(new FileInputStream(filePath));
      } catch (IOException e)
      {
         log.info("Error: Cannot laod configuration file");
         log.error("Error: Cannot laod configuration file");
      }
   }
}