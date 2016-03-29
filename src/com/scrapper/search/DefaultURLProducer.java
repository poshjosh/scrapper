package com.scrapper.search;

import com.bc.json.config.JsonConfig;
import java.io.Serializable;











public class DefaultURLProducer
  extends AbstractURLProducer
  implements Serializable
{
  private String path;
  private String queryBeforeSearchTerm;
  private String queryAfterSearchTerm;
  
  public DefaultURLProducer() {}
  
  public DefaultURLProducer(JsonConfig config)
  {
    if (isUseMappings(config)) {
      throw new UnsupportedOperationException("Rather create an instance of: " + MappingsURLProducer.class.getName());
    }
    init(config);
  }
  
  public static boolean isUseMappings(JsonConfig config) {
    Boolean b = config.getBoolean(new Object[] { "url", "search", "useMappings" });
    return (b != null) && (b.booleanValue());
  }
  
  public static boolean accept(JsonConfig config) {
    return config.getObject(new Object[] { "url", "search" }) != null;
  }
  
  protected void init(JsonConfig config) {
    if (!accept(config)) {
      throw new UnsupportedOperationException("Required property not found: url.search");
    }
    this.path = config.getString(new Object[] { "url", "search", "path" });
    this.queryBeforeSearchTerm = config.getString(new Object[] { "url", "search", "queryBeforeSearchTerm" });
    this.queryAfterSearchTerm = config.getString(new Object[] { "url", "search", "queryAfterSearchTerm" });
  }
  
  protected String getURLPart()
  {
    return this.path;
  }
  
  protected String getQueryBeforeSearchTerm()
  {
    return this.queryBeforeSearchTerm;
  }
  
  protected String getQueryAfterSearchTerm()
  {
    return this.queryAfterSearchTerm;
  }
}
