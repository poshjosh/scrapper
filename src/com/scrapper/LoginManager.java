package com.scrapper;

import com.bc.io.CharFileIO;
import com.bc.net.ConnectionManager;
import com.bc.util.XLogger;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @(#)LoginManager.java   14-Dec-2013 11:58:27
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
public class LoginManager extends ConnectionManager {
    
    public LoginManager() { }
    
    public LoginManager(int maxRetrials, long retrialInterval) {
        super(maxRetrials, retrialInterval);
    }
    
    /**
     * @param target url
     * @return The list of login cookies
     */
    public List<String> login(String target,
            Map<String, String> outputParams) 
            throws MalformedURLException, IOException {
        
        URL loginURL = new URL(target);
XLogger.getInstance().log(Level.FINE, "Login url: {0}", this.getClass(), target);

        HashMap<String, Object> connProps = new HashMap<>();
        String charset = "UTF-8";
        connProps.put("Accept-Charset", charset);
        connProps.put("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
        
        this.setGenerateRandomUserAgent(true); 
        
        this.setGetCookies(true);

        InputStream in = this.getInputStream(loginURL, connProps, outputParams, true);

        int responseCode = this.getResponseCode();
XLogger.getInstance().log(Level.FINE, "Login responseCode: {0}", this.getClass(), responseCode);            

        if(responseCode != 200) {
            StringBuilder builder = new StringBuilder("Login Failed. Server response: ");
            builder.append(this.getResponseCode());
            builder.append(' ').append(this.getResponseMessage());
            CharFileIO ioMgr = new CharFileIO(charset);
            CharSequence cs = ioMgr.readChars(in);
XLogger.getInstance().log(Level.FINER, "Login Raw response:\n{0}", this.getClass(), cs);            
            throw new IOException(builder.toString());
        }

        return this.getCookies();
    }
}
