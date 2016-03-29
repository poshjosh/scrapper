package com.scrapper.search;

import com.bc.jpa.EntityController;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.CapturerApp;
import com.scrapper.ContextDataConsumer;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;











public abstract class DefaultSearchDataConsumer
  extends ContextDataConsumer
{
  private boolean ignoreMatchingRecords;
  private String database;
  private String productTable;
  
  public DefaultSearchDataConsumer(String database, String tableName, String sitename)
  {
    this(database, tableName, CapturerApp.getInstance().getConfigFactory().getContext(sitename));
  }
  

  public DefaultSearchDataConsumer(String database, String tableName, CapturerContext context)
  {
    super(context);
    
    this.database = database;
    this.productTable = tableName;
    XLogger.getInstance().log(Level.FINER, "Database: {0}, table: {1}", getClass(), database, tableName);
  }
  




  protected abstract Map findMatch(Map paramMap)
    throws SQLException;
  



  protected abstract EntityController getEntityController();
  



  protected void addTableName(PageNodes page, Map<String, Object> data)
  {
    data.put(getTableNameKey(), this.productTable);
    
    super.addTableName(page, data);
  }
  

  protected boolean doConsume(PageNodes page, Map data)
  {
    Level level = Level.FINER;
    
    if (XLogger.getInstance().isLoggable(level, getClass())) {
      XLogger.getInstance().log(level, "Site: {0}, table: {1}, extract: {2}", getClass(), getContext().getConfig().getName(), getProductTable(), data == null ? null : data.keySet());
    }
    

    Map matchingRecord = null;
    if (!this.ignoreMatchingRecords) {
      try {
        matchingRecord = findMatch(data);
      } catch (SQLException e) {
        XLogger.getInstance().log(Level.WARNING, "Caught exception while checking for matching records: {0}", getClass(), e.toString());
      }
    }
    


    EntityController ec = getEntityController();
    
    Object productId = null;
    
    if (matchingRecord == null)
    {
      Object entity = ec.persist(data);
      
      if (entity != null)
      {
        productId = (Integer)ec.getId(entity);
        
        XLogger.getInstance().log(Level.FINER, "Inserted {0} with columns {1}", getClass(), productId, data.keySet());
        

        data.put(ec.getIdColumnName(), productId);
      }
    }
    else
    {
      productId = matchingRecord.get(ec.getIdColumnName());
      
      XLogger.getInstance().log(Level.FINER, "Record already exists in the database at {0} with columns {1}", getClass(), productId, matchingRecord.keySet());
      

      data = matchingRecord;
      XLogger.getInstance().log(Level.FINE, "A matching record already exists in the database", getClass());
    }
    
    boolean success = productId != null;
    
    if (XLogger.getInstance().isLoggable(level, getClass())) {
      XLogger.getInstance().log(level, "Success: {0}, data: {1}", getClass(), Boolean.valueOf(success), data == null ? null : data.keySet());
    }
    
    return success;
  }
  
  public String getDatabase() {
    return this.database;
  }
  
  public String getProductTable() {
    return this.productTable;
  }
  
  public boolean isIgnoreMatchingRecords() {
    return this.ignoreMatchingRecords;
  }
  
  public void setIgnoreMatchingRecords(boolean ignoreMatchingRecords) {
    this.ignoreMatchingRecords = ignoreMatchingRecords;
  }
}
