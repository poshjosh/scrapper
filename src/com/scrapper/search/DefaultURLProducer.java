package com.scrapper.search;

import com.bc.json.config.JsonConfig;
import java.io.Serializable;

/**
 * @(#)DefaultURLProducer.java   11-Apr-2014 22:56:58
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class DefaultURLProducer 
        extends AbstractURLProducer implements Serializable {
    
    private String path;
    private String queryBeforeSearchTerm;
    private String queryAfterSearchTerm;
    
    public DefaultURLProducer() { }
    
    public DefaultURLProducer(JsonConfig config) {
        if(DefaultURLProducer.isUseMappings(config)) {
            throw new UnsupportedOperationException("Rather create an instance of: "+MappingsURLProducer.class.getName());
        }
        DefaultURLProducer.this.init(config);
    }
    
    public static boolean isUseMappings(JsonConfig config) {
        Boolean b = config.getBoolean("url", "search", "useMappings");
        return b != null && b;
    }
    
    public static boolean accept(JsonConfig config) {
        return config.getObject("url", "search") != null;
    }
    
    protected void init(JsonConfig config) {
        if(!DefaultURLProducer.accept(config)) {
            throw new UnsupportedOperationException("Required property not found: url.search");
        }
        this.path = config.getString("url", "search", "path");
        this.queryBeforeSearchTerm = config.getString("url", "search", "queryBeforeSearchTerm");
        this.queryAfterSearchTerm = config.getString("url", "search", "queryAfterSearchTerm");        
    }

    @Override
    protected String getURLPart() {
        return this.path;
    }
    
    @Override
    protected String getQueryBeforeSearchTerm() {
        return this.queryBeforeSearchTerm;
    }

    @Override
    protected String getQueryAfterSearchTerm() {
        return this.queryAfterSearchTerm;
    }
}
