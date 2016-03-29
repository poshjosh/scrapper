package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.util.Map;
import org.htmlparser.util.ParserException;












public class ResumableScrapper
  extends Scrapper
  implements Resumable
{
  private ResumeHandler resumeHandler;
  
  public ResumableScrapper() {}
  
  public ResumableScrapper(CapturerContext context)
  {
    super(context);
  }
  
  public Map extractData(PageNodes page)
    throws ParserException
  {
    Map extractedData = super.extractData(page);
    
    if ((extractedData != null) && (!extractedData.isEmpty()) && (isResumable()))
    {
      if (this.resumeHandler != null) {
        this.resumeHandler.updateStatus(page);
      }
    }
    
    return extractedData;
  }
  

  protected boolean isAttempted(String link)
  {
    return (super.isAttempted(link)) || ((isResume()) && (isInDatabase(link)));
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
  
  public boolean isResumable()
  {
    return true;
  }
  
  public boolean isResume()
  {
    return false;
  }
  
  public String getTaskName()
  {
    return ResumableScrapper.class.getName() + "(Scrapper for site: " + getSitename() + ")";
  }
  
  public void print(StringBuilder builder)
  {
    super.print(builder);
    builder.append(", site: ").append(getSitename());
    builder.append(", resume: ").append(isResume());
    builder.append(", resumable: ").append(isResumable());
  }
  
  public ResumeHandler getResumeHandler() {
    return this.resumeHandler;
  }
  
  public void setResumeHandler(ResumeHandler resumeHandler) {
    this.resumeHandler = resumeHandler;
  }
}
