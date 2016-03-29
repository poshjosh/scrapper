package com.scrapper;

import com.scrapper.config.Config;
import com.bc.manager.Filter;
import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import com.bc.json.config.JsonConfig;
import com.scrapper.util.HtmlContentFilter;
import com.scrapper.util.HtmlLinkFilter;
import com.scrapper.util.Util;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;

/**
 * @(#)Crawler.java   29-Aug-2013 15:15:45
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @see com.scrapper.URLParser
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class Crawler extends ResumableUrlParser {
    
    private boolean captureQueryLinks;
    
    private boolean startCollectingLinks = true;
    
    private boolean stopCollectingLinks;

    /**
     * Number of links crawled
     */
    private int crawled;
    
    /**
     * A link is crawled when it is selected for parsing.
     * The maximum amount of pageLinks to crawl.
     */
    private int crawlLimit;
    
    private String baseUrl;
    
    private String startUrl;
    
    /**
     * Determines if a URL will be crawled
     */
    private Filter<String> urlFilter;
    
    private Filter<String> htmlLinkFilter;
    
    private Filter<String> htmlContentFilter;
    
    private CapturerContext context;

    public Crawler(CapturerContext context) {

        super(context.getConfig().getName());
XLogger.getInstance().log(Level.FINER, "Creating", this.getClass());
        
        Crawler.this.setContext(context); 
        
        Crawler.this.setCaptureQueryLinks(true);
        PrototypicalNodeFactory factory = (PrototypicalNodeFactory)this.getParser().getNodeFactory();
        factory.registerTag(new LinkCollectingLinkTag ());
        factory.registerTag(new LinkCollectingFrameTag ());
XLogger.getInstance().log(Level.FINER, "Done creating: {0}", this.getClass(), Crawler.this);
    }
    
    public boolean isWithinCrawlLimit() {
        boolean withinLimit = this.isWithLimit(crawled, crawlLimit);
XLogger.getInstance().log(Level.FINEST, "URLs: {0}, limit: {1}, within crawl limit: {2}", 
this.getClass(), getPageLinks().size(), crawlLimit, withinLimit);
        return withinLimit;
    }

    /**
     * Filters off URLs which will not be parsed. URLs are first parsed
     * and subsequently downloaded to be scrapped. Each URL parsed could 
     * spin off many new URLs.
     * <br/>
     * Returns false if the link contains a <code>#</code> because presumably 
     * the full page with that reference has already been captured previously. 
     * <br/>
     * If {@linkplain #captureQueryLinks} is <code>true</code> then links 
     * containing <code>?</code> will be captured.
     * <br/><br/>
     * <b>Note:</b><br/>
     * If you intend to save the urls then you must handle links containing 
     * <code>?</code> specially to avoid saving both <code>abc.com</code>
     * and <code>abc.com?d=f</code> referring to the samefile.
     * @param link The link to be checked.
     * @return <code>true</code> if the link is one we are interested in.
     */
    @Override
    public boolean isToBeCrawled(String link) {
XLogger.getInstance().log(Level.FINEST, "@isToBeCaptured. Link: {0}", 
        this.getClass(), link);

        boolean toBeCrawled;
        
        if(link.equals(this.startUrl)) {
            
            toBeCrawled = true;
            
        }else{
            
            toBeCrawled = super.isToBeCrawled(link);
            
            if(toBeCrawled) {
                // We remain within the current site
                //
                if(this.baseUrl.trim().isEmpty()) {
                    toBeCrawled = true;
                }else if(link.trim().isEmpty()) {
                    toBeCrawled = false;
                }else{
                    try{
// @NOTE when www.site_1.com is redirected to www.site_2.com 
// The baseURL property is www.site_1.com whereas all links in the site 
// will be of the format www.site_2.com and this will fail. Therefore:
// @todo consider cross checking each link to the final redirectBaseUrl in 
// addition to this                        
                        // Resolve discrepancies between www.abc.com and abc.com
                        toBeCrawled = Util.toWWWFormat(link).startsWith(Util.toWWWFormat(this.baseUrl));
                    }catch(MalformedURLException e) {
                        toBeCrawled = false;
                    }
                }    
            }    

            if(toBeCrawled) {
                toBeCrawled = !link.contains("#");
            }

            if(toBeCrawled) {           
                toBeCrawled = !(-1 != link.indexOf("?") && !this.isCaptureQueryLinks());
            }   

            if(toBeCrawled && this.getUrlFilter() != null) {

XLogger.getInstance().log(Level.FINER, "@isToBeCaptured. Filtering with: {0}", 
    this.getClass(), this.getUrlFilter().getClass());

                toBeCrawled = this.getUrlFilter().accept(link);
XLogger.getInstance().log(Level.FINE, "Accepted by URL Filter: {0}, Link: {1}", this.getClass(), toBeCrawled, link);
                
            }
        }    
            
Level level = toBeCrawled ? Level.FINE : Level.FINER;        
XLogger.getInstance().log(level, "To be captured: {0}, Link: {1}", this.getClass(), toBeCrawled, link);

        return toBeCrawled;
    }
    
    /**
     * Returns <code>true</code> if the link contains text/html content.
     */
    protected boolean isHtml (String link) {
        
        // We do this to try and avoid the coming expensive operation
        // However this may not be reliable
        //
        if(htmlLinkFilter == null) {
            htmlLinkFilter = new HtmlLinkFilter();
        }
        
        if(htmlLinkFilter.accept(link)) {
            return true;
        }

        // expensive operation
        //
        if(htmlContentFilter == null) {
            htmlContentFilter = new HtmlContentFilter();
        }
        
        return htmlContentFilter.accept(link);
    }

    class LinkCollectingLinkTag extends LinkTag {
        
        @Override
        public void doSemanticAction () throws ParserException {

            if(!startCollectingLinks || stopCollectingLinks) {
                return;
            }
            
            // getObject the link
            String link = getLink ();
            
            // check if it needs to be captured
            if (isWithinCrawlLimit() && isToBeCrawled (link)) {

                
                synchronized(pageLock) {
                    // add the link to a list to be processed
                    if (!getPageLinks().contains (link)) {

                        // this test is expensive, do it reluctantly

                        boolean html = isHtml (link);

                        if (html) {
XLogger.getInstance().log(Level.FINER, "Adding: {0}", this.getClass(), link);                            
                            getPageLinks().add (link);
                            
                            ++crawled;
                        }    
                    }
                }
            }
        }
    }

    class LinkCollectingFrameTag extends FrameTag {
        @Override
        public void doSemanticAction () throws ParserException {
            
            if(!startCollectingLinks || stopCollectingLinks) {
                return;
            }
            
            // getObject the link
            String link = getFrameLocation ();
            
            // check if it needs to be captured
            if (isWithinCrawlLimit() && isToBeCrawled (link)) {
                
                synchronized(pageLock) {
                    // add the link to a list to be processed
                    if (!getPageLinks().contains (link)) {

                        // this test is expensive, do it reluctantly
                        boolean html = isHtml (link);

                        if (html) {
                            
                            getPageLinks().add (link);
                            
                            ++crawled;
                        }    
                    }
                }
            }
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * <b>Note: The input should not end with '/' or '\\' as the case may be</b>
     * <br/>http://www.abc.com rather than http://www.abc.com/
     * <br/><br/>
     * This updates the {@linkplain #startUrl}, if it is null, with one created
     * from the input.
     */
    public void setBaseUrl(String baseUrl) {
        if(baseUrl == null) {
            throw new NullPointerException();
        }
        this.baseUrl = baseUrl;
        if(this.startUrl == null) {
            this.startUrl = baseUrl;
        }
    }

    public String getStartUrl() {
        return startUrl;
    }

    /**
     * This updates the {@linkplain #baseUrl}, if it is null, with one created
     * from the input.
     */
    public void setStartUrl(String startUrl) {
        if(startUrl == null) {
            throw new NullPointerException();
        }
        if(!this.getPageLinks().contains(startUrl)) {
            this.getPageLinks().add(0, startUrl);
        }
        this.startUrl = startUrl;
        if(this.baseUrl == null) {
            this.baseUrl = Util.getBaseURL(startUrl);
        }
    }
    
    public CapturerContext getContext() {
        return this.context;
    }
    
    public void setContext(CapturerContext context) {
XLogger.getInstance().log(Level.FINER, "Updating context for: {0}", this.getClass(), this);
        
        this.context = context;
        
        JsonConfig config = context.getConfig();
        
        this.setSitename(config.getName());
        
        String url_start = config.getString("url", Config.Site.start);
        if(url_start == null || url_start.isEmpty()) {
            baseUrl = (String)config.getString("url", "value");
            if(baseUrl == null || baseUrl.isEmpty()) {
                throw new NullPointerException("Both url.start and url.value cannot be null in config: "+config.getName());
            }
            this.setStartUrl(baseUrl);
        }else{
            this.setStartUrl(url_start);
        }
        
        Crawler.this.setUrlFilter(context.getCaptureUrlFilter());
        
        this.setFormatter(context.getUrlFormatter());
        
        int limit = config.getInt(Config.Extractor.parseLimit);
        
        this.setParseLimit(limit);
XLogger.getInstance().log(Level.FINER, "Updated context for: {0}", this.getClass(), this);
    }

    public int getCrawled() {
        return crawled;
    }

    // Getters and Setters
    
    public boolean isCaptureQueryLinks() {
        return captureQueryLinks;
    }

    public void setCaptureQueryLinks(boolean captureQueryLinks) {
        this.captureQueryLinks = captureQueryLinks;
    }
    
    public Filter<String> getUrlFilter() {
        return urlFilter;
    }

    public void setUrlFilter(Filter<String> filter) {
        this.urlFilter = filter;
    }

    public boolean isStartCollectingLinks() {
        return startCollectingLinks;
    }

    public void setStartCollectingLinks(boolean startCollectingLinks) {
        this.startCollectingLinks = startCollectingLinks;
    }

    public boolean isStopCollectingLinks() {
        return stopCollectingLinks;
    }

    public void setStopCollectingLinks(boolean stopCollectingLinks) {
        this.stopCollectingLinks = stopCollectingLinks;
    }

    public int getCrawlLimit() {
        return crawlLimit;
    }

    public void setCrawlLimit(int limit) {
        this.crawlLimit = limit;
    }
    
    @Override
    public void print(StringBuilder builder) {
        super.print(builder);
        builder.append(", startCollectingLinks: ").append(this.startCollectingLinks);
        builder.append(", stopCollectingLinks: ").append(this.stopCollectingLinks);
        builder.append(", baseURL").append(this.baseUrl);
        builder.append(", startURL").append(this.startUrl);
    }
}
