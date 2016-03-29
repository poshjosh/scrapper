package com.scrapper.util;

import com.bc.util.Util;
import com.bc.util.XLogger;
import com.scrapper.search.SearchSite;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public abstract class LimitedCache<T extends Serializable>
  extends AbstractList<T>
  implements Serializable, Comparator<String>
{
  private int released;
  private final String[] names;
  private Object[] cache;
  private boolean shutdown;
  private final transient Serializable lock = new Serializable() {};
  
  private transient ScheduledExecutorService lockReleaserService;
  
  public LimitedCache(List<String> names)
  {
    this.names = sort(names);
  }
  
  protected abstract T newObject(String paramString);
  
  public abstract int getMaxActive();
  
  public abstract boolean isDone(T paramT);
  
  public int getLimit() {
    return getNames().length;
  }
  
  public boolean isReleased(int index)
  {
    return index < this.released;
  }
  


  public boolean isActive(int index)
  {
    boolean isActive;

    if (!isReleased(index))
    {
      isActive = false;
    }
    else
    {

      T type = doGet(index);
      
      isActive = !isDone(type);
    }
    
    return isActive;
  }
  
  protected String[] sort(List<String> toSort)
  {
    Collections.sort(toSort, this);
    
    return (String[])toSort.toArray(new String[0]);
  }
  
  public int compare(String o1, String o2)
  {
    return o1.compareTo(o2);
  }
  




  public T get(int index)
  {
    try
    {
      T type;

      if (!needsRegulation())
      {
        type = doGet(index);
      }
      else
      {
        boolean mayGetMore = waitForLimit();
        
        if (!mayGetMore) {
          return null;
        }
        
        type = doGet(index);
        
        if (index >= this.released) {
          this.released += 1;
          XLogger.getInstance().log(Level.FINER, "Total released: {0}, last released: {1}={2}", getClass(), Integer.valueOf(this.released), Integer.valueOf(index), type);
        }
      }
      

      return type;
    }
    catch (RuntimeException e)
    {
      shutdownLockReleaserService();
      
      throw e;
    }
  }
  
  private T doGet(int index)
  {
    try
    {
      XLogger.getInstance().log(Level.FINER, "Index: {0}, Objects created: {1}, size: {2} names supplied: {3}", getClass(), Integer.valueOf(index), Integer.valueOf(this.names == null ? 0 : this.names.length), Integer.valueOf(size()), this.names);

      if ((index < 0) || (index >= size())) {
        throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size());
      }
      
      String name = this.names[index];
      
      XLogger.getInstance().log(Level.FINER, "Index: {0}, Name: {1}", getClass(), Integer.valueOf(index), name);
      
      Object type;
      
      if (this.cache != null)
      {
        type = this.cache[index];
        
        XLogger.getInstance().log(Level.FINER, "Loaded from cache: {0}", getClass(), type);

      }
      else
      {
        type = null;
      }
      
      if (type == null)
      {
        type = newObject(name);
        
        if (type == null) {
          throw new IllegalArgumentException("Failed to create an instanceof " + SearchSite.class.getName() + " for: " + name);
        }
        
        XLogger.getInstance().log(Level.FINER, "Created: {0}", getClass(), type);
        
        if (this.cache == null) {
          this.cache = new Object[size()];
        }
        
        this.cache[index] = type;
      }
      
      XLogger.getInstance().log(Level.FINER, "Index: {0}, object: {1}", getClass(), Integer.valueOf(index), type);
      

      return (T)type;
    }
    catch (RuntimeException e)
    {
      shutdownLockReleaserService();
      
      throw e;
    }
  }
  
  public int countActive()
  {
    int active = 0;
    
    for (int i = 0; i < this.released; i++)
    {
      if (isActive(i)) {
        active++;
      }
    }
    XLogger.getInstance().log(Level.FINER, "Active: {0}", getClass(), Integer.valueOf(active));
    return active;
  }
  
  private boolean waitForLimit()
  {
    synchronized (this.lock)
    {
      try
      {
        initLockReleaserService(1L, TimeUnit.SECONDS);
        
        XLogger.getInstance().log(Level.FINER, "Waiting for next", getClass());
        while (!mayHaveMore())
        {
          this.lock.wait();
        }
        
        XLogger.getInstance().log(Level.FINER, "Done waiting for next", getClass());
        return true;

      }
      catch (InterruptedException e)
      {

        XLogger.getInstance().log(Level.WARNING, "Interrupted while waiting for active element count to reduce below: " + getMaxActive(), getClass(), e);
        



        return false;
      }
    }
  }
  
  private void initLockReleaserService(long interval, TimeUnit timeUnit)
  {
    if (this.lockReleaserService != null) {
      return;
    }
    
    this.lockReleaserService = Executors.newSingleThreadScheduledExecutor();
    
    this.lockReleaserService.scheduleWithFixedDelay(new LockReleaser(), interval, interval, timeUnit);
  }
  

  public int size()
  {
    int limit = getLimit();
    int len = this.names.length;
    return len > limit ? limit : len;
  }
  
  private boolean needsRegulation() {
    int size = size();
    XLogger.getInstance().log(Level.FINER, "Needs regulation: {0}, released: {1}, size: {2}", getClass(), Boolean.valueOf(this.released < size), Integer.valueOf(this.released), Integer.valueOf(size));
    
    return this.released < size;
  }
  
  private boolean mayHaveMore() {
    int maxActive = getMaxActive();
    if (maxActive <= 0) {
      throw new UnsupportedOperationException("Max active must be > 0 : " + maxActive);
    }
    if (maxActive > getLimit()) {
      throw new UnsupportedOperationException("Max active must be < limit : " + getLimit());
    }
    int active = countActive();
    XLogger.getInstance().log(Level.FINER, "May have more: {0}, active: {1}, max active: {2}", getClass(), Boolean.valueOf(active < maxActive), Integer.valueOf(active), Integer.valueOf(maxActive));
    
    return active < maxActive;
  }
  

  public int getReleased() { return this.released; }
  
  private class LockReleaser implements Runnable, Serializable {
    private LockReleaser() {}
    
    public void run() {
      try {
        doRun();
      } catch (RuntimeException e) {
        XLogger.getInstance().log(Level.WARNING, "Exception in run method of: " + getClass().getName(), getClass(), e);
      }
    }
    
    private void doRun()
    {
      if (!LimitedCache.this.needsRegulation()) {
        XLogger.getInstance().log(Level.FINER, "Releasing lock and ending lock watch", getClass());
        synchronized (LimitedCache.this.lock) {
          LimitedCache.this.lock.notifyAll();
        }
        LimitedCache.this.shutdownLockReleaserService();
        return;
      }
      
      if (LimitedCache.this.mayHaveMore())
      {
        XLogger.getInstance().log(Level.FINER, "Releasing lock", getClass());
        synchronized (LimitedCache.this.lock) {
          LimitedCache.this.lock.notifyAll();
        }
      }
    }
  }
  
  private void shutdownLockReleaserService() {
    if (this.lockReleaserService == null) {
      return;
    }
    try {
      this.shutdown = true;
      Util.shutdownAndAwaitTermination(this.lockReleaserService, 100L, TimeUnit.MILLISECONDS);
    } finally {
      this.lockReleaserService = null;
    }
  }
  
  public boolean isShutdown() {
    return this.shutdown;
  }
  
  public String[] getNames() {
    return this.names;
  }
  
  public ScheduledExecutorService getLockReleaserService() {
    return this.lockReleaserService;
  }
}
