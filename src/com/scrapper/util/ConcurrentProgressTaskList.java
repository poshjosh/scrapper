package com.scrapper.util;

import com.bc.task.AbstractStoppableTaskList;
import com.bc.task.StoppableTask;
import com.bc.util.Util;
import com.bc.util.XLogger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;































public abstract class ConcurrentProgressTaskList
  extends AbstractStoppableTaskList
{
  private int shutdownTimeout;
  private int submitted;
  private int maxConcurrentProcesses;
  private transient ExecutorService executorService;
  
  public ConcurrentProgressTaskList()
  {
    this.maxConcurrentProcesses = 3;
    
    this.shutdownTimeout = 100;
  }
  

  public void reset()
  {
    super.reset();
    
    this.submitted = 0;
  }
  


  public int getPos()
  {
    int adjusted = countStopped(false);
    
    return adjusted;
  }
  

  public void execute(StoppableTask task)
  {
    try
    {
      XLogger.getInstance().log(Level.FINER, "Submitting sub process: {0}", getClass(), task);
      
      if ((task == null) || (task.isCompleted())) {
        return;
      }
      
      if (this.maxConcurrentProcesses <= 0) {
        throw new UnsupportedOperationException("Max concurrent processes must be > 0");
      }
      if (this.executorService == null) {
        this.executorService = Executors.newFixedThreadPool(this.maxConcurrentProcesses);
      }
      
      this.executorService.submit(task);
      
      this.submitted += 1;

    }
    catch (RuntimeException e)
    {

      XLogger.getInstance().log(Level.WARNING, "Exception encountered while executing task: " + task, getClass(), e);
    }
    XLogger.getInstance().log(Level.FINER, "Done submitting sub process: {0}", getClass(), task);
  }
  
  public int getSubmitted()
  {
    return this.submitted;
  }
  

  public void stop()
  {
    super.stop();
    



    shutdownAndAwaitTermination(this.shutdownTimeout, TimeUnit.MILLISECONDS);
  }
  
  public void shutdownAndAwaitTermination(long timeout, TimeUnit timeUnit)
  {
    XLogger.getInstance().log(Level.FINE, "Shutting down in {0} {1}. {2}", getClass(), Long.valueOf(timeout), timeUnit, getTaskName());
    
    if (this.executorService == null) {
      return;
    }
    try
    {
      Util.shutdownAndAwaitTermination(this.executorService, timeout, timeUnit);
    }
    finally {
      this.executorService = null;
    }
  }
  
  public int getMaxConcurrentProcesses() {
    return this.maxConcurrentProcesses;
  }
  
  public void setMaxConcurrentProcesses(int maxConcurrentProcesses) {
    if (maxConcurrentProcesses <= 0) {
      throw new UnsupportedOperationException("Max concurrent processes must be > 0");
    }
    this.maxConcurrentProcesses = maxConcurrentProcesses;
  }
  



  public int getShutdownTimeout()
  {
    return this.shutdownTimeout;
  }
  



  public void setShutdownTimeout(int shutdownTimeout)
  {
    this.shutdownTimeout = shutdownTimeout;
  }
  
  public void print(StringBuilder builder)
  {
    super.print(builder);
    builder.append(", shutdownTimeout: ").append(this.shutdownTimeout);
    builder.append(", maxConcurrentProcesses: ").append(this.maxConcurrentProcesses);
  }
}
