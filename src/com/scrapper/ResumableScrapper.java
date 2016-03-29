package com.scrapper;

import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.util.Map;
import org.htmlparser.util.ParserException;

/**
 * @(#)ResumableScrapper.java   29-Nov-2013 23:29:40
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
public class ResumableScrapper extends Scrapper implements Resumable {
    
    private ResumeHandler resumeHandler;
    
    public ResumableScrapper() { }
    
    public ResumableScrapper(CapturerContext context) {
        super(context);
    }
    
    @Override
    public Map extractData (PageNodes page) throws ParserException {

        Map extractedData = super.extractData(page);
        
        if(extractedData != null && !extractedData.isEmpty() && this.isResumable()) {

            if(resumeHandler != null) {
                resumeHandler.updateStatus(page);
            }
        }
        
        return extractedData;
    }
    
    @Override
    protected boolean isAttempted(String link) {
        
        return super.isAttempted(link) || 
                (this.isResume() && this.isInDatabase(link));
    }
    
    protected boolean isInDatabase(String link) {
        boolean found = false;
        if(resumeHandler != null) {
            found = resumeHandler.isInDatabase(link);
        }
        return found;
    }
    
    public String getSitename() {
        return this.getContext().getConfig().getName();
    }

    @Override
    public boolean isResumable() {
        return true;
    }

    @Override
    public boolean isResume() {
        return false;
    }

    @Override
    public String getTaskName() {
        return ResumableScrapper.class.getName()+"(Scrapper for site: "+this.getSitename()+")";
    }

    @Override
    public void print(StringBuilder builder) {
        super.print(builder);
        builder.append(", site: ").append(this.getSitename());
        builder.append(", resume: ").append(this.isResume());
        builder.append(", resumable: ").append(this.isResumable());
    }

    public ResumeHandler getResumeHandler() {
        return resumeHandler;
    }

    public void setResumeHandler(ResumeHandler resumeHandler) {
        this.resumeHandler = resumeHandler;
    }
}
