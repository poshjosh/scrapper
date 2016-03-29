package com.scrapper.search;

import com.bc.util.XLogger;
import com.bc.jpa.EntityController;
import com.scrapper.CapturerApp;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

/**
 * @(#)DefaultDataConsumer.java   22-Oct-2014 20:11:53
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public abstract class DefaultSearchDataConsumer extends com.scrapper.ContextDataConsumer {

    private boolean ignoreMatchingRecords;
    
    private String database;
    
    private String productTable;
    
    public DefaultSearchDataConsumer(String database, String tableName, String sitename) { 
        
        this(database, tableName, 
                CapturerApp.getInstance().getConfigFactory().getContext(sitename));
    }
    
    public DefaultSearchDataConsumer(String database, String tableName, CapturerContext context) { 

        super(context);
        
        this.database = database;
        this.productTable = tableName;
XLogger.getInstance().log(Level.FINER, "Database: {0}, table: {1}", 
        this.getClass(), database, tableName);        
    }
    
    /**
     * Searches the database for a match to the input data
     * @throws SQLException 
     */
    protected abstract Map findMatch(Map data) throws SQLException;
    
    protected abstract EntityController getEntityController();

    @Override
    protected void addTableName(PageNodes page, Map<String, Object> data) {

        // IMPORTANT
        //
        // We add the table specified by the user during the search
        // This value supercedes the table specified in the config
        // 
        data.put(this.getTableNameKey(), productTable);
        
        super.addTableName(page, data);
    }

    @Override
    protected boolean doConsume(PageNodes page, Map data) {

final Level level = Level.FINER;
        
if(XLogger.getInstance().isLoggable(level, this.getClass()))        
XLogger.getInstance().log(level, "Site: {0}, table: {1}, extract: {2}", 
this.getClass(), this.getContext().getConfig().getName(), 
this.getProductTable(), data==null?null:data.keySet());                    

        Map matchingRecord = null;
        if(!ignoreMatchingRecords) {
            try{
                matchingRecord = this.findMatch(data);
            }catch(SQLException e) {
                XLogger.getInstance().log(Level.WARNING, 
                "Caught exception while checking for matching records: {0}", 
                this.getClass(), e.toString());
            }
        }

        EntityController ec = this.getEntityController();

        Object productId = null;
        
        if(matchingRecord == null) {

            Object entity = ec.persist(data);

            if(entity != null) {

                productId = (Integer)ec.getId(entity);

XLogger.getInstance().log(Level.FINER, "Inserted {0} with columns {1}", 
    this.getClass(), productId, data.keySet());                    

                data.put(ec.getIdColumnName(), productId);
            }
        }else{

            // We set the productId here to indicate success
            productId = matchingRecord.get(ec.getIdColumnName());

XLogger.getInstance().log(Level.FINER, "Record already exists in the database at {0} with columns {1}", 
    this.getClass(), productId, matchingRecord.keySet());                    

            data = matchingRecord;
XLogger.getInstance().log(Level.FINE, "A matching record already exists in the database", this.getClass());                    
        }    
        
        boolean success = productId != null;

if(XLogger.getInstance().isLoggable(level, this.getClass()))
XLogger.getInstance().log(level, "Success: {0}, data: {1}", 
this.getClass(), success, data==null?null:data.keySet());                    

        return success;
    }

    public String getDatabase() {
        return database;
    }

    public String getProductTable() {
        return productTable;
    }
    
    public boolean isIgnoreMatchingRecords() {
        return ignoreMatchingRecords;
    }

    public void setIgnoreMatchingRecords(boolean ignoreMatchingRecords) {
        this.ignoreMatchingRecords = ignoreMatchingRecords;
    }
}
