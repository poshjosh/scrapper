package com.scrapper.extractor;

import org.htmlparser.Tag;

/**
 * @(#)AttributesExtractor.java   20-Feb-2014 23:04:27
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
public interface AttributesExtractor {
    
    String extract(Tag tag);

    String[] getAttributesToExtract();

    void setAttributesToExtract(String[] attributesToExtract);
}
