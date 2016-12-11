package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;

public abstract class ContextDataConsumer extends AbstractPageDataConsumer {
    
  private CapturerContext context;
  
  public ContextDataConsumer(String sitename){
    this(CapturerApp.getInstance().getConfigFactory().getContext(sitename));
  }
  
  public ContextDataConsumer(CapturerContext context) {
      
    this.context = context;
    
    JsonConfig config = context.getConfig();
    
    int i = config.getInt(new Object[] { Config.Extractor.minDataToAccept }).intValue();
    setMinimumParameters(i);
    
    String s = context.getConfig().getList(new Object[] { Config.Site.tables }).get(0).toString();
    setDefaultTableName(s);
    
    setFormatter(context.getFormatter());
  }
  
  public CapturerContext getContext() {
    return this.context;
  }
}
