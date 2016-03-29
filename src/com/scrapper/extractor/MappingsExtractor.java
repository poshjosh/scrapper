package com.scrapper.extractor;

import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.MappingsConfig;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

/**
 * @(#)MappingsExtractor.java   13-Dec-2013 00:58:51
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
public abstract class MappingsExtractor implements DataExtractor<String>, Serializable {

    private boolean caseSensitive;
    
    public static enum Type{targetNode,page,url}
    
    private JsonConfig config;
    
    private String [] columns;
    
    public MappingsExtractor() {  }
    
    protected MappingsExtractor(JsonConfig config) { 
        this.config = config;
        columns = this.initColumns();
    }
    
    public abstract String getId();
    
    public static MappingsExtractor getInstance(final String prefix, JsonConfig config) {
        MappingsExtractor ex = new MappingsExtractor(config) {
            @Override
            public String getId() {
                return prefix;
            }
        };
        if(ex.hasTableKeys() || ex.hasColumnKeys()) {
            return ex;
        }else{
            return null;
        }
    }

    public boolean hasColumnKeys() {
        return initColumns() != null;
    }
    
    public boolean hasTableKeys() {
        Object [] jsonPath = new MappingsConfig(config).getPropertyPath(
                this.getId(), Config.Extractor.table.name());
        Object prop = config.getObject(jsonPath);
        return prop != null;
    }

    private String [] initColumns() {

        Object [] jsonPath = new MappingsConfig(config).getPropertyPath(this.getId(), null);
        
        Set<Object[]> propertyIds = config.getFullPaths(jsonPath);
        
        HashSet<String> cols = new HashSet<>();
        
        for(Object[] path:propertyIds) {
            
            cols.add(path[2].toString());
        }
        
        return cols.isEmpty() ? null : cols.toArray(new String[0]);
    }
    
    @Override
    public Map extractData(String src) {
        
        if(src == null) {
            throw new NullPointerException();
        }
        
        HashMap data = new HashMap();
        
        if(columns != null){
            
            for(String column:columns) {

                this.extractData(src, column, data);
            }
        }
        
        //@todo Note that the word 'table' cannot be a column name
        this.extractData(src, Config.Extractor.table.name(), data);
        
        return data;
    }
    
    private void extractData(String src, String suffix, Map appendTo) {
        
        final Object [] jsonPath = new MappingsConfig(config).getPropertyPath(this.getId(), suffix);
        
        Map<Object, Object> params = config.getMap(jsonPath);
        
        if(params == null) {
            return;
        }

        if(!this.isCaseSensitive()) {
            src = src.toLowerCase();
        }
        
        src = this.format(src);
        
        for(Entry entry:params.entrySet()) {

            // Format: /hand-bags/=1
            String key = this.format(entry.getKey().toString());
            Object val = entry.getValue();
            
            boolean contains = src.contains(!this.isCaseSensitive() ? key.toLowerCase() : key);

XLogger.getInstance().log(Level.FINER, "{0} contains {1}: {2}", 
        this.getClass(), src, key, contains);
            
            if(contains) {
                
                Object value = appendTo.get(suffix);

                boolean put = value == null ? true : this.isReplace(jsonPath);

                if(put) {
                    
XLogger.getInstance().log(Level.FINE, "Extracted {0}={1} from {2}", 
        this.getClass(), suffix, val, src);

                    appendTo.put(suffix, val);
                    
                }

                break;
            }
        }
    }
    
    private String format(String s) {
        final String amp = "&amp;";
        if(s.length()>=amp.length()) {
            s = s.replace("&amp;", "&");
        }
        try{
            s = URLDecoder.decode(s, "UTF-8");
        }catch(UnsupportedEncodingException | RuntimeException ignored) { 
        }
        return s;
    }

    private boolean isReplace(Object [] path) {
        
        path = config.getPath(path, Config.Extractor.replace);
        
        Boolean replace = config.getBoolean(path);
        
        // No value implies. default value=true
        return replace == null ? true : replace;
    }
    
    public String[] getColumns() {
        return columns;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
}

