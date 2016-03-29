package com.scrapper;

import com.bc.webdatex.extractor.NodeExtractorIx;
import com.bc.webdatex.filter.NodeVisitingFilterIx;
import com.bc.webdatex.locator.TagLocatorIx;
import com.bc.manager.Filter;
import com.scrapper.util.PageNodes;
import com.bc.util.XLogger;
import com.bc.webdatex.extractor.NodeExtractor;
import com.bc.webdatex.locator.TransverseBuilder;
import com.scrapper.config.ScrapperConfig;
import com.scrapper.context.CapturerContext;
import com.scrapper.extractor.DataExtractor;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @(#)Scrapper.java   25-Aug-2013 21:06:30
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
public class Scrapper implements
        DataExtractor<PageNodes>, Serializable {
    
    private PageNodes source;

    /**
     * The tolerance at which the last Page was successfully scrapped
     */
    private float lastSuccessfulTolerance;
    
    private float maxTolerance = 0.3f;
    
    private int scrappCount;
    
    private Set<String> attempted;
    
    private Set<String> failed;
    
    private CapturerContext context;
    
    private transient NodeFilter filter;
    
    private transient Filter<String> urlFilter;
    
    private transient MultipleNodesExtractorIx extractor;
    
    private transient DataExtractor<String> urlDataExtractor;
    
    public Scrapper() {
        
        this(null);
    }
    
    public Scrapper(CapturerContext context) {

        this.context = context;

        this.attempted = new HashSet<>();
        
        this.failed = new HashSet<>();
    }
    
    @Override
    public Map extractData (PageNodes page) throws ParserException {

        try{
            
            String url = page.getURL();

            // Filter off URLs which will not be scrapped
            // This is different from method #isToBeCaptured 
            // which filters off URLs which will not be parsed
            // URLs are first parsed and subsequently scrapped
            // IMPORTANT to note that each URL parsed could spin off
            // many new URLs
            boolean toBeScrapped = this.isToBeScrapped(url);

            if(!toBeScrapped) {
                return null;
            }
            
            this.attempted.add(url);
            
            if(this.filter == null) {
                this.filter = context.getFilter();
            }else{
                if (filter instanceof HasBounds) {
                    ((HasBounds)filter).reset();
                }    
            }

            if(filter != null) {
XLogger.getInstance().log(Level.FINEST, "BEFORE filter. Nodes: {0}", 
    this.getClass(), page.getNodeList()==null?null:page.getNodeList().size());

                page.getNodeList().keepAllNodesThatMatch (filter, true);

XLogger.getInstance().log(Level.FINER, "AFTER filter. Nodes: {0}", 
    this.getClass(), page.getNodeList()==null?null:page.getNodeList().size());
            }

//XLogger.getInstance().log(Level.INFO, "{0}", 
//    this.getClass(), page.getNodeList().toHtml());
//try{
//    String html = page.getNodeList().toHtml();
//    new CharFileIO().write(html, 
//            System.getProperty("user.home")+"/page.html", true);
//}catch(IOException e) { }            

            if(page.getNodeList().size() == 0) {
XLogger.getInstance().log(Level.WARNING, "After filter, found 0 Nodes in page: {0}.", this.getClass(), page.getURL());            
                return null;
            }
            
            if(extractor == null) {
                extractor = context.getExtractor();
            }else{
                this.extractor.reset();
                
            }
            
XLogger.getInstance().log(Level.FINE, "Scrapped: {0}, scrapping: {1}",
        this.getClass(), this.scrappCount, page.getFormattedURL());        

            // Update the tolerance to the last known successful tolerance
            //
            this.updateTolerance(extractor, extractor.getNodeExtractorIds(), lastSuccessfulTolerance);
            
            Map extractedData = extractor.extractData(page.getNodeList());

            float tolerance = this.lastSuccessfulTolerance;
                
XLogger.getInstance().log(Level.FINE, "Tolerance: {0}, extracted: {1}", 
this.getClass(), this.lastSuccessfulTolerance, extractedData==null?null:extractedData.keySet());            

            // Retry a couple of times, increasing tolerance for each 
            // subsequent try
            //
            if(extractor != null) {
                
                boolean success = this.isSuccessfulCompletion(extractedData, extractor);
                
                if(!success) {
                    
                    this.retryExtractWithGenericAttributes(page, extractedData);
                    
                    success = this.isSuccessfulCompletion(extractedData, extractor);
                    
                    if(!success) {
                        
                        this.retryExtractsWithIncreasedTolerance(page, extractedData, tolerance);
                    }
                }
            }

            extractedData = this.addImagesAndUrlExtracts(url, extractor, extractedData);

            if(extractedData != null && !extractedData.isEmpty()) {
                
                ++scrappCount;
                
                if(this.isSuccessfulCompletion(extractedData, extractor)) {

                    lastSuccessfulTolerance = tolerance;
                }
            }else{

                failed.add(url);
            }

            return extractedData;
        
        }catch(ParserException e) {
            
            XLogger.getInstance().log(Level.WARNING, "Scrapp failed for: {0}. Reason: {1}", 
                    this.getClass(), page.getURL(), e);
            
            return null;
            
        }catch(RuntimeException e) {
            
            throw e;
        }
    }
    
    protected void retryExtractWithGenericAttributes(
            PageNodes page, Map extractedData) throws ParserException {
        
        Set<String> failedIds = extractor.getFailedNodeExtractors();

        if(failedIds == null || failedIds.isEmpty()) {
         
            return;
        }    
            
        TransverseBuilder tb = new TransverseBuilder();

        Set<String> allIds = extractor.getNodeExtractorIds();

        Iterator<String> iter = allIds.iterator();

        boolean [] states = new boolean[allIds.size()];

        List<String> [][] transverses = new List [allIds.size()][];
        
        try{
            
            for(int i=0; iter.hasNext(); i++) {

                String id = iter.next();

                NodeExtractor nodeExt = extractor.getExtractor(id);

                states[i] = nodeExt.isEnabled();
                transverses[i] = nodeExt.getTransverse();

                if(failedIds.contains(id) && transverses[i] != null) {

                    List<String> [] updatedTransverse = tb.format(
                            transverses[i], TransverseBuilder.TransverseFormat.GENERIC_ATTRIBUTES);

                    nodeExt.setTransverse(updatedTransverse);

                    Map moreData = extractor.extractData(page.getNodeList());

XLogger.getInstance().log(Level.FINE, "Extracted: {0}", this.getClass(), (moreData==null?null:moreData.keySet()));

                    if(moreData != null) {

                        extractedData.putAll(moreData);
                    }

                    nodeExt.setEnabled(true);

                }else{
                    nodeExt.setEnabled(false);
                }
            }
        }finally{
            
            iter = allIds.iterator();

            for(int i=0; iter.hasNext(); i++) {

                String id = iter.next();

                NodeExtractor nodeExt = extractor.getExtractor(id);
                
                nodeExt.setEnabled(states[i]);
                
                nodeExt.setTransverse(transverses[i]);
            }    
        }
    }
    
    protected void retryExtractsWithIncreasedTolerance(
            PageNodes page, Map extractedData, float tolerance) throws ParserException {
        
        do {

            if((tolerance) >= this.maxTolerance) {
                break;
            }

            tolerance += 0.1f;

            Map moreData = this.retryExtract(
                    page.getNodeList(), extractor, tolerance);

            if(moreData != null) {

                extractedData.putAll(moreData);
            }
        }while(!this.isSuccessfulCompletion(extractedData, extractor));
    }
    
    protected boolean isSuccessfulCompletion(Map extractedData, MultipleNodesExtractorIx pageExt) {
        Set<String> columns = null;
        try{
            columns = ((ScrapperConfig)this.getContext().getConfig()).getColumns();
        }catch(ClassCastException ignored) { }
        boolean output;
        if(columns != null) {
            output = extractedData.size() >= columns.size();
        }else{
            output = pageExt.getFailedNodeExtractors().isEmpty();
        }
XLogger.getInstance().log(Level.FINER, "Successful completion: {0}", 
        this.getClass(), output);
        return output;
    }
    
    protected Map retryExtract(NodeList nodes, 
            MultipleNodesExtractorIx pageExt, 
            float tolerance) throws ParserException {
    
        Set<String> failedIds = pageExt.getFailedNodeExtractors();

        Set<String> successIds = pageExt.getSuccessfulNodeExtractors();
        
XLogger.getInstance().log(Level.FINE, "Success Ids: {0}, Failed Ids: {1}", 
    this.getClass(), successIds, failedIds);

        // Only return false if retry failed
        //
        if(failedIds.isEmpty()) {
            return null;
        }
        
        float oldTolerance = this.updateTolerance(
                pageExt, failedIds, tolerance);
        
        try{

            if(oldTolerance != -1) {

                // Disable successful extractors
                for(String id:successIds) {
                    pageExt.getExtractor(id).setEnabled(false);
                }

                Map extractedData = pageExt.extractData(nodes);
                
XLogger.getInstance().log(Level.FINE, "Tolerance: {0}, extracted: {1}", 
this.getClass(), tolerance, extractedData==null?null:extractedData.keySet());
                
                return extractedData;
                
            }else{

                return null;
            }

        }finally{
            
            // Enable all
            //
            for(String id:pageExt.getNodeExtractorIds()) {
                pageExt.getExtractor(id).setEnabled(true);
            }
            
            // Reset tolerance 
            //
            this.updateTolerance(pageExt, failedIds, oldTolerance);
        }
    }

    /**
     * Update tolerance for the extractors with the specified ids
     * @return float the previous tolerance 
     */
    private float updateTolerance(
            MultipleNodesExtractorIx pageExt,
            Set<String> ids, float tolerance) throws ParserException {
    
        float oldTolerance = -1;
        
        if(ids.isEmpty()) {
            return oldTolerance;
        }

        for(String id:ids) {

            NodeExtractorIx nodeExt = pageExt.getExtractor(id);
            NodeVisitingFilterIx visitingFilter = nodeExt.getFilter();
            TagLocatorIx tagLocator = visitingFilter.getTagLocator();
            if(tagLocator != null) {
                oldTolerance = tagLocator.getTolerance();
                tagLocator.setTolerance(tolerance);
            }
        }

XLogger.getInstance().log(Level.FINER, "Updated tolerance from {0} to {1}", 
    this.getClass(), oldTolerance, tolerance);

        
        return oldTolerance;
    }
    
    protected boolean isToBeScrapped(String link) {

XLogger.getInstance().log(Level.FINEST, "@isToScrapped. Link: {0}", 
        this.getClass(), link);

        boolean toBeScrapped = true;
        
        if(this.urlFilter == null) {
            this.urlFilter = context.getScrappUrlFilter();
        }
        
        if(this.urlFilter != null) {

            toBeScrapped = this.urlFilter.accept(link);
            
XLogger.getInstance().log(Level.FINER, "URLFilter accepted: {0}, URL: {1}", 
    this.getClass(), toBeScrapped, link);
            
        }
        
        if(toBeScrapped) {
            
            toBeScrapped = !this.isAttempted(link);

if(!toBeScrapped) {            
XLogger.getInstance().log(Level.FINER, "Scrapp previously attempted {0}, for: {1}", 
    this.getClass(), !toBeScrapped, link);
}
        }

XLogger.getInstance().log(Level.FINE, "To be scrapped: {0}, url: {1}", 
        this.getClass(), toBeScrapped, link);        

        return toBeScrapped;
    }
    
    protected boolean isAttempted(String link) {
        
        boolean isAttempted = attempted.contains(link);

        return isAttempted;
    }
    
    private Map addImagesAndUrlExtracts(String url, MultipleNodesExtractorIx extractor, Map extractedData) {

        if(this.urlDataExtractor == null) {
            this.urlDataExtractor = context.getUrlDataExtractor();
        }
        
        if(this.urlDataExtractor != null) {
            
XLogger.getInstance().log(Level.FINER, "{0}. BEFORE urlExtractor parameters: {1}", 
        this.getClass(), extractedData); 

            // This extracts data from the url
            //
            Map m;
            try{
                
                m = this.urlDataExtractor.extractData(url);
                
                if(m != null) {
                    extractedData.putAll(m);
                }
            }catch(Exception e) {
                XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            }
XLogger.getInstance().log(Level.FINER, "{0}. AFTER urlExtractor parameters:\n{1}", 
        this.getClass(), extractedData);        
        }    

        // Add the images
        //
        if(extractor instanceof HasImages) {

            HasImages hasImages = ((HasImages)extractor);

            Set<String> imagePaths = hasImages.getImages();
            
XLogger.getInstance().log(Level.FINE, "Images: {0}", this.getClass(), imagePaths);

            if(imagePaths != null && !imagePaths.isEmpty()) {

                int i = 0;
                for(String image:imagePaths) {
                    extractedData.put("image" + (++i), image);
                }
                
                // *** VERY IMPORTANT ***
                //
                // Clear the original
                //
                imagePaths.clear();
            }
        }

XLogger.getInstance().log(Level.FINER, "Parameter Keys: {0}", 
        this.getClass(), extractedData.keySet());

//logger.log(Level.FINER, "{0}. Image 1: {1}", 
//        new Object[]{logger.getTaskName(), parameters.getObject("image1")});

XLogger.getInstance().log(Level.FINEST, "Parameters: {0}", 
        this.getClass(), extractedData);

        return extractedData;
    }
    
    // Getters
    
    public int getScrappCount() {
        return scrappCount;
    }
    
    public Set<String> getAttempted() {
        return attempted;
    }

    public Set<String> getFailed() {
        return failed;
    }

    public float getMaxTolerance() {
        return maxTolerance;
    }

    public void setMaxTolerance(float maxTolerance) {
        this.maxTolerance = maxTolerance;
    }

    // Getters and setters 
    
    public CapturerContext getContext() {
        return context;
    }

    public void setContext(CapturerContext context) {
        this.context = context;
    }

    public PageNodes getSource() {
        return source;
    }

    public void setSource(PageNodes source) {
        this.source = source;
    }
    
    public String getTaskName() {
        return Scrapper.class.getName()+(this.context==null?"":"#"+this.context.getConfig().getName());
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.print(builder);
        return builder.toString();
    }
    
    public void print(StringBuilder builder) {
        builder.append(this.getTaskName());
        builder.append(", Attempted: ").append(this.attempted==null?null:this.attempted.size());
        builder.append(", Failed: ").append(this.failed==null?null:this.failed.size());
        builder.append(", Scrapp count: ").append(this.scrappCount);
    }
}
