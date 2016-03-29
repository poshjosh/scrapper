package com.scrapper.extractor.attributes;

import java.util.logging.Level;
import org.htmlparser.Attribute;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import com.bc.util.XLogger;
import com.scrapper.extractor.DefaultAttributesExtractor;

/**
 * @(#)KarimuAttributesExtractor.java   20-Feb-2014 23:09:45
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * <code>
 * <b>Given input tag:</b><br/><br/>
 * &lt;img src="http://www.kiramu.ng/uploads/003/353/526/LLyQs6ocQkeKyLwBFSpnxQ_3.jpg" data-large="/uploads/003/353/526/LLyQs6ocQkeKyLwBFSpnxQ_2.jpg" alt="img" /&gt;<br/><br/>
 * <b>Extracts 'http://www.kiramu.ng' + {data-large attribute}:</b><br/><br/>
 * <b>Output:</b><br/><br/>
 * http://www.kiramu.ng/uploads/003/353/526/LLyQs6ocQkeKyLwBFSpnxQ_2.jpg
 * </code>
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class KarimuAttributesExtractor extends DefaultAttributesExtractor {
    
    public KarimuAttributesExtractor() {
        this.setAttributesToExtract(new String[]{"src"});
    }
    
    @Override
    public String extract(Tag tag) {
        
        StringBuilder extract = new StringBuilder();
        
        boolean isImageTag = tag instanceof ImageTag;
        
        for(String name:this.getAttributesToExtract()) {
            
            Attribute attr = tag.getAttributeEx(name);
            
            if(attr == null) {
                continue;
            }
            
            String value = attr.getValue();
            
            if(value == null) {
                continue;
            }

            if(isImageTag && name.equals("src")) {
                
                Attribute data_large = tag.getAttributeEx("data-large");
                
                if(data_large != null) {
                    
                    String data_large_value = data_large.getValue();
                    
                    if(data_large_value != null) {
                        
                        value = "http://www.kiramu.ng" + data_large_value; 
XLogger.getInstance().log(Level.FINER, "Extracting: {0}", this.getClass(), value);                        
                    }
                }
            }
            
            extract.append(value).append(' ');
        }

        return extract.length() == 0 ? null : extract.toString().trim();
    }
}
