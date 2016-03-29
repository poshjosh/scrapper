package com.scrapper;

import com.bc.util.XLogger;
import com.scrapper.util.PageNodes;
import com.scrapper.util.Util;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
























public abstract class AbstractPageDataConsumer
  implements PageDataConsumer, Serializable
{
  private int minimumParameters;
  private String defaultTableName;
  private Formatter<Map<String, Object>> formatter;
  
  protected abstract boolean doConsume(PageNodes paramPageNodes, Map paramMap);
  
  public boolean consume(PageNodes page, Map data)
  {
    XLogger.getInstance().log(Level.FINEST, " URL: {0}\nData: {1}", getClass(), page == null ? null : page.getURL(), data);
    




    if (!accept(page, data)) {
      return false;
    }
    
    data = format(page, data);
    
    XLogger.getInstance().log(Level.FINEST, "After all formats, parameters: {0}", getClass(), data);
    

    return doConsume(page, data);
  }
  
  public boolean accept(PageNodes page, Map data)
  {
    if (data == null) {
      throw new NullPointerException();
    }
    
    boolean accept = data.size() >= this.minimumParameters;
    
    if (!accept) {
      XLogger.getInstance().log(Level.FINE, "Insufficient data: {0}", getClass(), data.keySet());
    }
    

    return accept;
  }
  
  public Map format(PageNodes page, Map data)
  {
    addTableName(page, data);
    
    addExtraDetails(page, data);
    
    if (this.formatter != null) {
      data = (Map)this.formatter.format(data);
    }
    
    return data;
  }
  
  protected void addTableName(PageNodes page, Map<String, Object> data)
  {
    String tablenameKey = getTableNameKey();
    


    String tablenameVal = Util.getTableValue(data, getDefaultTableName());
    
    XLogger.getInstance().log(Level.FINER, "Adding: {0}={1}", getClass(), tablenameKey, tablenameVal);
    
    data.put(tablenameKey, tablenameVal);
  }
  
  protected void addExtraDetails(PageNodes page, Map<String, Object> data)
  {
    if (page == null) {
      return;
    }
    
    String url = page.getURL();
    
    Object value = data.get("extraDetails");
    
    if (value == null) {
      XLogger.getInstance().log(Level.FINER, "Adding extraDetails: {0}", getClass(), url);
      data.put("extraDetails", "url=" + url);
    } else {
      Map temp = Util.getParameters(value.toString(), "&");
      temp.put("url", url);
      value = Util.appendQuery(temp, null);
      XLogger.getInstance().log(Level.FINER, "Updated extraDetails: {0}", getClass(), temp);
      data.put("extraDetails", value);
    }
  }
  
  public String getTableNameKey() {
    return AppProperties.getProperty("tablenameKey");
  }
  
  public int getMinimumParameters() {
    return this.minimumParameters;
  }
  
  public void setMinimumParameters(int minimumParameters) {
    this.minimumParameters = minimumParameters;
  }
  
  public Formatter<Map<String, Object>> getFormatter() {
    return this.formatter;
  }
  
  public void setFormatter(Formatter<Map<String, Object>> formatter) {
    this.formatter = formatter;
  }
  
  public String getDefaultTableName() {
    return this.defaultTableName;
  }
  
  public void setDefaultTableName(String defaultTableName) {
    this.defaultTableName = defaultTableName;
  }
}
