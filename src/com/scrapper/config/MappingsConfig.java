package com.scrapper.config;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MappingsConfig
{
  private JsonConfig config;
  
  public MappingsConfig() {}
  
  public MappingsConfig(JsonConfig config)
  {
    this.config = config;
  }
  
  public boolean isColumnMapping(Mapping mapping) {
    return mapping.getColumn() != null;
  }
  

  public String getTablePropertyForColumnMapping(String prefix, String value, boolean useDefaultIfNoneFound)
  {
    Object[] propertyKey = getPropertyPath(prefix, Config.Extractor.table.name());
    

    Map params = getConfig().getMap(propertyKey);
    
    Object output;
    if (params != null)
    {
      output = params.get(value);
    }
    else {
      if (useDefaultIfNoneFound)
      {
        output = this.config.getList(new Object[] { Config.Site.tables }).get(0).toString();
      } else {
        output = null;
      }
    }
    
    return output == null ? null : output.toString();
  }
  

  public Mapping toMapping(String table, String column, String category, String value)
  {
    return new DefaultMapping(table, column, category, value);
  }
  
  private Map<String, String> getCache(String key, Map<String, Map<String, String>> cache) {
    Map<String, String> sub = (Map)cache.get(key);
    if (sub == null) {
      sub = new HashMap()
      {
        public Object put(Object key, Object value) {
          if ((key == null) || (value == null)) throw new NullPointerException();
          return super.put(key, value);
        }
      };
      cache.put(key, sub);
    }
    return sub;
  }
  

  public boolean isTableMapping(Mapping mapping)
  {
    return (mapping.getColumn() == null) || (mapping.getCategory() == null);
  }
  
  public Map.Entry<String, String> getTableEntry(Mapping mapping)
    throws IllegalArgumentException
  {
    if (mapping.getTable() == null) {
      throw new IllegalArgumentException("Please specify a table for value: " + mapping.getValue());
    }

    return getEntry(mapping.getValue(), mapping.getTable());
  }
  
  private Map.Entry<String, String> getEntry(final String key, final String val)
  {
    return new Map.Entry<String, String>()
    {
      public String getKey() {
        return key;
      }
      
      public String getValue() {
        return val;
      }
      
      public String setValue(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    };
  }
  
  public Object[] getPropertyPath(String prefix, String suffix)
  {
    if (prefix == null)
      throw new NullPointerException();
    Object[] output;
    if (suffix == null) {
      output = new String[] { prefix, "mappings" };
    } else {
      output = new String[] { prefix, "mappings", suffix };
    }
    return output;
  }
  


  private void setProperty(JsonConfig props, Object[] pathToValue, Map<String, String> value)
  {
    if ((value == null) || (value.isEmpty()))
    {
      XLogger.getInstance().log(Level.FINER, "Removing: {0}", getClass(), pathToValue);
      

      props.remove(pathToValue);
    }
    else
    {
      XLogger.getInstance().log(Level.FINER, "Updating: {0}={1}", getClass(), pathToValue, value);
      

      props.setObject(pathToValue, value);
    }
  }
  

  private static class DefaultMapping
    implements MappingsConfig.Mapping
  {
    private String table;
    
    private String column;
    
    private String category;
    
    private String value;
    
    private DefaultMapping(String table, String column, String category, String value)
    {
      if (value == null) {
        throw new NullPointerException("Please enter the required input(s) before proceeding");
      }
      if ((table == null) && (column == null) && (category == null)) {
        throw new NullPointerException("Please enter the required input(s) before proceeding");
      }
      this.table = table;
      this.column = column;
      this.category = category;
      this.value = value;
    }
    
    public String getCategory() {
      return this.category;
    }
    
    public String getColumn() {
      return this.column;
    }
    
    public String getTable() {
      return this.table;
    }
    
    public String getValue() {
      return this.value;
    }
    

    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      if (getColumn() != null) {
        builder.append(getValue()).append('=').append(getTable());
        builder.append('>').append(getColumn()).append('>').append(getCategory());
        return builder.toString();
      }
      builder.append(getValue()).append('=').append(getTable());
      return builder.toString();
    }
    
    public boolean equals(Object o)
    {
      if (this == o) {
        return true;
      }
      if (!(o instanceof MappingsConfig.Mapping)) {
        return false;
      }
      if (hashCode() != o.hashCode()) {
        return false;
      }
      return toString().equals(o.toString());
    }
    
    public int hashCode() {
      int hash = 7;
      hash = 59 * hash + (this.table != null ? this.table.hashCode() : 0);
      hash = 59 * hash + (this.column != null ? this.column.hashCode() : 0);
      hash = 59 * hash + (this.category != null ? this.category.hashCode() : 0);
      hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
      return hash;
    }
  }
  
  public JsonConfig getConfig() {
    return this.config;
  }
  
  public void setConfig(JsonConfig config) {
    this.config = config;
  }
  
  public static abstract interface Mapping
  {
    public abstract String getValue();
    
    public abstract String getTable();
    
    public abstract String getColumn();
    
    public abstract String getCategory();
  }
}
