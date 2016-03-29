package com.scrapper.util;

import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.htmlparser.Attribute;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;

public class AttributesManager
{
  private boolean useIdExclusively;
  
  public AttributesManager()
  {
    this.useIdExclusively = true;
  }
  
  public Object[] updateAttributes(JsonConfig config, String nodeName, Tag selectedTag)
  {
    Map<String, String> attrPropVals = getAttributes(selectedTag);
    
    Object[] attrPropKey;
    
    if ((attrPropVals != null) && (!attrPropVals.isEmpty())) { 
      if ((isUseIdExclusively()) && (attrPropVals.containsKey("id")))
      {

        String idVal = (String)attrPropVals.get("id");
        
        attrPropVals.clear();
        attrPropVals.put("id", idVal);
        
        attrPropKey = new Object[] { nodeName, Config.Extractor.attributes };
      }
      else {
        if (updateUniqueAttributes(attrPropVals)) {
          attrPropKey = new Object[] { nodeName, Config.Extractor.attributesRegex };
        } else {
          attrPropKey = new Object[] { nodeName, Config.Extractor.attributes };
        }
      }
      
      config.setObject(attrPropKey, attrPropVals);
    }
    else
    {
      attrPropKey = null;
    }
    
    return attrPropKey;
  }
  
  public Map<String, String> getAttributes(Tag tag) {
    Map<String, String> output = new HashMap()
    {
      public String put(String key, String value) {
        if ((key == null) || (value == null)) throw new NullPointerException();
        return (String)super.put(key, value);
      }
    };
    List attributes = tag.getAttributesEx();
    for (Object oval : attributes) {
      Attribute attr = (Attribute)oval;
      if ((attr.getName() != null) && (attr.getValue() != null))
      {

        output.put(attr.getName(), attr.getValue()); }
    }
    return output;
  }
  
  public boolean updateUniqueAttributes(Map<String, String> map)
  {
    Iterator<String> iter = map.keySet().iterator();
    
    HashMap<String, String> replaceme = new HashMap();
    
    while (iter.hasNext()) {
      String key = (String)iter.next();
      if (isUnique(key)) {
        String newVal = ((String)map.get(key)).isEmpty() ? ".*?" : ".+?";
        replaceme.put(key, newVal);
      }
    }
    
    if (!replaceme.isEmpty()) {
      map.putAll(replaceme);
      return true;
    }
    return false;
  }
  

  public boolean isUnique(String attrKey)
  {
    attrKey = attrKey.toLowerCase();
    
    return (attrKey.equals("src")) || (attrKey.equals("title")) || (attrKey.equals("alt")) || (attrKey.equals("width")) || (attrKey.equals("height")) || (attrKey.startsWith("on"));
  }
  






  public void updateAttributesExtractionRequirements(JsonConfig props, String targetNode, Tag tag, List cols)
  {
    if (((tag instanceof ImageTag)) || ((tag instanceof LinkTag)))
    {
      props.setObject(new Object[] { targetNode, Config.Extractor.nodeTypesToAccept }, new Object[] { "tag" });
      


      props.setObject(new Object[] { targetNode, Config.Extractor.nodesToRetainAttributes }, new Object[] { tag.getTagName() });
      


      String attributeName = getDefaultAttributeToExtract(tag);
      
      props.setObject(new Object[] { targetNode, Config.Extractor.attributesToExtract }, new Object[] { attributeName });


    }
    else if ((cols != null) && (cols.size() == 1) && (cols.contains("description")))
    {
      props.setObject(new Object[] { targetNode, Config.Extractor.nodeTypesToAccept }, new Object[] { "tag", "text" });
    }
  }
  

  public String getDefaultAttributeToExtract(Tag tag)
  {
    String attributeName;
    if ((tag instanceof ImageTag)) {
      attributeName = "src"; 
    } else { 
      if ((tag instanceof LinkTag)) {
        attributeName = "href";
      } else {
        throw new UnsupportedOperationException("Expected: <img> or <a> tag found: " + tag.getTagName()); 
      }  
    }
    return attributeName;
  }
  
  public boolean isUseIdExclusively() {
    return this.useIdExclusively;
  }
  
  public void setUseIdExclusively(boolean useIdExclusively) {
    this.useIdExclusively = useIdExclusively;
  }
}
