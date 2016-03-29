package com.scrapper.scheduling;

import com.bc.manager.Filter;
import com.bc.util.XLogger;
import com.scrapper.CapturerApp;
import com.scrapper.Crawler;
import com.scrapper.DefaultSiteCapturer;
import com.scrapper.SiteCapturer;
import com.scrapper.URLParser;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * @(#)CaptureSitesManager.java   11-Jul-2014 11:39:38
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
public class CaptureSitesManager 
        implements Filter<JsonConfig>, Serializable {
    
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
        
        ScrapperConfigFactory factory = this.getConfigFactory();
        
        Set<String> accepted = new HashSet<String>();
        
        Set<String> sitenames = factory.getSitenames();
        
        for(String sitename:sitenames) {

            JsonConfig config = factory.getConfig(sitename);
            
            if(accept(config)) {
                accepted.add(sitename);
            }
        }
        
        XLogger.getInstance().log(Level.FINE, "Sitenames: {0}", this.getClass(), accepted);        

        return accepted;
    }
    
    @Override
    public boolean accept(JsonConfig config) {

        // We only accept configs which are not disabled
        //
        Boolean disabled = config.getBoolean(Config.Extractor.disabled);
        
        boolean accept = disabled == null || !disabled;
        
XLogger.getInstance().log(Level.FINER, "Config: {0}, accepted: {1}", 
        this.getClass(), config.getName(), accept);                

        return accept;
    }

    public SiteCapturer newTask(String sitename) {
        
        JsonConfig config = this.getConfigFactory().getConfig(sitename);

        if(config == null) {
            throw new NullPointerException("Failed to load site config for: "+sitename);
        }
        
        CapturerContext context = this.getConfigFactory().getContext(config);
        
        DefaultSiteCapturer capturer = new DefaultSiteCapturer(context, null, true, true);
        
        URLParser crawler = capturer.getCrawler();
        
        if(crawler instanceof Crawler) {
            ((Crawler)crawler).setCrawlLimit(crawlLimit);
        }
        crawler.setParseLimit(parseLimit);
        capturer.setScrappLimit(scrappLimit);

        if(batchSize > 0) {
            crawler.setBatchSize(batchSize);
        }
        
        if(batchInterval > 0) {
            crawler.setBatchInterval(batchInterval);
        }
        
        return capturer;
    }
    
    public final Object getTaskId(String sitename) {
        return sitename;
    }

    public int getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(int batchInterval) {
        this.batchInterval = batchInterval;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getCrawlLimit() {
        return crawlLimit;
    }

    public void setCrawlLimit(int crawlLimit) {
        this.crawlLimit = crawlLimit;
    }

    public int getParseLimit() {
        return parseLimit;
    }

    public void setParseLimit(int parseLimit) {
        this.parseLimit = parseLimit;
    }

    public int getScrappLimit() {
        return scrappLimit;
    }

    public void setScrappLimit(int scrappLimit) {
        this.scrappLimit = scrappLimit;
    }
}
