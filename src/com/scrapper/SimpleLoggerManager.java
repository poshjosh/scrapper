package com.scrapper;

import com.bc.util.XLogger;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;









public class SimpleLoggerManager
  implements LoggerManager
{
  private String ln;
  private Formatter f;
  
  public SimpleLoggerManager()
  {
    addFileHandler();
    
    if (getLogLevel() != null) {
      XLogger.getInstance().setLogLevel(getLoggerName(), getLogLevel());
    }
  }
  

  public boolean addFileHandler()
  {
    boolean output = false;
    
    if (getLogFilePattern() != null)
    {
      try
      {
        Handler fileHandler = createFileHandler();
        
        addHandler(fileHandler);
        
        output = true;
      }
      catch (IOException e) {
        XLogger.getInstance().log(Level.WARNING, "Error creating log file handler", getClass(), e);
      }
    } else {
      output = false;
    }
    
    return output;
  }
  
  public Handler createFileHandler()
    throws IOException
  {
    return createFileHandler(getLogFilePattern(), getLimit(), getCount(), isAppend());
  }
  
  private Handler createFileHandler(String logFilePattern, int limit, int count, boolean append) throws IOException
  {
    XLogger.getInstance().log(Level.INFO, "Log file pattern: {0}", getClass(), logFilePattern);
    
    FileHandler fileHandler = new FileHandler(logFilePattern, limit, count, append);
    
    return fileHandler;
  }
  

  public void addHandler(Handler handler)
  {
    handler.setFormatter(getFormatter());
    
    getLogger().addHandler(handler);
  }
  

  public Logger getLogger()
  {
    return Logger.getLogger(getLoggerName());
  }
  
  public boolean isAppend()
  {
    return true;
  }
  
  public int getCount()
  {
    return 100;
  }
  
  public int getLimit()
  {
    return 999000;
  }
  
  public String getLogFilePattern()
  {
    return null;
  }
  
  public Level getLogLevel()
  {
    return Level.INFO;
  }
  

  public String getLoggerName()
  {
    if (this.ln == null) {
      String cn = getClass().getName();
      String[] parts = cn.split("\\.");
      if (parts.length > 1) {
        this.ln = (parts[0] + '.' + parts[1]);
      } else {
        throw new UnsupportedOperationException("Unexpected class name: " + cn);
      }
    }
    return this.ln;
  }
  

  public Formatter getFormatter()
  {
    if (this.f == null) {
      this.f = new SimpleFormatter();
    }
    return this.f;
  }
}
