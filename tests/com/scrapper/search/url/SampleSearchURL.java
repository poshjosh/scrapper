package com.scrapper.search.url;

import com.scrapper.context.CapturerContext;
import com.scrapper.search.AbstractURLProducer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @(#)SampleSearchURL.java   25-Oct-2014 14:04:03
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
public class SampleSearchURL extends AbstractURLProducer {

    @Override
    public List<String> getCategoryURLs(CapturerContext context, String tableName) {
        return null;
    }

    @Override
    public List<String> getSearchURLs(CapturerContext context, String tableName, Map parameters, String searchTerm) {
        return Arrays.asList(new String[]{"file:/"+System.getProperty("user.home")+"/Desktop/sample.htm"});
    }

    @Override
    protected String getQueryAfterSearchTerm() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String getQueryBeforeSearchTerm() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String getURLPart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
