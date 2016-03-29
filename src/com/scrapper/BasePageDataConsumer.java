package com.scrapper;

import com.scrapper.util.PageNodes;
import java.net.URL;
import java.util.Map;














public class BasePageDataConsumer
  extends AbstractPageDataConsumer
{
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
  

  public boolean doConsume(PageNodes page, Map data)
  {
    Object productTable = data.get(getTableNameKey());
    
    this.uploader.getUploadParameters().put(getTableNameKey(), productTable);
    
    return this.uploader.uploadRecord(data) == 200;
  }
}
