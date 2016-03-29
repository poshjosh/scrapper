package com.scrapper.util;

import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.scrapper.AppProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public abstract class ManagedTasks<T extends StoppableTask>
  extends ConcurrentProgressTaskList
{
  private int timeout;
  private List<T> tasks;
  private final SortByFrequency sortByFrequency;
  
  public ManagedTasks()
  {
    this.sortByFrequency = new SortByFrequency();
    
    init();
  }
  
  public ManagedTasks(List<String> sitenames, String category)
  {
    this.sortByFrequency = new SortByFrequency();
    
    init();
    
    loadTasks(category, sitenames);
  }
  
  private void init() {
    this.timeout = getInt("siteSearch.timeout");
    XLogger.getInstance().log(Level.FINER, "Setting timeout to: {0} for {1}", getClass(), Integer.valueOf(this.timeout), this);
    int maxConcurrent = getInt("siteSearch.maxConcurrentProcesses");
    setMaxConcurrentProcesses(maxConcurrent);
  }
  
  protected abstract T newTask(String paramString);
  
  public void reset()
  {
    throw new UnsupportedOperationException("Not supported");
  }
  
  public void loadTasks(String category, List<String> sitenames)
  {
    if ((category == null) || (sitenames == null)) {
      throw new NullPointerException();
    }
    
    this.sortByFrequency.setCategory(category);
    
    Collections.sort(sitenames, this.sortByFrequency);
    






    this.sortByFrequency.rearrange(sitenames, 0.2F);
    
    this.tasks = new ArrayList(sitenames.size());
    
    for (String sitename : sitenames) {
      T task = newTask(sitename);
      this.tasks.add(task);
    }
  }
  

  public void doRun()
  {
    checkTimeout();
    
    try
    {
      super.doRun();
    }
    finally
    {
      shutdownAndAwaitTermination(this.timeout, TimeUnit.MILLISECONDS);
    }
  }
  
  private void checkTimeout() {
    assert (getTimeout() != 0) : "timeout cannot be equal to 0";
  }
  
  public int computeLimitPerSite() {
    int maxResults = getMaxResults();
    int maxConcurr = getMaxConcurrentProcesses();
    int limit = maxResults / maxConcurr;
    int rem = maxResults % maxConcurr;
    if (rem > 0) {
      limit++;
    }
    if (limit <= 0) {
      limit = 1;
    }
    XLogger.getInstance().log(Level.FINE, "Limit: {0}", getClass(), Integer.valueOf(limit));
    return limit;
  }
  
  protected void preTaskUpdateTimeTaken(String name)
  {
    checkTimeout();
    





    updateTimeTaken(name, getTimeout());
  }
  
  protected void postTaskUpdateTimeTaken(String name, long lastTaskTime, int lastTaskTimeTaken)
  {
    checkTimeout();
    XLogger.getInstance().log(Level.FINER, "Tasks: {0}", getClass(), this.tasks);
    






    Map<String, Integer> rt = this.sortByFrequency.getRequestTimes();
    
    XLogger.getInstance().log(Level.FINER, "Product table: {0}, Request times: {1}", getClass(), this.sortByFrequency.getCategory(), this.sortByFrequency.getRequestTimes());
    

    if (rt == null) {
      return;
    }
    
    Integer i = (Integer)rt.get(name);
    
    int adjustment = i.intValue() * 2 - getTimeout();
    
    updateTimeTaken(name, adjustment);
    

    assert (lastTaskTime > 0L) : (ManagedTasks.class.getName() + ".lastRequestTime == 0. This value should be set each time a request is sent to a URL");
    

    updateTimeTaken(name, lastTaskTimeTaken);
    
    XLogger.getInstance().log(Level.FINER, "Average request times for{0}={1}", getClass(), this.sortByFrequency.getCategory(), this.sortByFrequency.getRequestTimes());
  }
  

  private void updateTimeTaken(String sitename, int timeTaken)
  {
    Map<String, Integer> rt = this.sortByFrequency.getRequestTimes();
    
    XLogger.getInstance().log(Level.FINER, "Product table: {0}, Request times: {1}", getClass(), this.sortByFrequency.getCategory(), this.sortByFrequency.getRequestTimes());
    

    if (rt == null) {
      return;
    }
    
    Integer previousTimetaken = (Integer)rt.get(sitename);
    


    long average = previousTimetaken == null ? timeTaken : (previousTimetaken.intValue() + timeTaken) / 2;
    
    rt.put(sitename, Integer.valueOf((int)average));
  }
  
  protected List<StoppableTask> getList()
  {
    return (List<StoppableTask>)this.tasks;
  }
  
  private int getInt(String name) {
    String value = AppProperties.getProperty(name);
    return Integer.parseInt(value);
  }
  
  private int mr = -1;
  
  public int getMaxResults() { if (this.mr == -1) {
      this.mr = getInt("siteSearch.maxResults");
    }
    return this.mr;
  }
  
  public String getProductTable() {
    return this.sortByFrequency.getCategory();
  }
  
  public void setProductTable(String productTable) {
    this.sortByFrequency.setCategory(productTable);
  }
  
  public List<T> getTasks() {
    return this.tasks;
  }
  
  public String getTaskName()
  {
    return getClass().getName();
  }
  
  public int getTimeout() {
    return this.timeout;
  }
  
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
}
