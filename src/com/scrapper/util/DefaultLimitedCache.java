package com.scrapper.util;

import com.bc.util.XLogger;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class DefaultLimitedCache<T extends Serializable>
  extends LimitedCache<T>
{
  private String category;
  private static transient TableRequestTimes rt;
  
  public DefaultLimitedCache(String category, List<String> sitenames)
  {
    super(sitenames);
    
    this.category = category;
  }
  

  protected String[] sort(List<String> toSort)
  {
    Collections.sort(toSort, this);
    
    rearrangeToEnsureEqualOpportunity(toSort);
    
    return (String[])toSort.toArray(new String[0]);
  }
  

  public int compare(String s1, String s2)
  {
    Map<String, Integer> tableRequestTimes = getRequestTimes();
    
    if (tableRequestTimes == null) {
      return super.compare(s1, s2);
    }
    
    Integer i1 = (Integer)tableRequestTimes.get(s1);
    Integer i2 = (Integer)tableRequestTimes.get(s2);
    int output;
    if ((i1 == null) && (i2 == null)) {
      output = 0; 
    } else { 
      if ((i1 != null) && (i2 != null)) {
        output = i1.compareTo(i2);
      } else {
        output = 0;
      }
    }
    return output;
  }
  
  protected void rearrangeToEnsureEqualOpportunity(List<String> sorted) {
    XLogger.getInstance().log(Level.FINER, "Before rearrange: {0}", getClass(), sorted);
    int toRelocate = getMaxActive() / 2;
    int size = sorted.size();
    if ((toRelocate > 0) && (toRelocate < size))
    {
      int relocated = 0;
      Map<String, Integer> times = getRequestTimes();
      
      if (times == null) {
        return;
      }
      
      for (int attempts = 0; attempts < size; attempts++) {
        String last = (String)sorted.get(size - 1);
        Integer i = (Integer)times.get(last);
        if (i == null) {
          sorted.remove(last);
          sorted.add(toRelocate, last);
          relocated++; if (relocated == toRelocate) {
            break;
          }
        }
      }
    }
    XLogger.getInstance().log(Level.FINER, "After rearrange: {0}", getClass(), sorted);
  }
  
  public Map<String, Integer> getRequestTimes()
  {
    if (this.category == null) {
      return null;
    }
    if (rt == null) {
      rt = new TableRequestTimes();
    }
    return rt.getRequestTimes(this.category);
  }
  
  public String getCategory() {
    return this.category;
  }
  
  public void setCategory(String category) {
    this.category = category;
  }
}
