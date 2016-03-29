package com.scrapper.util;

import com.bc.util.XLogger;
import com.scrapper.AppProperties;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;























public class TableRequestTimes
  implements Serializable
{
  private static Map<String, Map<String, Integer>> allRequestTimes;
  
  public String getFileName()
  {
    return "SearchSites.timetaken.map";
  }
  
  public Map<String, Integer> getRequestTimes(String cat) {
    Map<String, Integer> tableRequestTimes = (Map)getAllRequestTimes().get(cat);
    if (tableRequestTimes == null) {
      tableRequestTimes = new HashMap();
      getAllRequestTimes().put(cat, tableRequestTimes);
    }
    return tableRequestTimes;
  }
  
  private Map<String, Map<String, Integer>> getAllRequestTimes()
  {
    if (allRequestTimes == null)
    {
      final String fname = getFileName();
      
      try
      {
        allRequestTimes = (Map)readObject(fname);
        
        if (getBoolean("siteSearch.refreshRequestTimes"))
        {
          allRequestTimes.clear();
          
          writeReqTimes(fname, allRequestTimes);
        }
      } catch (ClassNotFoundException e) {
        XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
      } catch (IOException ioe) {
        XLogger.getInstance().logSimple(Level.WARNING, getClass(), ioe);
      }
      
      if (allRequestTimes == null)
      {
        allRequestTimes = new HashMap();
      }
      
      XLogger.getInstance().log(Level.FINER, "Request times: {0}", getClass(), allRequestTimes);
      
      Thread updateRequestTimes = new Thread()
      {
        public synchronized void run() {
          try {
            if ((TableRequestTimes.allRequestTimes == null) || (TableRequestTimes.allRequestTimes.isEmpty())) {
              return;
            }
            
            TableRequestTimes.this.writeReqTimes(fname, TableRequestTimes.allRequestTimes);
          }
          catch (RuntimeException e) {
            XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
          }
          
        }
      };
      Runtime.getRuntime().addShutdownHook(updateRequestTimes);
    }
    
    return allRequestTimes;
  }
  
  private void writeReqTimes(String fname, Map m) {
    try {
      XLogger.getInstance().log(Level.INFO, "Saving request times: {0}", getClass(), m);
      if ((m == null) || (m.isEmpty())) {
        return;
      }
      writeObject(fname, m);
    } catch (IOException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
  }
  
  private boolean getBoolean(String name) {
    String value = AppProperties.getProperty(name);
    return Boolean.parseBoolean(value);
  }
  
  private Object readObject(String source) throws ClassNotFoundException, IOException
  {
    
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    ObjectInputStream ois = null;
    
    try
    {
      fis = new FileInputStream(source);
      bis = new BufferedInputStream(fis);
      ois = new ObjectInputStream(bis);
      
      return ois.readObject();
    }
    catch (IOException e)
    {
      throw e;
    }
    finally
    {
      if (ois != null) try { ois.close(); } catch (IOException e) {}
      if (bis != null) try { bis.close(); } catch (IOException e) {}
      if (fis != null) try { fis.close();
        }
        catch (IOException e) {}
    }
  }
  
  private void writeObject(String destination, Object obj) throws FileNotFoundException, IOException
  {
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    ObjectOutputStream oos = null;
    
    try
    {
      fos = new FileOutputStream(destination);
      bos = new BufferedOutputStream(fos);
      oos = new ObjectOutputStream(bos);
      
      oos.writeObject(obj); return;
    }
    catch (IOException e)
    {
      throw e;
    }
    finally
    {
      if (oos != null) try { oos.close(); } catch (IOException e) { Logger.getLogger(getClass().getName()).log(Level.WARNING, "", e); }
      if (bos != null) try { bos.close(); } catch (IOException e) { Logger.getLogger(getClass().getName()).log(Level.WARNING, "", e); }
      if (fos != null) try { fos.close(); } catch (IOException e) { Logger.getLogger(getClass().getName()).log(Level.WARNING, "", e);
        }
    }
  }
}
