package com.scrapper.util;

/**
 * @(#)HtmlLinkFilter.java   26-Aug-2013 14:17:17
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
public class HtmlLinkFilter implements com.bc.manager.Filter<String> {
    
    @Override
    public boolean accept(String link) {
        
        String lowercaseLink = link.toLowerCase();
        
        boolean htmlLink = (accept(lowercaseLink, ".html") || 
           accept(lowercaseLink, ".php") ||
           accept(lowercaseLink, ".htm") ||
           accept(lowercaseLink, ".xhtml") ||
           accept(lowercaseLink, ".asp") ||
           accept(lowercaseLink, ".aspx") ||
           accept(lowercaseLink, ".jsp") ||
           accept(lowercaseLink, ".jspx") ||
           accept(lowercaseLink, ".php") ||
           accept(lowercaseLink, ".xml")
        );
                
        return htmlLink;        
    }
    
    private boolean accept(String link, String extension) {
        boolean accept = false;
        if(link.endsWith(extension)) {
            // page.html is valid
            accept = true;
        }else{
            // page.html?view=results is also valid
            int n = link.lastIndexOf('/');
            if(link.indexOf(extension+"?", n) != -1) {
                accept = true;
            }
        }
        return accept;
    }
}
