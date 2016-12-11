package com.scrapper;

import com.scrapper.context.CapturerContext;
import java.util.Map;
import org.htmlparser.util.ParserException;
import com.bc.dom.HtmlPageDom;

public class ResumableScrapper extends Scrapper implements Resumable {
  
  private final boolean toResume;
  
  private final boolean resumable;
  
  private ResumeHandler resumeHandler;
  
  public ResumableScrapper() {
    this(null, true, false);    
  }
  
  public ResumableScrapper(CapturerContext context) {
    this(context, true, false);
  }

  public ResumableScrapper(CapturerContext context, boolean resumable, boolean toResume) {
    super(context);
    this.resumable = resumable;
    this.toResume = toResume;
  }
  
  @Override
  public Map extractData(HtmlPageDom page) throws ParserException {
      
    Map extractedData = super.extractData(page);
    
    if ((extractedData != null) && (!extractedData.isEmpty()) && (isResumable())) {
        
      if (this.resumeHandler != null) {
        this.resumeHandler.updateStatus(page);
      }
    }
    
    return extractedData;
  }
  
  @Override
  protected boolean isAttempted(String link) {
      
    return (super.isAttempted(link)) || ((isToResume()) && (isInDatabase(link)));
  }
  
  protected boolean isInDatabase(String link)
  {
    boolean found = false;
    if (this.resumeHandler != null) {
      found = this.resumeHandler.isInDatabase(link);
    }
    return found;
  }
  
  public String getSitename() {
    return getContext().getConfig().getName();
  }
  
  @Override
  public final boolean isResumable() {
    return resumable;
  }
  
  @Override
  public final boolean isToResume() {
    return toResume;
  }
  
  @Override
  public String getTaskName() {
    return ResumableScrapper.class.getName() + "(Scrapper for site: " + getSitename() + ")";
  }
  
  @Override
  public void print(StringBuilder builder) {
    super.print(builder);
    builder.append(", site: ").append(getSitename());
    builder.append(", resume: ").append(isToResume());
    builder.append(", resumable: ").append(isResumable());
  }
  
  public ResumeHandler getResumeHandler() {
    return this.resumeHandler;
  }
  
  public void setResumeHandler(ResumeHandler resumeHandler) {
    this.resumeHandler = resumeHandler;
  }
}
