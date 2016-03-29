package com.scrapper.filter;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class CaptureUrlFilter
  extends DefaultUrlFilter
{
  public CaptureUrlFilter(JsonConfig config)
  {
    setId("captureUrlFilter");
    
    Object[] arr = config.getArray(new Object[] { Config.Extractor.captureUrlFilter_required });
    if ((arr != null) && (arr.length > 0)) {
      setRequired((String[])Arrays.copyOf(arr, arr.length, String[].class));
    }
    
    arr = config.getArray(new Object[] { Config.Extractor.captureUrlFilter_unwanted });
    if ((arr != null) && (arr.length > 0)) {
      setUnwanted((String[])Arrays.copyOf(arr, arr.length, String[].class));
    }
    
    XLogger.getInstance().log(Level.FINE, "Text::\nRequired: {0}\nUnwanted: {1}", getClass(), getRequired() == null ? null : Arrays.toString(getRequired()), getUnwanted() == null ? null : Arrays.toString(getUnwanted()));
    



    String regex = config.getString(new Object[] { Config.Extractor.captureUrlFilter_requiredRegex });
    
    if ((regex != null) && (!regex.trim().isEmpty())) {
      setRequiredPattern(Pattern.compile(regex, 2));
    }
    
    regex = config.getString(new Object[] { Config.Extractor.captureUrlFilter_unwantedRegex });
    if ((regex != null) && (!regex.trim().isEmpty())) {
      setUnwantedPattern(Pattern.compile(regex, 2));
    }
    XLogger.getInstance().log(Level.FINE, "Regex::\nRequired: {0}\nUnwanted: {1}", getClass(), getRequiredPattern(), getUnwantedPattern());
  }
}
