package com.scrapper;

import com.bc.util.XLogger;
import com.bc.webdatex.filter.Filter;
import com.scrapper.config.ScrapperConfig;
import com.scrapper.context.CapturerContext;
import com.bc.webdatex.extractor.DataExtractor;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import com.bc.webdatex.locator.TagLocator;
import com.bc.webdatex.nodefilter.NodeVisitingFilter;
import com.bc.webdatex.extractor.node.NodeExtractor;
import com.bc.webdatex.locator.impl.TagLocatorImpl;
import com.bc.webdatex.locator.impl.TransverseNodeMatcherImpl;
import com.bc.dom.HtmlPageDom;

public class Scrapper implements DataExtractor<HtmlPageDom>, Serializable {
    
  private HtmlPageDom source;
  private float lastSuccessfulTolerance;
  private float maxTolerance = 0.3F;
  
  private int scrappCount;
  
  private Set<String> attempted;
  
  private Set<String> failed;
  
  private CapturerContext context;
  
  private transient NodeFilter filter;
  
  private transient Filter<String> urlFilter;
  
  private transient MultipleNodesExtractorIx extractor;
  
  private transient DataExtractor<String> urlDataExtractor;
  
  public Scrapper() {
    this(null);
  }
  
  public Scrapper(CapturerContext context) {
    this.context = context;
    
    this.attempted = new HashSet();
    
    this.failed = new HashSet();
  }
  
  @Override
  public Map extractData(HtmlPageDom page) throws ParserException {
      
    try {
        
      String url = page.getURL();
      






      boolean toBeScrapped = isToBeScrapped(url);
      
      if (!toBeScrapped) {
        return null;
      }
      
      this.attempted.add(url);
      
      if (this.filter == null) {
        this.filter = this.context.getFilter();
      }
      else if ((this.filter instanceof HasBounds)) {
        ((HasBounds)this.filter).reset();
      }
      

      if (this.filter != null) {
        XLogger.getInstance().log(Level.FINEST, "BEFORE filter. Nodes: {0}", getClass(), page.getElements() == null ? null : Integer.valueOf(page.getElements().size()));
        

        page.getElements().keepAllNodesThatMatch(this.filter, true);
        
        XLogger.getInstance().log(Level.FINER, "AFTER filter. Nodes: {0}", getClass(), page.getElements() == null ? null : Integer.valueOf(page.getElements().size()));
      }
      









      if (page.getElements().size() == 0) {
        XLogger.getInstance().log(Level.WARNING, "After filter, found 0 Nodes in page: {0}.", getClass(), page.getURL());
        return null;
      }
      
      if (this.extractor == null) {
        this.extractor = this.context.getExtractor();
      } else {
        this.extractor.reset();
      }
      

      XLogger.getInstance().log(Level.FINE, "Scrapped: {0}, scrapping: {1}", 
              getClass(), this.scrappCount, page.getURL());
      



      updateTolerance(this.extractor, this.extractor.getNodeExtractorIds(), this.lastSuccessfulTolerance);
      
      Map extractedData = this.extractor.extractData(page.getElements());
      
      float tolerance = this.lastSuccessfulTolerance;
      
      XLogger.getInstance().log(Level.FINE, "Tolerance: {0}, extracted: {1}", getClass(), Float.valueOf(this.lastSuccessfulTolerance), extractedData == null ? null : extractedData.keySet());
      




      if (this.extractor != null)
      {
        boolean success = isSuccessfulCompletion(extractedData, this.extractor);
        
        if (!success)
        {
          retryExtractsWithIncreasedTolerance(page, extractedData, tolerance);
          
          success = isSuccessfulCompletion(extractedData, this.extractor);
        }
      }
      
      extractedData = addImagesAndUrlExtracts(url, this.extractor, extractedData);
      
      if ((extractedData != null) && (!extractedData.isEmpty()))
      {
        this.scrappCount += 1;
        
        if (isSuccessfulCompletion(extractedData, this.extractor))
        {
          this.lastSuccessfulTolerance = tolerance;
        }
      }
      else {
        this.failed.add(url);
      }
      
      return extractedData;
    }
    catch (ParserException e)
    {
      XLogger.getInstance().log(Level.WARNING, "Scrapp failed for: {0}. Reason: {1}", getClass(), page.getURL(), e);
      

      return null;
    }
    catch (RuntimeException e)
    {
      throw e;
    }
  }
  
