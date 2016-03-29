package com.scrapper.util;

import com.bc.util.XLogger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 * @(#)SortByFrequency.java   23-Feb-2015 09:15:04
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
public class SortByFrequency implements Comparator<String> {
    
    private String category;
    
    public SortByFrequency() { }

    public SortByFrequency(String category) { 
        this.category = category;
    }
    
    @Override
    public int compare(String s1, String s2) {
        
        Map<String, Integer> tableRequestTimes = this.getRequestTimes();
        
        if(tableRequestTimes == null) {
            // No sorting if requestTimes is null
            return 0;
        }
        
        Integer i1 = tableRequestTimes.get(s1);
        Integer i2 = tableRequestTimes.get(s2);
        int output;
        if(i1 == null && i2 == null) {
            output = 0;
        }else if(i1 != null && i2 != null) {
            output = i1.compareTo(i2);
        }else {
            output = 0;
        }
        
        return output;
    }

    /**
     * <p>Input: <tt>[A, B, C, D, E, F, G, H, I, J]</tt></p>
     * <p>Factor: <tt>0.2</tt></p>
     * <p>Output <tt>[A, B, I, J, C, D, E, F, G, H]</tt></p>
     * @see #rearrange(java.util.List, int, int) 
     * @param list
     * @param factor A float greater than zero and less than one.
     */
    public void rearrange(List list, float factor) {
        
        if(factor <= 0) {
            throw new IllegalArgumentException("Factor <= 0. factor: "+factor);
        }
        if(factor >= 1) {
            throw new IllegalArgumentException("Factor >= 1. factor: "+factor);
        }
        
        int toRelocate;
        int offset;
        
        offset = toRelocate = Math.round(list.size() * factor); 
        
        if(offset > 0) {
            
            rearrange(list, offset, toRelocate);
        }
    }
    
    /**
     * <p>Input: <tt>[A, B, C, D, E, F, G, H, I, J]</tt></p>
     * <p>Offset: <tt>2</tt></p>
     * <p>To relocate: <tt>3</tt></p>
     * <p>Output <tt>[A, B, H, I, J, C, D, E, F, G]</tt></p>
     * @param list
     * @param offset
     * @param toRelocate 
     */
    public void rearrange(List list, int offset, int toRelocate) {
        
XLogger.getInstance().log(Level.FINER, "Before rearrange: {0}", this.getClass(), list);        

        Collections.rotate(list.subList(offset, list.size()), toRelocate);
        
XLogger.getInstance().log(Level.FINER, "After rearrange: {0}", this.getClass(), list);        
    }
    
    private transient static TableRequestTimes rt;
    public Map<String, Integer> getRequestTimes() {
        if(category == null) {
            return null;
        }
        if(rt == null) {
            rt = new TableRequestTimes();
        }
        return rt.getRequestTimes(category);
    }
    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
