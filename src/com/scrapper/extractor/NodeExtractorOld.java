package com.scrapper.extractor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.htmlparser.Tag;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * @(#)NodeExtractor.java   12-Dec-2013 22:04:33
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
public class NodeExtractorOld
        extends NodeVisitor 
        implements DataExtractor<Tag>, Serializable {

    private Map extractedData;
    
    public NodeExtractorOld() {
        this.extractedData = new HashMap();
    }
    
    public void reset() {
        // The previous reference may still be used else where
        // So we don't call clear
        this.extractedData = new HashMap();
    }
    
    @Override
    public Map extractData(Tag tag) throws ParserException {
        this.reset();
        tag.accept(this);
        return this.extractedData;
    }
    
    protected Map getExtractedData() {
        return this.extractedData;
    }
}
