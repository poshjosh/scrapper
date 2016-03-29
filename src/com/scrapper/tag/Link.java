package com.scrapper.tag;

import org.htmlparser.util.ParserUtils;
import org.htmlparser.nodes.TagNode;

/**
 * @(#)LinkInHeadTag.java   16-Nov-2013 18:18:25
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
public class Link extends TagNode {
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"LINK"};

    /**
     * The URL where the link points to
     */
    protected String mLink;

    public Link () {
    }

    /**
     * Return the set of names handled by this tag.
     * @return The names to be matched that create tags of this type.
     */
    public String[] getIds ()
    {
        return (mIds);
    }

    /**
     * Returns the url as a string, to which this link points.
     * This string has had the "mailto:" and "javascript:" protocol stripped
     * off the front (if those predicates return <code>true</code>) but not
     * for other protocols. Don't ask me why, it's a legacy thing.
     * @return The URL for this <code>A</code> tag.
     */
    public String getLink()
    {
        if (null == mLink)
        {
            mLink = extractLink ();
        }
        return (mLink);
    }

    /**
     * Return the contents of this link node as a string suitable for debugging.
     * @return A string representation of this node.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Link to : "+ getLink() + "; begins at : "+getStartPosition ()+"; ends at : "+getEndPosition ()+"\n");
        return sb.toString();
    }

    /**
     * Set the <code>HREF</code> attribute.
     * @param link The new value of the <code>HREF</code> attribute.
     */
    public void setLink(String link)
    {
        mLink = link;
        setAttribute ("HREF", link);
    }

    /**
     * Extract the link from the HREF attribute.
     * @return The URL from the HREF attibute. This is absolute if the tag has
     * a valid page.
     */
    public String extractLink ()
    {
        String ret;

        ret =  getAttribute ("HREF");
        if (null != ret)
        {
            ret = ParserUtils.removeChars (ret,'\n');
            ret = ParserUtils.removeChars (ret,'\r');
        }
        if (null != getPage ())
            ret = getPage ().getAbsoluteURL (ret);

        return (ret);
    }
}
