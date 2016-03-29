package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;

/**
 * @(#)AbstractDataConsumer.java   06-Apr-2014 00:14:14
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public abstract class ContextDataConsumer 
        extends AbstractPageDataConsumer {
    
    private CapturerContext context;
    
    public ContextDataConsumer(String sitename) { 
        
        this(CapturerApp.getInstance().getConfigFactory().getContext(sitename));
    }
    
    public ContextDataConsumer(CapturerContext context) { 
        
        this.context = context;
    
        JsonConfig config = context.getConfig();

        int i = config.getInt(Config.Extractor.minDataToAccept);
        ContextDataConsumer.this.setMinimumParameters(i);
        
        String s = context.getConfig().getList(Config.Site.tables).get(0).toString();
        ContextDataConsumer.this.setDefaultTableName(s);
        
        ContextDataConsumer.this.setFormatter(context.getFormatter());
    }
    
    public CapturerContext getContext() {
        return context;
    }
}
