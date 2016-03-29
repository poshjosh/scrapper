package com.scrapper;

import com.bc.util.XLogger;
import java.io.File;
import java.io.FileFilter;
import java.util.Set;
import java.util.logging.Level;

/**
 * @(#)PathContextImpl.java   02-Dec-2013 14:43:22
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
public class PathContextImpl extends AbstractPathContext {
    
    private String context;
    
    /**
     * The real path C:/.../context\web
     * @see {@link javax.servlet.ServletContext#getRealPath(java.lang.String)}
     */
    private String realPath;

    /**
     * The parent folder of {@linkplain #realPath}
     */
    private String rootPath;

    public PathContextImpl (String path) { 
        this.init(path);
    }
    
    private void init(String path) {
        
        if(path == null) {
            throw new NullPointerException();
        }

        realPath = path;
        
        File dir = new File(realPath);
        
        if(!dir.isDirectory()) {
            throw new RuntimeException("Installation dir not found: "+realPath);
        }
        
        this.context = File.separator + dir.getName();
        
        rootPath = dir.getParent();
        
XLogger.getInstance().log(Level.INFO, 
"Root path: {0}, Context: {1}", this.getClass(), rootPath, context);
    }

    @Override
    public String getBaseURL() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getRealPath() {
        return realPath;
    }

    @Override
    public String getRootPath() {
        return rootPath;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public Set<String> getPaths(String relativeDirPath, FileFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRelativePath(String absolutePath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
