package com.scrapper.search;

import com.bc.util.XLogger;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.Util;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @(#)AbstractURLProducer.java   22-Mar-2014 02:55:53
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
public abstract class AbstractURLProducer 
        implements URLProducer {
    
    private boolean formatSlashes;
    
    protected abstract String getURLPart();
    
    protected abstract String getQueryBeforeSearchTerm();
    
    protected abstract String getQueryAfterSearchTerm();
    
    @Override
    public List<String> getCategoryURLs(CapturerContext context, String tableName) {
        
        JsonConfig config = context.getConfig();
        
        Map typeMappings = config.getMap(Config.Site.url, "mappings", "type");
        
        if(typeMappings == null || typeMappings.isEmpty()) {
            return null;
        }

        String baseUrl = this.getBaseUrl(config);

        if(baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length()-1);
        }
        
        StringBuilder builder = new StringBuilder(baseUrl);
        
        Map tableMappings = null;
        
        if(tableName != null) {
            
            tableMappings = config.getMap(Config.Site.url, "mappings", 
                    Config.Extractor.table.name());

            if(tableMappings == null || tableMappings.isEmpty()) {
XLogger.getInstance().log(Level.WARNING, 
"No table mappings found for config: {0}", 
        this.getClass(), config.getName());            
            }
        }

        ArrayList<String> urls = new ArrayList<String>(typeMappings.size());
        
        for(Object key:typeMappings.keySet()) {
            
            builder.setLength(baseUrl.length());
            
            String sval = key.toString();
            
            if(!this.isTable(tableMappings, tableName, sval)) {
                continue;
            }
            
            this.append(sval, builder);
            
//            if(formatSlashes) {
//                char lastChar = builder.charAt(builder.length()-1);
//                if(lastChar != '/' && !sval.startsWith("/")) {
//                    builder.append('/');
//                }
//            }    
            
//            builder.append(sval);
            
            urls.add(builder.toString());
        }
        
        return urls;
    }
    
    private boolean isTable(Map tableMappings, String tableName, Object categoryString) {
        if(tableName == null) {
            return true;
        }
        if(tableMappings == null || categoryString == null) {
            throw new NullPointerException();
        }
        // Format categoryString=tableName e.g shoes=fashion
        Object categoryTable = tableMappings.get(categoryString);
        
        return tableName.equals(categoryTable);
    }

    @Override
    public List<String> getSearchURLs(CapturerContext context, String tableName, 
            Map parameters, String searchTerm) {

        JsonConfig config = context.getConfig();
        
        String baseUrl = this.getBaseUrl(config);
        
        if(baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length()-1);
        }
        
        ArrayList<String> output = new ArrayList<String>();
        
        StringBuilder appendTo = new StringBuilder();
        
        this.appendURL(baseUrl, null, parameters, searchTerm, appendTo);
            
        output.add(appendTo.toString());

XLogger.getInstance().log(Level.FINE, "URLs: {0}, {1}", this.getClass(), output.size(), output);        
        return output;
    }
    
    protected void appendURL(String baseUrl, Object part, Map parameters, String searchTerm, StringBuilder appendTo) {
        
        appendTo.append(baseUrl);
        
        this.append(this.getURLPart(), appendTo);
        
        if(part != null) {
            this.append(part.toString(), appendTo);
        }
        
        this.appendSearchTerm(parameters, searchTerm, appendTo);
    }
    
    protected void append(String part, StringBuilder appendTo) {
        if(part == null) return;
        if(this.formatSlashes) {
            char lastChar = appendTo.charAt(appendTo.length()-1);
            if(lastChar != '/' && !part.startsWith("/")) {
                appendTo.append('/');
            } 
        }
        appendTo.append(part);
    }
    
    protected void appendSearchTerm(Map parameters, String searchTerm, StringBuilder appendTo) {
        
        String b4 = this.getQueryBeforeSearchTerm();
  
        this.append(b4, appendTo);
//        if(formatSlashes) {
            
//            boolean hasSlash = b4 != null && b4.startsWith("/");

//            if(!hasSlash && appendTo.charAt(appendTo.length()-1) != '/') {
//                appendTo.append('/');
//            }
//        }    
        
//        if(b4 != null) {
//            appendTo.append(b4);
//        }
        
        try{
            searchTerm = URLEncoder.encode(searchTerm, "UTF-8");
        }catch(UnsupportedEncodingException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }catch(RuntimeException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
        
        appendTo.append(searchTerm);
        
        if(this.getQueryAfterSearchTerm() != null) {
            appendTo.append(this.getQueryAfterSearchTerm());
        }
    }
    
    public String getBaseUrl(JsonConfig config) {
        
        String baseUrl = config.getString(Config.Site.url, "value");

        if(baseUrl == null) {
            String s = config.getString(Config.Site.url, "start");
            if(s == null) {
                throw new NullPointerException("Both url.value and url.start cannot be null in config: "+config.getName());
            }else{
                baseUrl = Util.getBaseURL(s);
            }        
        }
XLogger.getInstance().log(Level.FINER, "Base URL: {0}", this.getClass(), baseUrl);        
        return baseUrl;
    }

    public boolean isFormatSlashes() {
        return formatSlashes;
    }

    public void setFormatSlashes(boolean formatSlashes) {
        this.formatSlashes = formatSlashes;
    }
}

