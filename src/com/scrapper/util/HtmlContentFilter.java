package com.scrapper.util;

import com.scrapper.Filter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;























public class HtmlContentFilter
  implements Filter<String>
{
  public boolean accept(String link)
  {
    boolean ret = false;
    try {
      URL url = new URL(link);
      URLConnection connection = url.openConnection();
      String type = connection.getContentType();
      if (type == null) {
        ret = false;
      } else {
        ret = type.startsWith("text/html");
      }
    }
    catch (IOException e) {}
    
    return ret;
  }
}
