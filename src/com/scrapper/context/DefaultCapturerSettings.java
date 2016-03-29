package com.scrapper.context;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;


/**
 * @(#)CapturerSettingsContext.java   08-Oct-2015 22:23:27
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class DefaultCapturerSettings implements Serializable, CapturerSettings {

    private JsonConfig config;
    
    public DefaultCapturerSettings() { 
        this(null);
    }
    
    public DefaultCapturerSettings(JsonConfig config) { 
        this.config = config;
    }

    public JsonConfig getConfig() {
        return config;
    }

    public void setConfig(JsonConfig config) {
        this.config = config;
    }
    
    public String [] getTransverse(String id) {
        return this.getStringArray(id, Config.Extractor.transverse);
    }
    
    @Override
    public String [] getTextToDisableOn(String id) {
        return this.getStringArray(id, Config.Extractor.textToDisableOn);
    }
    
    @Override
    public String [] getTextToReject(String id) {
        return this.getStringArray(id, Config.Extractor.textToReject);
    }

    @Override
    public Boolean isConcatenateMultipleExtracts() {
        Boolean b = this.getConfig().getBoolean(Config.Extractor.append);
        return b == null ? null : b;
    }
    
    @Override
    public Boolean isConcatenateMultipleExtracts(String id) {
        Boolean b = this.getConfig().getBoolean(id, Config.Extractor.append);
        return b == null ? null : b;
    }
    
    @Override
    public String getLineSeparator() {
        return this.getConfig().getString(Config.Extractor.lineSeparator);
    }
    
    @Override
    public String getPartSeparator() {
        return this.getConfig().getString(Config.Extractor.partSeparator);
    }

    @Override
    public String getDefaultTitle() {
        return this.getConfig().getString(Config.Extractor.defaultTitle);
    }
    
    @Override
    public String [] getColumns(String id) {
        
        return this.getStringArray(id, Config.Extractor.columns);
    }
    
    @Override
    public String [] getNodesToRetainAttributes(String id) {
        
        JsonConfig cfg = this.getConfig();
        
        // Init nodesToRetainAttributes
        // The node and global values are joined together
        //
        List defaultNodes = cfg.getList(Config.Extractor.nodesToRetainAttributes);
        List nodes = (List)cfg.getList(id, Config.Extractor.nodesToRetainAttributes);
        
        List<String> list = new ArrayList<>();
        
        if(defaultNodes != null) {
            list.addAll(defaultNodes);
        }
        if(nodes != null) {
            list.addAll(nodes);
        }

        String [] nodesToRetainAttributes = list.toArray(new String[0]);
        
        return nodesToRetainAttributes;
    }
    
    @Override
    public boolean isReplaceNonBreakingSpace(String id) {
        
        // Init replaceNonBreakingSpace
        //
        Boolean defaultVal = this.getConfig().getBoolean(Config.Extractor.replaceNonBreakingSpace);

        boolean replaceNonBreakingSpace;
        if(defaultVal == null) {
            replaceNonBreakingSpace = config.getBoolean(
            id, Config.Extractor.replaceNonBreakingSpace);
        }else{
            replaceNonBreakingSpace = defaultVal;
        }
        return replaceNonBreakingSpace;
    }
    
    @Override
    public String [] getAttributesToAccept(String id) {
        // Init attributesToAccept
        //
        return this.getStringArray(id, Config.Extractor.attributesToAccept);
    }
    
    @Override
    public boolean isExtractAttributes(String id) {
        
        return this.getAttributesToExtract(id) != null;
    }
    
    @Override
    public String [] getAttributesToExtract(String id) {
        
        String [] arr = this.getStringArray(id,  Config.Extractor.attributesToExtract);
        
XLogger.getInstance().log(Level.FINER, "Attributes to extract: {0}", this.getClass(), arr==null?null:Arrays.toString(arr));

        return arr;
    }
    
    @Override
    public String [] getNodeTypesToAccept(String id) {
        
        return this.toLowercaseStringArray(this.getStringArray(id, Config.Extractor.nodeTypesToAccept));
    }

    @Override
    public String [] getNodeTypesToReject(String id) {
        
        return this.toLowercaseStringArray(this.getStringArray(id, Config.Extractor.nodeTypesToReject));
    }

    @Override
    public String [] getNodesToAccept(String id) {
        
        return this.toLowercaseStringArray(this.getStringArray(id, Config.Extractor.nodesToAccept));
    }
    
    @Override
    public String [] getNodeToReject(String id) {
        
        return this.toLowercaseStringArray(this.getStringArray(id, Config.Extractor.nodesToReject));
    }

    private String [] toLowercaseStringArray(Object [] arr) {
        String [] output;
        if(arr == null) {
            output = null;
        }else{
            output = new String[arr.length];
            for(int i=0; i<arr.length; i++) {
                output[i] = arr[i].toString().toLowerCase();
            }
        }
        return output;
    }

    private String [] getStringArray(String first, Object second) {
    
        // If there is no value fall back on the default
        Object [] src = getConfig().getArray(first, second);
        
        if(src == null) {
            
            // Check if a global is available
            src = getConfig().getArray(second);
        }
        
        String [] output;
        if(src != null) {
            output = new String[src.length];
            System.arraycopy(src, 0, output, 0, src.length);
        }else{
            output = null;
        }
        
        return output;
    }
}
