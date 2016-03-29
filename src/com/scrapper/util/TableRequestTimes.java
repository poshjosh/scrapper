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

/**
 * @(#)TableRequestTimes.java   05-Nov-2014 11:01:06
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
public class TableRequestTimes implements Serializable {
    
    /**
     * Format:<br/><br/>
     * <pre>
     * {pages={site1=121000,site2=200000}},
     * {autos={site1=1200000,site2=2300100,site3=432000},
     * jobs={site1=1500000,site2=1550000,site3=402000},
     * gifts={site1=1201112,site2=2330101,site3=132000}}
     * </pre>
     */
    private static Map<String, Map<String, Integer>> allRequestTimes;
    
    public TableRequestTimes() { }
    
    public String getFileName() {
        // legacy bullshit
        return "SearchSites.timetaken.map";
    }

    public Map<String, Integer> getRequestTimes(String cat) {
        Map<String, Integer> tableRequestTimes = getAllRequestTimes().get(cat);
        if(tableRequestTimes == null) {
            tableRequestTimes = new HashMap<String, Integer>();
            getAllRequestTimes().put(cat, tableRequestTimes);
        }
        return tableRequestTimes;
    }

    private Map<String, Map<String, Integer>> getAllRequestTimes() {
        
        if(allRequestTimes == null) {
            
            final String fname = this.getFileName();
        
            try{
                
                allRequestTimes = (Map<String, Map<String, Integer>>)this.readObject(fname);
                
                if(getBoolean(AppProperties.REFRESH_REQUEST_TIMES)) {
                    
                    allRequestTimes.clear();
                    
                    this.writeReqTimes(fname, allRequestTimes);
                }
            }catch(ClassNotFoundException e) { 
                XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            }catch(IOException ioe) { 
                XLogger.getInstance().logSimple(Level.WARNING, this.getClass(), ioe);
            }
            
            if(allRequestTimes == null) {
                
                allRequestTimes = new HashMap<String, Map<String, Integer>>();
                
            }
XLogger.getInstance().log(Level.FINER, "Request times: {0}", this.getClass(), allRequestTimes);        

            Thread updateRequestTimes = new Thread() {
                @Override
                public synchronized void run() {
                    try{
                        if(allRequestTimes == null || allRequestTimes.isEmpty()) {
                            return;
                        }
                        
                        TableRequestTimes.this.writeReqTimes(fname, allRequestTimes);
                        
                    }catch(RuntimeException e) {
                        XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
                    }
                }
            };

            Runtime.getRuntime().addShutdownHook(updateRequestTimes);
        }
        
        return allRequestTimes;
    }
    
    private void writeReqTimes(String fname, Map m) {
        try{
XLogger.getInstance().log(Level.INFO, "Saving request times: {0}", this.getClass(), m);                    
            if(m == null || m.isEmpty()) {
                return;
            }
            this.writeObject(fname, m);
        }catch(IOException e) { 
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
    }
    
    private boolean getBoolean(String name) {
        String value = AppProperties.getProperty(name);
        return Boolean.parseBoolean(value);
    }
    
    private Object readObject(String source) throws ClassNotFoundException, IOException {
        
        Object result = null;
        
        FileInputStream     fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream   ois = null;
        
        try {

            fis = new FileInputStream(source);
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);

            result = ois.readObject();
        
        }catch(IOException e) {
            
            throw e;
        
        }finally {
        
            if (ois != null) try { ois.close(); }catch(IOException e) {}
            if (bis != null) try { bis.close(); }catch(IOException e) {}
            if (fis != null) try { fis.close(); }catch(IOException e) {}
        }
        
        return result;
    }

    private void writeObject(String destination, Object obj) throws FileNotFoundException, IOException {
        
        FileOutputStream     fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream   oos = null;
        
        try{
            
            fos = new FileOutputStream(destination);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);

            oos.writeObject(obj);
        
        }catch(IOException e) {
            
            throw e;
        
        }finally {
        
            if (oos != null) try { oos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
            if (bos != null) try { bos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
            if (fos != null) try { fos.close(); }catch(IOException e) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e); }
        }
    }
}
