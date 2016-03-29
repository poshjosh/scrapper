package com.scrapper.filter;

import com.bc.util.XLogger;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

/**
 * @(#)BoundsFilter.java   05-Oct-2013 15:16:48
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
public class BoundsFilter extends BoundsMarker 
        implements FilterHasBounds {

    private boolean strict;
    
    private NodeFilter filter;
    
    public BoundsFilter() {  }
    
    public BoundsFilter(String id, NodeFilter f0, NodeFilter startAt, NodeFilter stopAt) { 
        super(id, startAt, stopAt);
        this.filter = f0;
    }
    
    @Override
    public boolean accept(Node node) {
        
        // These filter must taste/visit the node even if the node is accepted
        // by default. Hence they are placed first.
        //
        this.visitStartTag(node);
        
        if(this.isDefaultAccept(node)) {
            return true;
        }
        
        boolean boundsAccept = this.isStarted() && !this.isDone();
        
        boolean filterAccept = (filter == null || filter.accept(node));
        
        boolean accepted;
        if(this.isStrict()) {
            // Good for extraction stage
            accepted = filterAccept && boundsAccept;
            //return (filteraccept || boundsEntered) && (filteraccept && !boundsExited);
        }else{
            // Good for filter stage
            accepted = filterAccept || boundsAccept;
        }
        
XLogger.getInstance().log(Level.FINER, 
"{0}-BoundsFilter, Accepted by, Filter: {1}, BoundsMarker: {2}, Node: {3}", 
this.getClass(), this.getId(), filterAccept, boundsAccept, node);        

        return accepted;
    }
    
    protected boolean isDefaultAccept(Node node) { 
        return false;
    }
    
    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public NodeFilter getFilter() {
        return filter;
    }

    public void setFilter(NodeFilter filter) {
        this.filter = filter;
    }
}//~END
