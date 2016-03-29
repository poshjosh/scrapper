package com.scrapper.search.url;

import com.scrapper.search.AbstractMappingsURLProducer;
import java.io.Serializable;
import java.util.Map;

/**
 * @(#)ShopkudiSearchURL.java   29-Mar-2014 22:40:00
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
public class ShopkudiSearchURL extends AbstractMappingsURLProducer implements Serializable {
    
    public ShopkudiSearchURL() {
        this.setFormatSlashes(true);
    }
    
    @Override
    protected String getDefaultUrlSub() {
        return null;
    }
    
    @Override
    protected String getURLPart() {
        return null;
    }

    @Override
    protected String getQueryBeforeSearchTerm() {
        return null;
    }
    
    @Override
    protected String getQueryAfterSearchTerm() {
        return null;
    }

    @Override
    protected void appendSearchTerm(Map parameters, 
    String searchTerm, StringBuilder appendTo) { }
}

