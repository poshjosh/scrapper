package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import com.bc.dom.HtmlPageDom;

public class ScrappUploader extends ContextDataConsumer {
    
  private DataUploader uploader;
  
  public ScrappUploader(CapturerContext context) {
      
    super(context);
    
    URL insertURL = null;
    try {
      insertURL = new URL(com.scrapper.CapturerApp.getInstance().getProperty("insertUrl"));
      if (insertURL == null) {
        throw new NullPointerException("Insert URL == null");
      }
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Insert URL: " + insertURL);
    }
    
    JsonConfig config = context.getConfig();
    
    final Map m = config.getMap(new Object[] { "uploadParameters" });
    
    if ((m == null) || (m.isEmpty())) {
      throw new NullPointerException();
    }
    
    this.uploader = new DataUploader(insertURL){
      @Override
      public Map getUploadParameters() {
        return m;
      }
    };
  }
  

  @Override
  public boolean doConsume(HtmlPageDom page, Map data){
      
    Object productTable = data.get(getTableNameKey());
    
    this.uploader.getUploadParameters().put(getTableNameKey(), productTable);
    
    return this.uploader.uploadRecord(data) == 200;
  }
}
