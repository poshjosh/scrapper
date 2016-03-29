package com.scrapper.util;

import com.bc.util.XLogger;
import com.ftpmanager.DefaultFTPClient;
import com.ftpmanager.Formatter;
import com.scrapper.AppProperties;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @(#)MyFTPClient.java   02-Dec-2013 15:27:07
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
public class MyFTPClient extends DefaultFTPClient {         
    
    public MyFTPClient() { 
        this(AppProperties.instance());
    }
    
   /**
     * ERRORS ENCOUNTRED FOR SLOW CONNECTIONS
https://www.mail-archive.com/user@commons.apache.org/msg07896.html
            this.setConnectTimeout(30000);
            this.setDataTimeout(30000);
            this.setDefaultTimeout(30000);
            this.setControlKeepAliveTimeout(30000);
            this.setControlKeepAliveReplyTimeout(30000);
            this.setSoTimeout(30000);            
     */    
    
    public MyFTPClient(Properties props) { 
        
        // Very important ... the default was 0
        this.setBufferSize(8192);        
        
        this.setDataTimeout(30000);
        this.setDefaultTimeout(30000);
        
        int maxRetrials = Integer.parseInt(props.getProperty(AppProperties.FTP_MAXRETRIALS));
        this.setMaxRetrials(maxRetrials);
        
        int retrialInterval = Integer.parseInt(props.getProperty(AppProperties.FTP_RETRIALINTERVAL));
        this.setRetrialInterval(retrialInterval);
                
        String host = props.getProperty(AppProperties.FTP_HOST);
        this.setHost(host);
        
        int port = Integer.parseInt(props.getProperty(AppProperties.FTP_PORT)); 
        this.setPort(port);
        
        String user = props.getProperty(AppProperties.FTP_USER);
        this.setUser(user);
        
        String password = props.getProperty(AppProperties.FTP_PASS);
        this.setPassword(password);
        
        // This is normalized
        String ftpDir = normalize(props.getProperty(AppProperties.FTP_DIR));
        this.setFtpDir(ftpDir);
        
        Formatter formatter = new Formatter<String>() {
            @Override
            public String format(String path) {
                
                // Order of method call important
                
                path = MyFTPClient.this.normalize(path);
                
                int len = MyFTPClient.this.getFtpDir().length();
                
                int offset = path.indexOf(MyFTPClient.this.getFtpDir());
                
                if(offset != -1) {
                    path = path.substring(offset + len);
                }
                
                if(path.startsWith("/")) {
                    path = path.substring(1);
                }
XLogger.getInstance().log(Level.FINER, "FTP Path: {0}", this.getClass(), path);                
                return path;
            }
        };
        this.setFormatter(formatter);
    }
    
    private String normalize(String s) {
        return s.replace('\\', '/');
    }
}                
