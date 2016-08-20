package com.scrapper.formatter;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDateFormat extends SimpleDateFormat {
    
  private Pattern validationPattern;
  private String[] acceptedPatterns;
  
  public MyDateFormat(){
    setLenient(false);
  }
  
  public MyDateFormat(String[] patterns) {
    this.acceptedPatterns = patterns;
    setLenient(false);
  }
  
  @Override
  public Date parse(String text, ParsePosition pos) {
      
    Calendar cal = null;
    
    for (String pattern : this.acceptedPatterns) {
        
      applyPattern(pattern);
      
      Date date = super.parse(text, pos);
      
      if (date != null) {

        pattern = pattern.trim();
        
        if (!isValidationRequired(pattern)) {
            
          return date;
        }
        
        if (cal == null) {
          cal = Calendar.getInstance();
        }
        
        if (isValid(text, pattern, date, cal)) {
          return date;
        }
      }
    }
    return null;
  }
  
  private boolean isValidationRequired(String pattern){
      
    if (!isLenient()) {
      return false;
    }
    
    if (this.validationPattern == null) {
      this.validationPattern = Pattern.compile("(MM(-|\\s|/){1}dd)|(dd(-|\\s|/){1}MM)");
    }
    
    pattern = pattern.trim();
    
    Matcher m = this.validationPattern.matcher(pattern);
    
    return (m.find()) && (m.start() == 0);
  }
  
  private boolean isValid(String input, String pattern, Date date, Calendar cal){
      
    boolean isValid = true;
    
    cal.setTime(date);
    
    boolean a = (pattern.contains("yyyy")) && (matchesYear(input, "yyyy", cal));
    boolean b = (pattern.contains("yy")) && (matchesYear(input, "yy", cal));
    
    boolean matchesYear = (a) || (b);
    
    if (!matchesYear) {
      isValid = false;
    }
    else if (pattern.startsWith("MM")) {
        
      boolean matchesMonth = matchesFirstField(2, input, cal, 2);
      
      if (!matchesMonth)
      {
        isValid = false;
      }
    } else if (pattern.startsWith("dd")) {
        
      boolean matchesDate = matchesFirstField(2, input, cal, 5);
      
      if (!matchesDate)
      {
        isValid = false;
      }
    }
    
    return isValid;
  }
  
  private boolean matchesYear(String input, String patternPart, Calendar cal) {
      
    int field = cal.get(1);
    
    String sval = Integer.toString(field);
    
    if (patternPart.length() < sval.length()) {
      int diff = sval.length() - patternPart.length();
      sval = sval.substring(diff);
    }
    
    StringBuilder buff = new StringBuilder();
    buff.append(' ').append(sval).append(' ');
    boolean contains;
    if (!input.contains(buff)) {
        
      contains = false;
    }else {
        
      contains = true;
    }
    
    return contains;
  }
  
  private boolean matchesFirstField(
      int digitsInField, String input, Calendar cal, int calendarField) {
      
    String expected = input.trim().substring(0, digitsInField);
    
    int toAdd;
    
    switch (calendarField) {
      case 2: 
        toAdd = 1; break;
      case 5: 
        toAdd = 0; break;
      default: 
        throw new UnsupportedOperationException("Unexpected calendar field: " + calendarField);
    }
    
    int n = cal.get(calendarField) + toAdd;
    
    String prefix = n < 10 ? "0" : "";
    
    String found = prefix + n;
    
    return expected.equals(found);
  }
  
  public String[] getAcceptedPatterns() {
    return this.acceptedPatterns;
  }
  
  public void setAcceptedPatterns(String[] acceptedPatterns) {
    this.acceptedPatterns = acceptedPatterns;
  }
}
