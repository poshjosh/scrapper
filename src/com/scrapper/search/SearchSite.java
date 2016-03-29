package com.scrapper.search;

import com.bc.jpa.fk.Keywords;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.BaseSiteCapturer;
import com.scrapper.CapturerApp;
import com.scrapper.MultipleSourcesCrawler;
import com.scrapper.ResumableScrapper;
import com.scrapper.ResumeHandler;
import com.scrapper.Scrapper;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SearchSite
  extends BaseSiteCapturer
{
  private long lastRequestTime;
  private int lastRequestTimeTaken;
  private String productTable;
  private URLProducer urlProducer;
  private List<String> urls;
  private ResumeHandler parseResumeHandler;
  private ResumeHandler scrappResumeHandler;
  
  public SearchSite() {}
  
  public SearchSite(String sitename)
  {
    this(CapturerApp.getInstance().getConfigFactory(true).getContext(sitename));
  }
  
  public SearchSite(CapturerContext context)
  {
    super(context);
    
    if (!context.isUrlProducing())
    {
      throw new IllegalArgumentException("Url producer could not be created for: " + context.getConfig().getName());
    }
    
    this.urlProducer = createURLProducer(context);
  }
  
  protected void handleException(Exception e)
  {
    stop();
  }
  


  public void update(String tableName, Map searchParameters, String searchTerm)
    throws UnsupportedOperationException
  {
    XLogger.getInstance().log(Level.FINER, "Category: {0}, search text: {1}, parameters: {2}", getClass(), tableName, searchTerm, searchParameters);
    

    this.productTable = tableName;
    
    setLogin(false);
    
    CapturerContext context = getContext();
    





    boolean resume = false;
    


    boolean resumable = true;
    
    MultipleSourcesCrawler crawler = createCrawler(context, false, true);
    setCrawler(crawler);
    
    Scrapper scrapper = createScrapper(context, false, true, searchTerm);
    setScrapper(scrapper);
    
    doUpdate(tableName, searchParameters, searchTerm);
  }
  

  protected void preParse(String url) {}
  
  protected void postParse(PageNodes page) {}
  
  public void run()
  {
    if (!isHasSources()) {
      XLogger.getInstance().log(Level.WARNING, "RemoteSearch has no sources for: {0}", getClass(), getTaskName());
      return;
    }
    
    super.run();
  }
  
  public boolean isHasSources() {
    return (this.urls != null) && (!this.urls.isEmpty());
  }
  
  private void doUpdate(String tableName, Map parameters, String searchTerm)
  {
    Keywords keywords = getContext().getKeywords();
    XLogger.getInstance().log(Level.FINER, "Keywords: {0}", getClass(), keywords);
    Map<String, Integer> keys;
    if (keywords != null)
    {
      keywords.setTableName(tableName);
      
      keys = keywords.extractKeys(searchTerm, true);
      
      XLogger.getInstance().log(Level.FINER, "Keys: {0}", getClass(), keys);
      
      for (Map.Entry<String, Integer> entry : keys.entrySet())
      {
        boolean add = false;
        if (parameters != null) {
          if (!parameters.containsKey(entry.getKey())) {
            add = true;
          }
        } else {
          parameters = new HashMap(keys.size(), 1.0F);
          add = true;
        }
        
        if (add)
        {


          XLogger.getInstance().log(Level.FINER, "Adding: {0}={1}", getClass(), entry.getKey(), entry.getValue());
          

          parameters.put(entry.getKey(), entry.getValue());
        }
      }
    }
    XLogger.getInstance().log(Level.FINER, "Parameters: {0}", getClass(), parameters);
    
    this.urls = this.urlProducer.getSearchURLs(getContext(), tableName, parameters, searchTerm);
    
    if (XLogger.getInstance().isLoggable(Level.FINER, getClass())) {
      XLogger.getInstance().log(Level.FINER, "URLs: {0}\nSubs: {1}", getClass(), this.urls, (this.urlProducer instanceof HasUrlSubs) ? ((HasUrlSubs)this.urlProducer).getUrlSubs() : null);
    }
    

    getCrawler().getPageLinks().clear();
    
    if (!isHasSources())
    {
      throw new IllegalArgumentException("Search URLs could not be generated for: " + this);
    }
    
    ((MultipleSourcesCrawler)getCrawler()).setSources(this.urls);
  }
  



  protected MultipleSourcesCrawler createCrawler(CapturerContext context, final boolean resume, final boolean resumable)
  {
    final HasUrlSubs hasUrlSubs = (this.urlProducer instanceof HasUrlSubs) ? (HasUrlSubs)this.urlProducer : null;
    

    MultipleSourcesCrawler multiCrawler = new MultipleSourcesCrawler(context, this.urls)
    {
      int parsed;
      int numberOfSources = -1;
      

      protected void preParse(String url)
      {
        SearchSite.this.lastRequestTime = System.currentTimeMillis();
        

        SearchSite.this.lastRequestTimeTaken = -1;
        
        SearchSite.this.preParse(url);
        
        super.preParse(url);
      }
      

      protected void postParse(PageNodes page)
      {
        SearchSite.this.lastRequestTimeTaken = ((int)(System.currentTimeMillis() - SearchSite.this.lastRequestTime));
        
        SearchSite.this.postParse(page);
        
        if (this.numberOfSources == -1) {
          this.numberOfSources = getSources().size();
        }
        
        if (++this.parsed >= this.numberOfSources) {
          setStopCollectingLinks(true);
        }
        
        super.postParse(page);
      }
      


      protected void checkListGenerationMode(JsonConfig config) {}
      
      public boolean isToBeCrawled(String link)
      {
        boolean generated = SearchSite.this.urls.contains(link);
        boolean output;
        if (hasUrlSubs == null) {
          output = (generated) || (super.isToBeCrawled(link));
        } else {
          boolean foundSub = false;
          List<String> urlSubs = hasUrlSubs.getUrlSubs();
          for (String urlSub : urlSubs) {
            if (link.contains(urlSub)) {
              foundSub = true;
              break;
            }
          }
          output = (foundSub) || (generated) || (super.isToBeCrawled(link));
        }
        XLogger.getInstance().log(Level.FINER, "To be crawled: {0}, url: {1}", getClass(), Boolean.valueOf(output), link);
        return output;
      }
      
      public String getTaskName()
      {
        return SearchSite.class.getName() + "$" + MultipleSourcesCrawler.class.getName();
      }
      
      public boolean isResumable() {
        return resumable;
      }
      
      public boolean isResume() {
        return resume;
      }
    };
    
    if (this.parseResumeHandler != null) {
      multiCrawler.setResumeHandler(this.parseResumeHandler);
    }
    
    multiCrawler.setBatchInterval(0L);
    

    multiCrawler.setConnectionMonitor(null);
    
    return multiCrawler;
  }
  




  protected Scrapper createScrapper(CapturerContext context, final boolean resume, final boolean resumable, final String searchTerm)
  {
    Boolean bval = context.getConfig().getBoolean(new Object[] { Config.Extractor.hasExplicitLinks });
    final boolean hasExplicitLinks = bval == null ? false : bval.booleanValue();
    
    ResumableScrapper scrapper = new ResumableScrapper(context)
    {
      @Override
      protected boolean isToBeScrapped(String link) {
        boolean output;
        if (hasExplicitLinks)
        {
          boolean match = link.toLowerCase().contains(searchTerm.toLowerCase());
          
          output = (match) && (super.isToBeScrapped(link));
        }
        else
        {
          output = super.isToBeScrapped(link);
        }
        
        return output;
      }
      
      public boolean isResumable() {
        return resumable;
      }
      
      public boolean isResume() {
        return resume;
      }
    };
    
    if (this.scrappResumeHandler != null) {
      scrapper.setResumeHandler(this.scrappResumeHandler);
    }
    
    return scrapper;
  }
  

  public void setContext(CapturerContext context)
    throws UnsupportedOperationException
  {
    super.setContext(context);
    
    this.urlProducer = createURLProducer(context);
  }
  
  protected URLProducer createURLProducer(CapturerContext context)
    throws UnsupportedOperationException
  {
    if (!context.isUrlProducing())
    {
      throw new IllegalArgumentException("Url producer could not be created for: " + context.getConfig().getName());
    }
    
    URLProducer output = context.getUrlProducer();
    
    if (output == null) {
      StringBuilder msg = new StringBuilder("Failed to initialize: ");
      msg.append(URLProducer.class.getName());
      msg.append(". Search is not possible for site: ");
      msg.append(getName());
      throw new UnsupportedOperationException(msg.toString());
    }
    
    return output;
  }
  



  public long getLastRequestTime()
  {
    return this.lastRequestTime;
  }
  


  public int getLastRequestTimeTaken()
  {
    return this.lastRequestTimeTaken;
  }
  
  public String getProductTable() {
    return this.productTable;
  }
  
  public URLProducer getUrlProducer() {
    return this.urlProducer;
  }
  
  public void setUrlProducer(URLProducer urlProducer) {
    this.urlProducer = urlProducer;
  }
  
  public String getTaskName()
  {
    CapturerContext context = getContext();
    String name = context.getConfig().getName();
    return SearchSite.class.getName() + "(Searches site: " + name + ")";
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
