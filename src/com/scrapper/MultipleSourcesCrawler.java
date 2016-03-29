package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import com.scrapper.url.ConfigURLPartList;
import com.scrapper.util.PageNodes;
import com.scrapper.util.Util;
import java.util.List;
import java.util.logging.Level;

public class MultipleSourcesCrawler
  extends Crawler
{
  boolean addedIndirectSource;
  private int sourcesIndex;
  private List<String> sources;
  
  public MultipleSourcesCrawler(CapturerContext context, List<String> sources)
  {
    this(context);
    
    setSources(sources);
  }
  
  public MultipleSourcesCrawler(CapturerContext context)
  {
    super(context);
    
    checkListGenerationMode(context.getConfig());
  }
  
  protected void checkListGenerationMode(JsonConfig config)
  {
    ConfigURLPartList serialPart = ConfigURLPartList.getSerialPart(config, "counter");
    
    if (serialPart != null) {
      throw new IllegalArgumentException("Invalid format for property: url.counter.part");
    }
  }

  public boolean hasNext()
  {
    updateBaseUrl(this.sources);
    
    if (hasMoreSources())
    {
      String addedSrc = (String)this.sources.get(this.sourcesIndex);
      
      getPageLinks().add(addedSrc);
      
      this.addedIndirectSource = true;
      
      XLogger.getInstance().log(Level.FINE, "Added indirect source: {0}", getClass(), addedSrc);

    }
    else
    {
      this.addedIndirectSource = false;
    }
    
    return super.hasNext();
  }
  

  public PageNodes next()
  {
    updateBaseUrl(this.sources);
    
    String addedSrc;
    if (this.addedIndirectSource)
    {
      addedSrc = (String)this.sources.get(this.sourcesIndex++);
    }
    else
    {
      addedSrc = null;
    }
    
    PageNodes page = super.next();
    
    if (addedSrc == null)
    {
      return page;
    }
    
    if (page == null) {
      return null;
    }
    
    if (page.getURL().equals(addedSrc)) {
      XLogger.getInstance().log(Level.FINE, "After crawling, ignoring indirect source: {0}", getClass(), addedSrc);
      
      return null;
    }
    

    return page;
  }
  
  public boolean hasMoreSources()
  {
    return (this.sources != null) && (this.sourcesIndex < this.sources.size());
  }
  
  public final void updateBaseUrl(List<String> urls)
  {
    if ((getBaseUrl() == null) && (urls != null) && (!urls.isEmpty()))
    {
      String firstSource = (String)urls.get(0);
      
      if (firstSource != null)
      {
        setBaseUrl(com.bc.util.Util.getBaseURL(firstSource));
      }
    }
  }
  
  public List<String> getSources() {
    return this.sources;
  }
  
  public void setSources(List<String> sources) {
    this.sources = sources;
    updateBaseUrl(sources);
  }
}
