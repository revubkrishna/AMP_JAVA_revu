package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class ATFTestNGRun
{
   private static final Logger log = LoggerFactory.getLogger(ATFTestNGRun.class);
   private static String group = "";


   @Test
   public void runTestSuite()
   {
      String groupToRun = ATFProperties.GetProperty("testScripts").trim();
      List<String> methodDetails = new ArrayList<>();
      if ( !groupToRun.contains("#"))
      {
         methodDetails = getGroupMethods(groupToRun);
      }
      else
      {
         groupToRun = "Individual";
         methodDetails = getIndividualMethods();
      }
      group = ToTitleCase(groupToRun).trim();
      if ( !methodDetails.isEmpty())
      {
         runTheSuite(methodDetails);
      }
      else
      {
         log.info("Test Scripts are not available with this group: " + groupToRun);
         Assert.fail("Test Scripts are not available with this group: " + groupToRun);
      }
   }


   private List<String> getIndividualMethods()
   {
      List<String> methodNames = new ArrayList<>();
      String[] testScriptDetails = ATFProperties.GetProperty("testScripts").trim().split(",");
      Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()).setScanners(new MethodAnnotationsScanner()));
      Set<Method> methods = reflections.getMethodsAnnotatedWith(Test.class);
      for (Method method : methods)
      {
         String packageName = method.getDeclaringClass().getPackage().getName() + ".";
         try
         {
            String methodName = method.getName().trim();
            String className = method.getDeclaringClass().getSimpleName().toString().trim();
            for (String script : testScriptDetails)
            {
               if (script.trim().split("#")[0].equals(className) && script.trim().split("#")[1].equals(methodName))
               {
                  methodNames.add(packageName + className + "#" + methodName);
               }
            }
         } catch (Exception e)
         {
            // System.out.println(e.toString());
         }
      }
      return methodNames;
   }


   private void runTheSuite(List<String> methodDetails)
   {
      Collections.sort(methodDetails);
      TestNG myTestNG = new TestNG();
      XmlSuite mySuite = new XmlSuite();
      mySuite.setName(group + " Test Suite");
      XmlTest myTest = new XmlTest(mySuite);
      myTest.setName(group + " Test Scripts");
      List<String> methodsList = methodDetails;
      String classNamesList = " ";
      for (int i = 0; i < methodsList.size(); i++)
      {
         String testScript = methodDetails.get(i);
         testScript.replaceAll("'", "").trim();
         String[] scriptsList = testScript.split("#");
         String className = scriptsList[0];
         classNamesList = classNamesList + " " + className;
      }
      String[] classList = classNamesList.split(" ");
      StringBuilder stringBuilder = new StringBuilder();
      Set<String> wordsHashSet = new HashSet<>();
      for (String singleClass : classList)
      {
         if (wordsHashSet.contains(singleClass.toLowerCase()))
         {
            continue;
         }
         wordsHashSet.add(singleClass.toLowerCase());
         stringBuilder.append(singleClass).append(" ");
      }
      String uniqueClasses = stringBuilder.toString().trim();
      classList = uniqueClasses.split(" ");
      List<XmlClass> myClasses = new ArrayList<>();
      for (String singleClass : classList)
      {
         XmlClass myclass = new XmlClass(singleClass);
         myClasses.add(myclass);
         for (String testScript : methodDetails)
         {
            testScript = testScript.replaceAll("'", "").trim();
            String[] scriptsList = testScript.split("#");
            String a = scriptsList[1];
            if (singleClass.equals(scriptsList[0]))
            {
               myclass.getIncludedMethods().add(new XmlInclude(a));
            }
         }
      }
      myTest.setXmlClasses(myClasses);
      List<XmlTest> myTests = new ArrayList<>();
      myTests.add(myTest);
      mySuite.setTests(myTests);
      List<XmlSuite> mySuites = new ArrayList<>();
      mySuites.add(mySuite);
      myTestNG.setXmlSuites(mySuites);
      mySuite.setFileName(group + "Suite.xml");
      myTestNG.run();
      for (XmlSuite suite : mySuites)
      {
         // createXmlFile(suite);
      }
   }


   private void createXmlFile(XmlSuite mSuite)
   {
      FileWriter writer;
      try
      {
         String a = System.getProperty("user.dir") + "/" + group.trim() + "Suite.xml";
         writer = new FileWriter(new File(a));
         writer.write(mSuite.toXml());
         writer.flush();
         writer.close();
      } catch (IOException e)
      {
         e.printStackTrace();
      }
   }


   private List<String> getGroupMethods(String groupToExecute)
   {
      Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()).setScanners(new MethodAnnotationsScanner()));
      Set<Method> methodsNames = reflections.getMethodsAnnotatedWith(Test.class);
      List<String> methodNames = new ArrayList<>();
      for (Method method : methodsNames)
      {
         String declaredPackageName = method.getDeclaringClass().getPackage().getName() + ".";
         try
         {
            String groups = "";
            for (String testGroup : method.getDeclaredAnnotation(Test.class).groups())
            {
               groups = groups + testGroup + ", ";
            }
            groups = groups.trim();
            if (groups.contains(groupToExecute))
            {
               methodNames.add(declaredPackageName + method.getDeclaringClass().getSimpleName().toString() + "#" + method.getName());
            }
         } catch (Exception e)
         {
            // System.out.println(e.toString());
         }
      }
      return methodNames;
   }


   private static String ToTitleCase(String input)
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