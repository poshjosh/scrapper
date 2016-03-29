package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.HtmlContentFilter;
import com.scrapper.util.HtmlLinkFilter;
import com.scrapper.util.Util;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;

public class Crawler
  extends ResumableUrlParser
{
  private boolean captureQueryLinks;
  private boolean startCollectingLinks = true;
  

  private boolean stopCollectingLinks;
  

  private int crawled;
  

  private int crawlLimit;
  

  private String baseUrl;
  

  private String startUrl;
  

  private Filter<String> urlFilter;
  

  private Filter<String> htmlLinkFilter;
  

  private Filter<String> htmlContentFilter;
  

  private CapturerContext context;
  

  public Crawler(CapturerContext context)
  {
    super(context.getConfig().getName());
    XLogger.getInstance().log(Level.FINER, "Creating", getClass());
    
    setContext(context);
    
    setCaptureQueryLinks(true);
    PrototypicalNodeFactory factory = (PrototypicalNodeFactory)getParser().getNodeFactory();
    factory.registerTag(new LinkCollectingLinkTag());
    factory.registerTag(new LinkCollectingFrameTag());
    XLogger.getInstance().log(Level.FINER, "Done creating: {0}", getClass(), this);
  }
  
  public boolean isWithinCrawlLimit() {
    boolean withinLimit = isWithLimit(this.crawled, this.crawlLimit);
    XLogger.getInstance().log(Level.FINEST, "URLs: {0}, limit: {1}, within crawl limit: {2}", getClass(), Integer.valueOf(getPageLinks().size()), Integer.valueOf(this.crawlLimit), Boolean.valueOf(withinLimit));
    
    return withinLimit;
  }

  public boolean isToBeCrawled(String link)
  {
    XLogger.getInstance().log(Level.FINEST, "@isToBeCaptured. Link: {0}", getClass(), link);
    
    boolean toBeCrawled;
    if (link.equals(this.startUrl))
    {
      toBeCrawled = true;
    }
    else
    {
      toBeCrawled = super.isToBeCrawled(link);
      
      if (toBeCrawled)
      {

        if (this.baseUrl.trim().isEmpty()) {
          toBeCrawled = true;
        } else if (link.trim().isEmpty()) {
          toBeCrawled = false;

        }
        else
        {

          try
          {

            toBeCrawled = Util.toWWWFormat(link).startsWith(Util.toWWWFormat(this.baseUrl));
          } catch (MalformedURLException e) {
            toBeCrawled = false;
          }
        }
      }
      
      if (toBeCrawled) {
        toBeCrawled = !link.contains("#");
      }
      
      if (toBeCrawled) {
        toBeCrawled = (-1 == link.indexOf("?")) || (isCaptureQueryLinks());
      }
      
      if ((toBeCrawled) && (getUrlFilter() != null))
      {
        XLogger.getInstance().log(Level.FINER, "@isToBeCaptured. Filtering with: {0}", getClass(), getUrlFilter().getClass());
        

        toBeCrawled = getUrlFilter().accept(link);
        XLogger.getInstance().log(Level.FINE, "Accepted by URL Filter: {0}, Link: {1}", getClass(), Boolean.valueOf(toBeCrawled), link);
      }
    }
    

    Level level = toBeCrawled ? Level.FINE : Level.FINER;
    XLogger.getInstance().log(level, "To be captured: {0}, Link: {1}", getClass(), Boolean.valueOf(toBeCrawled), link);
    
    return toBeCrawled;
  }
  






  protected boolean isHtml(String link)
  {
    if (this.htmlLinkFilter == null) {
      this.htmlLinkFilter = new HtmlLinkFilter();
    }
    
    if (this.htmlLinkFilter.accept(link)) {
      return true;
    }
    


    if (this.htmlContentFilter == null) {
      this.htmlContentFilter = new HtmlContentFilter();
    }
    
    return this.htmlContentFilter.accept(link);
  }
  
  class LinkCollectingLinkTag extends LinkTag
  {
    LinkCollectingLinkTag() {}
    
    public void doSemanticAction() throws ParserException {
      if ((!Crawler.this.startCollectingLinks) || (Crawler.this.stopCollectingLinks)) {
        return;
      }
      

      String link = getLink();
      

      if ((Crawler.this.isWithinCrawlLimit()) && (Crawler.this.isToBeCrawled(link)))
      {

        synchronized (Crawler.this.pageLock)
        {
          if (!Crawler.this.getPageLinks().contains(link))
          {


            boolean html = Crawler.this.isHtml(link);
            
            if (html) {
              XLogger.getInstance().log(Level.FINER, "Adding: {0}", getClass(), link);
              Crawler.this.getPageLinks().add(link);
              
              ++crawled;
            }
          }
        }
      }
    }
  }
  
  class LinkCollectingFrameTag extends FrameTag {
    LinkCollectingFrameTag() {}
    
    public void doSemanticAction() throws ParserException {
      if ((!Crawler.this.startCollectingLinks) || (Crawler.this.stopCollectingLinks)) {
        return;
      }
      

      String link = getFrameLocation();
      

      if ((Crawler.this.isWithinCrawlLimit()) && (Crawler.this.isToBeCrawled(link)))
      {
        synchronized (Crawler.this.pageLock)
        {
          if (!Crawler.this.getPageLinks().contains(link))
          {

            boolean html = Crawler.this.isHtml(link);
            
            if (html)
            {
              Crawler.this.getPageLinks().add(link);
              
              ++crawled;
            }
          }
        }
      }
    }
  }
  
  public String getBaseUrl() {
    return this.baseUrl;
  }
  






  public void setBaseUrl(String baseUrl)
  {
    if (baseUrl == null) {
      throw new NullPointerException();
    }
    this.baseUrl = baseUrl;
    if (this.startUrl == null) {
      this.startUrl = baseUrl;
    }
  }
  
  public String getStartUrl() {
    return this.startUrl;
  }
  



  public void setStartUrl(String startUrl)
  {
    if (startUrl == null) {
      throw new NullPointerException();
    }
    if (!getPageLinks().contains(startUrl)) {
      getPageLinks().add(0, startUrl);
    }
    this.startUrl = startUrl;
    if (this.baseUrl == null) {
      this.baseUrl = com.bc.util.Util.getBaseURL(startUrl);
    }
  }
  
  public CapturerContext getContext() {
    return this.context;
  }
  
  public void setContext(CapturerContext context) {
    XLogger.getInstance().log(Level.FINER, "Updating context for: {0}", getClass(), this);
    
    this.context = context;
    
    JsonConfig config = context.getConfig();
    
    setSitename(config.getName());
    
    String url_start = config.getString(new Object[] { "url", Config.Site.start });
    if ((url_start == null) || (url_start.isEmpty())) {
      this.baseUrl = config.getString(new Object[] { "url", "value" });
      if ((this.baseUrl == null) || (this.baseUrl.isEmpty())) {
        throw new NullPointerException("Both url.start and url.value cannot be null in config: " + config.getName());
      }
      setStartUrl(this.baseUrl);
    } else {
      setStartUrl(url_start);
    }
    
    setUrlFilter(context.getCaptureUrlFilter());
    
    setFormatter(context.getUrlFormatter());
    
    int limit = config.getInt(new Object[] { Config.Extractor.parseLimit }).intValue();
    
    setParseLimit(limit);
    XLogger.getInstance().log(Level.FINER, "Updated context for: {0}", getClass(), this);
  }
  
  public int getCrawled() {
    return this.crawled;
  }
  

  public boolean isCaptureQueryLinks()
  {
    return this.captureQueryLinks;
  }
  
  public void setCaptureQueryLinks(boolean captureQueryLinks) {
    this.captureQueryLinks = captureQueryLinks;
  }
  
  public Filter<String> getUrlFilter() {
    return this.urlFilter;
  }
  
  public void setUrlFilter(Filter<String> filter) {
    this.urlFilter = filter;
  }
  
  public boolean isStartCollectingLinks() {
    return this.startCollectingLinks;
  }
  
  public void setStartCollectingLinks(boolean startCollectingLinks) {
    this.startCollectingLinks = startCollectingLinks;
  }
  
  public boolean isStopCollectingLinks() {
    return this.stopCollectingLinks;
  }
  
  public void setStopCollectingLinks(boolean stopCollectingLinks) {
    this.stopCollectingLinks = stopCollectingLinks;
  }
  
  public int getCrawlLimit() {
    return this.crawlLimit;
  }
  
  public void setCrawlLimit(int limit) {
    this.crawlLimit = limit;
  }
  
  public void print(StringBuilder builder)
  {
    super.print(builder);
    builder.append(", startCollectingLinks: ").append(this.startCollectingLinks);
    builder.append(", stopCollectingLinks: ").append(this.stopCollectingLinks);
    builder.append(", baseURL").append(this.baseUrl);
    builder.append(", startURL").append(this.startUrl);
  }
}
