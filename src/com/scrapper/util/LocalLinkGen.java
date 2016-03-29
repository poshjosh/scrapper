package com.scrapper.util;

import com.bc.manager.Formatter;

/**
 * @(#)LocalLinkGen.java   26-Aug-2013 13:42:09
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Converts a link to local format.
 * A relative link can be used to construct both a URL and a file name.
 * Basically, the operation is to strip off the base url, if any,
 * and then prepend as many dot-dots as necessary to make
 * it relative to the current page.
 * A bit of a kludge handles the root page specially by calling it
 * the value of {@linkplain #rootPageAlias} (the default is index.html), 
 * even though that probably isn't it's real file name.
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class LocalLinkGen implements Formatter<String> {
    
    private String rootPageAlias;
    
    private String baseURL;
    
    /**
     * The page which referred to the link to be made local or an empty string
     * if it's an absolute URL
     */
    private String referrer;
    
    public LocalLinkGen() {
        this.rootPageAlias = "index.html";
    }
    
    /**
     * Converts a link to local format.
     * A relative link can be used to construct both a URL and a file name.
     * Basically, the operation is to strip off the base url, if any,
     * and then prepend as many dot-dots as necessary to make
     * it relative to the current page.
     * A bit of a kludge handles the root page specially by calling it
     * the value of {@linkplain #rootPageAlias} (the default is index.html), 
     * even though that probably isn't it's real file name.
     * @param link The link to make relative.
     */
    @Override
    public String format(String link) {
        int i;
        int j;
        String ret;

        if (link.equals (getBaseURL ()) || (!getBaseURL ().endsWith ("/") && link.equals (getBaseURL () + "/")))
            ret = this.getRootPageAlias(); // handle the root page specially
        else if (link.startsWith (getBaseURL ())
                && (link.length () > getBaseURL ().length ()))
            ret = link.substring (getBaseURL ().length () + 1);
        else
            ret = link; // give up
            
        // make it relative to the current page by prepending "../" for
        // each '/' in the current local path
        if ((null != referrer)
            && link.startsWith (getBaseURL ())
            && (referrer.length () > getBaseURL ().length ()))
        {
            referrer = referrer.substring (getBaseURL ().length () + 1);
            i = 0;
            while (-1 != (j = referrer.indexOf ('/', i)))
            {
                ret = "../" + ret;
                i = j + 1;
            }
        }

        return (ret);
    }
    
    private String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getRootPageAlias() {
        return rootPageAlias;
    }

    public void setRootPageAlias(String rootPageAlias) {
        this.rootPageAlias = rootPageAlias;
    }
}
