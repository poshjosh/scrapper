package com.scrapper;

import com.bc.webdatex.URLParser;
import com.bc.util.XLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import com.bc.webdatex.nodedata.Dom;

public class ResumableUrlParser<E> extends URLParser<E> implements Resumable {

  private final boolean resumable;
  
  private final boolean toResume;
  
  private ResumeHandler resumeHandler;
  
  public ResumableUrlParser() {
    this(new ArrayList(), true, false);
  }

  public ResumableUrlParser(List<String> urls) {
    this(urls, true, false);
  }
  
  public ResumableUrlParser(List<String> urls, boolean resumable, boolean toResume) {
      
    super(urls);
    
    XLogger.getInstance().log(Level.FINER, "Creating", getClass());
    
    this.resumable = resumable;
    
    this.toResume = toResume;
    
    if (toResume) {
        
      if (this.resumeHandler != null) {

        List<String> inclusive = this.resumeHandler.getAllPendingUrls(urls);
        
        setPageLinks(Collections.synchronizedList(inclusive));
      }
    }
  }
  
  @Override
  protected void preParse(String url) {
      
    if (!isResumable()) {
      return;
    }
    
    XLogger.getInstance().log(Level.FINER, "Preparse URL: {0}", getClass(), url);
    
    if (this.resumeHandler != null) {
      this.resumeHandler.saveIfNotExists(url);
    }
  }
  
  @Override
  protected void postParse(Dom dom) {
    if (!isResumable()) {
      return;
    }
    
    if (this.resumeHandler != null) {
      this.resumeHandler.updateStatus(dom);
    }
  }
  
  @Override
  protected boolean isAttempted(String link) {
      
    return (super.isAttempted(link)) || ((isToResume()) && (isInDatabase(link)));
  }
  
  protected boolean isInDatabase(String link) {
      
    boolean found = false;
    if (this.resumeHandler != null) {
      found = this.resumeHandler.isInDatabase(link);
    }
    
    return found;
  }
  
  @Override
  public final boolean isToResume() {
    return toResume;
  }
  
  @Override
  public final boolean isResumable() {
    return resumable;
  }
  
  public ResumeHandler getResumeHandler() {
    return this.resumeHandler;
  }
  
  public void setResumeHandler(ResumeHandler resumeHandler) {
    this.resumeHandler = resumeHandler;
  }
}
