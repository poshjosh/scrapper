package com.scrapper;

import java.io.FileFilter;
import java.util.Set;

/**
 * @(#)PathContext.java   29-Apr-2013 00:01:33
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
public interface PathContext {

    String getRootPath();

    String getRealPath();
    
    String getBaseURL();

    /**
     * @return A String of the form <tt>{protocol}://{authority}/{context}</tt>.
     * i.e {baseURL}{context}<br/><br/>
     * <b>Example</b> <tt>'http://www.looseboxes.com/loosebox'</tt><br/><br/>
     */
    String getContextURL();
    
    String normalize(String path);
        
    String getContext();

    /**
     * Given a web application deployed at <tt>C:/sites/public_html/mywebapp</tt>,
     * for an input of <tt>/index.html</tt> or <tt>/mywebapp/index.html</tt> this
     * method will return <tt>C:/sites/public_html/mywebapp/index.html</tt>
     */
    String getPath(String relativePath);

    Set<String> getPaths(String relativeDirPath, FileFilter filter);

    /**
     * Takes an absolute path and constructs a relative path
     * The input path must begin with {@linkplain #getRealPath()}.
     * @see #getRealPath()
     */
    String getRelativePath(String absolutePath);

    /**
     * Takes a URL and constructs a relative URL.
     * The input URL must begin with {@linkplain #getContextURL()}.
     * @see #getContextURL()
     */
    String getRelativeURL(String url);

    /**
     * Input <tt>String</tt> must begin with a '/'<br/>
     * @param relativePath
     * @return The URL constructed from concatenating the {@linkplain #baseURL}
     * and the input argument.
     */
    String getURL(String relativePath);
    
    Set<String> getURLs(String relativeDirPath, FileFilter filter);
}
