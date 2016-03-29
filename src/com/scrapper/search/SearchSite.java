package com.scrapper.search;

import com.bc.util.XLogger;
import com.bc.jpa.fk.Keywords;
import com.scrapper.BaseSiteCapturer;
import com.scrapper.CapturerApp;
import com.scrapper.MultipleSourcesCrawler;
import com.scrapper.ResumableScrapper;
import com.scrapper.ResumeHandler;
import com.scrapper.Scrapper;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @(#)SearchSite.java   01-Mar-2014 10:49:01
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
public class SearchSite extends BaseSiteCapturer {
    
    /**
     * The timestamp for the last instance a request was sent to a URL
     */
    private long lastRequestTime;
    
    /**
     * The time it took for the last request to complete
     */
    private int lastRequestTimeTaken;
    
    /**
     * This value is updated for each call to 
     * {@link #update(java.lang.String, java.util.Map, java.lang.String, com.scrapper.PageDataConsumer)}
     */
    private String productTable;
    
    private URLProducer urlProducer;

    private List<String> urls;
    
    private ResumeHandler parseResumeHandler;
    
    private ResumeHandler scrappResumeHandler;
    
    public SearchSite() { }

    public SearchSite(String sitename) { 

        // Note we use the search instance of ScrapperConfigFactory
        //
        this(CapturerApp.getInstance().getConfigFactory(true).getContext(sitename));
    }

    public SearchSite(CapturerContext context) { 
        
        super(context);
        
        if(!context.isUrlProducing()) {
//XLogger.getInstance().log(Level.WARNING, "Url producer could not be created for: {0}", this.getClass(), sitename);                
            throw new IllegalArgumentException("Url producer could not be created for: "+context.getConfig().getName());
        }

        this.urlProducer = SearchSite.this.createURLProducer(context);
    }
    
    @Override
    protected void handleException(Exception e) { 
        this.stop();
    }
    
    public void update(String tableName, 
            Map searchParameters, 
            final String searchTerm) 
            throws UnsupportedOperationException {
        
XLogger.getInstance().log(Level.FINER, "Category: {0}, search text: {1}, parameters: {2}",
        this.getClass(), tableName, searchTerm, searchParameters);
        
        this.productTable = tableName;
        
        this.setLogin(false);
        
        final CapturerContext context = this.getContext();
        
        // This updates these values for both the crawler and scrapper
        //
        // If set to true previously processed (and saved) urls will be loaded.
        // before new urls are considered
        //
        final boolean resume = false;
        
        // If set to true. processed urls will be saved
        //
        final boolean resumable = true;
        
        MultipleSourcesCrawler crawler = this.createCrawler(context, resume, resumable);
        this.setCrawler(crawler);

        Scrapper scrapper = this.createScrapper(context, resume, resumable, searchTerm);
        this.setScrapper(scrapper);

        this.doUpdate(tableName, searchParameters, searchTerm);
    }

    protected void preParse(String url) { }
    
    protected void postParse(PageNodes page) { }

    @Override
    public void run() {
        
        if(!this.isHasSources()) {
XLogger.getInstance().log(Level.WARNING, "RemoteSearch has no sources for: {0}", this.getClass(), this.getTaskName());
            return;
        }
        
        super.run();
    }

    public boolean isHasSources() {
        return urls != null && !urls.isEmpty();
    }
    
    private void doUpdate(String tableName, Map parameters, String searchTerm) {

        Keywords keywords = this.getContext().getKeywords();
XLogger.getInstance().log(Level.FINER, "Keywords: {0}", this.getClass(), keywords);                
        
        if(keywords != null) {
            
            keywords.setTableName(tableName);
            
            Map<String, Integer> keys = keywords.extractKeys(searchTerm, true);

XLogger.getInstance().log(Level.FINER, "Keys: {0}", this.getClass(), keys);                
            
            for(Map.Entry<String, Integer> entry:keys.entrySet()) {

                boolean add = false;
                if(parameters != null) {
                    if(!parameters.containsKey(entry.getKey())) {
                        add = true;
                    }
                }else{
                    parameters = new HashMap(keys.size(), 1.0f);
                    add = true;
                }
                
                if(!add) {
                    continue;
                }
                
XLogger.getInstance().log(Level.FINER, "Adding: {0}={1}", 
        this.getClass(), entry.getKey(), entry.getValue());                

                parameters.put(entry.getKey(), entry.getValue());
            }
        }
        
XLogger.getInstance().log(Level.FINER, "Parameters: {0}", this.getClass(), parameters);

        this.urls = this.urlProducer.getSearchURLs(this.getContext(), tableName, parameters, searchTerm);

if(XLogger.getInstance().isLoggable(Level.FINER, this.getClass()))        
XLogger.getInstance().log(Level.FINER, "URLs: {0}\nSubs: {1}", this.getClass(), urls,
urlProducer instanceof HasUrlSubs ? ((HasUrlSubs)urlProducer).getUrlSubs() : null);        
        
        
        this.getCrawler().getPageLinks().clear();
        
        if(!this.isHasSources()) {
//XLogger.getInstance().log(Level.WARNING, "Search URLs could not be generated for: {0}", this.getClass(), sitename);                
            throw new IllegalArgumentException("Search URLs could not be generated for: "+this);
        }

        ((MultipleSourcesCrawler)this.getCrawler()).setSources(urls);
    }
    
    protected MultipleSourcesCrawler createCrawler(
            CapturerContext context,
            final boolean resume,
            final boolean resumable) {
        
        final HasUrlSubs hasUrlSubs = urlProducer instanceof HasUrlSubs ?
                (HasUrlSubs)urlProducer : null;
        
        MultipleSourcesCrawler multiCrawler = new MultipleSourcesCrawler(context, urls){
            
            int parsed;
            int numberOfSources = -1;

            @Override
            protected void preParse(String url) {
                
                SearchSite.this.lastRequestTime = System.currentTimeMillis();
                // Indicates the request has not completed
                // This value is used to determine if the request completed
                SearchSite.this.lastRequestTimeTaken = -1;
                
                SearchSite.this.preParse(url);
                
                super.preParse(url);
            }
            
            @Override
            protected void postParse(PageNodes page) {
                
                SearchSite.this.lastRequestTimeTaken = (int)(System.currentTimeMillis()-SearchSite.this.lastRequestTime);
                
                SearchSite.this.postParse(page);
                
                if(numberOfSources == -1){
                    numberOfSources = this.getSources().size();
                }
                
                if(++parsed >= numberOfSources) {
                    this.setStopCollectingLinks(true);
                }
                
                super.postParse(page);
            }
            
            @Override
            protected void checkListGenerationMode(JsonConfig config) { }
            
            @Override
            public boolean isToBeCrawled(String link) {

                boolean output;
                
                boolean generated = urls.contains(link);
                
                if(hasUrlSubs == null) {
                    output = generated || super.isToBeCrawled(link);
                }else{
                    boolean foundSub = false;
                    // Get url subs will return correctly at this point
                    List<String> urlSubs = hasUrlSubs.getUrlSubs();
                    for(String urlSub:urlSubs) {
                        if(link.contains(urlSub)) {
                            foundSub = true;
                            break;
                        }
                    }
                    output = foundSub || (generated || super.isToBeCrawled(link));
                }
XLogger.getInstance().log(Level.FINER, "To be crawled: {0}, url: {1}", this.getClass(), output, link);                
                return output;
            }

            @Override
            public String getTaskName() {
                return SearchSite.class.getName()+"$"+MultipleSourcesCrawler.class.getName();
            }
            @Override
            public boolean isResumable() {
                return resumable;
            }
            @Override
            public boolean isResume() {
                return resume;
            }
        };

        if(this.parseResumeHandler != null) {
            multiCrawler.setResumeHandler(parseResumeHandler);
        }
        
        multiCrawler.setBatchInterval(0);
        
        // No need to slow us down with this
        multiCrawler.setConnectionMonitor(null);
        
        return multiCrawler;
    }
    
    protected Scrapper createScrapper(
            CapturerContext context, 
            final boolean resume,
            final boolean resumable,
            final String searchTerm) {
        
        Boolean bval = context.getConfig().getBoolean(Config.Extractor.hasExplicitLinks);
        final boolean hasExplicitLinks = bval == null ? false : bval.booleanValue();
        
        ResumableScrapper scrapper = new ResumableScrapper(context){
            @Override
            protected boolean isToBeScrapped(String link) {
                
                boolean output;
                if(hasExplicitLinks) {
                    
                    boolean match = link.toLowerCase().contains(searchTerm.toLowerCase());
                    
                    output = match && super.isToBeScrapped(link);
                    
                }else{
                    
                    output = super.isToBeScrapped(link);
                }
                
                return output;
            }
            @Override
            public boolean isResumable() {
                return resumable;
            }
            @Override
            public boolean isResume() {
                return resume;
            }
        };
        
        if(scrappResumeHandler != null) {
            scrapper.setResumeHandler(scrappResumeHandler);
        }
        
        return scrapper;
    }
    
    @Override
    public void setContext(CapturerContext context) 
            throws UnsupportedOperationException {
        
        super.setContext(context);

        this.urlProducer = this.createURLProducer(context);
    }    
    
    protected URLProducer createURLProducer(CapturerContext context) 
            throws UnsupportedOperationException {
        
        if(!context.isUrlProducing()) {
//XLogger.getInstance().log(Level.WARNING, "Url producer could not be created for: {0}", this.getClass(), sitename);                
            throw new IllegalArgumentException("Url producer could not be created for: "+context.getConfig().getName());
        }
        
        URLProducer output = context.getUrlProducer();
        
        if(output == null) {
            StringBuilder msg = new StringBuilder("Failed to initialize: ");
            msg.append(URLProducer.class.getName());
            msg.append(". Search is not possible for site: ");
            msg.append(this.getName());
            throw new UnsupportedOperationException(msg.toString());
        }
        
        return output;
    }

    /**
     * @return long. The timestamp for the last instance a request was sent 
     * to a URL
     */
    public long getLastRequestTime() {
        return lastRequestTime;
    }

    /**
     * @return The time it took for the last request to complete
     */
    public int getLastRequestTimeTaken() {
        return lastRequestTimeTaken;
    }

    public String getProductTable() {
        return productTable;
    }

    public URLProducer getUrlProducer() {
        return urlProducer;
    }

    public void setUrlProducer(URLProducer urlProducer) {
        this.urlProducer = urlProducer;
    }
    
    @Override
    public String getTaskName() {
        CapturerContext context = this.getContext();
        String name = context.getConfig().getName();
        return SearchSite.class.getName()+"(Searches site: "+(name)+")";
    }

    public ResumeHandler getParseResumeHandler() {
        return parseResumeHandler;
    }

    public void setParseResumeHandler(ResumeHandler parseResumeHandler) {
        this.parseResumeHandler = parseResumeHandler;
    }

    public ResumeHandler getScrappResumeHandler() {
        return scrappResumeHandler;
    }

    public void setScrappResumeHandler(ResumeHandler scrappResumeHandler) {
        this.scrappResumeHandler = scrappResumeHandler;
    }
}
