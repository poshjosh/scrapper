package com.scrapper;

import com.scrapper.config.Config;
import com.bc.util.XLogger;
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

/**
 * @(#)BaseSiteCapturer.java   12-Mar-2014 21:44:02
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class BaseSiteCapturer implements SiteCapturer, Serializable {
    
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
    
    public BaseSiteCapturer() { }
    
    public BaseSiteCapturer(final CapturerContext context) { 
        
        this.login = true;
        this.context = context;
        
XLogger.getInstance().log(Level.FINE, "Created capturer for SiteConfig: {0}", 
        this.getClass(), context.getConfig().getName());        
    }
    
    protected void handleException(Exception e) { }
    
    @Override
    public void run() {

        try{

XLogger.getInstance().log(Level.FINE, "Running: {0}", this.getClass(), this);

            this.started = true;
            this.stopInitiated = false;
            this.stopped = false;

            this.startTime = new Date();

            if(login) {
                try{
                    this.login();
                }catch(IOException e) {
                    XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
                    this.handleException(e);
                    return;
                }
            }

XLogger.getInstance().log(Level.FINER, 
"Crawler: {0}\nScrapper: {1}", this.getClass(), crawler, scrapper);
            
            while(crawler.hasNext() && this.isWithinScrappLimit()) {

                if(this.isStopInitiated()) {
                    break;
                }

                PageNodes pageNodes = crawler.next();

XLogger.getInstance().log(Level.FINER, "PageNodes: {0}", this.getClass(), pageNodes);                
//System.out.println(pageNodes);                

                if(pageNodes == null) {
                    continue;
                }

                Map extractedData = scrapper.extractData(pageNodes);
                
                if(extractedData != null) {
                  
XLogger.getInstance().log(Level.FINER, "Extracted: {0} from: {1}", 
this.getClass(), extractedData.keySet(), pageNodes.getFormattedURL());                    
//System.out.println("Extracted "+extractedData.keySet()+" from: "+pageNodes.getFormattedURL());                

                    if(dataConsumer == null) {

XLogger.getInstance().log(Level.INFO, "{0} not specified. URL: {1}\nExtracted columns: {2}", 
this.getClass(), PageDataConsumer.class.getName(), pageNodes.getFormattedURL(), extractedData.keySet());                    

XLogger.getInstance().log(Level.FINER, "Extract:\n{0}", this.getClass(), extractedData);                    

                    }else{
                    
                        if(dataConsumer.consume(pageNodes, extractedData)) {

                            ++successCount;
                        }
                    }
                }
            }

        }catch(ParserException | RuntimeException e) {
            
            XLogger.getInstance().log(Level.WARNING, this+"\nProcess terminated prematurely", this.getClass(), e);
            
            this.handleException(e);
         
        }finally{

            try{
                crawler.completePendingActions();
            }finally{
                
                this.stopped = true;
            }
        }
    }
    
    public boolean isWithinScrappLimit() {
        boolean within = scrappLimit <= 0 || scrapper.getScrappCount() < scrappLimit;
XLogger.getInstance().log(Level.FINER, "Within scrapp limit: {0}", this.getClass(), within);        
        return within;
    }
    
    @Override
    public void login() throws MalformedURLException, IOException {

        String loginUrl = this.context.getConfig().getString(Config.Site.url, Config.Site.login);
        
XLogger.getInstance().log(Level.FINER, "Login URL: {0}", this.getClass(), loginUrl);

        Map loginCredentials = this.context.getConfig().getMap(Config.Login.loginCredentials);
        
        if(loginUrl != null && loginCredentials != null && !loginCredentials.isEmpty()) {

            HashMap<String, String> outputParams = new HashMap(loginCredentials);
            
            List<String> cookies = new LoginManager(2, 5000).login(loginUrl, outputParams);
            
XLogger.getInstance().log(Level.FINER, "Login cookies: {0}", this.getClass(), cookies);

            if(cookies == null) {
                throw new NullPointerException("Login cookies == null, for url: "+loginUrl);
            }

            crawler.setCookies(cookies);
        }
    }
    
    public String getName() {
        return this.getContext().getConfig().getName();
    }

    public int getSuccessCount() {
        return successCount;
    }
    
    public int getScrappCount() {
        return scrapper == null ? -1 : scrapper.getScrappCount();
    }
    
    @Override
    public boolean isRunning() {
        return this.isStarted() && !this.isStopped();
    }

    /**
     * @see com.scrapper.Resumable#isResume() 
     */
    @Override
    public boolean isResume() {
        return crawler instanceof Resumable &&
                ((Resumable)this.crawler).isResume() &&
                scrapper instanceof Resumable &&
                ((Resumable)this.scrapper).isResume();
    }
    
    /**
     * @see com.scrapper.Resumable#isResumable() 
     */
    @Override
    public boolean isResumable() {
        return crawler instanceof Resumable &&
                ((Resumable)this.crawler).isResumable() &&
                scrapper instanceof Resumable &&
                ((Resumable)this.scrapper).isResumable();
    }

    @Override
    public boolean isCompleted() {
        boolean isCompleted = this.started && this.stopped && !this.stopInitiated;
        boolean isCrawlerCompleted = this.crawler != null && this.crawler.isCompleted();
        return isCompleted && isCrawlerCompleted;
    }

    @Override
    public void stop() {
XLogger.getInstance().log(Level.FINE, "Stop Initiated: {0}", this.getClass(), this);            
        this.stopInitiated = true;
        if(this.crawler != null) {
            this.crawler.stop();
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isStopped() {
        return stopped && (crawler != null && crawler.isStopped());
    }

    @Override
    public boolean isStopInitiated() {
        return this.stopInitiated;
    }

    @Override
    public boolean isLogin() {
        return login;
    }

    @Override
    public void setLogin(boolean login) {
        this.login = login;
    }

    @Override
    public URLParser getCrawler() {
        return crawler;
    }

    @Override
    public void setCrawler(URLParser crawler) {
        this.crawler = crawler;
    }

    @Override
    public Scrapper getScrapper() {
        return scrapper;
    }

    @Override
    public void setScrapper(Scrapper scrapper) {
        this.scrapper = scrapper;
    }

    @Override
    public CapturerContext getContext() {
        return context;
    }

    @Override
    public void setContext(CapturerContext context) {
        this.context = context;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getScrappLimit() {
        return scrappLimit;
    }

    public void setScrappLimit(int scrappLimit) {
        this.scrappLimit = scrappLimit;
    }

    @Override
    public PageDataConsumer getDataConsumer() {
        return dataConsumer;
    }

    @Override
    public void setDataConsumer(PageDataConsumer dataConsumer) {
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
    
    @Override
    public String getTaskName() {
        return this.getClass().getName()+"(Site: "+this.getName()+")";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        print(builder);
        return builder.toString();
    }
    
    public void print(StringBuilder builder) {
        builder.append(this.getTaskName());
        if(this.isStarted()) {
            builder.append(". Started:").append(this.startTime);
            if(this.isStopped()) {
                builder.append(". Stopped");
            }
            if(crawler != null) {
                builder.append(". Crawl:: attempted:").append(crawler.getAttempted()==null?0:crawler.getAttempted().size());
                builder.append(" failed:").append(crawler.getFailed()==null?null:crawler.getFailed().size());
                builder.append(" left:").append(crawler.getPageLinks()==null?null:crawler.getPageLinks().size());
            }
            if(scrapper != null) {
                // attempted may include URLs previously attempted and stored in
                // the database.
    //            int attempted = scrapper.getAttempted()==null?0:scrapper.getAttempted().size();
                int scrappCount = scrapper.getScrappCount();
                builder.append(". Scrapp:: success:").append(successCount);
                builder.append(" failed:").append(scrappCount-successCount);
            }
        }else{
            builder.append(". Started: false");
        }
    }
}

