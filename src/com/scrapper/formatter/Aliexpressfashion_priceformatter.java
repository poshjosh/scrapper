package com.scrapper.formatter;

import com.bc.manager.Formatter;
import java.io.Serializable;
import java.util.Map;

/**
 * @(#)Aliexpressfashion_priceformatter.java   25-Oct-2013 17:55:19
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * If price in USD is greater than {@linkplain #maxPrice} clears the input
 * Map of all entries. Otherwise adds {@linkplain #margin} to the price.
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class Aliexpressfashion_priceformatter 
        implements Formatter<Map>, Serializable {
    
    private int maxPrice;
    
    private int margin;
    
    public Aliexpressfashion_priceformatter() {
        maxPrice = 16;
        margin = 10;
    }
    
    @Override
    public Map format(Map parameters) {
        
        Object oval = parameters.get("price");
        
        if(oval == null) return parameters;
        
        String sval = oval.toString().trim();
        
        float f = Float.parseFloat(sval);
        
        if(f > maxPrice) {
//# This refers to USD as our currency is USD
//# We reject the whole extract if price is greater than 16
    
            parameters.clear();
            
        }else{
            
//# We add 10 units to the price to cover fluctuations etc
            
            parameters.put("price", Float.toString(f + margin));
            
        }
        
        return parameters;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }
}
