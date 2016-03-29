package com.scrapper;

import com.bc.io.CharFileIO;
import com.scrapper.util.PageNodes;
import com.bc.manager.Formatter;
import com.bc.net.RetryConnectionFilter;
import com.bc.process.StoppableTask;
import com.bc.util.XLogger;
import com.scrapper.tag.ArticleTag;
import com.scrapper.tag.Link;
import com.scrapper.tag.NoscriptTag;
import com.scrapper.tag.StrongTag;
import com.scrapper.tag.Tbody;
import com.scrapper.tag.Tfoot;
import com.scrapper.tag.Thead;
import com.scrapper.util.PageNodesImpl;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.http.ConnectionMonitor;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @(#)URLParser.java   27-Aug-2013 22:26:55
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * <b>Usage:</b><br/>
 * <pre>
 * 
 * // You could configure a crawler using a SiteConfig
 * 
 * String sitename = "afdb"; // Example
 * 
 * SiteConfig siteConfig = DefaultSiteConfig(sitename);
 * 
 * int limit = 1000; // Maximum amount of pages to parse
 * 
 * URLParser producer = new SiteCrawler(siteConfig, limit);
 * 
 * // You could also configure a crawler manually
 * 
 * // Replace this with the site you want to process
 * String source = "http://www.looseboxes.com"; 
 * 
 * com.bc.manager.util.Filter&lt;String&gt; filter = 
 * new com.bc.manager.util.Filter&lt;String&gt;(){
 *     public boolean accept(String link) {
 *         // Decide wether to accept the link
 *     }
 * };
 * 
 * com.bc.manager.util.Formatter&lt;String&gt; formatter = 
 * new com.bc.manager.util.Formatter&lt;String&gt;(){
     public String format(String link) {
         // Format if necessary or leave as is
     }
 };
 
 producer = new SiteCrawler();
 
 producer.setSource(source);
 
 producer.setFilter(filter);
 
 producer.setFormatter(formatter);
 
 // The maximum amount of pageLinks to process
 producer.setParseLimit(1000); 
 
 // Now after configuring the site crawler
 
 // Start generating URLs. 
 
 while(producer.hasNext()) {
 
     PageNodes pageNodes = producer.next(); 
 
     // Do something with the PageNodes
     String url = pageNodes.getURL();
 
     NodeList nodeList = pageNodes.getNodeList();
 }
 
 // finally
 
 producer.completePendingActions();
 
 </pre>
 * <br/><br/>
 * Each URL parsed could spin off many new URLs. 
 * If {@linkplain #captureQueryLinks} is <code>true</code> then links 
 * containing <code>?</code> will be parsed.
 * <br/><br/>
 * <b>Note:</b><br/>
 * If you intend to save the urls then you must handle links containing 
 * <code>?</code> specially to avoid saving both <code>abc.com</code>
 * and <code>abc.com?d=f</code> to the same file.
 * 
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class URLParser implements Iterator<PageNodes>, 
        StoppableTask, Serializable {
    
    private boolean started;
    private boolean stopInitiated;
    private boolean stopped;

    protected final Serializable pageLock = new Serializable(){};
    
    private boolean removeParsedUrls;
    
    private int parsePos;
    
    /**
     * A link is parsed when it's contents are downloaded and 
     * parsed into {@link org.htmlparser.Node}s.
     * The maximum amount of pageLinks to process. If some of the pageLinks 
     * result in MalformedURLException then the amount of URL produced will be 
     * short by that amount.
     */
    private int parseLimit;
    
    private int batchSize;
    private int indexWithinBatch;
    
    private long batchInterval;
    
    private long startTime;
    
    private Set<String> attempted;
    
    private Set<String> failed;
    
    /**
     * The list of pageLinks to parse.
     */
    private List<String> pageLinks;

    private Formatter<String> formatter;
    
    private Parser parser;
    
    /**
     * List of cookies to add to the Connection before connecting
     */
    private List<String> cookies;
    
    private RetryConnectionFilter reconnectAfterExceptionFilter;
    
    private com.bc.net.ConnectionManager connMgr;
    
    private com.bc.net.UserAgents userAgents;
    
    /**
     * Create a web site capturer.
     */
    public URLParser() {
        this(new ArrayList<String>());
    }
    
    public URLParser(List<String> urlList) {
        init(urlList);
    }
    
    private void init(List<String> urlList) {
XLogger.getInstance().log(Level.FINER, "Creating", this.getClass());

        removeParsedUrls = false;
        
        batchSize = 10; 
        
        batchInterval = 10000; // 10 seconds
        
        attempted = new HashSet<>();
        
        failed = new HashSet<>();

        pageLinks = Collections.synchronizedList(urlList);

//@todo make these properties        
        reconnectAfterExceptionFilter = new RetryConnectionFilter(2, 2000);
  
        parser = new Parser();

        Parser.setConnectionManager(new com.bc.webdatex.ParserConnectionManager());
        
        org.htmlparser.http.ConnectionManager cm = Parser.getConnectionManager();
        
        cm.setRedirectionProcessingEnabled (true);
        
        cm.setCookieProcessingEnabled (true);
        
        cm.setMonitor(this.newConnectionMonitor());

        PrototypicalNodeFactory factory = new PrototypicalNodeFactory ();
        
        factory.registerTag(new StrongTag());
        factory.registerTag(new Tbody());
        factory.registerTag(new Thead());
        factory.registerTag(new Tfoot());
        factory.registerTag(new NoscriptTag());
        factory.registerTag(new ArticleTag());
        factory.registerTag(new Link());
        
        parser.setNodeFactory (factory);
    }
    
    protected void preParse(String url) { }
    
    protected void postParse(PageNodes page) { }
    
    public void completePendingActions() { }
    
    private void setStarted() {
        if(!started) {
            this.started = true;
            this.stopInitiated = false;
            this.stopped = false;
        }
    }
    
    @Override
    public boolean hasNext() {

        this.setStarted();
        
        if(this.isStopInitiated()) {
            return false;
        }
        
        boolean output = false;
        
        while(this.isWithinParseLimit() && this.hasMoreUrls()) {

            if(this.isStopInitiated()) {
                output = false;
                break;
            }

            String s = this.pageLinks.get(parsePos);
XLogger.getInstance().log(Level.FINER, "UrlParser.hasNext. checking: {0}", this.getClass(), s);        

            if( this.isToBeCrawled(s) ) {
                
                output = true;
                
                break;
                
            }else{
                
                this.moveForward();
            }
        }
        
XLogger.getInstance().log(Level.FINE, "UrlParser.hasNext: {0}", this.getClass(), output);        
        return output;
    }
    
    @Override
    public PageNodes next() {
        
        this.setStarted();

        // process logic
        if(batchSize > 0 && ++indexWithinBatch >= batchSize) {

            indexWithinBatch = 0;

            this.waitBeforeNextBatch(batchInterval);
        }  

        // get the next URL
        final String rawUrl = pageLinks.get(parsePos);

        PageNodes page;
        try{

            this.preParse(rawUrl);

            // Keep a record of the captured urls
            // This is based on attempted captures

            this.getAttempted().add(rawUrl);

            // This book mark must set before parsing
            //
            int bookmark = pageLinks.size();

            // Format just before parsing
            final String url = formatter == null? 
                    rawUrl.replace("&amp;", "&") : 
                    formatter.format(rawUrl.replace("&amp;", "&"));

XLogger.getInstance().log(Level.FINER, "Raw: {0}\nURL: {1}", this.getClass(), rawUrl, url);            

            final NodeList list = this.parse(url);

            page = new PageNodesImpl(rawUrl, url, list);
            
            if(this.isNoFollow(page)) {
//                this.nofollow(bookmark);
//                page = null;
            }
            
            this.postParse(page);
            
            this.moveForward();
            
            return page;
            
        }catch(Exception e) { // When we use more specific ParserException, UnknownHostException which is not known to be thrown slipped through

            if(XLogger.getInstance().isLoggable(Level.FINE, this.getClass())) {
                XLogger.getInstance().log(Level.WARNING, "Parse failed for: "+rawUrl, this.getClass(), e);
            }else{
                XLogger.getInstance().log(Level.WARNING, 
                "Parse failed for: {0}. Reason: {1}", this.getClass(), rawUrl, e.toString());
            }
            
            this.failed.add(rawUrl);
            
            return null;
        }
    }
    
    private void moveForward() {
XLogger.getInstance().log(Level.FINEST, "Before moving forward. Parse pos: {0}, Url: {1}", 
        this.getClass(), parsePos, pageLinks.get(parsePos));

        if(!this.isRemoveParsedUrls()) {
            ++parsePos;
        }else{
            pageLinks.remove(parsePos);
        }
        
XLogger.getInstance().log(Level.FINER, "After moving forward. Parse pos {0}, Url: {1}", 
        this.getClass(), parsePos, parsePos<pageLinks.size()?pageLinks.get(parsePos):"no more URLs");
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    // StoppableTask interface methods
    
    @Override
    public final void run() {
        startTime = System.currentTimeMillis();
        try{
            doRun();
        }catch(Exception e) {
            // This may occur often so we log lightly
            XLogger.getInstance().log(Level.WARNING, "Unexpected exception: {0}", this.getClass(), e.toString());
        }finally{
            this.completePendingActions();
        }
    }
    
    protected void doRun() {
        throw new UnsupportedOperationException("Please provide an implementation of this method");
    }
    
    @Override
    public boolean isStopInitiated() {
        return stopInitiated;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void stop() {
XLogger.getInstance().log(Level.FINER, "Stop Initiated: {0}", this.getClass(), this);        
        stopInitiated = true;
    }

    @Override
    public boolean isCompleted() {
        return started && stopped && !stopInitiated;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    protected boolean hasMoreUrls() {
        boolean hasMore = (parsePos < pageLinks.size());
//System.out.println(this.getClass().getName()+". hasMore "+hasMore);            
XLogger.getInstance().log(Level.FINEST, "Has more urls: {0}", this.getClass(), hasMore);        
        return hasMore;
    }
    
    public boolean isWithinParseLimit() {
        boolean withinLimit = this.isWithLimit(parsePos, parseLimit);
//System.out.println(this.getClass().getName()+". within limit "+withinLimit);            
XLogger.getInstance().log(Level.FINEST, "Parse pos: {0}, limit: {1}, within parse limit: {2}", 
this.getClass(), parsePos, parseLimit, withinLimit);
        return withinLimit;
    }

    protected boolean isWithLimit(int offset, int limit) {
        boolean withinLimit = true;
        if(limit > 0) {
            withinLimit = offset < limit;
        }
        return withinLimit;
    }
    
    /**
     * Parse a single URL link.
     * @param url
     * @return 
     * @exception ParserException If a parse error occurs.
     */
    public NodeList parse(String url) throws ParserException { 
    
        NodeList list;
        
        try{
            
            // Get a random User agent
            //
            if(userAgents == null) {
                userAgents = new com.bc.net.UserAgents();
            }
            String userAgent;
            try{
                userAgent = userAgents.getAny(url, false);
            }catch(MalformedURLException e) {
                XLogger.getInstance().log(Level.WARNING, "URL: "+url, this.getClass(), e);
                userAgent = userAgents.getAny(false);
            }
            org.htmlparser.http.ConnectionManager.getDefaultRequestProperties().put(
                   "User-Agent", userAgent);

            try {

                list = this.doParse(url);

            }catch (EncodingChangeException ece) {

                list = this.applyBugfix991895(ece);
            }

        }catch(ParserException e) {

            boolean retry = this.isRetry(e, url);
            
            if(retry) {

                // We have to reprocess this
                list = parse(url);

            }else{
                
                list = null;
            }
            
            if(list == null) {

                throw e;
            }
        }

XLogger logger = XLogger.getInstance();        
if(logger.isLoggable(Level.FINEST, this.getClass())) {
logger.log(Level.FINEST, "URL: {0}, Nodes Found Html:\n{1}", this.getClass(), url, list.toHtml(false));
}else if(logger.isLoggable(Level.FINER, this.getClass())) {
logger.log(Level.FINER, "URL: {0}, Nodes Found : {1}", this.getClass(), url, list==null?null:list.size());
}

        return list;
    }

////////////////////////////////////////////////////////////////////////////////
// @bug    
// @doParse_0 The Parser.setURL was returning gibberish.. problably has to do with charset   
// Fixed this bug by using a com.bc.webdatex.ParserConnectionManager in the Parser
// @doParse_1 was used temporarily and may yet suffice    
////////////////////////////////////////////////////////////////////////////////    
    protected NodeList doParse(String url) throws ParserException {
        return doParse_0(url);
    }
    
    protected NodeList doParse_0(String url) throws ParserException {
XLogger.getInstance().log(Level.FINER, "Pages left: {0}, crawling: {1}",
        this.getClass(), this.getPageLinks().size()-parsePos, url);        
        // fetch the page and gather the list of nodes
        parser.setURL (url);

        NodeList list = null;

        NodeIterator e = parser.elements();
        
        while(e.hasNext()) {

            Node node = e.next();
            
            if(list == null) {
                list = new NodeList();
            }
            
            // URL conversion occurs in the tags
            list.add (node); 
        } 

XLogger.getInstance().log(Level.FINER, "Found: {0} nodes in page", 
        this.getClass(), list==null?null:list.size());
        
        return list;
    }
    
    protected NodeList doParse_1(String urlString) throws ParserException {
XLogger.getInstance().log(Level.FINE, "Pages left: {0}, crawling: {1}",
        this.getClass(), this.getPageLinks().size()-parsePos, urlString);        

        // First set the default cookie manager.
//@todo integrate CookieHandler with ConnectionManager
//        CookieHandler cookieHandler = CookieHandler.getDefault();
//        if(cookieHandler == null) {
//            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
//        }
            
        try{
            
            if(connMgr == null) {
                connMgr = new com.bc.net.ConnectionManager();
                connMgr.setAddCookies(true);
                connMgr.setGetCookies(true);
            }

            URL url = new URL(null, urlString, new com.bc.net.HttpStreamHandlerForBadStatusLine());

            InputStream in = connMgr.getInputStream(url); 

            CharSequence html = new CharFileIO().readChars(in);

            parser.setInputHTML(html.toString());
            
        }catch(IOException e) {
            
            // Light logging .. this may occur often
            XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
            
            // fetch the page and gather the list of nodes
            parser.setURL (urlString); 
        }
        
        NodeList list = null;

        NodeIterator e = parser.elements();
        
        while(e.hasNext()) {

            Node node = e.next();
            
            if(list == null) {
                list = new NodeList();
            }
            
            // URL conversion occurs in the tags
            list.add (node); 
        } 

XLogger.getInstance().log(Level.FINER, "Found: {0} nodes in page", 
        this.getClass(), list==null?null:list.size());
        
        return list;
    }

    protected boolean isRetry(ParserException e, String url) 
            throws ParserException {
        
        if(reconnectAfterExceptionFilter == null) {
            return false;
        }
        
        Throwable t = e.getThrowable(); 

        boolean retry = false;

        String msg = null;

        if (null != t) {

            if(reconnectAfterExceptionFilter.accept(t)) {

                retry = true;    

            }else{

                if (t instanceof FileNotFoundException || 
                    (t = t.getCause()) instanceof FileNotFoundException) {
                    msg = "Broken link ignored";
                }    
            }
        }    
        
        if(retry) {
            
            XLogger.getInstance().log(Level.FINE, "Caught:{0}, Retrying: {1}", 
                    this.getClass(), t, url==null?"":url);

        }else{
            
            if(msg == null) msg = "Link ignored";

// This is generated often so FINE not WARNING            
            XLogger.getInstance().log(Level.FINE, "{0}. {1}", 
                    this.getClass(), msg, e);
        }

        return retry;
    }
    
    private NodeList applyBugfix991895(
            EncodingChangeException ece) throws ParserException {
        
XLogger.getInstance().log(Level.WARNING, 
"PARSER CRASHED! Caught: "+ece.getClass().getName()+"\nApplying bug fix #991895",
    this.getClass(), ece);

        // fix bug #998195 SiteCatpurer just crashed
        // try again with the encoding now set correctly
        // hopefully pageList won't be corrupted
        parser.reset();

        NodeList list = new NodeList();

        for (NodeIterator e = parser.elements (); e.hasNext (); ) {
            list.add (e.next ());
        }    

        return list;
    }
    
    private boolean isNoFollow(PageNodes page) {
        if(page.getRobots() == null) {
            return false;
        }
        String content = page.getRobots().getAttribute("content");
        return (content.contains("none")) || (content.contains("nofollow"));
    }
    
    protected void nofollow(int bookmark) {
        synchronized(pageLock) {
            // reset 
            for (int i = bookmark; i < pageLinks.size (); i++) {
                pageLinks.remove (i);
            }  
        }
    }

    /**
     * Filters off URLs which will not be parsed. Each URL parsed could 
     * spin off many new URLs.
     * <br/>
     * Returns false if the link contains a <code>#</code> because presumably 
     * the full page with that reference has already been captured previously. 
     * @param link The link to be checked.
     * @return <code>true</code> if the link is one we are interested in.
     */
    public boolean isToBeCrawled(String link) {

        boolean toBeCrawled = !this.isAttempted(link);
if(!toBeCrawled) {
XLogger.getInstance().log(Level.FINE, "Already attempted: {0}", this.getClass(), link);
}            
            
XLogger.getInstance().log(Level.FINER, "To be crawled: {0}, Link: {1}", 
        this.getClass(), toBeCrawled, link);

        return toBeCrawled;
    }
    
    protected boolean isAttempted(String link) {
        return this.getAttempted().contains(link);
    }
    
    protected synchronized void waitBeforeNextBatch(long interval) {
    
        try{

            if(this.reconnectAfterExceptionFilter != null) {
                this.reconnectAfterExceptionFilter.reset();
            }
            
            if(interval > 0) {
                
long freeMemory = Runtime.getRuntime().freeMemory();
XLogger.getInstance().log(Level.FINER, "Waiting for {0} milliseconds, free memory: {1}", 
    this.getClass(), interval, freeMemory);
                
                this.wait(interval);
                
XLogger.getInstance().log(Level.FINE, "Done waiting for {0} milliseconds, memory saved: {1}", 
    this.getClass(), interval, freeMemory-Runtime.getRuntime().freeMemory());
            }

        }catch(InterruptedException e) {
            XLogger.getInstance().log(Level.WARNING, "", this.getClass(), e);
        }finally{
            notifyAll();
        }
    }
    
    private ConnectionMonitor newConnectionMonitor() {
        
        return new ConnectionMonitor() {
            @Override
            public void preConnect(HttpURLConnection connection) throws ParserException {
XLogger.getInstance().log(Level.FINER, "@preConnect. Connection: {0}", this.getClass(), connection);                

                this.addCookies(connection, cookies);
            }
            public void addCookies(URLConnection connection, List<String> cookies) {
                
                if(cookies == null || cookies.isEmpty()) return;
                
                for (String cookie : cookies) {
        // The split(";", 2)[0] is there to get rid of cookie attributes which are
        // irrelevant for the server side like expires, path, etc.
                    String str = cookie.split(";", 2)[0];
XLogger.getInstance().log(Level.FINER, "Adding cookie: {0}", this.getClass(), str);            
                    connection.addRequestProperty("Cookie", str);
                }
            }
            @Override
            public void postConnect(HttpURLConnection connection) throws ParserException {
            }
        };
    }

    public int getParsePos() {
        return parsePos;
    }
    
    public Set<String> getAttempted() {
        return attempted;
    }

    public Set<String> getFailed() {
        return failed;
    }
    
    // Getters and Setters

    public boolean isRemoveParsedUrls() {
        return removeParsedUrls;
    }

    public void setRemoveParsedUrls(boolean b) {
        this.removeParsedUrls = b;
    }

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public List<String> getPageLinks() {
        return pageLinks;
    }

    public void setPageLinks(List<String> pageLinks) {
        this.pageLinks = Collections.synchronizedList(pageLinks);
    }

    public long getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(long batchInterval) {
        this.batchInterval = batchInterval;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public RetryConnectionFilter getConnectionMonitor() {
        return reconnectAfterExceptionFilter;
    }

    public void setConnectionMonitor(RetryConnectionFilter connectionMonitor) {
        this.reconnectAfterExceptionFilter = connectionMonitor;
    }

    public Formatter<String> getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter<String> urlFormatter) {
        this.formatter = urlFormatter;
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    /**
     * @see #parseLimit
     * @return The maximum number of links that may be parsed
     */
    public int getParseLimit() {
        return parseLimit;
    }

    /**
     * @see #parseLimit
     * @param limit The maximum number of links that may be parsed
     */
    public void setParseLimit(int limit) {
        this.parseLimit = limit;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String getTaskName() {
        return this.getClass().getName();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.print(builder);
        return builder.toString();
    }
    
    public void print(StringBuilder builder) {
        builder.append(this.getTaskName());
        builder.append(", started: ").append(this.started);
        builder.append(", stopInitiated: ").append(this.stopInitiated);
        builder.append(", stopped: ").append(this.stopped);
        builder.append(", ParsePos: ").append(this.parsePos);
        builder.append(", Urls Left: ").append(this.pageLinks==null?null:this.pageLinks.size()-parsePos);
        builder.append(", Attempted: ").append(this.attempted==null?null:this.attempted.size());
        builder.append(", Failed: ").append(this.failed==null?null:this.failed.size());
    }
}

