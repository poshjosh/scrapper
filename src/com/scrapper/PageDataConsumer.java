package com.scrapper;

import com.scrapper.util.PageNodes;
import java.util.Map;

/**
 * @(#)PageDataConsumer.java   01-Mar-2014 12:06:23
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
public interface PageDataConsumer {
    
    boolean consume(PageNodes page, Map data);
}
