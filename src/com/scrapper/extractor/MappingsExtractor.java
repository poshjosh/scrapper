package com.scrapper.extractor;

import com.bc.webdatex.extractor.DataExtractor;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.config.MappingsConfig;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public abstract class MappingsExtractor
  implements DataExtractor<String>, Serializable
{
  private boolean caseSensitive;
  private JsonConfig config;
  private String[] columns;
  public MappingsExtractor() {}
  
  public static enum Type
  {
    targetNode,  page,  url;
    

    private Type() {}
  }
  

  protected MappingsExtractor(JsonConfig config)
  {
    this.config = config;
    this.columns = initColumns();
  }
  
  public abstract String getId();
  
  public static MappingsExtractor getInstance(final String prefix, JsonConfig config) {
    MappingsExtractor ex = new MappingsExtractor(config)
    {
      public String getId() {
        return prefix;
      }
    };
    if ((ex.hasTableKeys()) || (ex.hasColumnKeys())) {
      return ex;
    }
    return null;
  }
  
  public boolean hasColumnKeys()
  {
    return initColumns() != null;
  }
  
  public boolean hasTableKeys() {
    Object[] jsonPath = new MappingsConfig(this.config).getPropertyPath(getId(), Config.Extractor.table.name());
    
    Object prop = this.config.getObject(jsonPath);
    return prop != null;
  }
  
  private String[] initColumns()
  {
    Object[] jsonPath = new MappingsConfig(this.config).getPropertyPath(getId(), null);
    
    Set<Object[]> propertyIds = this.config.getFullPaths(jsonPath);
    
    HashSet<String> cols = new HashSet();
    
    for (Object[] path : propertyIds)
    {
      cols.add(path[2].toString());
    }
    
    return cols.isEmpty() ? null : (String[])cols.toArray(new String[0]);
  }
  

  public Map extractData(String src)
  {
    if (src == null) {
      throw new NullPointerException();
    }
    
    HashMap data = new HashMap();
    
    if (this.columns != null)
    {
      for (String column : this.columns)
      {
        extractData(src, column, data);
      }
    }
    

    extractData(src, Config.Extractor.table.name(), data);
    
    return data;
  }
  
  private void extractData(String src, String suffix, Map appendTo)
  {
    Object[] jsonPath = new MappingsConfig(this.config).getPropertyPath(getId(), suffix);
    
    Map<Object, Object> params = this.config.getMap(jsonPath);
    
    if (params == null) {
      return;
    }
    
    if (!isCaseSensitive()) {
      src = src.toLowerCase();
    }
    
    src = format(src);
    
    for (Map.Entry entry : params.entrySet())
    {

      String key = format(entry.getKey().toString());
      Object val = entry.getValue();
      
      boolean contains = src.contains(!isCaseSensitive() ? key.toLowerCase() : key);
      
      XLogger.getInstance().log(Level.FINER, "{0} contains {1}: {2}", getClass(), src, key, Boolean.valueOf(contains));
      

      if (contains)
      {
        Object value = appendTo.get(suffix);
        
        boolean put = value == null ? true : isReplace(jsonPath);
        
        if (!put)
          break;
        XLogger.getInstance().log(Level.FINE, "Extracted {0}={1} from {2}", getClass(), suffix, val, src);
        

        appendTo.put(suffix, val); break;
      }
    }
  }
  



  private String format(String s)
  {
    String amp = "&amp;";
    if (s.length() >= "&amp;".length()) {
      s = s.replace("&amp;", "&");
    }
    try {
      s = URLDecoder.decode(s, "UTF-8");
    }
    catch (UnsupportedEncodingException|RuntimeException ignored) {}
    return s;
  }
  
  private boolean isReplace(Object[] path)
  {
    path = this.config.getPath(path, new Object[] { Config.Extractor.replace });
    
    Boolean replace = this.config.getBoolean(path);
    

    return replace == null ? true : replace.booleanValue();
  }
  
  public String[] getColumns() {
    return this.columns;
  }
  
  public boolean isCaseSensitive() {
    return this.caseSensitive;
  }
  
  public void setCaseSensitive(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }
}
