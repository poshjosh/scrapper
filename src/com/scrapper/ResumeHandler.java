package com.scrapper;

import java.util.List;
import com.bc.dom.HtmlPageDom;

public abstract interface ResumeHandler<T>
{
  public abstract List<String> getAllPendingUrls(List<String> paramList);
  
  public abstract void updateStatus(HtmlPageDom paramPageNodes);
  
  public abstract boolean isInDatabase(String paramString);
  
  public abstract T saveIfNotExists(String paramString);
}
