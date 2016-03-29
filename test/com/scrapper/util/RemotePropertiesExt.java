package com.scrapper.util;

import com.bc.manager.util.PropertiesExt;
import com.bc.util.XLogger;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @(#)RemotePropertiesExt.java   02-Dec-2013 17:37:48
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
public class RemotePropertiesExt extends PropertiesExt {

    public RemotePropertiesExt() { }
    
    public RemotePropertiesExt(String file) 
            throws IOException {
        super(file);
    }

    public RemotePropertiesExt(String defaultFile, String file
            ) throws IOException {
        super(defaultFile, file);
    }

    public RemotePropertiesExt(String defaultFile, String file, 
            String description) throws IOException {
        super(defaultFile, file, description);
    }
    
    @Override
    public void load(Properties props, String path) throws IOException {

        if(path == null) return;
        
        MyFTPClient ftp = new MyFTPClient();
        
        boolean success = ftp.downloadProperties(props, path, true);
        
XLogger.getInstance().log(Level.FINER, "Downloaded: {0}, Path: {1}", 
        this.getClass(), success, path);
    }
    
    @Override
    protected void save(Properties props, String path) throws IOException {

        MyFTPClient ftp = new MyFTPClient();
        
        boolean success = ftp.uploadProperties(props, path, true);
        
XLogger.getInstance().log(Level.FINER, "Uploaded: {0}, Path: {1}", 
        this.getClass(), success, path);
    }
}
