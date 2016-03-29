package com.scrapper;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;












public abstract class AbstractLogHandler
  extends Handler
{
  private boolean closed;
  
  public AbstractLogHandler()
  {
    setFormatter(new SimpleFormatter());
  }
  

  public synchronized void publish(LogRecord record)
  {
    if ((this.closed) || (!isLoggable(record))) {
      return;
    }
    
    try
    {
      doPublish(record);

    }
    catch (Exception ex)
    {
      reportError(null, ex, 5);
      return;
    }
    
    flush();
  }
  
  protected abstract void doPublish(LogRecord paramLogRecord)
    throws Exception;
  
  public void flush() {}
  
  public void close()
  {
    this.closed = true;
    flush();
  }
}
