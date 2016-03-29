package com.scrapper;

import com.scrapper.util.PageNodes;
import java.util.List;


/**
 * @(#)ResumeHandler.java   29-Nov-2014 11:59:51
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 * @param <T>
 */
public interface ResumeHandler<T> {
    
    List<String> getAllPendingUrls(List<String> localPendingUrls);

    void updateStatus(PageNodes page);

    boolean isInDatabase(String link);
    
    T saveIfNotExists(String link);
}
