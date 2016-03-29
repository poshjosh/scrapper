package com.scrapper;

import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract interface LoggerManager
{
  public abstract boolean addFileHandler();
  
  public abstract void addHandler(Handler paramHandler);
  
  public abstract Handler createFileHandler()
    throws IOException;
  
  public abstract int getCount();
  
  public abstract Formatter getFormatter();
  
  public abstract int getLimit();
  
  public abstract String getLogFilePattern();
  
  public abstract Level getLogLevel();
  
  public abstract Logger getLogger();
  
  public abstract String getLoggerName();
  
  public abstract boolean isAppend();
}
