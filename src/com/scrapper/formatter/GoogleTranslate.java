package com.scrapper.formatter;

import com.bc.io.CharFileIO;
import com.bc.manager.Formatter;
import com.bc.net.ConnectionManager;
import com.bc.util.XLogger;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @(#)GoogleTranslate.java   22-Oct-2013 21:14:26
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * http://translate.google.com/#en/de/man
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class GoogleTranslate implements Formatter<String> {
    
    private String charset;
    
    private String inputLang;
    
    private String outputLang;
    
    private final CharFileIO fileIO;
    
    private final ConnectionManager mgr;

    public GoogleTranslate() {
        charset = "UTF-8";
        mgr = new ConnectionManager();
        mgr.setGenerateRandomUserAgent(true); 
        fileIO = new CharFileIO(charset);
    }
    
    @Override
    public String format(String input) {
    
        StringBuilder urlStr = this.getURL();
        
        try{
            input = URLEncoder.encode(input, charset);
        }catch(UnsupportedEncodingException | RuntimeException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }

        urlStr.append('/').append(input);
        
        URL url = null;
        try{
            url = new URL(urlStr.toString());
        }catch(MalformedURLException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
        
        CharSequence output = null;
        
        if(url != null) {
            try{
                InputStream in = mgr.getInputStream(url, this.getDefaultProperties(url));
                output = fileIO.readChars(in);
            }catch(IOException e) {
                XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            }
        }
        
        return output == null ? null : output.toString();
    }
    
    public StringBuilder getURL() {
        StringBuilder builder = new StringBuilder("http://translate.google.com/");
        builder.append('#').append(this.getOutputLang());
        builder.append('/').append(this.getInputLang());
        return builder;
    }

    private Map<String, Object> getDefaultProperties(URL url) {
        HashMap<String, Object> props = new HashMap<>();
        props.put("Content-Type", "application/x-www-form-urlencoded");
        props.put("Accept-Charset", charset);
        return props;
    }
    
    public String getInputLang() {
        return inputLang;
    }

    public void setInputLang(String inputLang) {
        this.inputLang = inputLang;
    }

    public String getOutputLang() {
        return outputLang;
    }

    public void setOutputLang(String outputLang) {
        this.outputLang = outputLang;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
