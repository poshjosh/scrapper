package com.scrapper.search;

import com.scrapper.context.CapturerContext;
import java.util.List;
import java.util.Map;

public abstract interface URLProducer
{
  public abstract List<String> getCategoryURLs(CapturerContext paramCapturerContext, String paramString);
  
  public abstract List<String> getSearchURLs(CapturerContext paramCapturerContext, String paramString1, Map paramMap, String paramString2);
}
