package com.scrapper.filter;

import com.bc.util.XLogger;
import com.bc.webdatex.filter.Filter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DefaultUrlFilter
  implements Filter<String>
{
  private String id;
  private Pattern requiredPattern;
  private Pattern unwantedPattern;
  private String[] required;
  private String[] unwanted;
  private Calendar _c;
  private StringBuilder _b;
  
  private Set<String> getDateStrings(String regex, String url)
  {
      
    if(true) {
        return Collections.EMPTY_SET;
    }  
    
    Set<String> output = new HashSet(4);
    
    Calendar calendar = getCalendar();
    
    appendDateString(output, calendar, regex, true);
    appendDateString(output, calendar, regex, false);
    
    int amount = 1;
    try {
      calendar.add(5, -amount);
      appendDateString(output, calendar, regex, true);
      appendDateString(output, calendar, regex, false);
    } finally {
      calendar.add(5, amount);
    }
    
    XLogger.getInstance().log(Level.FINER, "URL: {0}, dates: {1}", getClass(), url, output);
    
    return output;
  }
  
  private void appendDateString(Set<String> set, Calendar calendar, String regex, boolean addZeroToMonthsLessThanTen) {
    String s = getDateString(calendar, regex, addZeroToMonthsLessThanTen);
    if (s != null) {
      set.add(s);
    }
  }
  
  private String getDateString(Calendar calendar, String regex, boolean addZeroToMonthsLessThanTen)
  {
    int year = calendar.get(1);
    int month = calendar.get(2) + 1;
    int dayOfMonth = calendar.get(5);
    
    if ((regex.contains("/\\d{4}/\\d{1,2}/\\d{1,2}/")) || (regex.contains("/\\d{4}/\\d{2}/\\d{2}/"))) {
      StringBuilder builder = getBuilder();
      builder.append('/').append(year).append('/');
      if ((addZeroToMonthsLessThanTen) && (month < 10)) {
        builder.append('0');
      }
      builder.append(month).append('/').append(dayOfMonth).append('/');
      return builder.toString(); }
    if ((regex.contains("/\\d{4}/\\d{1,2}/")) || (regex.contains("/\\d{4}/\\d{2}/"))) {
      StringBuilder builder = getBuilder();
      builder.append('/').append(year).append('/');
      if ((addZeroToMonthsLessThanTen) && (month < 10)) {
        builder.append('0');
      }
      builder.append(month).append('/');
      return builder.toString(); }
    if (regex.contains("/\\d{4}/")) {
      StringBuilder builder = getBuilder();
      builder.append('/').append(year).append('/');
      return builder.toString();
    }
    return null;
  }

  @Override
  public boolean accept(String url)
  {
    url = format(url);
    
    boolean output = true;
    try
    {
      if (this.unwantedPattern != null)
      {
//        Set<String> set = getDateStrings(this.unwantedPattern.pattern(), url);
        
//        if ((!set.isEmpty()) && (contains(url, set))) {
//          output = false;
//          return false;
//        }
        
        boolean found = this.unwantedPattern.matcher(url).find();
        XLogger.getInstance().log(Level.FINER, "{0}, Accepted: {1}, URL: {2}, unwanted regex: {3}", getClass(), this.id, Boolean.valueOf(!found), url, this.unwantedPattern.pattern());
        
        if (found) {
          output = false;
          return false;
        }
      }
      boolean contains;
      if ((this.unwanted != null) && (this.unwanted.length > 0)) {
        for (String s : this.unwanted) {
          contains = url.contains(s);
          XLogger.getInstance().log(Level.FINER, "{0}, Accepted: {1}, URL: {2}, unwanted text: {3}", getClass(), this.id, Boolean.valueOf(!contains), url, s);
          
          if (contains) {
            output = false;
            return false;
          }
        }
      }
      
      if (this.requiredPattern != null)
      {
//        Set<String> set = getDateStrings(this.requiredPattern.pattern(), url);
        
//        XLogger.getInstance().log(Level.FINER, "URL: {0}, dates: {1}", getClass(), url, set);
        
//        if ((!set.isEmpty()) && (!contains(url, set))) {
//          output = false;
//          return false;
//        }
        
        output = this.requiredPattern.matcher(url).find();
        
        XLogger.getInstance().log(Level.FINER, "{0}, Accepted: {1}, URL: {2}, required regex: {3}", getClass(), this.id, Boolean.valueOf(output), url, this.requiredPattern.pattern());

        if (output) { 
          return true;
        }
      } else {
        output = true;
      }
      
      if ((this.required != null) && (this.required.length > 0))
      {
        output = false;
        
        for (String s : this.required) {
          output = url.contains(s);
          XLogger.getInstance().log(Level.FINER, "{0}, Accepted: {1}, URL: {2}, required text: {3}", getClass(), this.id, Boolean.valueOf(output), url, s);
          
          if (output) { 
            return true;
          }
        }
      }
      return output;

    }
    finally
    {
      Level level = (this.id != null) && (this.id.toLowerCase().contains("capture")) ? Level.FINER : Level.FINE;
      
      XLogger.getInstance().log(level, "Accepted: {0}, URL: {1}", getClass(), Boolean.valueOf(output), url);
    }
  }
  

  private String format(String s)
  {
    s = s.toLowerCase();
    final String AMP = "&amp;";
    if (s.length() >= AMP.length()) {
      s = s.replace(AMP, "&");
    }
    try {
      s = URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException|RuntimeException ignored) {}
    return s;
  }
  
  private Calendar getCalendar()
  {
    if (this._c == null) {
      this._c = Calendar.getInstance();
    }
    return this._c;
  }
  
  private boolean contains(String url, Set<String> set) {
    for (String s : set) {
      if ((s != null) && (url.contains(s))) {
        return true;
      }
    }
    return false;
  }
  
  private StringBuilder getBuilder()
  {
    if (this._b == null) {
      this._b = new StringBuilder(15);
    } else {
      this._b.setLength(0);
    }
    return this._b;
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String[] getRequired() {
    return this.required;
  }
  
  public void setRequired(String[] arr) {
    this.required = new String[arr.length];
    for (int i = 0; i < arr.length; i++) {
      this.required[i] = format(arr[i]);
    }
  }
  
  public String[] getUnwanted() {
    return this.unwanted;
  }
  
  public void setUnwanted(String[] arr) {
    this.unwanted = new String[arr.length];
    for (int i = 0; i < arr.length; i++) {
      this.unwanted[i] = format(arr[i]);
    }
  }
  
  public Pattern getRequiredPattern() {
    return this.requiredPattern;
  }
  
  public void setRequiredPattern(Pattern requiredPattern) {
    this.requiredPattern = requiredPattern;
  }
  
  public Pattern getUnwantedPattern() {
    return this.unwantedPattern;
  }
  
  public void setUnwantedPattern(Pattern unwantedPattern) {
    this.unwantedPattern = unwantedPattern;
  }
}
