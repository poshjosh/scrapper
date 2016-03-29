package com.scrapper;

import com.bc.util.XLogger;
import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.url.ConfigURLPartList;
import com.scrapper.util.PageNodes;
import com.scrapper.util.Util;
import java.util.List;
import java.util.logging.Level;

/**
 * @(#)MultipleSourcesCrawler.java   29-Dec-2013 00:06:17
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class MultipleSourcesCrawler extends Crawler {
    
    boolean addedIndirectSource;

    private int sourcesIndex;
    
    /**
     * These are sources from which urls will be generated. These sources
     * are links to actual urls which will be extracted; Therefore each url 
     * in this list will only be crawled/parsed but not scrapped
     */
    private List<String> sources;
    
    public MultipleSourcesCrawler(CapturerContext context, List<String> sources) {
        
        this(context);
        
        MultipleSourcesCrawler.this.setSources(sources);
    }
    
    public MultipleSourcesCrawler(CapturerContext context) {
        
        super(context);
        
        MultipleSourcesCrawler.this.checkListGenerationMode(context.getConfig());
    }
    
    protected void checkListGenerationMode(JsonConfig config) { 
    
        ConfigURLPartList serialPart = ConfigURLPartList.getSerialPart(config, "counter");
        
        if(serialPart != null) {
            throw new IllegalArgumentException("Invalid format for property: url.counter.part");
        }
    }
    
//    @Override
//    protected void preparseUpdate(String url) {
//        if(this.addedIndirectSource) {
//XLogger.getInstance().log(Level.INFO, "@preparse ignoring indirect source: {0}", 
//        this.getClass(), sources.get(sourcesIndex-1));
//            return;
//        }
//        super.preparseUpdate(url);
//    }
    
//    @Override
//    protected void postparseUpdate() {
//        if(this.addedIndirectSource) {
//XLogger.getInstance().log(Level.INFO, "@postparse ignoring indirect source: {0}", 
//        this.getClass(), sources.get(sourcesIndex-1));
//            return;
//        }
//        super.postparseUpdate();
//    }
    
    @Override
    public boolean hasNext() {
        
        this.updateBaseUrl(sources);
        
        if(this.hasMoreSources()) {
            
            String addedSrc = this.sources.get(sourcesIndex);
            
            this.getPageLinks().add(addedSrc);
            
            addedIndirectSource = true;
            
XLogger.getInstance().log(Level.FINE, "Added indirect source: {0}", 
        this.getClass(), addedSrc);

        }else{
            
            addedIndirectSource = false;
        }
        
        return super.hasNext();
    }
    
    @Override
    public PageNodes next() {
        
        this.updateBaseUrl(this.sources);
        
        String addedSrc;
        
        if(addedIndirectSource) {
            
            addedSrc = this.sources.get(sourcesIndex++);

        }else{
            
            addedSrc = null;
        }  
        
        PageNodes page = super.next();
        
        if(addedSrc == null) {
            
            return page;
        }
        
        if(page == null) {
            return null;
        }

        if(page.getURL().equals(addedSrc)) {
XLogger.getInstance().log(Level.FINE, "After crawling, ignoring indirect source: {0}", this.getClass(), addedSrc);

            return null;
            
        }else{
            
            return page;
        }    
    }
    
    public boolean hasMoreSources() {
        return this.sources != null && sourcesIndex < this.sources.size();        
    }
    
    public final void updateBaseUrl(List<String> urls) {
        
        if(this.getBaseUrl() == null && urls != null && !urls.isEmpty()) {
            
            String firstSource = urls.get(0);
            
            if(firstSource != null) {
                
                this.setBaseUrl(Util.getBaseURL(firstSource));
            }
        }
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
        this.updateBaseUrl(sources);
    }
}
