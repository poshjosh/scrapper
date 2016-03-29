/**
 * @(#)StrongTag.java   17-Apr-2011 15:30:59
 *
 * Copyright 2009 BC Enterprise, Inc. All rights reserved.
 * BCE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.scrapper.tag;

import org.htmlparser.tags.CompositeTag;

/**
 * @author  chinomso bassey ikwuagwu
 * @version  0.3
 * @since   1.0
 */
public class StrongTag extends CompositeTag {

    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"STRONG"};

    /**
     * The set of end tag names that indicate the end of this tag.
     */
    private static final String[] mEndTagEnders = new String[] {"BODY", "HTML"};

    /**
     * Script code if different from the page contents.
     */
//    protected String mCode;

    /**
     * Create a new script tag.
     */
    public StrongTag () { }

    /**
     * Return the set of names handled by this tag.
     * @return The names to be matched that create tags of this type.
     */
    @Override
    public String[] getIds () {
        return (mIds);
    }

    /**
     * Return the set of end tag names that cause this tag to finish.
     * @return The names of following end tags that stop further scanning.
     */
    @Override
    public String[] getEndTagEnders (){
        return (mEndTagEnders);
    }
}//~END
