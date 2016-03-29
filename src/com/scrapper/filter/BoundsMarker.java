package com.scrapper.filter;

import com.bc.util.XLogger;
import com.scrapper.HasBounds;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import com.scrapper.filter.FilterFactory.FilterType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

/**
 * @(#)BoundsMarker.java   03-Oct-2013 23:39:59
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
public class BoundsMarker 
        implements HasBounds, Serializable {

    private boolean started;
    
    private boolean done;
    
    private String id;
    
    private NodeFilter startAtFilter;
    
    private NodeFilter stopAtFilter;
    
    public BoundsMarker() { }
    
    public BoundsMarker(String id, NodeFilter startAt, NodeFilter stopAt) {
        this.id = id;
        this.startAtFilter = startAt;
        this.stopAtFilter = stopAt;
    }
    
    public static BoundsMarker newInstance(JsonConfig config, String propertyKey) {
        
        if(propertyKey == null || propertyKey.isEmpty()) {
            throw new NullPointerException();
        }

        FilterType [] types = FilterFactory.getTagNameAndAttributesFilter(config, new Object[]{propertyKey});
        
        NodeFilter startAtFilter = getGroup(config, propertyKey, FilterFactory.START_AT, types);
        
        NodeFilter stopAtFilter = getGroup(config, propertyKey, FilterFactory.STOP_AT, types);

        if(startAtFilter == null && stopAtFilter == null) {
            return null;
        }
        
        BoundsMarker instance = new BoundsMarker(propertyKey, startAtFilter, stopAtFilter);

        return instance;
    }
    
    @Override
    public void reset() {
        this.done = false;
        this.started = false;
    }
    
    public void visitStartTag(Node node) {
        
        if(this.isDone()) return;
        
        if(!this.isStarted()) {
            started = startAtFilter == null || startAtFilter.accept(node);
            if(started) {
XLogger.getInstance().log(Level.FINER, "Started at Node: {0}", 
        this.getClass(), node);                
            }
        }else{
            done = stopAtFilter != null && stopAtFilter.accept(node);
            if(done) {
XLogger.getInstance().log(Level.FINER, "Ended at Node: {0}", 
        this.getClass(), node);                
            }
        }
    }
    
    private static NodeFilter getGroup(JsonConfig props, 
            String pathElement0, Object pathElement1, FilterType [] types) {
        
        int maxFiltersPerKey = props.getInt(Config.Extractor.maxFiltersPerKey);
        
        ArrayList<NodeFilter> filters = new ArrayList<>();
        
        Object [] fullPath = {pathElement0, null};
        
        for(int i=0; i<maxFiltersPerKey; i++) {
            
            // Add the last path component
            fullPath[fullPath.length-1] = pathElement1.toString() + i;
            
            NodeFilter filter = FilterFactory.get(props, fullPath, types, false);
            
            if(filter == null) continue;
            
            filters.add(filter);
        }

        return FilterFactory.get(filters, true);
    } 
    
    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }

    public NodeFilter getStartAtFilter() {
        return startAtFilter;
    }

    public void setStartAtFilter(NodeFilter startAtFilter) {
        this.startAtFilter = startAtFilter;
    }

    public NodeFilter getStopAtFilter() {
        return stopAtFilter;
    }

    public void setStopAtFilter(NodeFilter stopAtFilter) {
        this.stopAtFilter = stopAtFilter;
    }
}

