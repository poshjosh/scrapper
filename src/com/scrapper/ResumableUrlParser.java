package com.scrapper;

import com.bc.util.XLogger;
import com.scrapper.util.PageNodes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class ResumableUrlParser<E> extends URLParser<E> implements Resumable {
    
  private String sitename;
  private ResumeHandler resumeHandler;
  
  public ResumableUrlParser()
  {
    this(null, new ArrayList());
  }
  
  public ResumableUrlParser(String sitename)
  {
    this(sitename, new ArrayList());
  }
  
  public ResumableUrlParser(String sitename, List<String> urls)
  {
    super(urls);
    XLogger.getInstance().log(Level.FINER, "Creating", getClass());
    
    this.sitename = sitename;
    
    if (isResume())
    {
      if (this.resumeHandler != null)
      {

        List<String> inclusive = this.resumeHandler.getAllPendingUrls(urls);
        
        setPageLinks(Collections.synchronizedList(inclusive));
      }
    }
  }
  

  protected void preParse(String url)
  {
    if (this.sitename == null) {
      throw new NullPointerException("sitename == null");
    }
    
    if (!isResumable()) {
      return;
    }
    
    XLogger.getInstance().log(Level.FINER, "Preparse URL: {0}", getClass(), url);
    
    if (this.resumeHandler != null) {
      this.resumeHandler.saveIfNotExists(url);
    }
  }
  

  protected void postParse(PageNodes page)
  {
    if (!isResumable()) {
      return;
    }
    
    if (this.resumeHandler != null) {
      this.resumeHandler.updateStatus(page);
    }
  }
  

  protected boolean isAttempted(String link)
  {
    return (super.isAttempted(link)) || ((isResume()) && (isInDatabase(link)));
  }
  

  protected boolean isInDatabase(String link)
  {
    if (this.sitename == null) {
      throw new NullPointerException("sitename == null");
    }
    
    boolean found = false;
    if (this.resumeHandler != null) {
      found = this.resumeHandler.isInDatabase(link);
    }
    
    return found;
  }
  




  public boolean isResume()
  {
    return false;
  }
  




  public boolean isResumable()
  {
    return true;
  }
  
  public String getSitename() {
    return this.sitename;
  }
  
  public void setSitename(String sitename) {
    this.sitename = sitename;
  }
  
  public ResumeHandler getResumeHandler() {
    return this.resumeHandler;
  }
  
  public void setResumeHandler(ResumeHandler resumeHandler) {
    this.resumeHandler = resumeHandler;
  }
}
