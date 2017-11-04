package com.scrapper.util;

import com.bc.webdatex.filter.Filter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class HtmlContentFilter implements Filter<String> {
    
  @Override
  public boolean test(String link) {
//long mb4 = com.bc.util.Util.availableMemory();
//long tb4 = System.currentTimeMillis();
    boolean ret = false;
    try {
      URL url = new URL(link);
      URLConnection connection = url.openConnection();
      String type = connection.getContentType();
      if (type == null) {
        ret = false;
      } else {
        ret = type.toLowerCase().contains("html");
//        ret = type.startsWith("text/html");
      }
    } catch (IOException e) {}
//System.out.println("Is HTML content: "+ret+". Consumed:: memory: "+(mb4-com.bc.util.Util.usedMemory(mb4))+", time: "+(System.currentTimeMillis()-tb4)+", link: "+link);    
    return ret;
  }
}
