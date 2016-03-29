package com.scrapper.extractor;

import com.bc.webdatex.extractor.NodeExtractor;
import java.util.Set;


/**
 * @(#)DefaultPageExtractorIx.java   09-Oct-2015 00:20:33
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
public interface MultipleNodesExtractorIx extends PageExtractorIx {

    NodeExtractor createExtractor(String id);

    NodeExtractor getExtractor(String id);
    
    Set<String> getFailedNodeExtractors();

    Set<String> getNodeExtractorIds();

    Set<String> getSuccessfulNodeExtractors();

    boolean isSuccessfulCompletion();

    @Override
    void reset();

}
