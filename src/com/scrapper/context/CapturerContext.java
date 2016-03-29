package com.scrapper.context;

import com.bc.manager.Filter;
import com.bc.manager.Formatter;
import com.bc.jpa.fk.Keywords;
import com.bc.json.config.JsonConfig;
import com.scrapper.extractor.DataExtractor;
import com.scrapper.extractor.AttributesExtractor;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import com.scrapper.search.URLProducer;
import java.util.Map;
import org.htmlparser.NodeFilter;

/**
 * @(#)CapturerContext.java   07-Dec-2013 16:46:02
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
public interface CapturerContext {
    
    CapturerSettings getSettings();

    Keywords getKeywords();

    AttributesExtractor getAttributesExtractor(String propertyKey);
    
    void setConfig(JsonConfig config);
    
    JsonConfig getConfig();
    
    MultipleNodesExtractorIx getExtractor();

    NodeFilter getFilter();

    Filter<String> getCaptureUrlFilter();
    
    Filter<String> getScrappUrlFilter();
    
    Formatter<Map<String, Object>> getFormatter();

    Formatter<String> getUrlFormatter();
    
    DataExtractor<String> getUrlDataExtractor();
    
    boolean isUrlProducing();
    
    URLProducer getUrlProducer();
}
