package com.scrapper.util;

import com.bc.util.XLogger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SortByFrequency
  implements Comparator<String>
{
  private String category;
  private static transient TableRequestTimes rt;
  
  public SortByFrequency() {}
  
  public SortByFrequency(String category)
  {
    this.category = category;
  }
  

  public int compare(String s1, String s2)
  {
    Map<String, Integer> tableRequestTimes = getRequestTimes();
    
    if (tableRequestTimes == null)
    {
      return 0;
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
  








  public void rearrange(List list, float factor)
  {
    if (factor <= 0.0F) {
      throw new IllegalArgumentException("Factor <= 0. factor: " + factor);
    }
    if (factor >= 1.0F) {
      throw new IllegalArgumentException("Factor >= 1. factor: " + factor);
    }
    

    int toRelocate;
    
    int offset = toRelocate = Math.round(list.size() * factor);
    
    if (offset > 0)
    {
      rearrange(list, offset, toRelocate);
    }
  }
  









  public void rearrange(List list, int offset, int toRelocate)
  {
    XLogger.getInstance().log(Level.FINER, "Before rearrange: {0}", getClass(), list);
    
    Collections.rotate(list.subList(offset, list.size()), toRelocate);
    
    XLogger.getInstance().log(Level.FINER, "After rearrange: {0}", getClass(), list);
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
