package com.scrapper;

import com.bc.manager.Formatter;
import com.bc.util.XLogger;
import com.scrapper.util.PageNodes;
import com.scrapper.util.Util;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;


/**
 * @(#)PageDataConsumerImpl.java   08-Dec-2014 19:01:49
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public abstract class AbstractPageDataConsumer 
        implements PageDataConsumer, Serializable {
    
    /**
     * The minimum amount of data required to make an extract/search result valid
     */
    private int minimumParameters;
    
    private String defaultTableName;
    
    private Formatter<Map<String, Object>> formatter;
    
    public AbstractPageDataConsumer() { }
    
    protected abstract boolean doConsume(PageNodes page, Map data);
    
    @Override
    public boolean consume(PageNodes page, Map data) {

XLogger.getInstance().log(Level.FINEST, " URL: {0}\nData: {1}", 
        this.getClass(), page==null?null:page.getURL(), data);
        
        // This comes before format. format may add entries to the data
        // there by increase the size of the data and making it acceptable
        //
        if(!this.accept(page, data)) {
            return false;
        }
        
        data = this.format(page, data);
        
XLogger.getInstance().log(Level.FINEST, "After all formats, parameters: {0}",
        this.getClass(), data);        

        return doConsume(page, data);
    }
    
    public boolean accept(PageNodes page, Map data) {
        
        if(data == null) {
            throw new NullPointerException();
        }
        
        boolean accept = data.size() >= minimumParameters;
            
        if(!accept) {
XLogger.getInstance().log(Level.FINE, "Insufficient data: {0}", 
        this.getClass(), data.keySet());            
        }

        return accept;
    }
    
    public Map format(PageNodes page, Map data) {
    
        this.addTableName(page, data);
        
        this.addExtraDetails(page, data);
        
        if(formatter != null) {
            data = formatter.format(data);
        }

        return data;
    }
    
    protected void addTableName(PageNodes page, Map<String, Object> data) {
        
        final String tablenameKey = this.getTableNameKey();
        
        // The table name in the extract supercedes the default
        //
        final String tablenameVal = Util.getTableValue(data, this.getDefaultTableName());
        
XLogger.getInstance().log(Level.FINER, "Adding: {0}={1}", this.getClass(), tablenameKey, tablenameVal);
        
        data.put(tablenameKey, tablenameVal);
    }
    
    protected void addExtraDetails(PageNodes page, Map<String, Object> data) {

        if(page == null) {
            return;
        }
        
        String url = page.getURL();
        
        Object value = data.get("extraDetails");
        
        if(value == null) {
XLogger.getInstance().log(Level.FINER, "Adding extraDetails: {0}", this.getClass(), url);            
            data.put("extraDetails", "url="+url);
        }else{
            Map temp = Util.getParameters(value.toString(), "&");
            temp.put("url", url);
            value = Util.appendQuery(temp, null);
XLogger.getInstance().log(Level.FINER, "Updated extraDetails: {0}", this.getClass(), temp);            
            data.put("extraDetails", value);
        }
    }
    
    public String getTableNameKey() {
        return AppProperties.getProperty(AppProperties.TABLENAME_KEY);
    }

    public int getMinimumParameters() {
        return minimumParameters;
    }

    public void setMinimumParameters(int minimumParameters) {
        this.minimumParameters = minimumParameters;
    }

    public Formatter<Map<String, Object>> getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter<Map<String, Object>> formatter) {
        this.formatter = formatter;
    }

    public String getDefaultTableName() {
        return defaultTableName;
    }

    public void setDefaultTableName(String defaultTableName) {
        this.defaultTableName = defaultTableName;
    }
}

