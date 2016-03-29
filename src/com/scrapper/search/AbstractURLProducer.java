package com.scrapper.search;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class AbstractURLProducer
  implements URLProducer
{
  private boolean formatSlashes;
  
  protected abstract String getURLPart();
  
  protected abstract String getQueryBeforeSearchTerm();
  
  protected abstract String getQueryAfterSearchTerm();
  
  public List<String> getCategoryURLs(CapturerContext context, String tableName)
  {
    JsonConfig config = context.getConfig();
    
    Map typeMappings = config.getMap(new Object[] { Config.Site.url, "mappings", "type" });
    
    if ((typeMappings == null) || (typeMappings.isEmpty())) {
      return null;
    }
    
    String baseUrl = getBaseUrl(config);
    
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    
    StringBuilder builder = new StringBuilder(baseUrl);
    
    Map tableMappings = null;
    
    if (tableName != null)
    {
      tableMappings = config.getMap(new Object[] { Config.Site.url, "mappings", Config.Extractor.table.name() });
      

      if ((tableMappings == null) || (tableMappings.isEmpty())) {
        XLogger.getInstance().log(Level.WARNING, "No table mappings found for config: {0}", getClass(), config.getName());
      }
    }
    


    ArrayList<String> urls = new ArrayList(typeMappings.size());
    
    for (Object key : typeMappings.keySet())
    {
      builder.setLength(baseUrl.length());
      
      String sval = key.toString();
      
      if (isTable(tableMappings, tableName, sval))
      {


        append(sval, builder);
        









        urls.add(builder.toString());
      }
    }
    return urls;
  }
  
  private boolean isTable(Map tableMappings, String tableName, Object categoryString) {
    if (tableName == null) {
      return true;
    }
    if ((tableMappings == null) || (categoryString == null)) {
      throw new NullPointerException();
    }
    
    Object categoryTable = tableMappings.get(categoryString);
    
    return tableName.equals(categoryTable);
  }
  


  public List<String> getSearchURLs(CapturerContext context, String tableName, Map parameters, String searchTerm)
  {
    JsonConfig config = context.getConfig();
    
    String baseUrl = getBaseUrl(config);
    
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    
    ArrayList<String> output = new ArrayList();
    
    StringBuilder appendTo = new StringBuilder();
    
    appendURL(baseUrl, null, parameters, searchTerm, appendTo);
    
    output.add(appendTo.toString());
    
    XLogger.getInstance().log(Level.FINE, "URLs: {0}, {1}", getClass(), Integer.valueOf(output.size()), output);
    return output;
  }
  
  protected void appendURL(String baseUrl, Object part, Map parameters, String searchTerm, StringBuilder appendTo)
  {
    appendTo.append(baseUrl);
    
    append(getURLPart(), appendTo);
    
    if (part != null) {
      append(part.toString(), appendTo);
    }
    
    appendSearchTerm(parameters, searchTerm, appendTo);
  }
  
  protected void append(String part, StringBuilder appendTo) {
    if (part == null) return;
    if (this.formatSlashes) {
      char lastChar = appendTo.charAt(appendTo.length() - 1);
      if ((lastChar != '/') && (!part.startsWith("/"))) {
        appendTo.append('/');
      }
    }
    appendTo.append(part);
  }
  
  protected void appendSearchTerm(Map parameters, String searchTerm, StringBuilder appendTo)
  {
    String b4 = getQueryBeforeSearchTerm();
    
    append(b4, appendTo);
    











    try
    {
      searchTerm = URLEncoder.encode(searchTerm, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    } catch (RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    
    appendTo.append(searchTerm);
    
    if (getQueryAfterSearchTerm() != null) {
      appendTo.append(getQueryAfterSearchTerm());
    }
  }
  
  public String getBaseUrl(JsonConfig config)
  {
    String baseUrl = config.getString(new Object[] { Config.Site.url, "value" });
    
    if (baseUrl == null) {
      String s = config.getString(new Object[] { Config.Site.url, "start" });
      if (s == null) {
        throw new NullPointerException("Both url.value and url.start cannot be null in config: " + config.getName());
      }
      baseUrl = com.bc.util.Util.getBaseURL(s);
    }
    
    XLogger.getInstance().log(Level.FINER, "Base URL: {0}", getClass(), baseUrl);
    return baseUrl;
  }
  
  public boolean isFormatSlashes() {
    return this.formatSlashes;
  }
  
  public void setFormatSlashes(boolean formatSlashes) {
    this.formatSlashes = formatSlashes;
  }
}
