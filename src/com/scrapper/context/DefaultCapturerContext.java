package com.scrapper.context;

import com.bc.manager.Filter;
import com.bc.manager.Formatter;
import com.bc.util.XLogger;
import com.bc.jpa.fk.Keywords;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import com.scrapper.filter.CaptureUrlFilter;
import com.scrapper.filter.DefaultBoundsFilter;
import com.scrapper.filter.FilterFactory;
import com.scrapper.filter.ScrappUrlFilter;
import com.scrapper.formatter.DefaultFormatter;
import com.scrapper.extractor.DataExtractor;
import com.scrapper.extractor.DefaultAttributesExtractor;
import com.scrapper.extractor.AttributesExtractor;
import com.scrapper.extractor.MultipleNodesExtractor;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import com.scrapper.extractor.MappingsExtractor;
import com.scrapper.search.MappingsURLProducer;
import com.scrapper.search.DefaultURLProducer;
import com.scrapper.search.URLProducer;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;

/**
 * @(#)DefaultCapturerContext.java   07-Dec-2013 16:56:35
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class DefaultCapturerContext implements CapturerContext, Serializable {

    private JsonConfig config;
    
    public DefaultCapturerContext() { 
        this(null);
    }
    
    public DefaultCapturerContext(JsonConfig config) { 
        this.config = config;
    }
    
    private CapturerSettings _settings;
    @Override
    public CapturerSettings getSettings() {
        if(_settings == null) {
            _settings = new DefaultCapturerSettings(this.getConfig()); 
        }
        return _settings;
    }
    
    @Override
    public boolean isUrlProducing() {
        return isUrlProducing(getConfig());
    }
    
    public static boolean isUrlProducing(JsonConfig config) {
        return DefaultURLProducer.accept(config) || 
              config.getString(Config.Extractor.searchUrlProducerClassName) != null;
    }

    @Override
    public URLProducer getUrlProducer() {
        return this.getUrlProducer(getConfig());
    }

    public URLProducer getUrlProducer(JsonConfig config) {
        
        URLProducer urlProducer = (URLProducer)this.loadInstance(
                config, Config.Extractor.searchUrlProducerClassName.name());

        if(urlProducer == null && DefaultURLProducer.accept(getConfig())) {
            if(DefaultURLProducer.isUseMappings(getConfig())) {
                urlProducer = new MappingsURLProducer(getConfig());
            }else{
                urlProducer = new DefaultURLProducer(getConfig());
            }
        }
        
        return urlProducer;
    }
    
    @Override
    public AttributesExtractor getAttributesExtractor(String propertyKey) {
        return this.getAttributesExtractor(getConfig(), propertyKey);
    }
    
    public AttributesExtractor getAttributesExtractor(JsonConfig config, String propertyKey) {
        
        String [] toExtract = getArray(getConfig(), propertyKey, Config.Extractor.attributesToExtract); 

        if(toExtract == null) {
            return null;
        }    

        AttributesExtractor attributesExtractor = (AttributesExtractor)this.loadInstance(
                config, "attributesExtractor");

        if(attributesExtractor == null) {
            // Default
            // The string inputs are gotten from config propery files
            attributesExtractor = DefaultAttributesExtractor.getInstance(getConfig(), propertyKey);
        }
        
        return attributesExtractor;
    }
    
    @Override
    public void setConfig(JsonConfig config) {
        this.config = config;
    }
    
    @Override
    public JsonConfig getConfig() {
        return config;
    }

    @Override
    public Keywords getKeywords() {
        return null;
    }
    
    @Override
    public DataExtractor<String> getUrlDataExtractor() {
        return this.getUrlDataExtractor(getConfig());
    }
    
    public DataExtractor<String> getUrlDataExtractor(JsonConfig config) {
        
        DataExtractor<String> urlDataExtractor = (DataExtractor<String>)this.loadInstance(
                config, "urlDataExtractor");

        if(urlDataExtractor == null) {
            // Default. May be null
            // 
            urlDataExtractor = MappingsExtractor.getInstance(MappingsExtractor.Type.url.name(), config);
        }
        
        return urlDataExtractor;
    }
    
    private Filter<String> _cuf;
    @Override
    public Filter<String> getCaptureUrlFilter() {
        if(_cuf == null) {
            _cuf = createCaptureUrlFilter(getConfig());
        }
        return _cuf;
    }
    
    public Filter<String> createCaptureUrlFilter(JsonConfig config) {
        Filter<String> captureUrlFilter = (Filter<String>)this.loadInstance(
                config, "captureUrlFilter");
        if(captureUrlFilter == null) {
            // Default
            // The string inputs are gotten from config propery files
            captureUrlFilter = new CaptureUrlFilter(getConfig());
        }
        return captureUrlFilter;
    }
    
    @Override
    public MultipleNodesExtractorIx getExtractor() {
        return this.getExtractor(getConfig());
    }
    
    public MultipleNodesExtractorIx getExtractor(JsonConfig config) {
        MultipleNodesExtractorIx extractor = (MultipleNodesExtractorIx)this.loadInstance(
                config, "extractor");
        if(extractor == null) {
            // Default
            extractor = new MultipleNodesExtractor(this);
        }
        return extractor;
    }

    @Override
    public NodeFilter getFilter() {
        return this.getFilter(getConfig());
    }
    
    public NodeFilter getFilter(JsonConfig config) {

        // We only create a filter if parent node is specified
        //
        String key = config.getString(FilterFactory.PARENT, "value");

        if(key == null) {
            return null;
        }

        NodeFilter filter = (NodeFilter)this.loadInstance(getConfig(), "filter");

        if(filter == null) {
            // Default
            filter = new DefaultBoundsFilter(getConfig());
        }
        return filter;
    }

    @Override
    public Formatter<Map<String, Object>> getFormatter() {
        return this.getFormatter(this);
    }
    
    public Formatter<Map<String, Object>> getFormatter(CapturerContext context) {
        Formatter<Map<String, Object>> formatter = 
                (Formatter<Map<String, Object>>)this.loadInstance(
                context.getConfig(), "formatter");
        if(formatter == null) {
            // Default
            formatter = new DefaultFormatter(context);
        }
        return formatter;
    }

    private Filter<String> _suf;
    @Override
    public Filter<String> getScrappUrlFilter() {
        if(_suf == null) {
            _suf = this.createScrappUrlFilter(getConfig());
        }
        return _suf;
    }
    
    public Filter<String> createScrappUrlFilter(JsonConfig config) {
        Filter<String> scrappUrlFilter = (Filter<String>)this.loadInstance(
                config, "scrappUrlFilter");
        if(scrappUrlFilter == null) {
            // Default
            // The string inputs are gotten from config propery files
            scrappUrlFilter = new ScrappUrlFilter(getConfig());
        }
        return scrappUrlFilter;
    }

    @Override
    public Formatter<String> getUrlFormatter() {
        return this.getUrlFormatter(getConfig());
    }
    
    public Formatter<String> getUrlFormatter(JsonConfig config) {
        Formatter<String> urlFormatter = (Formatter<String>)this.loadInstance(
                config, "urlFormatter");
        // No default
        return urlFormatter;
    }

    private Object loadInstance(JsonConfig config, String propertyName) {

        if(config == null || propertyName == null) {
            throw new NullPointerException();
        } 
        
        String className = config.getString(propertyName);
        
        if(className == null) {
            return null;
        }

        Object output = this.newInstance(className, null, null);
        if(output == null) {
            output = this.newInstance(className, JsonConfig.class, config);
            if(output == null) {
                output = this.newInstance(className, this.getClass(), this); 
            }
        }
        return output;
    }
    
    private Object newInstance(String className, Class<?> parameterType, Object initarg) {

        if(className == null) {
            return null;
        }

        Object output = null;
        try{
            if(parameterType == null) {
                output = Class.forName(className).getConstructor().newInstance();
            }else{
                output = Class.forName(className).getConstructor(parameterType).newInstance(initarg);
            }
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());    
        }
        return output;
    }

    private String [] getArray(JsonConfig config, String prefix, Object key) {
    
        // If there is no value fall back on the default
        //
        List val = config.getList(prefix, key);
        
        if(val == null) {
            
            // Check if a global is available
            val = config.getList(key);
        }
        
        return val == null ? null : (String[])val.toArray(new String[0]);
    }
}
