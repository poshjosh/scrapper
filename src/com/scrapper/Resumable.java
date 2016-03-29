package com.scrapper;

/**
 * @(#)Resumable.java   29-Nov-2013 23:47:50
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
public interface Resumable {
    
    /**
     * If true parsed urls will be saved so that resume will be possible in 
     * the future.
     */
    boolean isResumable();
    
    /**
     * If true any previously saved URLs will be loaded and considered as 
     * already parsed before any current URLs are parsed
     */
    boolean isResume();
}
