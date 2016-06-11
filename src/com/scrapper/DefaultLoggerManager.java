package com.scrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class DefaultLoggerManager
  extends SimpleLoggerManager
{
  private Formatter formatter;
  
  public Level getLogLevel()
  {
    String levelString = AppProperties.getProperty("logLevel");
    
    Level level;
    if ((levelString != null) && (!levelString.isEmpty())) {
      level = Level.parse(levelString);
    } else {
      level = null;
    }
    
    return level;
  }
  
  public String getLogFilePattern()
  {
    return AppProperties.getProperty("logFilePattern");
  }
  

  public Formatter getFormatter()
  {
    if (this.formatter == null) {
      String formatterClassname = AppProperties.getProperty("logFormatter");
      try {
        Class aClass = Class.forName(formatterClassname);
        this.formatter = ((Formatter)aClass.getConstructor(new Class[0]).newInstance(new Object[0]));
      } catch (ClassNotFoundException|NoSuchMethodException|SecurityException|InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
        this.formatter = new SimpleFormatter();
      }
    }
    return this.formatter;
  }
}
