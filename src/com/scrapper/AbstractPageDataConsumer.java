package com.scrapper;

import com.bc.webdatex.formatter.Formatter;
import com.bc.util.XLogger;
import com.scrapper.util.Util;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import com.bc.dom.HtmlDocument;

public abstract class AbstractPageDataConsumer implements PageDataConsumer, Serializable {
    
  private int minimumParameters;
  private String defaultTableName;
  private Formatter<Map<String, Object>> formatter;
  
  protected abstract boolean doConsume(HtmlDocument pageDom, Map paramMap);
  
  @Override
  public boolean consume(HtmlDocument pageDom, Map data)
  {
    XLogger.getInstance().log(Level.FINEST, " URL: {0}\nData: {1}", getClass(), pageDom == null ? null : pageDom.getURL(), data);
    




    if (!accept(pageDom, data)) {
      return false;
    }
    
    data = format(pageDom, data);
    
    XLogger.getInstance().log(Level.FINEST, "After all formats, parameters: {0}", getClass(), data);
    

    return doConsume(pageDom, data);
  }
  
  public boolean accept(HtmlDocument page, Map data)
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
  
  public Map format(HtmlDocument page, Map data)
  {
    addTableName(page, data);
    
    addExtraDetails(page, data);
    
    if (this.formatter != null) {
      data = (Map)this.formatter.apply(data);
    }
    
    return data;
  }
  
  protected void addTableName(HtmlDocument page, Map<String, Object> data)
  {
    String tablenameKey = getTableNameKey();
    


    String tablenameVal = Util.getTableValue(data, getDefaultTableName());
    
    XLogger.getInstance().log(Level.FINER, "Adding: {0}={1}", getClass(), tablenameKey, tablenameVal);
    
    data.put(tablenameKey, tablenameVal);
  }
  
  protected void addExtraDetails(HtmlDocument page, Map<String, Object> data)
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
    return com.scrapper.CapturerApp.getInstance().getProperty("tablenameKey");
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
