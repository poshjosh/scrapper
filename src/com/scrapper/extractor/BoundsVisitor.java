package com.scrapper.extractor;

import com.bc.util.XLogger;
import com.scrapper.HasBounds;
import com.bc.json.config.JsonConfig;
import com.scrapper.filter.FilterFactory.FilterType;
import com.scrapper.filter.BoundsMarker;
import com.scrapper.filter.FilterFactory;
import java.io.Serializable;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.visitors.NodeVisitor;

/**
 * @(#)BoundsVisitor.java   04-Oct-2013 05:21:48
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
public class BoundsVisitor extends NodeVisitor 
        implements HasBounds, Serializable {
    
    private boolean strict;
    
    private boolean started;
    
    private boolean done;
    
    private boolean endAtNext;
    
    private String id;
    
    private Tag startTag;
    
    private Tag endTag;
    
    private NodeFilter filter;
    
    private BoundsMarker boundsMarker;
    
    public BoundsVisitor(JsonConfig props, String propertyKey, FilterType [] types) { 
        
        id = propertyKey;
        
        filter = FilterFactory.get(props, propertyKey, types);

        boundsMarker = BoundsMarker.newInstance(props, propertyKey);
        
XLogger.getInstance().log(Level.FINER, "{0}-BoundsFilter filter: {1}, startStopFilter: {2}", 
this.getClass(), propertyKey, filter, boundsMarker);  
    }

    public BoundsVisitor(NodeFilter f0, BoundsMarker f1) { 
        this.filter = f0;
        this.boundsMarker = f1;
        if(this.boundsMarker != null) {
            this.id = f1.getId();
        }
    }
    
    public BoundsVisitor(String id, NodeFilter f0, NodeFilter startAt, NodeFilter stopAt) { 
        this.id = id;
        this.filter = f0;
        if(startAt != null && stopAt != null) {
            this.boundsMarker = new BoundsMarker(id, startAt, stopAt);
        }
    }
    
    @Override
    public void reset() {
        if(this.boundsMarker != null) {
            this.boundsMarker.reset();
        }
        this.done = false;
        this.endAtNext = false;
        this.endTag = null;
        this.startTag = null;
        this.started = false;
    }
    
    @Override
    public void visitTag(Tag tag) {
        
        // Order of method call important
        //
        if(this.endAtNext) {
            done = true;
        }
        
        if(this.isDone()) {
            return;
        }
        
        // Bounds marker must viist all tags as long as we are not yet done
        //
        if(boundsMarker != null) {
            boundsMarker.visitStartTag(tag);
        }
        
        if(this.isStarted()) { // already started
            return;
        }
        
        // Both filter and boundsMarker accept ONLY start tags
        //
        boolean boundsAccept;
        if(boundsMarker != null) {
            boundsAccept = boundsMarker.isStarted() && !boundsMarker.isDone();
        }else{
            boundsAccept = true;
        }
        
        boolean filterAccept = (filter == null || filter.accept(tag));

XLogger.getInstance().log(Level.FINEST, 
"{0}-BoundsVisitor, Accepted by, Filter: {1}, BoundsMarker: {2}, Node: {3}", 
this.getClass(), this.id, filterAccept, boundsAccept, tag.getTagName());        
        
        if(this.isStrict()) {
            // Good for extraction stage
            started = filterAccept && boundsAccept;
//            started (filterAccept || boundsEntered) && (filterAccept && !boundsExited);
        }else{
            // Good for filter stage
            started = filterAccept || boundsAccept;
        }
        
        if(started) { 

XLogger.getInstance().log(Level.FINER, "{0}-BoundsVisitor Started at: {1}", 
        this.getClass(), id, tag);            

            startTag = tag;

            endTag = tag.getEndTag();
            
            // We have found a NON CompositeTag
            // Since and endTag signifies the end and NON CompositeTags
            // don't have end tag, we have to end at the next node.
            //
            if(endTag == null) {
                this.endAtNext = true;
            }
        }
    }
    
    @Override
    public void visitEndTag(Tag tag) {
        
        // Order of method call important
        //
        if(this.endAtNext) {
            done = true;
        }
        
        if(this.isDone()) {
            return;
        }

        if(!this.isStarted()) {
            return;
        }
        
        boolean foundEnd = false;
        if(tag.equals(endTag)) {
            foundEnd = true;
        }

XLogger.getInstance().log(Level.FINER, "{0}-BoundsVisitor Done at: {1}", 
    this.getClass(), id, foundEnd);            

        done = foundEnd;
    }
    
    @Override
    public void visitStringNode(Text string) { 
        if(this.endAtNext) {
            done = true;
        }
    }

    @Override
    public void visitRemarkNode(Remark remark) { 
        if(this.endAtNext) {
            done = true;
        }
    }
    
    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    protected void setEndTag(Tag endTag) {
        this.endTag = endTag;
    }

    public Tag getEndTag() {
        return endTag;
    }

    protected void setStartTag(Tag startTag) {
        this.startTag = startTag;
    }

    public Tag getStartTag() {
        return startTag;
    }

    public BoundsMarker getBoundsMarker() {
        return boundsMarker;
    }

    public NodeFilter getFilter() {
        return filter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean isEndAtNext() {
        return endAtNext;
    }

    protected void setEndAtNext(boolean endAtNext) {
        this.endAtNext = endAtNext;
    }
}
