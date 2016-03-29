package com.scrapper.extractor;

import java.util.Map;

/**
 * @(#)DataExtractor.java   12-Dec-2013 19:58:46
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
public interface DataExtractor<E> {
    
    Map extractData(E e) throws Exception;
}
