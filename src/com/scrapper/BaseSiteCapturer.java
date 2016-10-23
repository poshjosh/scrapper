package com.scrapper;

import com.bc.webdatex.URLParser;
import com.bc.task.AbstractStoppableTask;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.util.ParserException;
import com.bc.webdatex.nodedata.Dom;

public class BaseSiteCapturer extends AbstractStoppableTask<Integer> implements SiteCapturer, Serializable {
    
  private boolean login;
  private int successCount;
  private int scrappLimit;
  private CapturerContext context;
  private URLParser urlParser;
  private Scrapper scrapper;
  private PageDataConsumer dataConsumer;
  
  public BaseSiteCapturer() {}
  
  public BaseSiteCapturer(CapturerContext context) {
    this.login = true;
    this.context = context;
    XLogger.getInstance().log(Level.FINE, 
      "Created capturer for SiteConfig: {0}", 
      getClass(), context.getConfig().getName());
  }

  @Override
  public Integer doCall() throws IOException, ParserException {
      
      XLogger.getInstance().log(Level.FINE, "Running: {0}", getClass(), this);
      
      if (this.login) {
        login();
      }
      
      XLogger.getInstance().log(Level.FINER, "Crawler: {0}\nScrapper: {1}", getClass(), this.urlParser, this.scrapper);
      
      while ((this.urlParser.hasNext()) && (isWithinScrappLimit())) {
          
        if (isStopRequested()) {
          break;
        }
        
        Dom pageNodes = this.urlParser.next();
        
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
      
      return this.successCount;
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
      
      this.urlParser.setCookies(cookies);
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
  
  @Override
  public boolean isToResume() {
    return ((this.urlParser instanceof Resumable)) && (((Resumable)this.urlParser).isToResume()) && ((this.scrapper instanceof Resumable)) && (((Resumable)this.scrapper).isToResume());
  }

  @Override
  public boolean isResumable() {
    return ((this.urlParser instanceof Resumable)) && (((Resumable)this.urlParser).isResumable()) && ((this.scrapper instanceof Resumable)) && (((Resumable)this.scrapper).isResumable());
  }
  
  @Override
  public boolean isCompleted() {
    boolean isCompleted = super.isCompleted();
    boolean isCrawlerCompleted = (this.urlParser != null) && (this.urlParser.isCompleted());
    return (isCompleted) && (isCrawlerCompleted);
  }
  
  @Override
  public void stop() {
    super.stop();
    if (this.urlParser != null) {
      this.urlParser.stop();
    }
  }
  
  @Override
  public boolean isStopped() {
    return (super.isStopped()) && (this.urlParser != null) && (this.urlParser.isStopped());
  }
  
  @Override
  public boolean isLogin() {
    return this.login;
  }
  
  @Override
  public void setLogin(boolean login){
    this.login = login;
  }
  
  @Override
  public URLParser getUrlParser() {
    return this.urlParser;
  }
  
  @Override
  public void setUrlParser(URLParser urlParser){
    this.urlParser = urlParser;
  }
  
  @Override
  public Scrapper getScrapper() {
    return this.scrapper;
  }
  
  @Override
  public void setScrapper(Scrapper scrapper) {
    this.scrapper = scrapper;
  }
  
  @Override
  public CapturerContext getContext() {
    return this.context;
  }
  
  @Override
  public void setContext(CapturerContext context) {
    this.context = context;
  }
  
  public int getScrappLimit() {
    return this.scrappLimit;
  }
  
  public void setScrappLimit(int scrappLimit) {
    this.scrappLimit = scrappLimit;
  }
  
  @Override
  public PageDataConsumer getDataConsumer() {
    return this.dataConsumer;
  }
  
  @Override
  public void setDataConsumer(PageDataConsumer dataConsumer) {
    this.dataConsumer = dataConsumer;
  }
  
  public long getBatchInterval() {
    return this.urlParser.getBatchInterval();
  }
  
  public void setBatchInterval(long batchInterval) {
    this.urlParser.setBatchInterval(batchInterval);
  }
  
  public int getBatchSize() {
    return this.urlParser.getBatchSize();
  }
  
  public void setBatchSize(int batchSize) {
    this.urlParser.setBatchSize(batchSize);
  }
  
  @Override
  public String getTaskName() {
    return getClass().getName() + "(Site: " + getName() + ")";
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    print(builder);
    return builder.toString();
  }
  
  @Override
  public void print(StringBuilder builder) {
    builder.append(getTaskName());
    if (isStarted()) {
      builder.append(". Started:").append(this.getStartTime());
      if (isStopped()) {
        builder.append(". Stopped");
      }
      if (this.urlParser != null) {
        builder.append(". Crawl:: attempted:").append(this.urlParser.getAttempted() == null ? 0 : this.urlParser.getAttempted().size());
        builder.append(" failed:").append(this.urlParser.getFailed() == null ? null : Integer.valueOf(this.urlParser.getFailed().size()));
        builder.append(" left:").append(this.urlParser.getPageLinks() == null ? null : Integer.valueOf(this.urlParser.getPageLinks().size()));
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
