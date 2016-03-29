package com.scrapper.tag;

import org.htmlparser.nodes.TagNode;

/**
 * @(#)BreakTag.java   06-Oct-2013 06:23:42
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
public class BreakTag extends TagNode {
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"BR"};

    /**
     * Create a new input tag.
     */
    public BreakTag ()
    {
    }

    /**
     * Return the set of names handled by this tag.
     * @return The names to be matched that create tags of this type.
     */
    @Override
    public String[] getIds ()
    {
        return (mIds);
    }
}
