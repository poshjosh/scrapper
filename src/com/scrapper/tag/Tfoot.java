package com.scrapper.tag;

import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.IsEqualFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.TableRow;
import org.htmlparser.util.NodeList;

/**
 * @(#)Tfoot.java   28-Mar-2013 00:25:35
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
public class Tfoot extends CompositeTag {
    
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"TFOOT"};

    /**
     * The set of tag names that indicate the end of this tag.
     */
    private static final String[] mEnders = new String[] {"TFOOT", "TABLE", "BODY", "HTML"};

    /**
     * The set of end tag names that indicate the end of this tag.
     */
    private static final String[] mEndTagEnders = new String[] {"TABLE", "BODY", "HTML"};

    /**
     * Create a new table tag.
     */
    public Tfoot() { }

    /**
     * Return the set of names handled by this tag.
     * @return The names to be matched that create tags of this type.
     */
    @Override
    public String[] getIds (){
        return (mIds);
    }

    /**
     * Return the set of end tag names that cause this tag to finish.
     * @return The names of following end tags that stop further scanning.
     */
    @Override
    public String[] getEndTagEnders() {
        return (mEndTagEnders);
    }

    /**
     * Get the row tags within this table.
     * @return The rows directly contained by this table.
     */
    public TableRow[] getRows() {
        NodeList kids;
        NodeClassFilter cls;
        HasParentFilter recursion;
        NodeFilter filter;
        TableRow[] ret;

        kids = getChildren ();
        if (null != kids)
        {
            cls = new NodeClassFilter (Tbody.class);
            recursion = new HasParentFilter (null);
            filter = new OrFilter (
                        new AndFilter (
                            cls, 
                            new IsEqualFilter (this)),
                        new AndFilter ( // recurse up the parent chain
                            new NotFilter (cls), // but not past the first table
                            recursion));
            recursion.setParentFilter (filter);
            kids = kids.extractAllNodesThatMatch (
                // it's a row, and has this table as it's enclosing table
                new AndFilter (
                    new NodeClassFilter (TableRow.class),
                    filter), true);
            ret = new TableRow[kids.size ()];
            kids.copyToNodeArray (ret);
        }
        else
            ret = new TableRow[0];
        
        return (ret);
    }

    /**
     * Get the number of rows in this table.
     * @return The number of rows in this table.
     * <em>Note: this is a a simple count of the number of {@.html <TR>} tags and
     * may be incorrect if the {@.html <TR>} tags span multiple rows.</em>
     */
    public int getRowCount() {
        return (getRows ().length);
    }

    /**
     * Get the row at the given index.
     * @param index The row number (zero based) to get. 
     * @return The row for the given index.
     */
    public TableRow getRow (int index) {
        TableRow[] rows;
        TableRow ret;

        rows = getRows ();
        if (index < rows.length)
            ret = rows[index];
        else
            ret = null;
        
        return (ret);
    }

    /**
     * Return a string suitable for debugging display.
     * @return The table as HTML, sorry.
     */
    @Override
    public String toString() {
        return
            "Tbody\n" +
            "********\n"+
            toHtml();
    }
}
