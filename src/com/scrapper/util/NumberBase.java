package com.scrapper.util;

/**
 * @(#)NumberBase.java   04-Apr-2014 14:34:01
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Converts a base 10 number to the specified base (the specified base
 * must be base 10 or less)
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class NumberBase {
    
    /**
     * @see #convert(int, int, java.lang.StringBuilder) 
     */
    public static StringBuilder convert(int m, int n) { 
        
        return convert(m, n, new StringBuilder());
    } 

    /**
     * @param m The number to be converted to a new base
     * @param n Number greater than 1 less or equal to 10. The base.
     * @return The representation of number m in base n
     */
    public static StringBuilder convert(int m, int n, StringBuilder appendTo) { 
        
        if(n < 2) {
            throw new UnsupportedOperationException("Base must be > 1");
        }
        
        if(n > 10) {
            throw new UnsupportedOperationException("Base must be <= 10");
        }
        
        if (m < n) { // see if it's time to return 
            
            appendTo.setLength(0);
            
            return appendTo.append(m); 
            
        }else {
            
            return convert(m / n, n, appendTo).append(m % n);  
        }    
    } 
}
