package com.scrapper.util;

import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.htmlparser.Attribute;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;

/**
 * @(#)AttributesManager.java   12-Jul-2014 02:28:44
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
public class AttributesManager {
    
    private boolean useIdExclusively;
    
    public AttributesManager() {
        useIdExclusively = true;
    }
    
    public Object[] updateAttributes(JsonConfig config, String nodeName, Tag selectedTag) {

        Map<String, String> attrPropVals = this.getAttributes(selectedTag);
        
        Object [] attrPropKey;
            
        if(attrPropVals != null && !attrPropVals.isEmpty()) {

            if(this.isUseIdExclusively() && attrPropVals.containsKey("id")) {
                
                // Retain only the id
                //
                String idVal = attrPropVals.get("id");

                attrPropVals.clear();
                attrPropVals.put("id", idVal);
                
                attrPropKey = new Object[]{nodeName, Config.Extractor.attributes};
                
            }else{
            
                if(this.updateUniqueAttributes(attrPropVals)) {
                    attrPropKey = new Object[]{nodeName, Config.Extractor.attributesRegex};
                }else{
                    attrPropKey = new Object[]{nodeName, Config.Extractor.attributes};
                }
            }
            
            config.setObject(attrPropKey, attrPropVals);
            
        }else{
            
            attrPropKey = null;
        }
        
        return attrPropKey;
    }
    
    public Map<String, String> getAttributes(Tag tag) {
        Map<String, String> output = new HashMap<String, String>(){
            @Override
            public String put(String key, String value) {
                if(key == null || value == null) throw new NullPointerException();
                return super.put(key, value);
            }
        };
        List attributes = tag.getAttributesEx();
        for(Object oval:attributes) {
            Attribute attr = (Attribute)oval;
            if(attr.getName() == null || attr.getValue() == null) {
                continue;
            }
            output.put(attr.getName(), attr.getValue());
        }
        return output;
    }
    
    public boolean updateUniqueAttributes(Map<String, String> map) {
        
        Iterator<String> iter = map.keySet().iterator();
        
        HashMap<String, String> replaceme = new HashMap<String, String>();
        
        while(iter.hasNext()) {
            String key = iter.next();
            if(this.isUnique(key)) {
                String newVal = map.get(key).isEmpty() ? ".*?" : ".+?";
                replaceme.put(key, newVal);
            }
        }
        
        if(!replaceme.isEmpty()) {
            map.putAll(replaceme);
            return true;
        }else{
            return false;
        }
    }
    
    public boolean isUnique(String attrKey) {
        
        attrKey = attrKey.toLowerCase();
        
        return attrKey.equals("src") ||
                attrKey.equals("title") ||
                attrKey.equals("alt") ||
                attrKey.equals("width") ||
                attrKey.equals("height") ||
                attrKey.startsWith("on"); // onclick, onload etc
    }

    public void updateAttributesExtractionRequirements(
            JsonConfig props, String targetNode, Tag tag, List cols) {
        
        if(tag instanceof ImageTag || tag instanceof LinkTag) {
            
            props.setObject(
                    new Object[]{targetNode, Config.Extractor.nodeTypesToAccept}, 
                    new Object[]{"tag"});
            
            props.setObject(
                    new Object[]{targetNode, Config.Extractor.nodesToRetainAttributes}, 
                    new Object[]{tag.getTagName()});
            
            String attributeName = this.getDefaultAttributeToExtract(tag);

            props.setObject(
                    new Object[]{targetNode, Config.Extractor.attributesToExtract}, 
                    new Object[]{attributeName});
            
        }else if(cols != null && cols.size() == 1 && cols.contains("description")) {
            // If cols contains description and keywords etc then this does not apply
            props.setObject(
                    new Object[]{targetNode, Config.Extractor.nodeTypesToAccept}, 
                    new Object[]{"tag", "text"});
        }
    }
    
    public String getDefaultAttributeToExtract(Tag tag) {
        String attributeName;
        if( (tag instanceof ImageTag) ) {
            attributeName = "src";
        }else if( (tag instanceof LinkTag) ) {
            attributeName = "href";
        }else{
            throw new UnsupportedOperationException("Expected: <img> or <a> tag found: "+tag.getTagName());
        }
        return attributeName;
    }

    public boolean isUseIdExclusively() {
        return useIdExclusively;
    }

    public void setUseIdExclusively(boolean useIdExclusively) {
        this.useIdExclusively = useIdExclusively;
    }
}