  protected void retryExtractsWithIncreasedTolerance(HtmlPageDom page, Map extractedData, float tolerance)
    throws ParserException
  {
    do
    {
      if (tolerance >= this.maxTolerance) {
        break;
      }
      
      tolerance += 0.1F;
      
      Map moreData = retryExtract(page.getElements(), this.extractor, tolerance);
      

      if (moreData != null)
      {
        extractedData.putAll(moreData);
      }
    } while (!isSuccessfulCompletion(extractedData, this.extractor));
  }
  
  protected boolean isSuccessfulCompletion(Map extractedData, MultipleNodesExtractorIx pageExt) {
    Set<String> columns = null;
    try {
      columns = ((ScrapperConfig)getContext().getConfig()).getColumns(); } catch (ClassCastException ignored) {}
    boolean output;
    if (columns != null) {
      output = extractedData.size() >= columns.size();
    } else {
      output = pageExt.getFailedNodeExtractors().isEmpty();
    }
    XLogger.getInstance().log(Level.FINER, "Successful completion: {0}", getClass(), Boolean.valueOf(output));
    
    return output;
  }
  

  protected Map retryExtract(NodeList nodes, MultipleNodesExtractorIx pageExt, float tolerance)
    throws ParserException
  {
    Set<String> failedIds = pageExt.getFailedNodeExtractors();
    
    Set<String> successIds = pageExt.getSuccessfulNodeExtractors();
    
    XLogger.getInstance().log(Level.FINE, "Success Ids: {0}, Failed Ids: {1}", getClass(), successIds, failedIds);
    



    if (failedIds.isEmpty()) {
      return null;
    }
    
    float oldTolerance = updateTolerance(pageExt, failedIds, tolerance);
    
    try
    {
      Map extractedData;
      if (oldTolerance != -1.0F)
      {
          String id;
        for (Iterator i$ = successIds.iterator(); i$.hasNext();) { 
          id = (String)i$.next();
          pageExt.getExtractor(id).setEnabled(false);
        }
        extractedData = pageExt.extractData(nodes);
        
        XLogger.getInstance().log(Level.FINE, "Tolerance: {0}, extracted: {1}", getClass(), Float.valueOf(tolerance), extractedData == null ? null : extractedData.keySet());
        return extractedData;
      }
      return null;
    }
    finally
    {

      for (String id : pageExt.getNodeExtractorIds()) {
        pageExt.getExtractor(id).setEnabled(true);
      }
      
      updateTolerance(pageExt, failedIds, oldTolerance);
    }
  }
  
  private float updateTolerance(MultipleNodesExtractorIx pageExt, Set<String> ids, float tolerance)
    throws ParserException
  {
    float oldTolerance = -1.0F;
    
    if (ids.isEmpty()) {
      return oldTolerance;
    }
    
    for (String id : ids)
    {
      NodeExtractor nodeExt = pageExt.getExtractor(id);
      NodeVisitingFilter visitingFilter = nodeExt.getFilter();
      TagLocator tagLocator = visitingFilter.getTagLocator();
      if (tagLocator != null) {
        tagLocator = new TagLocatorImpl(
                tagLocator.getId(), 
                tagLocator.getTransverse(), 
                new TransverseNodeMatcherImpl(tolerance));
        visitingFilter.setTagLocator(tagLocator);
      }
    }
    
    if(XLogger.getInstance().isLoggable(Level.FINER, this.getClass())) {
      XLogger.getInstance().log(Level.FINER, "Updated tolerance from {0} to {1}", 
      getClass(), oldTolerance, tolerance);
    }

    return oldTolerance;
  }
  
