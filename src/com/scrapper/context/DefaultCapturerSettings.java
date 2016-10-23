package com.scrapper.context;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DefaultCapturerSettings implements Serializable, CapturerSettings {
    
  private final JsonConfig config;
  
  public DefaultCapturerSettings(JsonConfig config) {
    this.config = config;
  }
  
  public final JsonConfig getConfig() {
    return this.config;
  }

  @Override
  public Map getDefaults() {
    Map output = config.getMap(Config.Formatter.defaultValues);
    return output == null ? Collections.EMPTY_MAP : output;  
  }

  @Override
  public String[] getDatePatterns(){
    Object[] arr = this.config.getArray(new Object[] { Config.Formatter.datePatterns });
    return this.stringCopyOf(arr);
  }
  
  @Override
  public String[] getUrlDatePatterns(){
    Object[] arr = this.config.getArray(new Object[] { Config.Formatter.urlDatePatterns });
    return this.stringCopyOf(arr);
  }
  
  @Override
  public String[] getTransverse(String id) {
    return getStringArray(id, Config.Extractor.transverse);
  }
  
  @Override
  public String[] getTextToDisableOn(String id) {
    return getStringArray(id, Config.Extractor.textToDisableOn);
  }
  
  @Override
  public String[] getTextToReject(String id) {
    return getStringArray(id, Config.Extractor.textToReject);
  }
  
  @Override
  public boolean isConcatenateMultipleExtracts(String id, boolean defaultValue) {
    Boolean b = this.getBoolean(id, Config.Extractor.append, defaultValue);
    return b;
  }
  
  @Override
  public String getLineSeparator() {
    return getConfig().getString(new Object[] { Config.Extractor.lineSeparator });
  }
  
  @Override
  public String getPartSeparator() {
    return getConfig().getString(new Object[] { Config.Extractor.partSeparator });
  }
  
  @Override
  public String getDefaultTitle() {
    return getConfig().getString(new Object[] { Config.Extractor.defaultTitle });
  }
  
  @Override
  public String[] getColumns(String id) {
    return getStringArray(id, Config.Extractor.columns);
  }
  
  @Override
  public String[] getNodesToRetainAttributes(String id) {
      
    JsonConfig cfg = getConfig();
    
    List defaultNodes = cfg.getList(new Object[] { Config.Extractor.nodesToRetainAttributes });
    List nodes = cfg.getList(new Object[] { id, Config.Extractor.nodesToRetainAttributes });
    
    List<String> list = new ArrayList();
    
    if (defaultNodes != null) {
      list.addAll(defaultNodes);
    }
    if (nodes != null) {
      list.addAll(nodes);
    }
    
    String[] nodesToRetainAttributes = (String[])list.toArray(new String[0]);
    
    return nodesToRetainAttributes;
  }
  
  @Override
  public boolean isReplaceNonBreakingSpace(String id, boolean defaultValue) {
    return this.getBoolean(id, Config.Extractor.replaceNonBreakingSpace, defaultValue);
  }
  
  @Override
  public String[] getAttributesToAccept(String id) {
    return getStringArray(id, Config.Extractor.attributesToAccept);
  }
  
  @Override
  public String[] getAttributesToExtract(String id) {
      
    String[] arr = getStringArray(id, Config.Extractor.attributesToExtract);
    
    XLogger.getInstance().log(Level.FINER, "Attributes to extract: {0}", getClass(), arr == null ? null : Arrays.toString(arr));
    
    return arr;
  }
  
  @Override
  public String[] getNodeTypesToAccept(String id) {
    return toLowercaseStringArray(getStringArray(id, Config.Extractor.nodeTypesToAccept));
  }
  
  @Override
  public String[] getNodeTypesToReject(String id)  {
    return toLowercaseStringArray(getStringArray(id, Config.Extractor.nodeTypesToReject));
  }
  
  @Override
  public String[] getNodesToAccept(String id) {
    return toLowercaseStringArray(getStringArray(id, Config.Extractor.nodesToAccept));
  }
  
  @Override
  public String[] getNodeToReject(String id) { 
      return toLowercaseStringArray(getStringArray(id, Config.Extractor.nodesToReject)); 
  }
  
  private String[] toLowercaseStringArray(Object[] arr) {
    String[] output;
    if (arr == null) {
      output = null; //new String[0];
    } else {
      output = new String[arr.length];
      for (int i = 0; i < arr.length; i++) {
        output[i] = arr[i].toString().toLowerCase();
      }
    }
    return output;
  }
  
  private String[] getStringArray(String first, Object second) {
      
    Object[] arr = getConfig().getArray(new Object[] { first, second });
    
    if (arr == null) {
      arr = getConfig().getArray(new Object[] { second });
    }
    
    return this.stringCopyOf(arr);
  }

  private boolean getBoolean(String first, Object second, boolean defaultValue) {
    Boolean bool = getConfig().getBoolean(new Object[] { first, second });
    if (bool == null) {
      bool = config.getBoolean(new Object[] { second });
    }
    return bool==null?defaultValue:bool;
  }  
  
  private String[] stringCopyOf(Object... src)  {
    String[] output;
    if (src != null) {
      output = new String[src.length];
      System.arraycopy(src, 0, output, 0, src.length);
    } else {
      output = null; //new String[0];
    }
    
    return output;
  }
}
