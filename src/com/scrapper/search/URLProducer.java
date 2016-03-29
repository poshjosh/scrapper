package com.scrapper.search;

import com.scrapper.context.CapturerContext;
import java.util.List;
import java.util.Map;

/**
 * @(#)URLProducer.java   12-Mar-2014 09:58:32
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
public interface URLProducer {

    List<String> getCategoryURLs(CapturerContext context, String tableName);
    
    List<String> getSearchURLs(CapturerContext context, String tableName, 
            Map parameters, String searchText);
}
