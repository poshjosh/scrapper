package com.scrapper.formatter.url;

import com.bc.manager.Formatter;

/**
 * @(#)BracketRemovingURLFormatter.java   08-Apr-2014 23:13:40
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Input:<br/>
 * http://www.kara.com.ng/search.php/?search_query=dress (http://www.kara.com.ng/search.php/?search_query=dress).
 * <br/><br/>
 * Output:<br/>
 * http://www.kara.com.ng/search.php/?search_query=dress
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class BracketRemovingURLFormatter implements Formatter<String> {

    /**
     * Input:<br/>
     * http://www.kara.com.ng/search.php/?search_query=dress (http://www.kara.com.ng/search.php/?search_query=dress).
     * <br/><br/>
     * Output:<br/>
     * http://www.kara.com.ng/search.php/?search_query=dress
     */
    @Override
    public String format(String e) {
        
        int off = e.indexOf('(');
        
        if(off != -1) {
            
            e = e.substring(0, off);
        }
        
        return e.trim();
    }
}

