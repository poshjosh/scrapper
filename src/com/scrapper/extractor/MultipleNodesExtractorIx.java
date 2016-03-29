package com.scrapper.extractor;

import com.bc.webdatex.extractor.NodeExtractor;
import java.util.Set;

public abstract interface MultipleNodesExtractorIx
  extends PageExtractorIx
{
  public abstract NodeExtractor createExtractor(String paramString);
  
  public abstract NodeExtractor getExtractor(String paramString);
  
  public abstract Set<String> getFailedNodeExtractors();
  
  public abstract Set<String> getNodeExtractorIds();
  
  public abstract Set<String> getSuccessfulNodeExtractors();
  
  public abstract boolean isSuccessfulCompletion();
  
  public abstract void reset();
}
