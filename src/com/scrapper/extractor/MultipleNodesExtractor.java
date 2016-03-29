package com.scrapper.extractor;

import com.bc.webdatex.extractor.NodeExtractor;
import com.bc.webdatex.extractor.NodeExtractorIx;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.context.CapturerSettings;
import com.scrapper.filter.FilterFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.Translate;


/**
 * @(#)DefaultPageExtractor2.java   03-Oct-2015 23:55:13
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
public class MultipleNodesExtractor extends PageExtractor implements MultipleNodesExtractorIx {

    private Map<String, NodeExtractor> nodeVisitors;
    
    private Map<String, Object []> columns;
    
    private Map<String, MappingsExtractor> mappingsExtractors;
    
    private final CommentBoundsVisitor withinComments;
    
    public MultipleNodesExtractor(CapturerContext context) {
        
        super(context);

        JsonConfig config = context.getConfig();
        
        // This must come before creating the filters
        this.nodeVisitors = new HashMap<String, NodeExtractor>(){
            @Override
            public NodeExtractor put(String key, NodeExtractor value) {
                if(key == null || value == null) throw new NullPointerException();
                return super.put(key, value);
            }
        };

        this.columns = new HashMap<String, Object []>(){
            @Override
            public Object [] put(String key, Object[] value) {
                if(key == null || value == null) throw new NullPointerException();
                return super.put(key, value);
            }
        };
        
        // Add the parent node extractor
        //
        String parentNode = config.getString(FilterFactory.PARENT, "value");
        if(parentNode != null) {
            this.addExtractor(FilterFactory.PARENT);
        }
        
        int maxFiltersPerKey = config.getInt(Config.Extractor.maxFiltersPerKey);
        
        // Add the target node extractors
        //
        for(int i=0; i<maxFiltersPerKey; i++) {

            String propertyKey =  FilterFactory.TARGET + i;
            
            this.addExtractor(propertyKey);
            
            MappingsExtractor mappingsExt = MappingsExtractor.getInstance(propertyKey, config);
            
            if(mappingsExt == null) {
                continue;
            }
            
            if(this.mappingsExtractors == null) {
                this.mappingsExtractors = new HashMap<>();
            }
            this.mappingsExtractors.put(propertyKey, mappingsExt);
        }

        withinComments = new CommentBoundsVisitor();
    }
    
    @Override
    public void reset() {
        
        super.reset();
        
        this.withinComments.reset();
        
        for(NodeExtractorIx extractor:nodeVisitors.values()) {
            extractor.reset();
        }
    }
    
    @Override
    public boolean isSuccessfulCompletion() {
        Set<String> cols = null;
        try{
            cols = ((ScrapperConfig)this.getCapturerConfig()).getColumns();
        }catch(ClassCastException ignored) { }
        boolean output;
        if(cols != null) {
            output = this.getExtractedData().size() >= cols.size();
        }else{
            output = this.getFailedNodeExtractors().isEmpty();
        }
        return output;
    }
    
    @Override
    public void finishedParsing() {

        for(String name:this.nodeVisitors.keySet()) {
            
            NodeExtractorIx extractor = this.nodeVisitors.get(name);
            
            // Clear any unfinished business
            extractor.finishedParsing();
            
            Object [] cols = columns.get(name);
            
            boolean append = extractor.isConcatenateMultipleExtracts();
            
            if(cols != null) {

                for(Object column:cols) {
                    
XLogger.getInstance().log(Level.FINEST, "Extractor: {0}", this.getClass(), name);

                    this.add(column.toString(), extractor.getExtract(), append, false);
                }
            }
        }
XLogger.getInstance().log(Level.FINER, "Extractors: {0}, Extracted data: {1}", 
        this.getClass(), nodeVisitors.size(), this.getExtractedData().size());        
    }
    
    @Override
    public Set<String> getFailedNodeExtractors() {
        HashSet<String> failed = new HashSet<>();
        Set keys = this.getExtractedData().keySet();
        for(String key:this.nodeVisitors.keySet()) {
            NodeExtractorIx extractor = nodeVisitors.get(key);
            Object [] cols = columns.get(key);
            if(cols != null) {
                for(Object col:cols) {
                    if(!keys.contains(col)) {
                        failed.add(extractor.getId());
                        break;
                    }
                }
            }
        }
        return failed;
    }

    @Override
    public Set<String> getSuccessfulNodeExtractors() {
        Set<String> failed = this.getFailedNodeExtractors();
        Set<String> all = this.getNodeExtractorIds();
        all.removeAll(failed);
        return all;
    }
    
    @Override
    public Set<String> getNodeExtractorIds() {
        return new HashSet(this.nodeVisitors.keySet());
    }
    
    @Override
    public void visitTag(Tag tag) {

XLogger.getInstance().log(Level.FINER, "visitTag: {0}", this.getClass(), tag);        

        withinComments.visitTag(tag);
        
        if(this.rejectComment(tag)) {
            return;
        }
XLogger.getInstance().log(Level.FINER, "Extracting with: {0}", 
        this.getClass(), nodeVisitors.keySet());        
        
        for(String key:nodeVisitors.keySet()) {
            NodeExtractorIx extractor = nodeVisitors.get(key);
            extractor.visitTag(tag);
        }
    }
    
    @Override
    public void visitEndTag(Tag tag) {
        
XLogger.getInstance().log(Level.FINER, "visitEndTag: {0}", this.getClass(), tag);        

        withinComments.visitEndTag(tag);
        
        if(this.rejectComment(tag)) {
            return;
        }
        
        for(String key:this.nodeVisitors.keySet()) {
            NodeExtractorIx extractor = nodeVisitors.get(key);
            extractor.visitEndTag(tag);
        }
    }

    @Override
    public void visitStringNode(Text node){
        
XLogger.getInstance().log(Level.FINER, "visitStringNode: {0}", this.getClass(), node);        
        
        withinComments.visitStringNode(node);
        
        if(this.rejectComment(node)) {
            return;
        }
        
        for(String key:nodeVisitors.keySet()) {
            NodeExtractorIx extractor = nodeVisitors.get(key);
            extractor.visitStringNode(node);
        }

        if(this.mappingsExtractors == null) {
            return;
        }
        
        for(String id:mappingsExtractors.keySet()) {
            
            MappingsExtractor extractor = mappingsExtractors.get(id);
            
            Map m = extractor.extractData(Translate.decode(node.getText()));

            if(m != null) {
                
                for(Object key:m.keySet()) {

                    this.add(key.toString(), m.get(key), false, false);
                }
            }
        }
    }
    
    @Override
    public void visitRemarkNode(Remark node){
        
XLogger.getInstance().log(Level.FINER, "visitRemarkNode: {0}", this.getClass(), node);        
        
        withinComments.visitRemarkNode(node);
    }    
    
    @Override
    public NodeExtractor getExtractor(String id) {
        return this.nodeVisitors.get(id);
    }
    
    @Override
    public com.bc.webdatex.extractor.NodeExtractor createExtractor(String id) {
        
        com.bc.webdatex.extractor.NodeExtractor extractor = new com.bc.webdatex.extractor.NodeExtractor();

        CapturerSettings ss = this.getCapturerContext().getSettings();
        
        extractor.setAcceptScripts(false); 
        extractor.setAttributesToAccept(ss.getAttributesToAccept(id));
        extractor.setAttributesToExtract(ss.getAttributesToExtract(id));
//@update   PLEASE ENSURE ALL OTHER VALUES ARE FROM THE SETTINGS TOO 
        extractor.setConcatenateMultipleExtracts(ss.isConcatenateMultipleExtracts(id));  
        extractor.setEnabled(true);
//@update   PLEASE ENSURE ALL OTHER VALUES ARE FROM THE SETTINGS TOO
        extractor.setExtractAttributes(ss.isExtractAttributes(id));
        extractor.setId(id);
        extractor.setNodesToRetainAttributes(ss.getNodesToRetainAttributes(id));
        extractor.setNodeTypesToAccept(ss.getNodeTypesToAccept(id));
        extractor.setNodeTypesToReject(ss.getNodeTypesToReject(id));
        extractor.setNodesToAccept(ss.getNodesToAccept(id));
        extractor.setNodesToReject(ss.getNodeToReject(id));
        extractor.setPath(ss.getTransverse(id));
        extractor.setReplaceNonBreakingSpace(ss.isReplaceNonBreakingSpace(id));
//        extractor.setSeparator(???); // Default separator is used
        extractor.setTextToAccept(null);
        extractor.setTextToDisableOn(ss.getTextToDisableOn(id));
        extractor.setTextToReject(ss.getTextToReject(id));
//        extractor.setTransverse(???); // Same as setPath(Object[]) above
        return extractor;
    }
    
    private void addExtractor(String id) {
        
        CapturerSettings cs = this.getCapturerContext().getSettings();
        
        Object [] cols = cs.getColumns(id);
        
        if(cols == null) {
XLogger.getInstance().log(Level.FINE, "{0}.{1} == null", this.getClass(), id, Config.Extractor.columns);
        }else{
            this.columns.put(id, cols);
            com.bc.webdatex.extractor.NodeExtractor extractor = this.createExtractor(id);
            this.nodeVisitors.put(id, extractor);
        }

XLogger.getInstance().log(Level.FINER, "Added Extractor for property key: {0}", this.getClass(), id);        
    }
    
    private boolean rejectComment(Node node) {
        boolean reject = (withinComments.isStarted() && !withinComments.isDone());
        if(!reject) {
            reject = withinComments.isComment(node);
        }
        return reject;
    }
}//~END
