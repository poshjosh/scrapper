package com.scrapper;

import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.util.ParserException;

public class BaseSiteCapturer
  implements SiteCapturer, Serializable
{
  private boolean login;
  private boolean started;
  private boolean stopInitiated;
  private boolean stopped;
  private int successCount;
  private int scrappLimit;
  private Date startTime;
  private CapturerContext context;
  private URLParser crawler;
  private Scrapper scrapper;
  private PageDataConsumer dataConsumer;
  
  public BaseSiteCapturer() {}
  
  public BaseSiteCapturer(CapturerContext context)
  {
    this.login = true;
    this.context = context;
    
    XLogger.getInstance().log(Level.FINE, "Created capturer for SiteConfig: {0}", getClass(), context.getConfig().getName());
  }
  

  protected void handleException(Exception e) {}
  

  public void run()
  {
    try
    {
      XLogger.getInstance().log(Level.FINE, "Running: {0}", getClass(), this);
      
      this.started = true;
      this.stopInitiated = false;
      this.stopped = false;
      
      this.startTime = new Date();
      
      if (this.login) {
        try {
          login();
        } catch (IOException e) {
          XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
          handleException(e); return;
        }
      }
      

      XLogger.getInstance().log(Level.FINER, "Crawler: {0}\nScrapper: {1}", getClass(), this.crawler, this.scrapper);
      

      while ((this.crawler.hasNext()) && (isWithinScrappLimit()))
      {
        if (isStopInitiated()) {
          break;
        }
        
        PageNodes pageNodes = this.crawler.next();
        
        XLogger.getInstance().log(Level.FINER, "PageNodes: {0}", getClass(), pageNodes);
        

        if (pageNodes != null)
        {


          Map extractedData = this.scrapper.extractData(pageNodes);
          
          if (extractedData != null)
          {
            XLogger.getInstance().log(Level.FINER, "Extracted: {0} from: {1}", getClass(), extractedData.keySet(), pageNodes.getFormattedURL());
            


            if (this.dataConsumer == null)
            {
              XLogger.getInstance().log(Level.INFO, "{0} not specified. URL: {1}\nExtracted columns: {2}", getClass(), PageDataConsumer.class.getName(), pageNodes.getFormattedURL(), extractedData.keySet());
              

              XLogger.getInstance().log(Level.FINER, "Extract:\n{0}", getClass(), extractedData);


            }
            else if (this.dataConsumer.consume(pageNodes, extractedData))
            {
              this.successCount += 1;
            }
          }
        }
      }
    }
    catch (ParserException|RuntimeException e)
    {
      XLogger.getInstance().log(Level.WARNING, this + "\nProcess terminated prematurely", getClass(), e);
      
      handleException(e);
    }
    finally
    {
      try {
        this.crawler.completePendingActions();
      }
      finally {
        this.stopped = true;
      }
    }
  }
  
  public boolean isWithinScrappLimit() {
    boolean within = (this.scrappLimit <= 0) || (this.scrapper.getScrappCount() < this.scrappLimit);
    XLogger.getInstance().log(Level.FINER, "Within scrapp limit: {0}", getClass(), Boolean.valueOf(within));
    return within;
  }
  
  @Override
  public void login()
    throws MalformedURLException, IOException
  {
    String loginUrl = this.context.getConfig().getString(new Object[] { Config.Site.url, Config.Site.login });
    
    XLogger.getInstance().log(Level.FINER, "Login URL: {0}", getClass(), loginUrl);
    
    Map loginCredentials = this.context.getConfig().getMap(new Object[] { Config.Login.loginCredentials });
    
    if ((loginUrl != null) && (loginCredentials != null) && (!loginCredentials.isEmpty()))
    {
      HashMap<String, String> outputParams = new HashMap(loginCredentials);
      
      List<String> cookies = new LoginManager(2, 5000L).login(loginUrl, outputParams);
      
      XLogger.getInstance().log(Level.FINER, "Login cookies: {0}", getClass(), cookies);
      
      if (cookies == null) {
        throw new NullPointerException("Login cookies == null, for url: " + loginUrl);
      }
      
      this.crawler.setCookies(cookies);
    }
  }
  
  public String getName() {
    return getContext().getConfig().getName();
  }
  
  public int getSuccessCount() {
    return this.successCount;
  }
  
  public int getScrappCount() {
    return this.scrapper == null ? -1 : this.scrapper.getScrappCount();
  }
  
  public boolean isRunning()
  {
    return (isStarted()) && (!isStopped());
  }
  



  public boolean isResume()
  {
    return ((this.crawler instanceof Resumable)) && (((Resumable)this.crawler).isResume()) && ((this.scrapper instanceof Resumable)) && (((Resumable)this.scrapper).isResume());
  }
  






  public boolean isResumable()
  {
    return ((this.crawler instanceof Resumable)) && (((Resumable)this.crawler).isResumable()) && ((this.scrapper instanceof Resumable)) && (((Resumable)this.scrapper).isResumable());
  }
  



  public boolean isCompleted()
  {
    boolean isCompleted = (this.started) && (this.stopped) && (!this.stopInitiated);
    boolean isCrawlerCompleted = (this.crawler != null) && (this.crawler.isCompleted());
    return (isCompleted) && (isCrawlerCompleted);
  }
  
  public void stop()
  {
    XLogger.getInstance().log(Level.FINE, "Stop Initiated: {0}", getClass(), this);
    this.stopInitiated = true;
    if (this.crawler != null) {
      this.crawler.stop();
    }
  }
  
  public boolean isStarted()
  {
    return this.started;
  }
  
  public boolean isStopped()
  {
    return (this.stopped) && (this.crawler != null) && (this.crawler.isStopped());
  }
  
  public boolean isStopInitiated()
  {
    return this.stopInitiated;
  }
  
  public boolean isLogin()
  {
    return this.login;
  }
  
  public void setLogin(boolean login)
  {
    this.login = login;
  }
  
  public URLParser getCrawler()
  {
    return this.crawler;
  }
  
  public void setCrawler(URLParser crawler)
  {
    this.crawler = crawler;
  }
  
  public Scrapper getScrapper()
  {
    return this.scrapper;
  }
  
  public void setScrapper(Scrapper scrapper)
  {
    this.scrapper = scrapper;
  }
  
  public CapturerContext getContext()
  {
    return this.context;
  }
  
  public void setContext(CapturerContext context)
  {
    this.context = context;
  }
  
  public Date getStartTime()
  {
    return this.startTime;
  }
  
  public void setStartTime(Date startTime)
  {
    this.startTime = startTime;
  }
  
  public int getScrappLimit() {
    return this.scrappLimit;
  }
  
  public void setScrappLimit(int scrappLimit) {
    this.scrappLimit = scrappLimit;
  }
  
  public PageDataConsumer getDataConsumer()
  {
    return this.dataConsumer;
  }
  
  public void setDataConsumer(PageDataConsumer dataConsumer)
  {
    this.dataConsumer = dataConsumer;
  }
  
  public long getBatchInterval() {
    return this.crawler.getBatchInterval();
  }
  
  public void setBatchInterval(long batchInterval) {
    this.crawler.setBatchInterval(batchInterval);
  }
  
  public int getBatchSize() {
    return this.crawler.getBatchSize();
  }
  
  public void setBatchSize(int batchSize) {
    this.crawler.setBatchSize(batchSize);
  }
  
  public String getTaskName()
  {
    return getClass().getName() + "(Site: " + getName() + ")";
  }
  
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    print(builder);
    return builder.toString();
  }
  
  public void print(StringBuilder builder) {
    builder.append(getTaskName());
    if (isStarted()) {
      builder.append(". Started:").append(this.startTime);
      if (isStopped()) {
        builder.append(". Stopped");
      }
      if (this.crawler != null) {
        builder.append(". Crawl:: attempted:").append(this.crawler.getAttempted() == null ? 0 : this.crawler.getAttempted().size());
        builder.append(" failed:").append(this.crawler.getFailed() == null ? null : Integer.valueOf(this.crawler.getFailed().size()));
        builder.append(" left:").append(this.crawler.getPageLinks() == null ? null : Integer.valueOf(this.crawler.getPageLinks().size()));
      }
      if (this.scrapper != null)
      {


        int scrappCount = this.scrapper.getScrappCount();
        builder.append(". Scrapp:: success:").append(this.successCount);
        builder.append(" failed:").append(scrappCount - this.successCount);
      }
    } else {
      builder.append(". Started: false");
    }
  }
}
