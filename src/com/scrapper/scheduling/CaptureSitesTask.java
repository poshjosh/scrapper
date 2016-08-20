package com.scrapper.scheduling;

import com.bc.task.AbstractTaskList;
import com.bc.util.XLogger;
import com.scrapper.SiteCapturer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class CaptureSitesTask extends AbstractTaskList<String> {
    
  private boolean loop = true;
  
  private CaptureSitesManager taskManager;
  
  private final List<String> sitenames;
  
  private SiteCapturer current;
  private int timeoutHours;
  
  public CaptureSitesTask() {
      
    this(new CaptureSitesManager());
  }
  
  public CaptureSitesTask(CaptureSitesManager taskManager) {
      
    this.timeoutHours = 24;
    
    this.taskManager = taskManager;
    
    this.sitenames = new ArrayList(taskManager.getSitenames());
  }
  
  @Override
  public void reset(){
      
    super.reset();
    
    this.loop = true;
  }
  
  public Integer doCall() {
    try {
      return super.doCall();
    } catch (RuntimeException e) {
      this.loop = false;
      throw e;
    }
  }
  

  protected void post() {
      
    if (!this.loop) {
      return;
    }
    
    XLogger.getInstance().log(Level.INFO, "= x = x = x = RESETTING. size: {0}, pos: {1}", getClass(), Integer.valueOf(getTaskCount()), Integer.valueOf(getPos()));
    

    reset();
    
    XLogger.getInstance().log(Level.INFO, "= x = x = x = RERUNNING. size: {0}, pos: {1}", getClass(), Integer.valueOf(getTaskCount()), Integer.valueOf(getPos()));
    


    run();
  }
  
  protected List<String> getList()
  {
    return this.sitenames;
  }
  
  public void execute(String sitename)
  {
    XLogger.getInstance().log(Level.INFO, "Executing: {0}", getClass(), sitename);
    
    try
    {
      boolean createnew = false;
      
      if (this.current != null) {
        String currentName = this.current.getContext().getConfig().getName();
        if (!currentName.equals(sitename)) {
          createnew = true;
        } else {
          Date startTime = this.current.getStartTime();
          Calendar cal = Calendar.getInstance();
          cal.add(11, -this.timeoutHours);
          if (startTime.before(cal.getTime())) {
            createnew = true;
          }
        }
      } else {
        createnew = true;
      }
      
      if (createnew) {
        if (this.current != null) {
          this.current.stop();
        }
        this.current = this.taskManager.newTask(sitename);
        XLogger.getInstance().log(Level.INFO, "Created task: {0}", getClass(), this.current);
      }
      
      XLogger.getInstance().log(Level.INFO, "Running task: {0}", getClass(), this.current);
      this.current.run();
    }
    catch (RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, "Task failed: " + this.current, getClass(), e);
    }
    XLogger.getInstance().log(Level.INFO, "DONE Executing: {0}", getClass(), this.current);
  }
  
  public void stop(String sitename)
  {
    if ((this.current != null) && (this.current.getContext().getConfig().getName().equals(sitename))) {
      XLogger.getInstance().log(Level.INFO, "Stopping: {0}", getClass(), this.current);
      this.current.stop();
    }
  }
  
  public CaptureSitesManager getTaskManager() {
    return this.taskManager;
  }
  
  public void setTaskManager(CaptureSitesManager taskManager) {
    this.taskManager = taskManager;
  }
  
  public boolean isLoop() {
    return this.loop;
  }
  
  public void setLoop(boolean loop) {
    this.loop = loop;
  }
  
  public String getTaskName()
  {
    return CaptureSitesTask.class.getName();
  }
  
  public void print(StringBuilder builder)
  {
    super.print(builder);
    builder.append(", timeout (hours): ").append(this.timeoutHours);
    builder.append(", crawlLimit: ").append(this.taskManager == null ? null : Integer.valueOf(this.taskManager.getCrawlLimit()));
    builder.append(", parseLimit: ").append(this.taskManager == null ? null : Integer.valueOf(this.taskManager.getParseLimit()));
    builder.append(", scrappLimit: ").append(this.taskManager == null ? null : Integer.valueOf(this.taskManager.getScrappLimit()));
  }
}
