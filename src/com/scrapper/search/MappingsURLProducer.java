package com.scrapper.search;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MappingsURLProducer
  extends DefaultURLProducer
  implements HasUrlSubs
{
  private String defaultMapping;
  private List<String> urlSubs;
  
  public MappingsURLProducer()
  {
    setFormatSlashes(true);
  }
  
  public MappingsURLProducer(JsonConfig config) {
    if (!DefaultURLProducer.isUseMappings(config)) {
      throw new UnsupportedOperationException("Rather create an instance of: " + DefaultURLProducer.class.getName());
    }
    setFormatSlashes(true);
    init(config);
  }
  
  protected void init(JsonConfig config)
  {
    super.init(config);
    this.defaultMapping = config.getString(new Object[] { "url", "search", "defaultMapping" });
  }
  
  protected String getDefaultUrlSub() {
    return this.defaultMapping;
  }
  


  public List<String> getSearchURLs(CapturerContext context, String tableName, Map parameters, String searchTerm)
  {
    JsonConfig config = context.getConfig();
    
    String baseUrl = getBaseUrl(config);
    
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    
    this.urlSubs = getSubs(tableName, parameters, config);
    
    ArrayList<String> output = new ArrayList();
    
    StringBuilder appendTo = new StringBuilder();
    
    for (Object sub : this.urlSubs)
    {
      appendURL(baseUrl, sub.toString(), parameters, searchTerm, appendTo);
      
      output.add(appendTo.toString());
      
      appendTo.setLength(0);
    }
    XLogger.getInstance().log(Level.FINE, "URLs: {0}, {1}", getClass(), Integer.valueOf(output.size()), output);
    return output;
  }
  
  protected List getSubs(String tableName, Map parameters, JsonConfig config)
  {
    List subs = null;
    
    List tableSubs = getSubs(Config.Extractor.table.name(), tableName, config);
    
    Object typeId = parameters == null ? null : parameters.get(Config.Extractor.type.name());
    
    XLogger.getInstance().log(Level.FINEST, "{0}={1}", getClass(), Config.Extractor.type.name(), typeId);
    
    if (typeId != null)
    {

      List typeSubs = getSubs(Config.Extractor.type.name(), typeId, config);
      
      if ((typeSubs != null) && (tableSubs != null))
      {
        Iterator iter = typeSubs.iterator();
        
        while (iter.hasNext())
        {
          if (!tableSubs.contains(iter.next()))
          {
            iter.remove();
          }
        }
        XLogger.getInstance().log(Level.FINER, "After format URL parts: {0}", getClass(), typeSubs);
        if (!typeSubs.isEmpty()) {
          subs = typeSubs;
        }
      }
    }
    
    if (subs == null) {
      if (tableSubs != null) {
        subs = tableSubs;
      }
      else if (getDefaultUrlSub() != null) {
        subs = new ArrayList();
        subs.add(getDefaultUrlSub());
      }
    }
    

    return subs;
  }
  
  protected List getSubs(String name, Object value, JsonConfig config)
  {
    XLogger.getInstance().log(Level.FINER, "To find: [{0}={1}]", getClass(), name, value);
    
    ArrayList subs = null;
    
    Map tableMappings = config.getMap(new Object[] { Config.Site.url, "mappings", name });
    
    XLogger.getInstance().log(Level.FINEST, "url.mappings.{0}={1}", getClass(), name, tableMappings);
    

    for (Object entryObj : tableMappings.entrySet())
    {
      Map.Entry entry = (Map.Entry)entryObj;
      


      if (entry.getValue().toString().equals(value.toString()))
      {
        if (subs == null) {
          subs = new ArrayList();
        }
        XLogger.getInstance().log(Level.FINER, "Adding: {0}", getClass(), entry.getKey());
        

        subs.add(entry.getKey());
      }
    }
    
    XLogger.getInstance().log(Level.FINER, "URL parts: {0}", getClass(), subs);
    

    return subs;
  }
  
  public List<String> getUrlSubs()
  {
    return this.urlSubs;
  }
}
