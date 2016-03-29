package com.scrapper.context;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class DefaultCapturerSettings
  implements Serializable, CapturerSettings
{
  private JsonConfig config;
  
  public DefaultCapturerSettings()
  {
    this(null);
  }
  
  public DefaultCapturerSettings(JsonConfig config) {
    this.config = config;
  }
  
  public JsonConfig getConfig() {
    return this.config;
  }
  
  public void setConfig(JsonConfig config) {
    this.config = config;
  }
  
  public String[] getTransverse(String id) {
    return getStringArray(id, Config.Extractor.transverse);
  }
  
  public String[] getTextToDisableOn(String id)
  {
    return getStringArray(id, Config.Extractor.textToDisableOn);
  }
  
  public String[] getTextToReject(String id)
  {
    return getStringArray(id, Config.Extractor.textToReject);
  }
  
  public Boolean isConcatenateMultipleExtracts()
  {
    Boolean b = getConfig().getBoolean(new Object[] { Config.Extractor.append });
    return b == null ? null : b;
  }
  
  public Boolean isConcatenateMultipleExtracts(String id)
  {
    Boolean b = getConfig().getBoolean(new Object[] { id, Config.Extractor.append });
    return b == null ? null : b;
  }
  
  public String getLineSeparator()
  {
    return getConfig().getString(new Object[] { Config.Extractor.lineSeparator });
  }
  
  public String getPartSeparator()
  {
    return getConfig().getString(new Object[] { Config.Extractor.partSeparator });
  }
  
  public String getDefaultTitle()
  {
    return getConfig().getString(new Object[] { Config.Extractor.defaultTitle });
  }
  

  public String[] getColumns(String id)
  {
    return getStringArray(id, Config.Extractor.columns);
  }
  

  public String[] getNodesToRetainAttributes(String id)
  {
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
  



  public boolean isReplaceNonBreakingSpace(String id)
  {
    Boolean defaultVal = getConfig().getBoolean(new Object[] { Config.Extractor.replaceNonBreakingSpace });
    boolean replaceNonBreakingSpace;
    if (defaultVal == null) {
      replaceNonBreakingSpace = this.config.getBoolean(new Object[] { id, Config.Extractor.replaceNonBreakingSpace }).booleanValue();
    }
    else {
      replaceNonBreakingSpace = defaultVal.booleanValue();
    }
    return replaceNonBreakingSpace;
  }
  


  public String[] getAttributesToAccept(String id)
  {
    return getStringArray(id, Config.Extractor.attributesToAccept);
  }
  

  public boolean isExtractAttributes(String id)
  {
    return getAttributesToExtract(id) != null;
  }
  

  public String[] getAttributesToExtract(String id)
  {
    String[] arr = getStringArray(id, Config.Extractor.attributesToExtract);
    
    XLogger.getInstance().log(Level.FINER, "Attributes to extract: {0}", getClass(), arr == null ? null : Arrays.toString(arr));
    
    return arr;
  }
  

  public String[] getNodeTypesToAccept(String id)
  {
    return toLowercaseStringArray(getStringArray(id, Config.Extractor.nodeTypesToAccept));
  }
  

  public String[] getNodeTypesToReject(String id)
  {
    return toLowercaseStringArray(getStringArray(id, Config.Extractor.nodeTypesToReject));
  }
  

  public String[] getNodesToAccept(String id)
  {
    return toLowercaseStringArray(getStringArray(id, Config.Extractor.nodesToAccept));
  }
  



  public String[] getNodeToReject(String id) { return toLowercaseStringArray(getStringArray(id, Config.Extractor.nodesToReject)); }
  
  private String[] toLowercaseStringArray(Object[] arr) {
    String[] output;
    if (arr == null) {
      output = null;
    } else {
      output = new String[arr.length];
      for (int i = 0; i < arr.length; i++) {
        output[i] = arr[i].toString().toLowerCase();
      }
    }
    return output;
  }
  

  private String[] getStringArray(String first, Object second)
  {
    Object[] src = getConfig().getArray(new Object[] { first, second });
    
    if (src == null)
    {

      src = getConfig().getArray(new Object[] { second });
    }
    
    String[] output;
    if (src != null) {
      output = new String[src.length];
      System.arraycopy(src, 0, output, 0, src.length);
    } else {
      output = null;
    }
    
    return output;
  }
}
