package com.scrapper.search;

import com.bc.util.XLogger;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * @(#)MappingsURLProducer.java   11-Apr-2014 23:13:04
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
public class MappingsURLProducer 
        extends DefaultURLProducer implements HasUrlSubs {
    
    private String defaultMapping;
    
    private List<String> urlSubs;
    
    public MappingsURLProducer() { 
        this.setFormatSlashes(true);
    }
    
    public MappingsURLProducer(JsonConfig config) {
        if(!DefaultURLProducer.isUseMappings(config)) {
            throw new UnsupportedOperationException("Rather create an instance of: "+DefaultURLProducer.class.getName());
        } 
        this.setFormatSlashes(true);
        MappingsURLProducer.this.init(config);
    }
    
    @Override
    protected void init(JsonConfig config) {
        super.init(config);
        this.defaultMapping = config.getString("url", "search", "defaultMapping");
    }
    
    protected String getDefaultUrlSub() {
        return this.defaultMapping;
    }
    
    @Override
    public List<String> getSearchURLs(CapturerContext context, String tableName, 
            Map parameters, String searchTerm) {

        JsonConfig config = context.getConfig();
        
        String baseUrl = this.getBaseUrl(config);
        
        if(baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length()-1);
        }
        
        this.urlSubs = this.getSubs(tableName, parameters, config);

        ArrayList<String> output = new ArrayList<String>();
        
        StringBuilder appendTo = new StringBuilder();

        for(Object sub:urlSubs){
            
            this.appendURL(baseUrl, sub.toString(), parameters, searchTerm, appendTo);
            
            output.add(appendTo.toString());
            
            appendTo.setLength(0);
        }
XLogger.getInstance().log(Level.FINE, "URLs: {0}, {1}", this.getClass(), output.size(), output);        
        return output;
    }
    
    protected List getSubs(String tableName, Map parameters, JsonConfig config) {
        
        List subs = null;
        
        // Sample map entry for url.mappings.tables
        //  '/en/fashion--3343/'='fashion'
        //
        List tableSubs = this.getSubs(Config.Extractor.table.name(), tableName, config);
        
        Object typeId = parameters == null ? null : parameters.get(Config.Extractor.type.name());
        
XLogger.getInstance().log(Level.FINEST, "{0}={1}", this.getClass(), Config.Extractor.type.name(), typeId);        

        if(typeId != null) {
            
            // Sample map entry for url.mappings.type
            //  '/en/womens-clothes--3344/'='1'
            //
            List typeSubs = this.getSubs(Config.Extractor.type.name(), typeId, config);
            
            if(typeSubs != null && tableSubs != null) {
                
                Iterator iter = typeSubs.iterator();
                
                while(iter.hasNext()) {
                    
                    if(!tableSubs.contains(iter.next())) {
                        
                        iter.remove();
                    }
                }
XLogger.getInstance().log(Level.FINER, "After format URL parts: {0}", this.getClass(), typeSubs);
                if(!typeSubs.isEmpty()) {
                    subs = typeSubs;
                }
            }
        }
        
        if(subs == null) {
            if(tableSubs != null) {
                subs = tableSubs;
            }else{
                if(this.getDefaultUrlSub() != null) {
                    subs = new ArrayList();
                    subs.add(this.getDefaultUrlSub());
                }
            }
        }
        
        return subs;
    }

    protected List getSubs(String name, Object value, JsonConfig config) {
        
XLogger.getInstance().log(Level.FINER, "To find: [{0}={1}]", this.getClass(), name, value);        

        ArrayList subs = null;
        
        Map tableMappings = config.getMap(Config.Site.url, "mappings", name);

XLogger.getInstance().log(Level.FINEST, "url.mappings.{0}={1}", 
this.getClass(), name, tableMappings);

        for(Object entryObj:tableMappings.entrySet()) {
            
            Entry entry = (Entry)entryObj;
            
            // We use strings on both sides
            //
            if(entry.getValue().toString().equals(value.toString())) {
        
                if(subs == null) {
                    subs = new ArrayList();
                }
XLogger.getInstance().log(Level.FINER, "Adding: {0}", 
this.getClass(), entry.getKey());
             
                subs.add(entry.getKey());
            }
        }

XLogger.getInstance().log(Level.FINER, "URL parts: {0}", 
this.getClass(), subs);
        
        return subs;
    }
    
    @Override
    public List<String> getUrlSubs() {
        return urlSubs;
    }
}
