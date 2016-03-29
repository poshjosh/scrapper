package com.scrapper.config;

import com.bc.util.XLogger;
import com.scrapper.filter.FilterFactory;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;


















public class JsonType
{
  private Set<String> nodeTypes;
  private ArrayList<Enum[]> typedEnums = new ArrayList();
  
  public JsonType() {
    this.nodeTypes = new HashSet(Arrays.asList(FilterFactory.NODE_TYPES));
    this.typedEnums.add(Config.Extractor.values());
    this.typedEnums.add(Config.Formatter.values());
    this.typedEnums.add(Config.Site.values());
    this.typedEnums.add(Config.Login.values());
  }
  






  public Class getType(Enum[] arr, String propertyLink)
  {
    Class type = getCustomType(propertyLink);
    
    if (type == null)
    {
      Enum en = getMatchingEnum(arr, propertyLink);
      
      if (en == null) {
        type = String.class;
      } else {
        type = getType(en);
      }
    }
    
    return type;
  }
  




  public Class getType(Object[] pathToValue, Class defaultType)
  {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < pathToValue.length; i++) {
      builder.append(pathToValue[i]);
      if (i < pathToValue.length - 1) {
        builder.append('.');
      }
    }
    return getType(builder.toString(), defaultType);
  }
  





  public Class getType(String propertyLink, Class defaultType)
  {
    Class type = getCustomType(propertyLink);
    
    if (type == null)
    {
      Enum en = null;
      for (Enum[] arr : this.typedEnums) {
        en = getMatchingEnum(arr, propertyLink);
        if (en != null) {
          break;
        }
      }
      
      if (en == null) {
        type = defaultType;
      } else {
        type = getType(en);
      }
    }
    XLogger.getInstance().log(Level.FINER, "Property link: {0}, type: {1}", getClass(), propertyLink, type);
    
    return type;
  }
  
  private Enum getMatchingEnum(Enum[] arr, String pname)
  {
    int n = pname.indexOf('.');
    String pnamePart = null;
    if (n != -1)
    {
      String[] parts = pname.split("\\.");
      pnamePart = parts[(parts.length - 1)];
    }
    

    if (pname.equals("datePatterns")) {
      return Config.Formatter.datePatterns;
    }
    
    for (Enum en : arr) {
      if (matches(pnamePart == null ? pname : pnamePart, en)) {
        return en;
      }
    }
    PrintStream st = n == -1 ? System.out : System.err;
    st.println(getClass().getName() + ". No match found for: " + pname);
    return null;
  }
  
  public Class getType(Enum en) {
    Class enClass = en.getClass();
    if (enClass == Config.Formatter.class)
      return ((Config.Formatter)Enum.valueOf(Config.Formatter.class, en.name())).getType();
    if (enClass == Config.Extractor.class)
      return ((Config.Extractor)Enum.valueOf(Config.Extractor.class, en.name())).getType();
    if (enClass == Config.Site.class)
      return ((Config.Site)Enum.valueOf(Config.Site.class, en.name())).getType();
    if (enClass == Config.Login.class) {
      return ((Config.Login)Enum.valueOf(Config.Login.class, en.name())).getType();
    }
    throw new IllegalArgumentException("Unexpected PropertyEnum class: " + enClass);
  }
  
  private Class getCustomType(String pname)
  {
    if (pname.equals("url.start"))
    {
      return String.class; }
    if (pname.contains(".mappings.")) {
      if (pname.endsWith(".replace")) {
        return Boolean.class;
      }
      return Map.class;
    }
    if ((pname.startsWith("expression")) && (
      (pname.contains(".replace.")) || (pname.contains(".replaceRegex.")))) {
      return Map.class;
    }
    
    return null;
  }
  
  private boolean matches(String pnamePart, Enum ex) { String name = ex.name();
    if (pnamePart.equals(name)) {
      return true;
    }
    if (this.nodeTypes.contains(name)) {
      for (int i = 0; i < 20; i++) {
        if (pnamePart.equals(name + i))
          return true;
        if (pnamePart.equals(name + i + "Value")) {
          return true;
        }
      }
    }
    return false;
  }
}
