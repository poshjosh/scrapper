package com.scrapper.filter;

import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @(#)CaptureUrlFilter.java   25-Nov-2013 21:35:03
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
public class CaptureUrlFilter extends DefaultUrlFilter {
    
    public CaptureUrlFilter(JsonConfig config) {
 
        this.setId("captureUrlFilter");

        Object [] arr = config.getArray(Config.Extractor.captureUrlFilter_required);
        if(arr != null && arr.length > 0) {
            this.setRequired(Arrays.copyOf(arr, arr.length, String[].class));
        }
        
        arr = config.getArray(Config.Extractor.captureUrlFilter_unwanted);
        if(arr != null && arr.length > 0) {
            this.setUnwanted(Arrays.copyOf(arr, arr.length, String[].class));
        }    

XLogger.getInstance().log(Level.FINE, 
"Text::\nRequired: {0}\nUnwanted: {1}", this.getClass(), 
this.getRequired()==null?null:Arrays.toString(this.getRequired()),
this.getUnwanted()==null?null:Arrays.toString(this.getUnwanted()));        

        String regex = config.getString(Config.Extractor.captureUrlFilter_requiredRegex);
        
        if(regex != null && !regex.trim().isEmpty()) {
            this.setRequiredPattern(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
        
        regex = config.getString(Config.Extractor.captureUrlFilter_unwantedRegex);
        if(regex != null && !regex.trim().isEmpty()) {
            this.setUnwantedPattern(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
XLogger.getInstance().log(Level.FINE, 
"Regex::\nRequired: {0}\nUnwanted: {1}", this.getClass(), 
this.getRequiredPattern(), this.getUnwantedPattern());        
    }
}
