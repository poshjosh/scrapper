package com.scrapper.url;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigURLPartList extends AbstractList implements Serializable
{
  private int partIndex;
  private String type;
  private boolean ascending;
  private int start;
  private int end;
  private String value;
  private Map<String, List> replacements;
  public ConfigURLPartList() {}
  
  public static enum ListGenerationMode
  {
    none,  serial,  replacements;
    



    private ListGenerationMode() {}
  }
  



  public ConfigURLPartList(JsonConfig config, String type, int partIndex)
    throws UnsupportedOperationException
  {
    update(config, type, partIndex);
  }
  
  public static boolean accept(JsonConfig config, String type, int partIndex) {
    return config.getMap(new Object[] { Config.Site.url, type, "part" + partIndex }) != null;
  }
  
  private void update(JsonConfig config, String type, int partIndex) throws UnsupportedOperationException {
    if ((config == null) || (type == null)) {
      throw new NullPointerException();
    }
    if (!accept(config, type, partIndex)) {
      throw new UnsupportedOperationException("url.counter.part" + partIndex + " == null");
    }
    Map partValues = config.getMap(new Object[] { Config.Site.url, type, "part" + partIndex });
    Object oval = partValues.get("ascending");
    this.ascending = (oval == null ? true : Boolean.parseBoolean(oval.toString()));
    oval = partValues.get("start");
    this.start = (oval == null ? 0 : Integer.parseInt(oval.toString()));
    oval = partValues.get("end");
    this.end = (oval == null ? -1 : Integer.parseInt(oval.toString()));
    oval = partValues.get("value");
    this.value = (oval == null ? null : oval.toString());
    oval = partValues.get("replacements");
    
    this.replacements = (oval == null ? null : new HashMap((Map)oval));
    


    if (this.replacements != null)
    {
      HashMap<String, List> newValues = new HashMap();
      
      for (Map.Entry<String, List> entry : this.replacements.entrySet())
      {
        String toReplace = (String)entry.getKey();
        List possibleValues = (List)entry.getValue();
        String firstVal = possibleValues.get(0).toString().trim();
        if (firstVal.startsWith("@"))
        {



          firstVal = firstVal.substring(1);
          
          Object[] parts = firstVal.split("\\.");
          
          Object lastPart = parts[(parts.length - 1)];
          
          List updates = null;
          


          if (isMapIdentifier(lastPart)) {
            Object[] jsonPath = new Object[parts.length - 1];
            System.arraycopy(parts, 0, jsonPath, 0, jsonPath.length);
            Map referenced = (Map)config.getObject(jsonPath);
            updates = getList(referenced, lastPart);
          } else {
            updates = (List)config.getObject(parts);
          }
          
          XLogger.getInstance().log(Level.FINE, "Updating {0} to {1}", getClass(), possibleValues, updates);
          
          assert (updates != null);
          
          newValues.put(toReplace, updates);
        }
      }
      this.replacements.putAll(newValues);
    }
    
    if ((this.end == -1) && (this.value == null)) {
      throw new UnsupportedOperationException("Missing values in: url.counter.part" + partIndex);
    }
    
    this.partIndex = partIndex;
    this.type = type;
  }
  
  public static ConfigURLPartList getSerialPart(JsonConfig config, String type) {
    int max = 20;
    ConfigURLPartList part = new ConfigURLPartList();
    for (int i = 0; i < max; i++) {
      try {
        part.update(config, type, i);
        if (part.getListGenerationMode() == ListGenerationMode.serial)
        {
          return part;
        }
      } catch (RuntimeException ignored) {}
    }
    return null;
  }
  
  private List getList(Map map, Object identifier) {
    if (identifier.equals("values"))
      return new ArrayList(new HashSet(map.values()));
    if (identifier.equals("keys")) {
      return new ArrayList(map.keySet());
    }
    return new ArrayList(map.entrySet());
  }
  
  private boolean isMapIdentifier(Object obj)
  {
    return (obj.equals("keys")) || (obj.equals("values")) || (obj.equals("entries"));
  }
  
  public ListGenerationMode getListGenerationMode() {
    ListGenerationMode mode;
    if (this.end != -1) {
      mode = ListGenerationMode.serial;
    } else { 
      if (this.replacements != null) {
        mode = ListGenerationMode.replacements;
      } else { 
        if (this.value != null) {
          mode = ListGenerationMode.none;
        } else
          throw new NullPointerException(getClass() + "#getValue == null");
      }
    }
    return mode;
  }
  


  public Object get(int index)
  {
    Object output = null;
    
    ListGenerationMode mode = getListGenerationMode();
    
    boolean indexOutOfBounds = false;
    
    switch (mode)
    {
    case none: 
      if (index == 0) {
        output = this.value;
      } else {
        indexOutOfBounds = true;
      }
      break;
    
    case serial: 
      if ((index < 0) || (index >= size())) {
        indexOutOfBounds = true;
      }
      else if (this.ascending) {
        output = Integer.valueOf(index + this.start);
      } else {
        output = Integer.valueOf(this.end - index);
      }
      
      break;
    
    case replacements: 
      if ((index < 0) || (index >= size())) {
        indexOutOfBounds = true;
      } else {
        Iterator<Map.Entry<String, List>> iter = this.replacements.entrySet().iterator();
        int listIndex = index;
        while (iter.hasNext()) {
          Map.Entry<String, List> entry = (Map.Entry)iter.next();
          String toReplace = (String)entry.getKey();
          List possibleValues = (List)entry.getValue();
          if (listIndex >= possibleValues.size()) {
            listIndex -= possibleValues.size();
          }
          else {
            output = this.value.replaceFirst(toReplace, possibleValues.get(listIndex).toString());
          }
        }
      }
      break;
    
    default: 
      throw new UnsupportedOperationException(getUnexpectedModeMessage(mode));
    }
    
    if (indexOutOfBounds) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    
    return output;
  }
  



  public int size()
  {
    ListGenerationMode mode = getListGenerationMode();
    int output;
    switch (mode) {
    case none: 
      output = 1;
      break;
    
    case serial: 
      output = this.end - this.start;
      break;
    
    case replacements: 
      int size = 0;
      for (List possibleValues : this.replacements.values()) {
        size += possibleValues.size();
      }
      output = size;
      break;
    
    default: 
      throw new UnsupportedOperationException(getUnexpectedModeMessage(mode));
    }
    
    return output;
  }
  
  private String getUnexpectedModeMessage(ListGenerationMode mode) {
    String msg = "Unexpected: " + ListGenerationMode.class + ", found: " + mode + ", expected: " + Arrays.toString(ListGenerationMode.values());
    

    return msg;
  }
  
  public boolean isAscending() {
    return this.ascending;
  }
  
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }
  
  public int getEnd() {
    return this.end;
  }
  
  public void setEnd(int end) {
    this.end = end;
  }
  
  public Map getReplacements() {
    return this.replacements;
  }
  
  public void setReplacements(Map replacements) {
    this.replacements = replacements;
  }
  
  public int getStart() {
    return this.start;
  }
  
  public void setStart(int start) {
    this.start = start;
  }
  
  public String getValue() {
    return this.value;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public int getPartIndex() {
    return this.partIndex;
  }
  
  public void setPartIndex(int partIndex) {
    this.partIndex = partIndex;
  }
  
  public String getType() {
    return this.type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
}
