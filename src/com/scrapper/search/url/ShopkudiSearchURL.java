package com.scrapper.search.url;

import com.scrapper.search.AbstractMappingsURLProducer;
import java.io.Serializable;
import java.util.Map;










public class ShopkudiSearchURL
  extends AbstractMappingsURLProducer
  implements Serializable
{
  public ShopkudiSearchURL()
  {
    setFormatSlashes(true);
  }
  
  protected String getDefaultUrlSub()
  {
    return null;
  }
  
  protected String getURLPart()
  {
    return null;
  }
  
  protected String getQueryBeforeSearchTerm()
  {
    return null;
  }
  
  protected String getQueryAfterSearchTerm()
  {
    return null;
  }
  
  protected void appendSearchTerm(Map parameters, String searchTerm, StringBuilder appendTo) {}
}
