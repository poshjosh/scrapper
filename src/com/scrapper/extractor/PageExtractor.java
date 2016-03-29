package com.scrapper.extractor;

import com.bc.util.XLogger;
import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.CapturerSettings;
import com.scrapper.util.Util;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;

/**
 * @(#)PageExtractor.java   12-Dec-2013 23:05:08
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
public class PageExtractor 
        extends NodeListExtractor implements PageExtractorIx {

    private boolean done;
    
    private String pageTitle;

    private Tag titleTag;

    private boolean titleExtracted;
    
    private final CapturerContext context;

    public PageExtractor(CapturerContext context) {
        
        this.context = context;
        
XLogger.getInstance().log(Level.FINE, "Site name: {0}", 
        this.getClass(), context.getConfig().getName());                        
    }
    
    @Override
    public void reset() {
        super.reset();
        this.done = false;
        this.pageTitle = null;
        this.titleTag = null;
        this.titleExtracted = false;
    }

    @Override
    public void visitTag(Tag tag) {
        
        if(done) return;
        
XLogger.getInstance().log(Level.FINER, "visitTag: {0}", this.getClass(), tag);        

        super.visitTag(tag);

        if(tag.getTagName().equals("TITLE")) {
            this.titleTag = tag;
        }
    }

    @Override
    public void visitEndTag(Tag tag) {

        if(done) return;
        
XLogger.getInstance().log(Level.FINER, "visitEndTag: {0}", this.getClass(), tag);        

        super.visitEndTag(tag);

        if(tag.getTagName().equals("TITLE")) {
            this.titleTag = null;
        }
    }

    @Override
    public void visitStringNode(Text node){

        if(done) return;
        
XLogger.getInstance().log(Level.FINER, "#visitStringNode: {0}", this.getClass(), node);        

        super.visitStringNode(node);

        extractTitle(node);
    }

    @Override
    public void visitRemarkNode(Remark remark) {
        if(done) return;
        super.visitRemarkNode(remark);
    }
    
    private boolean extractTitle(Text node) {

        // Title will be extracted only once
        //
        if(!titleExtracted && this.withinTitleTag()) {

            // Setting this Tag to null signifies that it has been visited already
            //
            this.titleExtracted = true;

            this.doExtractTitle(node);

            this.titleTag = null;

            return true;
        }
        
        return false;
    }

    /**
     * @return  The Column name to which the specified value was paired 
     * before being added to the Map of extracted data
     */
    protected String add(String key, Object val) {
        return add(key, val, this.getCapturerSettings().isConcatenateMultipleExtracts(), true);
    }
    
    /**
     * @return  The Column name to which the specified value was paired 
     * before being added to the Map of extracted data
     */
    protected String add(String key, Object val, boolean append, boolean guessColumnNameFromKey) {
        
XLogger.getInstance().log(Level.FINER, "#add. Append: {0}, Key: {1}, Val: {2}", 
        this.getClass(), append, key, val);

        if(key == null || val == null) return null;

        if(key.trim().isEmpty() || val.toString().trim().isEmpty()) return null;

        String col = key;
        
        if(guessColumnNameFromKey) {
            
            Map keys = this.getCapturerConfig().getMap("keys");
            
            col = Util.findValueWithMatchingKey(keys, key);
            
XLogger.getInstance().log(Level.FINER, "#add. Key: {0}, Matching col: {1}", 
        this.getClass(), key, col);

        }
        if(col == null) {
            return null;
        }

        this.doAdd(col, val, append);
        
        return col;
    }

    private String doAdd(String col, Object val, boolean append) {

        Object oldVal = this.getExtractedData().get(col);

        if(oldVal == null) {
            
            // Note we use the column as key
            //
            this.getExtractedData().put(col, val);

XLogger.getInstance().log(Level.FINE, "#doAdd. Added: [{0}={1}]", 
        this.getClass(), col, val);

        }else{

            if(append) {

                if(!oldVal.equals(val)) {

                    String lineSep = this.getCapturerSettings().getLineSeparator();
                    String partSep = this.getCapturerSettings().getPartSeparator();
                    if(lineSep != null) {
                        val = val.toString().replace("\n", lineSep);
                    }

                    final String s = partSep != null ? partSep : "";

                    String newVal = new StringBuilder().append(oldVal).append(s).append(val).toString();

                    // Note we use the column as key
                    //
                    this.getExtractedData().put(col, newVal);
                    
XLogger.getInstance().log(Level.FINE, "#doAdd. Appended: [{0}={1}]", 
        this.getClass(), col, val);
                }
            }
        }
        return col;
    }

    protected boolean withinTitleTag() {
        return this.titleTag != null;
    }

    protected void doExtractTitle(Text node) {

        String title = this.getTitle(node);
        
        this.setPageTitle(title);
        
        node.setText(title);
    }

    protected String getTitle(Text node) {

        String val = node.getText();

        String defaultTitle = this.getCapturerSettings().getDefaultTitle();
        
        if(val == null || val.isEmpty()) return defaultTitle;
        
        if(defaultTitle == null || defaultTitle.isEmpty()) return val;

        return defaultTitle + " | " + val; 
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean isTitleExtracted() {
        return titleExtracted;
    }

    @Override
    public Tag getTitleTag() {
        return titleTag;
    }

    @Override
    public CapturerContext getCapturerContext() {
        return this.context;
    }
    
    @Override
    public CapturerSettings getCapturerSettings() {
        return this.context.getSettings();
    }
    
    @Override
    public JsonConfig getCapturerConfig() {
        return this.context.getConfig();
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public String getTaskName() {
        return this.getClass().getName();
    }
}//~END

