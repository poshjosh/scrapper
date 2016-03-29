package com.scrapper.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @(#)HtmlContentFilter.java   26-Aug-2013 14:15:11
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
public class HtmlContentFilter implements com.bc.manager.Filter<String> {
    
    /**
     * Returns <code>true</code> if the link contains text/html content.
     * @param link The URL to check for content type.
     * @return <code>true</code> if the HTTP header indicates the type is
     * "text/html".
     */
    @Override
    public boolean accept(String link) {
        
        URL url;
        URLConnection connection;
        String type;
        boolean ret;

        ret = false;
        try {
            url = new URL (link);
            connection = url.openConnection ();
            type = connection.getContentType ();
            if (type == null)
                ret = false;
            else
                ret = type.startsWith ("text/html");
        }catch (IOException e) {
//            XLogger.getInstance().log(Level.WARNING, "{0}. {1}", this.getClass(), e, link);
        }
        
        return (ret);
    }
}
