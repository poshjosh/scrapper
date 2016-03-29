package com.scrapper.formatter.url;

import com.bc.manager.Formatter;

/**
 * @(#)TaafooURLFormatter.java   13-Apr-2014 01:21:01
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Input:<br/>
 * http://www.taafoo.com/pages/searchresults.aspx/viewdetails.aspx?productId=110688&amp;ProdName=U.S-Polo-Assn-Blue-Boys--Jeans--wt-Belt
 * <br/><br/>
 * Output:<br/>
 * http://www.taafoo.com/pages/viewdetails.aspx?productId=110688
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class TaafooURLFormatter implements Formatter<String> {

    /**
     * Input:<br/>
     * http://www.taafoo.com/pages/searchresults.aspx/viewdetails.aspx?productId=110688&amp;ProdName=U.S-Polo-Assn-Blue-Boys--Jeans--wt-Belt
     * <br/><br/>
     * Output:<br/>
     * http://www.taafoo.com/pages/viewdetails.aspx?productId=110688
     */
    @Override
    public String format(String e) {
        
        int lenB4 = e.length();
        
        e = e.replace("searchresults.aspx/viewdetails.aspx?productId=", 
                "viewdetails.aspx?productId=");
        
        if(e.length() == lenB4) { // no change

            e = e.replace("searchresults.aspx/ViewDetails.aspx?productId=", 
                    "ViewDetails.aspx?productId=");
            
        }
        
        String elower = e.toLowerCase();
        
        int n = elower.indexOf("&amp;prodname=");
        
        if(n == -1) {
            n = elower.indexOf("&prodname=");
        }
        
        if(n != -1) {
            e = e.substring(0, n);
        }
        
        return e.trim();
    }
}

