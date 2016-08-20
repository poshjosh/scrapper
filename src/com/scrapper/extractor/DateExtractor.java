package com.scrapper.extractor;

import com.scrapper.util.Util;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateExtractor implements Serializable {
    
  public String extractDate(String input) {
      
    return extractDate(Util.getDatePatterns(), input);
  }
  
  public String extractDate(Pattern[] datePatterns, String input) {
      
    String output = null;
    
    for (int i = 0; i < datePatterns.length; i++) {
        
      Matcher m = datePatterns[i].matcher(input);
      
      if (m.find()) {
        output = m.group();
        break;
      }
    }
    
    if (output != null) { 
        output = output.replaceFirst("of\\s", "");
    }
    
    return output;
  }
}
