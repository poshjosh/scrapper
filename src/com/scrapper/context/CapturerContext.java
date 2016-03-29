package com.scrapper.context;

import com.bc.jpa.fk.Keywords;
import com.bc.json.config.JsonConfig;
import com.bc.webdatex.extractor.AttributesExtractor;
import com.scrapper.Filter;
import com.scrapper.Formatter;
import com.scrapper.extractor.DataExtractor;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import com.scrapper.search.URLProducer;
import java.util.Map;
import org.htmlparser.NodeFilter;

public abstract interface CapturerContext
{
  public abstract CapturerSettings getSettings();
  
  public abstract Keywords getKeywords();
  
  public abstract AttributesExtractor getAttributesExtractor(String paramString);
  
  public abstract void setConfig(JsonConfig paramJsonConfig);
  
  public abstract JsonConfig getConfig();
  
  public abstract MultipleNodesExtractorIx getExtractor();
  
  public abstract NodeFilter getFilter();
  
  public abstract Filter<String> getCaptureUrlFilter();
  
  public abstract Filter<String> getScrappUrlFilter();
  
  public abstract Formatter<Map<String, Object>> getFormatter();
  
  public abstract Formatter<String> getUrlFormatter();
  
  public abstract DataExtractor<String> getUrlDataExtractor();
  
  public abstract boolean isUrlProducing();
  
  public abstract URLProducer getUrlProducer();
}