  protected boolean isToBeScrapped(String link)
  {
    XLogger.getInstance().log(Level.FINEST, "@isToScrapped. Link: {0}", getClass(), link);
    

    boolean toBeScrapped = true;
    
    if (this.urlFilter == null) {
      this.urlFilter = this.context.getScrappUrlFilter();
    }
    
    if (this.urlFilter != null)
    {
      toBeScrapped = this.urlFilter.accept(link);
      
      XLogger.getInstance().log(Level.FINER, "URLFilter accepted: {0}, URL: {1}", getClass(), Boolean.valueOf(toBeScrapped), link);
    }
    


    if (toBeScrapped)
    {
      toBeScrapped = !isAttempted(link);
      
      if (!toBeScrapped) {
        XLogger.getInstance().log(Level.FINER, "Scrapp previously attempted {0}, for: {1}", getClass(), Boolean.valueOf(!toBeScrapped), link);
      }
    }
    

    XLogger.getInstance().log(Level.FINE, "To be scrapped: {0}, url: {1}", getClass(), Boolean.valueOf(toBeScrapped), link);
    

    return toBeScrapped;
  }
  
  protected boolean isAttempted(String link)
  {
    boolean isAttempted = this.attempted.contains(link);
    
    return isAttempted;
  }
  
  private Map addImagesAndUrlExtracts(String url, MultipleNodesExtractorIx extractor, Map extractedData)
  {
    if (this.urlDataExtractor == null) {
      this.urlDataExtractor = this.context.getUrlDataExtractor();
    }
    
    if (this.urlDataExtractor != null)
    {
      XLogger.getInstance().log(Level.FINER, "{0}. BEFORE urlExtractor parameters: {1}", getClass(), extractedData);
      




      try
      {
        Map m = this.urlDataExtractor.extractData(url);
        
        if (m != null) {
          extractedData.putAll(m);
        }
      } catch (Exception e) {
        XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
      }
      XLogger.getInstance().log(Level.FINER, "{0}. AFTER urlExtractor parameters:\n{1}", getClass(), extractedData);
    }
    



    if ((extractor instanceof HasImages))
    {
      HasImages hasImages = (HasImages)extractor;
      
      Set<String> imagePaths = hasImages.getImages();
      
      XLogger.getInstance().log(Level.FINE, "Images: {0}", getClass(), imagePaths);
      
      if ((imagePaths != null) && (!imagePaths.isEmpty()))
      {
        int i = 0;
        for (String image : imagePaths) {
          extractedData.put("image" + ++i, image);
        }
        




        imagePaths.clear();
      }
    }
    
    XLogger.getInstance().log(Level.FINER, "Parameter Keys: {0}", getClass(), extractedData.keySet());
    




    XLogger.getInstance().log(Level.FINEST, "Parameters: {0}", getClass(), extractedData);
    

    return extractedData;
  }
  

  public int getScrappCount()
  {
    return this.scrappCount;
  }
  
  public Set<String> getAttempted() {
    return this.attempted;
  }
  
  public Set<String> getFailed() {
    return this.failed;
  }
  
  public float getMaxTolerance() {
    return this.maxTolerance;
  }
  
  public void setMaxTolerance(float maxTolerance) {
    this.maxTolerance = maxTolerance;
  }
  

  public CapturerContext getContext()
  {
    return this.context;
  }
  
  public void setContext(CapturerContext context) {
    this.context = context;
  }
  
  public HtmlPageDom getSource() {
    return this.source;
  }
  
  public void setSource(HtmlPageDom source) {
    this.source = source;
  }
  
  public String getTaskName() {
    return Scrapper.class.getName() + (this.context == null ? "" : new StringBuilder().append("#").append(this.context.getConfig().getName()).toString());
  }
  
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    print(builder);
    return builder.toString();
  }
  
  public void print(StringBuilder builder) {
    builder.append(getTaskName());
    builder.append(", Attempted: ").append(this.attempted == null ? null : Integer.valueOf(this.attempted.size()));
    builder.append(", Failed: ").append(this.failed == null ? null : Integer.valueOf(this.failed.size()));
    builder.append(", Scrapp count: ").append(this.scrappCount);
  }
}
