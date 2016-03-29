package com.scrapper;

import com.bc.io.CharFileIO;
import com.bc.net.HttpStreamHandlerForBadStatusLine;
import com.bc.net.RetryConnectionFilter;
import com.bc.net.UserAgents;
import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.bc.webdatex.ParserConnectionManager;
import com.scrapper.tag.ArticleTag;
import com.scrapper.tag.Link;
import com.scrapper.tag.NoscriptTag;
import com.scrapper.tag.StrongTag;
import com.scrapper.tag.Tbody;
import com.scrapper.tag.Tfoot;
import com.scrapper.tag.Thead;
import com.scrapper.util.PageNodes;
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

public class URLParser
  implements Iterator<PageNodes>, StoppableTask, Serializable
{
  private boolean started;
  private boolean stopInitiated;
  private boolean stopped;
  protected final Serializable pageLock = new Serializable() {};
  
  private boolean removeParsedUrls;
  
  private int parsePos;
  
  private int parseLimit;
  
  private int batchSize;
  
  private int indexWithinBatch;
  
  private long batchInterval;
  
  private long startTime;
  
  private Set<String> attempted;
  

  private Set<String> failed;
  

  private List<String> pageLinks;
  

  private Formatter<String> formatter;
  

  private Parser parser;
  

  private List<String> cookies;
  
  private RetryConnectionFilter reconnectAfterExceptionFilter;
  
  private com.bc.net.ConnectionManager connMgr;

  public URLParser()
  {
    this(new ArrayList());
  }
  
  public URLParser(List<String> urlList) {
    init(urlList);
  }
  
  private void init(List<String> urlList) {
    XLogger.getInstance().log(Level.FINER, "Creating", getClass());
    
    this.removeParsedUrls = false;
    
    this.batchSize = 10;
    
    this.batchInterval = 10000L;
    
    this.attempted = new HashSet();
    
    this.failed = new HashSet();
    
    this.pageLinks = Collections.synchronizedList(urlList);

    this.reconnectAfterExceptionFilter = new RetryConnectionFilter(2, 2000L);
    
    this.connMgr = new com.bc.net.ConnectionManager();
    this.connMgr.setAddCookies(true);
    this.connMgr.setGetCookies(true);
    this.connMgr.setGenerateRandomUserAgent(true);
    
    this.parser = new Parser();
    
    Parser.setConnectionManager(new ParserConnectionManager());
    
    org.htmlparser.http.ConnectionManager cm = Parser.getConnectionManager();
    
    cm.setRedirectionProcessingEnabled(true);
    
    cm.setCookieProcessingEnabled(true);
    
    cm.setMonitor(newConnectionMonitor());
    
    PrototypicalNodeFactory factory = new PrototypicalNodeFactory();
    
    factory.registerTag(new StrongTag());
    factory.registerTag(new Tbody());
    factory.registerTag(new Thead());
    factory.registerTag(new Tfoot());
    factory.registerTag(new NoscriptTag());
    factory.registerTag(new ArticleTag());
    factory.registerTag(new Link());
    
    this.parser.setNodeFactory(factory);
  }
  
  protected void preParse(String url) {}
  
  protected void postParse(PageNodes page) {}
  
  public void completePendingActions() {}
  
  private void setStarted() {
    if (!this.started) {
      this.started = true;
      this.stopInitiated = false;
      this.stopped = false;
    }
  }

  public boolean hasNext()
  {
    setStarted();
    
    if (isStopInitiated()) {
      return false;
    }
    
    boolean output = false;
    
    while ((isWithinParseLimit()) && (hasMoreUrls()))
    {
      if (isStopInitiated()) {
        output = false;
        break;
      }
      
      String s = (String)this.pageLinks.get(this.parsePos);
      XLogger.getInstance().log(Level.FINER, "UrlParser.hasNext. checking: {0}", getClass(), s);
      
      if (isToBeCrawled(s))
      {
        output = true;
        
        break;
      }
      
      moveForward();
    }
    
    XLogger.getInstance().log(Level.FINE, "UrlParser.hasNext: {0}", getClass(), Boolean.valueOf(output));
    return output;
  }
  
  public PageNodes next()
  {
    setStarted();
    
    if ((this.batchSize > 0) && (++this.indexWithinBatch >= this.batchSize))
    {
      this.indexWithinBatch = 0;
      
      waitBeforeNextBatch(this.batchInterval);
    }
    
    String rawUrl = (String)this.pageLinks.get(this.parsePos);

    try
    {
      preParse(rawUrl);

      getAttempted().add(rawUrl);

      int bookmark = this.pageLinks.size();

      String url = this.formatter == null ? rawUrl.replace("&amp;", "&") : (String)this.formatter.format(rawUrl.replace("&amp;", "&"));

      XLogger.getInstance().log(Level.FINER, "Raw: {0}\nURL: {1}", getClass(), rawUrl, url);
      
      NodeList list = parse(url);
      
      PageNodes page = new PageNodesImpl(rawUrl, url, list);
      
      if (isNoFollow(page)) {}

      postParse(page);
      
      moveForward();
      
      return page;
    }
    catch (Exception e)
    {
      boolean added = this.failed.add(rawUrl);
      if (XLogger.getInstance().isLoggable(Level.FINE, getClass())) {
        XLogger.getInstance().log(Level.WARNING, "Parse failed for: " + rawUrl, getClass(), e);
      } else {
        if(added) { // We don't want to log the same URL twice
            XLogger.getInstance().log(Level.WARNING, "Parse failed for: {0}. Reason: {1}", getClass(), rawUrl, e.toString());
        }
      }
    }
    return null;
  }
  
  private void moveForward()
  {
    XLogger.getInstance().log(Level.FINEST, "Before moving forward. Parse pos: {0}, Url: {1}", getClass(), Integer.valueOf(this.parsePos), this.pageLinks.get(this.parsePos));
    
    if (!isRemoveParsedUrls()) {
      this.parsePos += 1;
    } else {
      this.pageLinks.remove(this.parsePos);
    }
    
    XLogger.getInstance().log(Level.FINER, "After moving forward. Parse pos {0}, Url: {1}", getClass(), Integer.valueOf(this.parsePos), this.parsePos < this.pageLinks.size() ? (String)this.pageLinks.get(this.parsePos) : "no more URLs");
  }

  public void remove()
  {
    throw new UnsupportedOperationException();
  }

  public final void run()
  {
    this.startTime = System.currentTimeMillis();
    try {
      doRun();
    }
    catch (Exception e) {
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception: {0}", getClass(), e.toString());
    }
    finally {
      completePendingActions();
    }
  }
  
  protected void doRun() {
    throw new UnsupportedOperationException("Please provide an implementation of this method");
  }
  
  public boolean isStopInitiated()
  {
    return this.stopInitiated;
  }
  
  public boolean isStopped()
  {
    return this.stopped;
  }
  
  public void stop()
  {
    XLogger.getInstance().log(Level.FINER, "Stop Initiated: {0}", getClass(), this);
    this.stopInitiated = true;
  }
  
  public boolean isCompleted()
  {
    return (this.started) && (this.stopped) && (!this.stopInitiated);
  }
  
  public boolean isStarted()
  {
    return this.started;
  }
  
  protected boolean hasMoreUrls() {
    boolean hasMore = this.parsePos < this.pageLinks.size();
    
    XLogger.getInstance().log(Level.FINEST, "Has more urls: {0}", getClass(), Boolean.valueOf(hasMore));
    return hasMore;
  }
  
  public boolean isWithinParseLimit() {
    boolean withinLimit = isWithLimit(this.parsePos, this.parseLimit);
    
    XLogger.getInstance().log(Level.FINEST, "Parse pos: {0}, limit: {1}, within parse limit: {2}", getClass(), Integer.valueOf(this.parsePos), Integer.valueOf(this.parseLimit), Boolean.valueOf(withinLimit));
    
    return withinLimit;
  }
  
  protected boolean isWithLimit(int offset, int limit) {
    boolean withinLimit = true;
    if (limit > 0) {
      withinLimit = offset < limit;
    }
    return withinLimit;
  }

  public NodeList parse(String url)
    throws ParserException
  {
    NodeList list;
    
    try
    {
        
      UserAgents userAgents = this.connMgr.getUserAgents();
      String userAgent;
      try{
        userAgent = userAgents.getAny(url, false);
      }catch(MalformedURLException e) {
          userAgent = userAgents.getAny(false);
      }
      org.htmlparser.http.ConnectionManager.getDefaultRequestProperties().put(
              "User-Agent", userAgent);

      try
      {
        list = doParse(url);
      }
      catch (EncodingChangeException ece)
      {
        list = applyBugfix991895(ece);
      }
    }
    catch (ParserException e) {

      boolean retry = isRetry(e, url);
      
      if (retry)
      {

        list = parse(url);
      }
      else
      {
        list = null;
      }
      
      if (list == null)
      {
        throw e;
      }
    }
    
    XLogger logger = XLogger.getInstance();
    if (logger.isLoggable(Level.FINEST, getClass())) {
      logger.log(Level.FINEST, "URL: {0}, Nodes Found Html:\n{1}", getClass(), url, list.toHtml(false));
    } else if (logger.isLoggable(Level.FINER, getClass())) {
      logger.log(Level.FINER, "URL: {0}, Nodes Found : {1}", getClass(), url, list == null ? null : Integer.valueOf(list.size()));
    }
    
    return list;
  }

  protected NodeList doParse(String url)
    throws ParserException
  {
    return doParse_0(url);
  }
  
  protected NodeList doParse_0(String url) throws ParserException {
    XLogger.getInstance().log(Level.FINE, "Pages left: {0}, crawling: {1}", getClass(), Integer.valueOf(getPageLinks().size() - this.parsePos), url);
    
    this.parser.setURL(url);
    
    NodeList list = null;
    
    NodeIterator e = this.parser.elements();
    
    while (e.hasNext())
    {
      Node node = e.next();
      
      if (list == null) {
        list = new NodeList();
      }

      list.add(node);
    }
    
    XLogger.getInstance().log(Level.FINER, "Found: {0} nodes in page", getClass(), list == null ? null : Integer.valueOf(list.size()));
    
    return list;
  }
  
  protected NodeList doParse_1(String urlString) throws ParserException {
    XLogger.getInstance().log(Level.FINE, "Pages left: {0}, crawling: {1}", getClass(), Integer.valueOf(getPageLinks().size() - this.parsePos), urlString);
    
    try
    {
      
      URL url = new URL(null, urlString, new HttpStreamHandlerForBadStatusLine());
      
      InputStream in = this.connMgr.getInputStream(url);
      
      CharSequence html = new CharFileIO().readChars(in);
      
      this.parser.setInputHTML(html.toString());

    }
    catch (IOException e)
    {
      XLogger.getInstance().log(Level.WARNING, "{0}", getClass(), e.toString());
      

      this.parser.setURL(urlString);
    }
    
    NodeList list = null;
    
    NodeIterator e = this.parser.elements();
    
    while (e.hasNext())
    {
      Node node = e.next();
      
      if (list == null) {
        list = new NodeList();
      }
      

      list.add(node);
    }
    
    XLogger.getInstance().log(Level.FINER, "Found: {0} nodes in page", getClass(), list == null ? null : Integer.valueOf(list.size()));
    

    return list;
  }
  
  protected boolean isRetry(ParserException e, String url)
    throws ParserException
  {
    if (this.reconnectAfterExceptionFilter == null) {
      return false;
    }
    
    Throwable t = e.getThrowable();
    
    boolean retry = false;
    
    String msg = null;
    
    if (null != t)
    {
      if (this.reconnectAfterExceptionFilter.accept(t))
      {
        retry = true;


      }
      else if (((t instanceof FileNotFoundException)) || (((t = t.getCause()) instanceof FileNotFoundException)))
      {
        msg = "Broken link ignored";
      }
    }
    

    if (retry)
    {
      XLogger.getInstance().log(Level.FINE, "Caught:{0}, Retrying: {1}", getClass(), t, url == null ? "" : url);

    }
    else
    {
      if (msg == null) { msg = "Link ignored";
      }
      
      XLogger.getInstance().log(Level.FINE, "{0}. {1}", getClass(), msg, e);
    }
    

    return retry;
  }
  
  private NodeList applyBugfix991895(EncodingChangeException ece)
    throws ParserException
  {
    XLogger.getInstance().log(Level.WARNING, "PARSER CRASHED! Caught: " + ece.getClass().getName() + "\nApplying bug fix #991895", getClass(), ece);
    





    this.parser.reset();
    
    NodeList list = new NodeList();
    
    for (NodeIterator e = this.parser.elements(); e.hasNext();) {
      list.add(e.next());
    }
    
    return list;
  }
  
  private boolean isNoFollow(PageNodes page) {
    if (page.getRobots() == null) {
      return false;
    }
    String content = page.getRobots().getAttribute("content");
    return (content.contains("none")) || (content.contains("nofollow"));
  }
  
  protected void nofollow(int bookmark) {
    synchronized (this.pageLock)
    {
      for (int i = bookmark; i < this.pageLinks.size(); i++) {
        this.pageLinks.remove(i);
      }
    }
  }

  public boolean isToBeCrawled(String link)
  {
    boolean toBeCrawled = !isAttempted(link);
    if (!toBeCrawled) {
      XLogger.getInstance().log(Level.FINE, "Already attempted: {0}", getClass(), link);
    }
    
    XLogger.getInstance().log(Level.FINER, "To be crawled: {0}, Link: {1}", getClass(), Boolean.valueOf(toBeCrawled), link);
    

    return toBeCrawled;
  }
  
  protected boolean isAttempted(String link) {
    return getAttempted().contains(link);
  }
  
  protected synchronized void waitBeforeNextBatch(long interval)
  {
    try
    {
      if (this.reconnectAfterExceptionFilter != null) {
        this.reconnectAfterExceptionFilter.reset();
      }
      
      if (interval > 0L)
      {
        long freeMemory = Runtime.getRuntime().freeMemory();
        XLogger.getInstance().log(Level.FINER, "Waiting for {0} milliseconds, free memory: {1}", getClass(), Long.valueOf(interval), Long.valueOf(freeMemory));
        

        wait(interval);
        
        XLogger.getInstance().log(Level.FINE, "Done waiting for {0} milliseconds, memory saved: {1}", getClass(), Long.valueOf(interval), Long.valueOf(freeMemory - Runtime.getRuntime().freeMemory()));
      }
    }
    catch (InterruptedException e)
    {
      XLogger.getInstance().log(Level.WARNING, "", getClass(), e);
    } finally {
      notifyAll();
    }
  }
  
  private ConnectionMonitor newConnectionMonitor()
  {
    return new ConnectionMonitor()
    {
      public void preConnect(HttpURLConnection connection) throws ParserException {
        XLogger.getInstance().log(Level.FINER, "@preConnect. Connection: {0}", getClass(), connection);
        
        addCookies(connection, URLParser.this.cookies);
      }
      
      public void addCookies(URLConnection connection, List<String> cookies) {
        if ((cookies == null) || (cookies.isEmpty())) { return;
        }
        for (String cookie : cookies)
        {

          String str = cookie.split(";", 2)[0];
          XLogger.getInstance().log(Level.FINER, "Adding cookie: {0}", getClass(), str);
          connection.addRequestProperty("Cookie", str);
        }
      }
      
      public void postConnect(HttpURLConnection connection) throws ParserException
      {}
    };
  }
  
  public int getParsePos() {
    return this.parsePos;
  }
  
  public Set<String> getAttempted() {
    return this.attempted;
  }
  
  public Set<String> getFailed() {
    return this.failed;
  }
  

  public boolean isRemoveParsedUrls()
  {
    return this.removeParsedUrls;
  }
  
  public void setRemoveParsedUrls(boolean b) {
    this.removeParsedUrls = b;
  }
  
  public Parser getParser() {
    return this.parser;
  }
  
  public void setParser(Parser parser) {
    this.parser = parser;
  }
  
  public List<String> getPageLinks() {
    return this.pageLinks;
  }
  
  public void setPageLinks(List<String> pageLinks) {
    this.pageLinks = Collections.synchronizedList(pageLinks);
  }
  
  public long getBatchInterval() {
    return this.batchInterval;
  }
  
  public void setBatchInterval(long batchInterval) {
    this.batchInterval = batchInterval;
  }
  
  public int getBatchSize() {
    return this.batchSize;
  }
  
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }
  
  public RetryConnectionFilter getConnectionMonitor() {
    return this.reconnectAfterExceptionFilter;
  }
  
  public void setConnectionMonitor(RetryConnectionFilter connectionMonitor) {
    this.reconnectAfterExceptionFilter = connectionMonitor;
  }
  
  public Formatter<String> getFormatter() {
    return this.formatter;
  }
  
  public void setFormatter(Formatter<String> urlFormatter) {
    this.formatter = urlFormatter;
  }
  
  public List<String> getCookies() {
    return this.cookies;
  }
  
  public void setCookies(List<String> cookies) {
    this.cookies = cookies;
  }
  



  public int getParseLimit()
  {
    return this.parseLimit;
  }
  



  public void setParseLimit(int limit)
  {
    this.parseLimit = limit;
  }
  
  public long getStartTime() {
    return this.startTime;
  }
  
  public String getTaskName()
  {
    return getClass().getName();
  }
  
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    print(builder);
    return builder.toString();
  }
  
  public void print(StringBuilder builder) {
    builder.append(getTaskName());
    builder.append(", started: ").append(this.started);
    builder.append(", stopInitiated: ").append(this.stopInitiated);
    builder.append(", stopped: ").append(this.stopped);
    builder.append(", ParsePos: ").append(this.parsePos);
    builder.append(", Urls Left: ").append(this.pageLinks == null ? null : Integer.valueOf(this.pageLinks.size() - this.parsePos));
    builder.append(", Attempted: ").append(this.attempted == null ? null : Integer.valueOf(this.attempted.size()));
    builder.append(", Failed: ").append(this.failed == null ? null : Integer.valueOf(this.failed.size()));
  }
}
