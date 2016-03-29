package com.scrapper.extractor;

import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.htmlparser.Attribute;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import com.bc.util.XLogger;
import java.io.Serializable;

/**
 * @(#)DefaultAttributesExtractor.java   20-Feb-2014 23:08:22
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
public class DefaultAttributesExtractor implements AttributesExtractor, Serializable {

    private String id;
    
    private String [] attributesToExtract;
    
    public DefaultAttributesExtractor() { }

    @Override
    public String extract(Tag tag) {
        
        StringBuilder extract = new StringBuilder();
        
        for(String name:this.attributesToExtract) {
            
            Attribute attr = tag.getAttributeEx(name);
            
            if(attr == null) {
                continue;
            }
            
            String value = attr.getValue();

            if(value == null) {
                continue;
            }
            
            if(tag instanceof ImageTag && name.equals("src")) {
//src=http://www.abc.com/image.jpg was often found to be 
//src=http://1.1.1.1/bmi/www.abc.com/image.jpg
                value = Pattern.compile("\\d\\.\\d\\.\\d\\.\\d/bmi/").matcher(value).replaceFirst("");                
XLogger.getInstance().log(Level.FINER, "Updated image source to: {0}", this.getClass(), value);                
            }
            
            extract.append(value).append(' ');
        }

        return extract.length() == 0 ? null : extract.toString().trim();
    }
    
    public static DefaultAttributesExtractor getInstance(JsonConfig config, String propertyKey) {

        String [] toExtract = getArray(config, propertyKey, Config.Extractor.attributesToExtract); 
        
        if(toExtract == null) {
            return null;
        }else{
            DefaultAttributesExtractor extractor = new DefaultAttributesExtractor();
            extractor.setId(propertyKey);
            extractor.attributesToExtract = toExtract;
XLogger.getInstance().log(Level.FINER, "{0}.attributesToExtract: {1}", 
DefaultAttributesExtractor.class, propertyKey, 
toExtract == null ? null : Arrays.toString(toExtract));
            return extractor;
        }
    }
    
    private static String [] getArray(JsonConfig config, String prefix, Object key) {
    
        // If there is no value fall back on the default
        //
        List val = config.getList(prefix, key);
        
        if(val == null) {
            
            // Check if a global is available
            val = config.getList(key);
        }
        
        return val == null ? null : (String[])val.toArray(new String[0]);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String[] getAttributesToExtract() {
        return attributesToExtract;
    }

    @Override
    public void setAttributesToExtract(String[] attributesToExtract) {
        this.attributesToExtract = attributesToExtract;
    }
}
