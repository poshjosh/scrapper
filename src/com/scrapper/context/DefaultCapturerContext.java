package com.scrapper.context;

import com.bc.jpa.fk.Keywords;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.bc.webdatex.extractor.node.AttributesExtractor;
import com.bc.webdatex.extractor.node.AttributesExtractorImpl;
import com.bc.webdatex.filter.Filter;
import com.bc.webdatex.formatter.Formatter;
import com.scrapper.config.Config;
import com.bc.webdatex.extractor.DataExtractor;
import com.scrapper.extractor.MappingsExtractor;
import com.scrapper.extractor.MultipleNodesExtractor;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import com.scrapper.filter.CaptureUrlFilter;
import com.scrapper.filter.DefaultBoundsFilter;
import com.scrapper.filter.ScrappUrlFilter;
import com.scrapper.formatter.DefaultFormatter;
import com.scrapper.search.DefaultURLProducer;
import com.scrapper.search.MappingsURLProducer;
import com.scrapper.search.URLProducer;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;









public class DefaultCapturerContext
  implements CapturerContext, Serializable
{
  private JsonConfig config;
  private CapturerSettings _settings;
  
  public DefaultCapturerContext()
  {
    this(null);
  }
  
  public DefaultCapturerContext(JsonConfig config) {
    this.config = config;
  }
  

  public CapturerSettings getSettings()
  {
    if (this._settings == null) {
      this._settings = new DefaultCapturerSettings(getConfig());
    }
    return this._settings;
  }
  
  public boolean isUrlProducing()
  {
    return isUrlProducing(getConfig());
  }
  
  public static boolean isUrlProducing(JsonConfig config) {
    if (!DefaultURLProducer.accept(config)) {} return config.getString(new Object[] { Config.Extractor.searchUrlProducerClassName }) != null;
  }
  

  public URLProducer getUrlProducer()
  {
    return getUrlProducer(getConfig());
  }
  
  public URLProducer getUrlProducer(JsonConfig config)
  {
    URLProducer urlProducer = (URLProducer)loadInstance(config, Config.Extractor.searchUrlProducerClassName.name());
    

    if ((urlProducer == null) && (DefaultURLProducer.accept(getConfig()))) {
      if (DefaultURLProducer.isUseMappings(getConfig())) {
        urlProducer = new MappingsURLProducer(getConfig());
      } else {
        urlProducer = new DefaultURLProducer(getConfig());
      }
    }
    
    return urlProducer;
  }
  
  public AttributesExtractor getAttributesExtractor(String propertyKey)
  {
    return getAttributesExtractor(getConfig(), propertyKey);
  }
  
  public AttributesExtractor getAttributesExtractor(JsonConfig config, String propertyKey)
  {
    String[] toExtract = getArray(getConfig(), propertyKey, Config.Extractor.attributesToExtract);
    
    if (toExtract == null) {
      return null;
    }
    
    AttributesExtractor attributesExtractor = (AttributesExtractor)loadInstance(config, "attributesExtractor");
    

    if (attributesExtractor == null)
    {
      AttributesExtractorImpl ae = new AttributesExtractorImpl();
      ae.setId(propertyKey);
      ae.setAttributesToExtract(getSettings().getAttributesToExtract(propertyKey));
      attributesExtractor = ae;
    }
    
    return attributesExtractor;
  }
  
  public void setConfig(JsonConfig config)
  {
    this.config = config;
  }
  
  public JsonConfig getConfig()
  {
    return this.config;
  }
  
  public Keywords getKeywords()
  {
    return null;
  }
  
  public DataExtractor<String> getUrlDataExtractor()
  {
    return getUrlDataExtractor(getConfig());
  }
  
  public DataExtractor<String> getUrlDataExtractor(JsonConfig config)
  {
    DataExtractor<String> urlDataExtractor = (DataExtractor)loadInstance(config, "urlDataExtractor");
    

    if (urlDataExtractor == null)
    {

      urlDataExtractor = MappingsExtractor.getInstance(MappingsExtractor.Type.url.name(), config);
    }
    
    return urlDataExtractor;
  }
  
  public Filter<String> getCaptureUrlFilter()
  {
    return getCaptureUrlFilter(getConfig());
  }
  
  public Filter<String> getCaptureUrlFilter(JsonConfig config) {
    Filter<String> captureUrlFilter = (Filter)loadInstance(config, "captureUrlFilter");
    
    if (captureUrlFilter == null)
    {

      captureUrlFilter = new CaptureUrlFilter(getConfig());
    }
    return captureUrlFilter;
  }
  
  public MultipleNodesExtractorIx getExtractor()
  {
    return getExtractor(getConfig());
  }
  
  public MultipleNodesExtractorIx getExtractor(JsonConfig config) {
      
    MultipleNodesExtractorIx extractor = (MultipleNodesExtractorIx)loadInstance(config, "extractor");
    
    if (extractor == null)
    {
      extractor = new MultipleNodesExtractor(this);
    }
    return extractor;
  }
  
  public NodeFilter getFilter()
  {
    return getFilter(getConfig());
  }
  


  public NodeFilter getFilter(JsonConfig config)
  {
    String key = config.getString(new Object[] { "parentNode", "value" });
    
    if (key == null) {
      return null;
    }
    
    NodeFilter filter = (NodeFilter)loadInstance(getConfig(), "filter");
    
    if (filter == null)
    {
      filter = new DefaultBoundsFilter(getConfig());
    }
    return filter;
  }
  
  public Formatter<Map<String, Object>> getFormatter()
  {
    return getFormatter(this);
  }
  
  public Formatter<Map<String, Object>> getFormatter(CapturerContext context) {
    Formatter<Map<String, Object>> formatter = (Formatter)loadInstance(context.getConfig(), "formatter");
    

    if (formatter == null)
    {
      formatter = new DefaultFormatter(context);
    }
    return formatter;
  }
  
  public Filter<String> getScrappUrlFilter()
  {
    return getScrappUrlFilter(getConfig());
  }
  
  public Filter<String> getScrappUrlFilter(JsonConfig config) {
    Filter<String> scrappUrlFilter = (Filter)loadInstance(config, "scrappUrlFilter");
    
    if (scrappUrlFilter == null)
    {

      scrappUrlFilter = new ScrappUrlFilter(getConfig());
    }
    return scrappUrlFilter;
  }
  
  public Formatter<String> getUrlFormatter()
  {
    return getUrlFormatter(getConfig());
  }
  
  public Formatter<String> getUrlFormatter(JsonConfig config) {
    Formatter<String> urlFormatter = (Formatter)loadInstance(config, "urlFormatter");
    

    return urlFormatter;
  }
  
  private Object loadInstance(JsonConfig config, String propertyName)
  {
    if ((config == null) || (propertyName == null)) {
      throw new NullPointerException();
    }
    
    String className = config.getString(new Object[] { propertyName });
    
    if (className == null) {
      return null;
    }
    
    Object output = newInstance(className, null, null);
    if (output == null) {
      output = newInstance(className, JsonConfig.class, config);
      if (output == null) {
        output = newInstance(className, getClass(), this);
      }
    }
    return output;
  }
  
  private Object newInstance(String className, Class<?> parameterType, Object initarg)
  {
    if (className == null) {
      return null;
    }
    
    Object output = null;
    try {
      if (parameterType == null) {
        output = Class.forName(className).getConstructor(new Class[0]).newInstance(new Object[0]);
      } else {
        output = Class.forName(className).getConstructor(new Class[] { parameterType }).newInstance(new Object[] { initarg });
      }
    } catch (ClassNotFoundException|InstantiationException|IllegalAccessException|NoSuchMethodException|InvocationTargetException e) {
      XLogger.getInstance().log(Level.WARNING, "{0}", getClass(), e.toString());
    }
    return output;
  }
  


  private String[] getArray(JsonConfig config, String prefix, Object key)
  {
    List val = config.getList(new Object[] { prefix, key });
    
    if (val == null)
    {

      val = config.getList(new Object[] { key });
    }
    
    return val == null ? null : (String[])val.toArray(new String[0]);
  }
}
