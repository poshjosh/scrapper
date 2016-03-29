package com.scrapper.context;

import com.bc.manager.Formatter;
import com.bc.json.config.JsonConfig;
import com.scrapper.util.Util;
import com.scrapper.formatter.DefaultFormatter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @(#)NigeriajobsonlineContext.java   22-Feb-2013 23:42:37
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
public class NigeriajobsonlineContext  extends DefaultCapturerContext {
    
    public NigeriajobsonlineContext() { }
    
    public NigeriajobsonlineContext(JsonConfig config) { 
        super(config);
    }
    
    public class NigeriajobsonlineFormatter extends DefaultFormatter {

        private Properties jobTypes = new Properties();

        public NigeriajobsonlineFormatter(CapturerContext context) throws IOException {

            super(context);

            final String filePath = "META-INF/nigerianjobsonline_jobtypes.properties";

            try{
                this.jobTypes.load(new FileInputStream(filePath));
            }catch(IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, e);
            }
        }

        @Override
        public Map<String, Object> format(Map<String, Object> parameters) {
    //Logger.getLogger(this.getClass().getName()).info(this.getClass().getName()+"BEFORE Params: "+parameters);

            // We have to format type first or it will cause problems for
            // the parent formatter
            //
            Map<String, Object> copy = this.formatType(parameters);

            return super.format(copy);
        }

        protected Map<String, Object> formatType(Map<String, Object> parameters) {

            String typeStr = (String)parameters.get("type");

            if(typeStr == null) return parameters;

            // Get the Properties containing types and their ids
            //
    //System.out.println(this.getClass().getName()+"#formatType KeySet: " +
    //        this.jobTypes.keySet());
            String typeId = Util.findValueWithMatchingKey(this.jobTypes, typeStr.trim().toLowerCase());
    //System.out.println(this.getClass().getName()+"#formatType typeStr: " +
    //        typeStr+", typeId: "+typeId);

            if(typeId != null) {

                // Replace the type with its id
                //
                parameters.put("type", new Integer(typeId));

            }

            return parameters;
        }
    }//~END

    public static class NigeriajobsonlineUrlFormatter implements Formatter<String> {

        private String baseURL;

        public NigeriajobsonlineUrlFormatter(String url) throws IOException {
            baseURL = url;
        }

        @Override
        public String format(String url) {

            if(!url.startsWith(baseURL)) {
                try{
                    // We use this to test if the link has a protocol i.e http, ftp etc
                    URL linkUrl = new URL(url);
                }catch(MalformedURLException e) {
                    String base = baseURL;
                    if(base.endsWith("/")) {
                        base.substring(0, base.length()-1);
                    }
                    if(!url.startsWith("/")) {
                        url = "/" + url;
                    }
                    url = base + url;
    Logger.getLogger(this.getClass().getName()).log(Level.FINE, "{0}#format. Output link: {1}", 
            new Object[]{this.getClass().getName(), url});
                }
            }
            return url;
    //        StringBuilder builder = new StringBuilder(url);
    //        if(!url.endsWith("/")) {
    //            builder.append('/');
    //        }
    //        return builder.append("index.php").toString();
        }
    }
}
