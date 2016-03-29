package com.scrapper;

import com.scrapper.util.PageNodes;
import java.net.URL;
import java.util.Map;


/**
 * @(#)BasePageDataConsumer.java   08-Dec-2014 19:16:18
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class BasePageDataConsumer extends AbstractPageDataConsumer {

    private DataUploader uploader;

    public BasePageDataConsumer(URL insertURL, final Map uploadParameters) { 

        if(insertURL == null) {
            throw new NullPointerException();
        }
        
        if(uploadParameters == null || uploadParameters.isEmpty()) {
            throw new NullPointerException();
        }
        
        this.uploader = new DataUploader(insertURL) {
            @Override
            public Map getUploadParameters() {
                return uploadParameters;
            }
        };
    }
    
    @Override
    public boolean doConsume(PageNodes page, Map data) {

        Object productTable = data.get(this.getTableNameKey());
        
        this.uploader.getUploadParameters().put(this.getTableNameKey(), productTable);
        
        return this.uploader.uploadRecord(data) == 200;
    }
}
