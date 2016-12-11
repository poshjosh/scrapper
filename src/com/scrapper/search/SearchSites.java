package com.scrapper.search;

import com.scrapper.PageDataConsumer;
import com.scrapper.util.ManagedTasks;
import java.util.List;
import java.util.Map;
import com.bc.dom.HtmlPageDom;

public abstract class SearchSites extends ManagedTasks<SearchSite> {
    
  private String searchText;
  private Map parameters;
  
  public SearchSites() {}
  
  public SearchSites(
          List<String> sitenames, String productTable, Map parameters, String searchText) {
      
    this.searchText = searchText;
    
    this.parameters = parameters;
    
    loadTasks(productTable, sitenames);
  }

  protected abstract PageDataConsumer newDataConsumer(SearchSite paramSearchSite);
  
  @Override
  protected SearchSite newTask(String site) {
      
    SearchSite searchSite = new SearchSite(site) {
        
      @Override
      protected void preParse(String url) {
        SearchSites.this.preTaskUpdateTimeTaken(getName());
      }
      
      @Override
      protected void postParse(HtmlPageDom page) {
        SearchSites.this.postTaskUpdateTimeTaken(getName(), getLastRequestTime(), getLastRequestTimeTaken());
      }
    };
    
    searchSite.update(getProductTable(), getParameters(), getSearchText());
    
    PageDataConsumer consumer = newDataConsumer(searchSite);
    
    searchSite.setDataConsumer(consumer);
    
    int scrappLimit = computeLimitPerSite();
    
    searchSite.setScrappLimit(scrappLimit);
    
    return searchSite;
  }
  
  public Map getParameters() {
    return this.parameters;
  }
  
  public void setParameters(Map parameters) {
    this.parameters = parameters;
  }
  
  public String getSearchText() {
    return this.searchText;
  }
  
  public void setSearchText(String searchText) {
    this.searchText = searchText;
  }
}
