package com.scrapper.filter;

import java.util.Collection;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;

/**
 * @(#)TagNameOnlyFilter.java   09-Feb-2013 17:09:35
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Accepts Tags of a specified tagName which do not have any attributes
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class TagNameOnlyFilter extends TagNameFilter {

    /**
     * Creates a new instance of TagNameFilter.
     * With no name, this would always return <code>false</code>
     * from {@link #accept}.
     */
    public TagNameOnlyFilter () {
        this ("");
    }

    /**
     * Creates a TagNameOnlyFilter that accepts tags with the given name
     * which do not have any attribute pairs
     * @param name The tag name to match.
     */
    public TagNameOnlyFilter (String name) {
        super(name);
    }
    
    /**
     * @param tagName Tags with this name and no attributes will be accepted
     * @return Returns a filter which accepts Tags of the specified tagName
     * which do not have any attributes
     */
    @Override
    public boolean accept(Node node) {
        if(node instanceof Tag) {
            Collection attributes = ((Tag)node).getAttributesEx();
            // A Tag which has no key=value attributes contains
            // the tagName as the only attributes
            if(attributes != null && attributes.size() > 1) return false;
        }
        return super.accept(node);
    }
}
