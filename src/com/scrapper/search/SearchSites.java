package com.scrapper.search;

import com.scrapper.PageDataConsumer;
import com.scrapper.util.ManagedTasks;
import com.scrapper.util.PageNodes;
import java.util.List;
import java.util.Map;

/**
 * @(#)SearchSites.java   15-Mar-2014 18:13:29
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * This task shuts down after the specified {@link #getTimeout()}
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public abstract class SearchSites extends ManagedTasks<SearchSite> {
    
    private String searchText;
    
    private Map parameters;

    public SearchSites() { }
    
    public SearchSites(
            List<String> sitenames, final String productTable, 
            final Map parameters, final String searchText) {

        this.searchText = searchText;
        
        this.parameters = parameters;
        
        // We only load tasks after iniitalizing the above parameters
        //
        this.loadTasks(productTable, sitenames);
    }
    
    protected abstract PageDataConsumer newDataConsumer(SearchSite searchSite);
    
    @Override
    protected SearchSite newTask(String site) {

        final SearchSite searchSite = new SearchSite(site){
            @Override
            protected void preParse(String url) { 
                SearchSites.this.preTaskUpdateTimeTaken(this.getName());
            }
            @Override
            protected void postParse(PageNodes page) { 
                SearchSites.this.postTaskUpdateTimeTaken(
                        this.getName(), 
                        this.getLastRequestTime(),
                        this.getLastRequestTimeTaken());
            }
        };

        searchSite.update(this.getProductTable(),
                        this.getParameters(),
                        this.getSearchText());

        PageDataConsumer consumer = SearchSites.this.newDataConsumer(searchSite);
        
        searchSite.setDataConsumer(consumer);

        final int scrappLimit = this.computeLimitPerSite();

        searchSite.setScrappLimit(scrappLimit);
        
        return searchSite;
    }

    public Map getParameters() {
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
