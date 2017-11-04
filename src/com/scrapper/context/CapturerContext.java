package com.scrapper.context;

import com.bc.webdatex.extractor.node.NodeExtractorConfig;
import com.bc.jpa.fk.Keywords;
import com.bc.json.config.JsonConfig;
import com.bc.webdatex.extractor.node.AttributesExtractor;
import com.bc.webdatex.filter.Filter;
import com.bc.webdatex.formatter.Formatter;
import com.bc.webdatex.extractor.DataExtractor;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import com.scrapper.search.URLProducer;
import java.util.Map;
import org.htmlparser.NodeFilter;

public abstract interface CapturerContext
{
  public abstract NodeExtractorConfig getNodeExtractorConfig();
  
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
