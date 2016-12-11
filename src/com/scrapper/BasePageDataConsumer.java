package com.scrapper;

import java.net.URL;
import java.util.Map;
import com.bc.dom.HtmlPageDom;

public class BasePageDataConsumer extends AbstractPageDataConsumer {
    
  private DataUploader uploader;
  
  public BasePageDataConsumer(URL insertURL, final Map uploadParameters)
  {
    if (insertURL == null) {
      throw new NullPointerException();
    }
    
    if ((uploadParameters == null) || (uploadParameters.isEmpty())) {
      throw new NullPointerException();
    }
    
    this.uploader = new DataUploader(insertURL)
    {
      public Map getUploadParameters() {
        return uploadParameters;
      }
    };
  }
  

  public boolean doConsume(HtmlPageDom page, Map data)
  {
    Object productTable = data.get(getTableNameKey());
    
    this.uploader.getUploadParameters().put(getTableNameKey(), productTable);
    
    return this.uploader.uploadRecord(data) == 200;
  }
}
