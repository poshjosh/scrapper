package com.scrapper;

import com.bc.webdatex.BaseCrawler;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import com.scrapper.url.ConfigURLList;
import com.scrapper.url.ConfigURLPartList;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DefaultSiteCapturer extends BaseSiteCapturer {
    
  private ResumeHandler parseResumeHandler;
  private ResumeHandler scrappResumeHandler;
  
  public DefaultSiteCapturer() {}
  
  public DefaultSiteCapturer(CapturerContext context) {
      
    super(context);
    
    init(context, null, false, true);
  }
  
  public DefaultSiteCapturer(CapturerContext context, List<String> urlList) {
      
    super(context);
    
    init(context, urlList, false, true);
  }
  
  public DefaultSiteCapturer(
          CapturerContext context, List<String> urlList, 
          boolean resume, boolean resumable) {
      
    super(context);
    
    init(context, urlList, resume, resumable);
  }
  
  public DefaultSiteCapturer(String sitename) {
    this(sitename, null, -1, -1, false, true);
  }
  
  public DefaultSiteCapturer(String sitename, List<String> urls) {
      
    this(sitename, urls, -1, -1, false, true);
  }
  
  public DefaultSiteCapturer(
      String sitename, List<String> urls, boolean resume, boolean resumable) {
      
    this(sitename, urls, -1, -1, resume, resumable);
  }
  

  public DefaultSiteCapturer(
      String sitename, List<String> urls, int batchSize, int batchInterval, 
          boolean resume, boolean resumable) {
      
    ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();
    
    JsonConfig config = factory.getConfig(sitename);
    
    if (config == null) {
      throw new NullPointerException("Failed to load site config for: " + sitename);
    }
    
    init(factory.getContext(config), urls, resume, resumable);
    
    if (urls == null) {
      setScrappLimit(config.getInt(new Object[] { Config.Extractor.scrappLimit.name() }).intValue());
    }

    if (batchSize > 0) {
      setBatchSize(batchSize);
    }
    
    if (batchInterval > 0) {
      setBatchInterval(batchInterval);
    }
  }
  
  private void init(
      CapturerContext context, List<String> urlList, 
      boolean resume, boolean resumable) {
      
    setLogin(true);
    setContext(context);
    
    BaseCrawler urlParser = createCrawler(context, urlList, resume, resumable);
    setUrlParser(urlParser);
    
    Scrapper scrapper = createScrapper(context, urlList, resume, resumable);
    setScrapper(scrapper);
    
    PageDataConsumer dataConsumer = createDataConsumer(context, urlList);
    setDataConsumer(dataConsumer);
    
    XLogger.getInstance().log(Level.FINE, "Created:: {0}", getClass(), this);
  }
  
  protected BaseCrawler createCrawler(
          CapturerContext context, List<String> urlList, 
          final boolean resume, final boolean resumable) {
    ResumableCrawler urlParser;
    
    if (urlList != null) {
      urlParser = new ResumableCrawler(urlList, resumable, resume);
    }
    else {
      JsonConfig config = context.getConfig();
      
      ConfigURLList urllist = new ConfigURLList();
      
      urllist.update(config, "counter");
      
      if (!urllist.isEmpty()) {
          
        ConfigURLPartList serialPart = ConfigURLPartList.getSerialPart(config, "counter");
        if (serialPart == null) {
          urlParser = new MultipleSourcesCrawler(context, urllist, resumable, resume);
        } else {
          urlParser = new DirectSourcesParser(context, urllist);
        }
      } else {
        urlParser = new WebCrawler(context, resumable, resume);
      }
    }
    
    if (this.parseResumeHandler != null) {
      urlParser.setResumeHandler(this.parseResumeHandler);
    }
    
    return urlParser;
  }
  
  protected Scrapper createScrapper(
          CapturerContext context, List<String> urlList, 
          final boolean resume, final boolean toResume){
      
    ResumableScrapper scrapper = new ResumableScrapper(context, toResume, resume);
    
    if (this.scrappResumeHandler != null) {
      scrapper.setResumeHandler(this.scrappResumeHandler);
    }
    return scrapper;
  }
  
  protected PageDataConsumer createDataConsumer(CapturerContext context, List<String> urlList) {
    if (hasUploaderSettings(context)) {
      PageDataConsumer uploader = new ScrappUploader(context);
      return uploader;
    }
    return null;
  }
  
  private boolean hasUploaderSettings(CapturerContext context) {
      
    URL url;
    try {
      String urlStr = com.scrapper.CapturerApp.getInstance().getProperty("insertUrl");
      if ((urlStr == null) || (urlStr.isEmpty())) {
        return false;
      }
      url = new URL(com.scrapper.CapturerApp.getInstance().getProperty("insertUrl"));
    } catch (MalformedURLException e) { 
      return false;
    }
    
    JsonConfig config = context.getConfig();
    
    Map m = config.getMap(new Object[] { "uploadParameters" });
    
    return (m != null) && (!m.isEmpty());
  }
  
  public ResumeHandler getParseResumeHandler() {
    return this.parseResumeHandler;
  }
  
  public void setParseResumeHandler(ResumeHandler parseResumeHandler) {
    this.parseResumeHandler = parseResumeHandler;
  }
  
  public ResumeHandler getScrappResumeHandler() {
    return this.scrappResumeHandler;
  }
  
  public void setScrappResumeHandler(ResumeHandler scrappResumeHandler) {
    this.scrappResumeHandler = scrappResumeHandler;
  }
}
