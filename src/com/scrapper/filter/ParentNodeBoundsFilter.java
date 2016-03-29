package com.scrapper.filter;

import com.bc.json.config.JsonConfig;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;

/**
 * @(#)ParentNodeBoundsFilter.java   04-Oct-2013 04:25:58
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
public class ParentNodeBoundsFilter extends BoundsFilter {

    /**
     * Create a {@link com.scrapper.filter.ParentNodeBoundsFilter} using properties
     * from the specified {@link com.bc.json.config.JsonConfig} 
     * @param props The properties to use in creating this filter.
     * @param parentFilterIsDirectMode  boolean. If true, the filter will only 
     * accept Nodes which matches the target Node. Otherwise the filter will accept:
     * <br/>
     * <ul>
     *   <li>Any tag which contains the target</li>
     *   <li>Any tag which matches the target</li>
     *   <li>Any tag which the target contains</li>
     * </ul>
     */
    public ParentNodeBoundsFilter(JsonConfig props, boolean parentFilterIsDirectMode) { 

        this.setId(FilterFactory.PARENT);
        
        NodeFilter filter = FilterFactory.newDefaultFilter(props, new Object[]{this.getId()});
        this.setFilter(filter);
        
        BoundsMarker boundsMarker = BoundsMarker.newInstance(props, this.getId());
        if(boundsMarker != null) {
            this.setStartAtFilter(boundsMarker.getStartAtFilter());
            this.setStopAtFilter(boundsMarker.getStopAtFilter());
        }

        this.setStrict(false);
        
        if(!parentFilterIsDirectMode) {
            
            NodeFilter target = this.getFilter();
            
            // Indirect mode
            
            // Accept any tag which contains the target
            // Accept any tag which matches the target
            // Accept any tag which the target contains
            
            HasChildFilter hasChild = new HasChildFilter();
            hasChild.setChildFilter(target);
            hasChild.setRecursive(true);

            HasParentFilter hasParent = new HasParentFilter();
            hasParent.setParentFilter(target);
            hasParent.setRecursive(true);

            OrFilter orFilter = new OrFilter();
            orFilter.setPredicates(new NodeFilter[]{
                    target, hasChild, hasParent});

            this.setFilter(orFilter);
        }
    }
    
    /**
     * @deprecated 
     * Use method {@linkplain #getFilter()} 
     */
    public NodeFilter getParentFilter() {
        return this.getFilter();
    }

    /**
     * @deprecated 
     * Use method {@linkplain #setFilter(org.htmlparser.NodeFilter)} 
     */
    public void setParentFilter(NodeFilter filter) {
        this.setFilter(filter);
    }
}//~END
/**
 * 
            if(parentFilter == null) {
                if(parentStartStopFilter == null) {
                    if(globalStartStopFilter == null) {
                        return true;
                    }else{
                        return globalStartStopFilter.accept(node);
                    }
                }else{
                    if(globalStartStopFilter == null) {
                        return parentStartStopFilter.accept(node);
                    }else{
                        return parentStartStopFilter.accept(node) && globalStartStopFilter.accept(node);
                    }
                }
            }else{
                if(parentStartStopFilter == null) {
                    if(globalStartStopFilter == null) {
                        return parentFilter.accept(node);
                    }else{
                        return parentFilter.accept(node) || globalStartStopFilter.accept(node);
                    }
                }else{
                    if(globalStartStopFilter == null) {
                        return parentFilter.accept(node) && parentStartStopFilter.accept(node);
                    }else{
                        return parentFilter.accept(node) && parentStartStopFilter.accept(node) && globalStartStopFilter.accept(node);
                    }
                }
            }
 * 
 */