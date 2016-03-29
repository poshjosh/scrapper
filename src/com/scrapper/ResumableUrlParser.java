package com.scrapper;

import com.bc.util.XLogger;
import com.scrapper.util.PageNodes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * @(#)ResumableUrlParser.java   28-Nov-2013 20:00:58
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
public class ResumableUrlParser extends URLParser implements Resumable {

    private String sitename;
    
    private ResumeHandler resumeHandler;

    public ResumableUrlParser() { 
        
        this(null, new ArrayList<String>());
    }
    
    public ResumableUrlParser(String sitename) { 
        
        this(sitename, new ArrayList<String>());
    }
    
    public ResumableUrlParser(String sitename, List<String> urls) { 
    
        super(urls);
XLogger.getInstance().log(Level.FINER, "Creating", this.getClass());
        
        this.sitename = sitename;
        
        if(ResumableUrlParser.this.isResume()) {

            if(this.resumeHandler != null) {
                // This is inclusive of urls from the database
                //
                List<String> inclusive = this.resumeHandler.getAllPendingUrls(urls);
                
                this.setPageLinks(Collections.synchronizedList(inclusive));
            }
        }
    }
    
    @Override
    protected void preParse(String url) { 

        if(this.sitename == null) {
            throw new NullPointerException("sitename == null");
        }

        if(!this.isResumable()) {
            return;
        }
        
XLogger.getInstance().log(Level.FINER, "Preparse URL: {0}", this.getClass(), url);

        if(resumeHandler != null) {
            resumeHandler.saveIfNotExists(url);
        }
    }
    
    @Override
    protected void postParse(PageNodes page) { 
    
        if(!this.isResumable()) {
            return;
        }

        if(resumeHandler != null) {
            resumeHandler.updateStatus(page);
        }
    }
    
    @Override
    protected boolean isAttempted(String link) {
        
        return super.isAttempted(link) || 
                (this.isResume() && this.isInDatabase(link));
    }
    
    protected boolean isInDatabase(String link) {
        
        if(this.sitename == null) {
            throw new NullPointerException("sitename == null");
        }
        
        boolean found = false;
        if(resumeHandler != null) {
            found = resumeHandler.isInDatabase(link);
        }
        
        return found;
    }
    
    /**
     * @return 
     * @see com.scrapper.Resumable#isResume() 
     */
    @Override
    public boolean isResume() {
        return false;
    }

    /**
     * @return 
     * @see com.scrapper.Resumable#isResumable() 
     */
    @Override
    public boolean isResumable() {
        return true;
    }

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = sitename;
    }

    public ResumeHandler getResumeHandler() {
        return resumeHandler;
    }

    public void setResumeHandler(ResumeHandler resumeHandler) {
        this.resumeHandler = resumeHandler;
    }
}
