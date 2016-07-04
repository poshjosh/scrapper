package com.scrapper.scheduling;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.CapturerApp;
import com.scrapper.Crawler;
import com.scrapper.DefaultSiteCapturer;
import com.scrapper.Filter;
import com.scrapper.SiteCapturer;
import com.scrapper.URLParser;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class CaptureSitesManager implements Filter<JsonConfig>, Serializable {
    
  private int batchSize;
  private int batchInterval;
  private int crawlLimit;
  private int parseLimit;
  private int scrappLimit;
  
  public CaptureSitesManager() {
    this.batchSize = 100;
    this.batchInterval = 60000;
    this.crawlLimit = 5000;
    this.parseLimit = 500;
    this.scrappLimit = 50;
  }
  
  public ScrapperConfigFactory getConfigFactory() {
    return CapturerApp.getInstance().getConfigFactory();
  }
  
  public Set<String> getSitenames() {
      
    ScrapperConfigFactory factory = getConfigFactory();
    
    Set<String> accepted = new HashSet();
    
    Set<String> sitenames = factory.getSitenames();
    
    for (String sitename : sitenames)
    {
      JsonConfig config = factory.getConfig(sitename);
      
      if (accept(config)) {
        accepted.add(sitename);
      }
    }
    
    XLogger.getInstance().log(Level.FINE, "Sitenames: {0}", getClass(), accepted);
    
    return accepted;
  }
  
  public boolean accept(JsonConfig config)
  {
    Boolean disabled = config.getBoolean(new Object[] { Config.Extractor.disabled });
    
    boolean accept = (disabled == null) || (!disabled.booleanValue());
    
    XLogger.getInstance().log(Level.FINER, "Config: {0}, accepted: {1}", getClass(), config.getName(), Boolean.valueOf(accept));
    

    return accept;
  }
  
  public SiteCapturer newTask(String sitename)
  {
    JsonConfig config = getConfigFactory().getConfig(sitename);
    
    if (config == null) {
      throw new NullPointerException("Failed to load site config for: " + sitename);
    }
    
    CapturerContext context = getConfigFactory().getContext(config);
    
    DefaultSiteCapturer capturer = new DefaultSiteCapturer(context, null, true, true);
    
    URLParser crawler = capturer.getCrawler();
    
    if ((crawler instanceof Crawler)) {
      ((Crawler)crawler).setCrawlLimit(this.crawlLimit);
    }
    crawler.setParseLimit(this.parseLimit);
    capturer.setScrappLimit(this.scrappLimit);
    
    if (this.batchSize > 0) {
      crawler.setBatchSize(this.batchSize);
    }
    
    if (this.batchInterval > 0) {
      crawler.setBatchInterval(this.batchInterval);
    }
    
    return capturer;
  }
  
  public final Object getTaskId(String sitename) {
    return sitename;
  }
  
  public int getBatchInterval() {
    return this.batchInterval;
  }
  
  public void setBatchInterval(int batchInterval) {
    this.batchInterval = batchInterval;
  }
  
  public int getBatchSize() {
    return this.batchSize;
  }
  
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }
  
  public int getCrawlLimit() {
    return this.crawlLimit;
  }
  
  public void setCrawlLimit(int crawlLimit) {
    this.crawlLimit = crawlLimit;
  }
  
  public int getParseLimit() {
    return this.parseLimit;
  }
  
  public void setParseLimit(int parseLimit) {
    this.parseLimit = parseLimit;
  }
  
  public int getScrappLimit() {
    return this.scrappLimit;
  }
  
  public void setScrappLimit(int scrappLimit) {
    this.scrappLimit = scrappLimit;
  }
}
