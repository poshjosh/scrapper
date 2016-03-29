/*
 * AbstractPathContext.java
 *
 * Created on May 12, 2011, 11:20 AM
 */

package com.scrapper;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author nonso
 */
public abstract class AbstractPathContext implements PathContext {

    public AbstractPathContext () { }
    
    public final boolean hasContext() {
        return !(getContext() == null || getContext().equals(""));
    }

    /**
     * @return A String of the form <tt>{protocol}://{authority}/{context}</tt>.
     * i.e {baseURL}{context}<br/><br/>
     * <b>Example</b> <tt>'http://www.looseboxes.com/loosebox'</tt><br/><br/>
     */
    @Override
    public String getContextURL() {
        if(this.getBaseURL() == null) {
            throw new UnsupportedOperationException("Cannot resolve Context URL. Base URL is null");
        }
        return getBaseURL() + getContext();
    }

    /**
     * Input <tt>String</tt> must begin with a '/'<br/>
     * @param relativePath
     * @return The URL constructed from concatenating the {@linkplain #baseURL}
     * and the input argument.
     */
    @Override
    public String getURL(String relativePath) {
        
        if(this.getBaseURL() == null) {
            throw new UnsupportedOperationException("Cannot resolve URL. Base URL is null");
        }

        relativePath = normalize(relativePath);

        validateRelativePath(relativePath);

        String output = null;

        if(relativePath.startsWith(getContext())) {
            output = getBaseURL() + relativePath;
        }else{
            output = getBaseURL() + getContext() + relativePath;
        }

//System.out.println(AbstractPathContext.class.getName()+". Output: "+output);
        return output;
    }

    /**
     * Given a web application deployed at <tt>C:/sites/public_html/mywebapp</tt>,
     * for an input of <tt>/index.html</tt> or <tt>/mywebapp/index.html</tt> this
     * method will return <tt>C:/sites/public_html/mywebapp/index.html</tt>
     */
    @Override
    public String getPath(String relativePath) {

        relativePath = normalize(relativePath);

        validateRelativePath(relativePath);

        if(hasContext() && relativePath.startsWith(getContext())) {
            return getRootPath() + relativePath;
        }else{
            return getRealPath() + relativePath;
        }
    }

    /**
     * Takes an absolute path and constructs a relative path
     * The input path must begin with {@linkplain #getRealPath()}.
     * @see #getRealPath() 
     */
    @Override
    public String getRelativePath(String absolutePath) {
        return getRelative(absolutePath, this.getRealPath());
    }

    /**
     * Takes a URL and constructs a relative URL.
     * The input URL must begin with {@linkplain #getContextURL()}.
     * @see #getContextURL() 
     */
    @Override
    public String getRelativeURL(String url) {
        return getRelative(url, this.getContextURL());
    }
    
    private String getRelative(String absolutePath, String base) {

        absolutePath = normalize(absolutePath);

// When deployed real path starts with '/'
//
//        if(absolutePath.charAt(0) == '/') {
//            throw new IllegalArgumentException();
//        }

        int index = absolutePath.indexOf(base);
        
        if(index < 0) throw new IllegalArgumentException("Input must start with: "+base+", Found: "+absolutePath);

        String output = absolutePath.substring(index + base.length());
//Logger.getLogger(this.getClass().getName()).info("Input: "+absolutePath+
//        "\nOutput: "+output);
        return output;
    }
    
    @Override
    public Set<String> getPaths(String relativeDirPath, FileFilter filter) {
        Set<String> paths = new HashSet<String>();
        File contextFolder = new File(getPath(relativeDirPath));
        addPaths(contextFolder, filter, paths);
        return paths;
    }

    @Override
    public Set<String> getURLs(String relativeDirPath, FileFilter filter) {
        Set<String> paths = new HashSet<String>();
        File contextFolder = new File(getURL(relativeDirPath));
        addPaths(contextFolder, filter, paths);
        return paths;
    }
    
    private void addPaths(File contextFolder,
            FileFilter filter, Set<String> paths) {
        File [] files = contextFolder.listFiles(filter);
        for(int i=0; i<files.length; i++) {
            if(files[i].isFile()) {
                paths.add(files[i].getPath());
            }else{
               addPaths(files[i], filter, paths);
            }
        }
    }

    @Override
    public String normalize(String path) {
        return path.replace('\\', '/').trim();
    }

    private void validateRelativePath(String relativePath) {
        if(relativePath.charAt(0) != '/') {
            String msg = "Expected: relative path beginning with '/', Found: " + relativePath;
            throw new IllegalArgumentException(msg);
        }
    }
}//END
