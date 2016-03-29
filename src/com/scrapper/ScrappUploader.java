package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;












public class ScrappUploader
  extends ContextDataConsumer
{
  private DataUploader uploader;
  
  public ScrappUploader(CapturerContext context)
  {
    super(context);
    
    URL insertURL = null;
    try {
      insertURL = new URL(AppProperties.getProperty("insertUrl"));
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
    
    this.uploader = new DataUploader(insertURL)
    {
      public Map getUploadParameters() {
        return m;
      }
    };
  }
  

  public boolean doConsume(PageNodes page, Map data)
  {
    Object productTable = data.get(getTableNameKey());
    
    this.uploader.getUploadParameters().put(getTableNameKey(), productTable);
    
    return this.uploader.uploadRecord(data) == 200;
  }
}
