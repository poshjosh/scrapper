package com.scrapper;

import com.scrapper.util.PageNodes;
import java.util.List;

public abstract interface ResumeHandler<T>
{
  public abstract List<String> getAllPendingUrls(List<String> paramList);
  
  public abstract void updateStatus(PageNodes paramPageNodes);
  
  public abstract boolean isInDatabase(String paramString);
  
  public abstract T saveIfNotExists(String paramString);
}
