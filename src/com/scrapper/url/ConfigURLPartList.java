package com.scrapper.url;

import com.bc.util.XLogger;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * @(#)ConfigURLPartList.java   03-Apr-2014 21:17:00
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
public class ConfigURLPartList extends AbstractList implements Serializable {
    
    public static enum ListGenerationMode{none,serial,replacements};

    private int partIndex;
    private String type;
    
    private boolean ascending;
    private int start;
    private int end;
    private String value;
    private Map<String, List> replacements;

    public ConfigURLPartList() { }
    
    public ConfigURLPartList(JsonConfig config, String type, int partIndex) throws UnsupportedOperationException { 
        this.update(config, type, partIndex);
    }
    
    public static boolean accept(JsonConfig config, String type, int partIndex) {
        return config.getMap(Config.Site.url, type, "part"+partIndex) != null;
    }
    
    private void update(JsonConfig config, String type, int partIndex) throws UnsupportedOperationException { 
        if(config == null || type == null) {
            throw new NullPointerException();
        }
        if(!ConfigURLPartList.accept(config, type, partIndex)) {
            throw new UnsupportedOperationException("url.counter.part"+partIndex+" == null");
        }
        Map partValues = config.getMap(Config.Site.url, type, "part"+partIndex);
        Object oval = partValues.get("ascending");
        ascending = oval == null ? true : Boolean.parseBoolean(oval.toString());
        oval = partValues.get("start");
        start = oval == null ? 0 : Integer.parseInt(oval.toString());
        oval = partValues.get("end");
        end = oval == null ? -1 : Integer.parseInt(oval.toString());
        oval = partValues.get("value");
        value = oval == null ? null : oval.toString();
        oval = partValues.get("replacements");
        // we use a copy
        replacements = oval == null ? null : new HashMap<String, List>((Map<String, List>)oval);
        
        // Resolve all pairs of format: toReplace:["@url.mappings.type.values"]
        // 
        if(replacements != null) {
            
            HashMap<String, List> newValues = new HashMap<String, List>();
            
            for(Entry<String, List> entry:replacements.entrySet()) {
                
                String toReplace = entry.getKey();
                List possibleValues = entry.getValue();
                String firstVal = possibleValues.get(0).toString().trim();
                if(!firstVal.startsWith("@")) {
                    continue;
                }
                
                // Convert @url.mappings.type.values to url.mappings.type.values
                firstVal = firstVal.substring(1);
                
                Object [] parts = firstVal.split("\\.");
                
                Object lastPart = parts[parts.length-1];
                
                List updates = null;
                
                // May be a Map reference, in which case the last part 
                // is an identifier (i.e keys/values/entries)
                if(this.isMapIdentifier(lastPart)) {
                    Object [] jsonPath = new Object[parts.length-1];
                    System.arraycopy(parts, 0, jsonPath, 0, jsonPath.length);
                    Map referenced = (Map)config.getObject(jsonPath);
                    updates = this.getList(referenced, lastPart);
                }else{
                    updates = (List)config.getObject(parts);
                }
                
XLogger.getInstance().log(Level.FINE, "Updating {0} to {1}", this.getClass(), possibleValues, updates);                

                assert updates != null;
                
                newValues.put(toReplace, updates);
            }
            
            replacements.putAll(newValues);
        }
            
        if(end == -1 && value == null) {
            throw new UnsupportedOperationException("Missing values in: url.counter.part"+partIndex);            
        }
        
        this.partIndex = partIndex;
        this.type = type;
    }
    
    public static ConfigURLPartList getSerialPart(JsonConfig config, String type) {
        int max = 20;
        ConfigURLPartList part = new ConfigURLPartList();
        for(int i=0; i<max; i++) {
            try{
                part.update(config, type, i);
                if(part.getListGenerationMode() == ListGenerationMode.serial) {
                    // Only one serial part per config is expected
                    return part;
                }
            }catch(RuntimeException ignored) { }
        }
        return null;
    }
    
    private List getList(Map map, Object identifier) {
        if(identifier.equals("values")) {
            return new ArrayList(new HashSet(map.values()));
        }else if(identifier.equals("keys")) {
            return new ArrayList(map.keySet());
        }else{
            return new ArrayList(map.entrySet());
        }
    }
    
    private boolean isMapIdentifier(Object obj) {
        return obj.equals("keys") || obj.equals("values") || obj.equals("entries");
    }
    
    public ListGenerationMode getListGenerationMode() {
        ListGenerationMode mode;
        if(end != -1) {
            mode = ListGenerationMode.serial;
        }else{
            if(this.replacements != null) {
                mode = ListGenerationMode.replacements;
            }else{
                if(value != null) {
                    mode = ListGenerationMode.none;
                }else{
                    throw new NullPointerException(this.getClass()+"#getValue == null");
                }
            } 
        }
        return mode;
    }
    
    // Abstract list methods
    @Override
    public Object get(int index) {
        
        Object output = null;
        
        final ListGenerationMode mode = this.getListGenerationMode();
        
        boolean indexOutOfBounds = false;
        
        switch(mode) {
            
            case none: 
                if(index == 0) {
                    output = value; 
                }else{
                    indexOutOfBounds = true;
                }
                break;
                
            case serial:
                if(index < 0 || index >= this.size()) {
                    indexOutOfBounds = true;
                }else{
                    if(this.ascending) {
                        output = index + start;
                    }else{
                        output = end - index;
                    }
                }
                break;
                
            case replacements:
                if(index < 0 || index >= this.size()) {
                    indexOutOfBounds = true;
                }else{
                    Iterator<Entry<String, List>> iter = replacements.entrySet().iterator();
                    int listIndex = index;
                    while(iter.hasNext()) {
                        Entry<String, List> entry = iter.next();
                        String toReplace = entry.getKey();
                        List possibleValues = entry.getValue();
                        if(listIndex >= possibleValues.size()) {
                            listIndex = listIndex-possibleValues.size();
                            continue;
                        }
                        output = value.replaceFirst(toReplace, possibleValues.get(listIndex).toString());
                        break;
                    }
                }
                break;
                
            default:
                throw new UnsupportedOperationException(this.getUnexpectedModeMessage(mode));
        }
        
        if(indexOutOfBounds) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        
        return output;
    }

    @Override
    public int size() {
        
        int output;
        
        final ListGenerationMode mode = this.getListGenerationMode();
        
        switch(mode) {
            
            case none: output = 1; 
                break;
                
            case serial:
                output = end - start;
                break;
                
            case replacements:
                int size = 0;
                for(List possibleValues:replacements.values()) {
                    size += possibleValues.size();
                }
                output = size;
                break;
                
            default:
                throw new UnsupportedOperationException(this.getUnexpectedModeMessage(mode));
        }
        
        return output;
    }
    
    private String getUnexpectedModeMessage(ListGenerationMode mode) {
        String msg = "Unexpected: "+
                ListGenerationMode.class+", found: "+mode+", expected: "+
                        Arrays.toString(ListGenerationMode.values());        
        return msg;        
    }
    
    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Map getReplacements() {
        return replacements;
    }

    public void setReplacements(Map replacements) {
        this.replacements = replacements;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPartIndex() {
        return partIndex;
    }

    public void setPartIndex(int partIndex) {
        this.partIndex = partIndex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

