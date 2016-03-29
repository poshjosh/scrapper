package com.scrapper.util;

//import com.bc.process.StoppableTask;
import com.bc.util.XLogger;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @(#)DefaultLimitedCache.java   03-Nov-2014 18:12:32
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Only {@link #getMaxActive()} elements may be active at any given time.
 * When this amount of elements are active, calls to {@link #get(int)}
 * block until the number of active elements are less than this amount.
 * Wether an element is active is decided by invoking {@link #isActive(int)}
 * @see #get(int) 
 * @see #isActive(int) 
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public abstract class DefaultLimitedCache<T extends Serializable> extends LimitedCache<T> {
    
    private String category;
    
    public DefaultLimitedCache(String category, List<String> sitenames) {
        
        super(sitenames);
        
        this.category = category;
    }
    
    @Override
    protected String [] sort(List<String> toSort) {

        Collections.sort(toSort, DefaultLimitedCache.this);
        
        this.rearrangeToEnsureEqualOpportunity(toSort);
        
        return toSort.toArray(new String[0]);
    }

    @Override
    public int compare(String s1, String s2) {
        
        Map<String, Integer> tableRequestTimes = this.getRequestTimes();
        
        if(tableRequestTimes == null) {
            return super.compare(s1, s2);
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
    
    protected void rearrangeToEnsureEqualOpportunity(List<String> sorted) {
XLogger.getInstance().log(Level.FINER, "Before rearrange: {0}", this.getClass(), sorted);        
        final int toRelocate = this.getMaxActive() / 2;
        final int size = sorted.size();
        if(toRelocate > 0 && toRelocate < size) {
            
            int relocated = 0;
            Map<String, Integer> times = this.getRequestTimes();
            
            if(times == null) {
                return;
            }
            
            for(int attempts=0; attempts < size; attempts++) {
                String last = sorted.get(size-1);
                Integer i = times.get(last);
                if(i == null) { 
                    sorted.remove(last);
                    sorted.add(toRelocate, last);
                    if(++relocated == toRelocate) {
                        break;
                    }
                }
            }
        }
XLogger.getInstance().log(Level.FINER, "After rearrange: {0}", this.getClass(), sorted);        
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
