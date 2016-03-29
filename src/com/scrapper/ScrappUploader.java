package com.scrapper;

/**
 * @(#)ScrappUploader.java   30-Nov-2012 13:39:47
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.util.Map;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author  chinomso bassey ikwuagwu
 * @version  0.3
 * @since   1.0
 */
public class ScrappUploader extends ContextDataConsumer {

    private DataUploader uploader;

    public ScrappUploader(CapturerContext context) { 
        
        super(context);
    
        URL insertURL = null;
        try{
            insertURL = new URL(AppProperties.getProperty(AppProperties.INSERT_URL));
            if(insertURL == null) {
                throw new NullPointerException("Insert URL == null");
            }
        }catch(MalformedURLException e) {
            throw new IllegalArgumentException("Insert URL: "+insertURL);
        }
        
        JsonConfig config = context.getConfig();
        
        final Map m = config.getMap("uploadParameters");
        
        if(m == null || m.isEmpty()) {
            throw new NullPointerException();
        }
        
        this.uploader = new DataUploader(insertURL) {
            @Override
            public Map getUploadParameters() {
                return m;
            }
        };
    }
    
    @Override
    public boolean doConsume(PageNodes page, Map data) {

        Object productTable = data.get(this.getTableNameKey());
        
        this.uploader.getUploadParameters().put(this.getTableNameKey(), productTable);
        
        return this.uploader.uploadRecord(data) == 200;
    }
}//~END
