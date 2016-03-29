package com.scrapper.extractor;

import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import com.scrapper.filter.FilterFactory;
import java.util.Iterator;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.util.ParserException;

/**
 * Extracts Data from multiple target nodes within a parent node.
 * Three nodes (keywords, price, type) are given special consideration.
 * This should be restricted to simple composite tags, with out nested
 * children, script or other non-basic markings
 * @author  chinomso bassey ikwuagwu
 * @version 0.1
 * @since   0.0
 */
public abstract class TargetNodeExtractor extends PageExtractor {
    
    private Tag currentTargetNode;
    private boolean doneCurrentTargetNode;
    private transient NodeFilter filter;

    public TargetNodeExtractor(CapturerContext context) {
        super(context);
    }

    @Override
    public void reset() {
        super.reset();
        this.currentTargetNode = null;
        this.doneCurrentTargetNode = false;
    }
    
    protected abstract NodeExtractorOld getTargetNodeVisitor();

    protected abstract NodeFilter getTargetNodeFilter();
    
    @Override
    public void visitTag(Tag tag) {
//System.out.println("TargetNodeExtractor.visitTag: "+tag.getTagName());
        if(this.isDone()) return;

//System.out.println("111111");        
//Logger.getLogger(this.getClass().getTaskName()).info("Tag: " +tag.getTagName());
        super.visitTag(tag);

        if(this.isNode(FilterFactory.TARGET, tag)) {
//System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxx Within target node");            
            this.currentTargetNode = tag;

            this.extractData(this.currentTargetNode);
        }
//System.out.println("TargetNodeExtractor.visitTag. Within parent: "+
//        this.withinParentNode+", Within target: "+this.withinTargetNode());        
    }

    @Override
    public void visitEndTag(Tag tag) {
//System.out.println("TargetNodeExtractor.visitEndTag: "+tag.getTagName());

        if(this.isDone()) return;
        
//Logger.getLogger(this.getClass().getTaskName()).info(" Tag: " +tag.getTagName());

        super.visitEndTag(tag);

        if(this.isNode(FilterFactory.TARGET, tag)) {

            this.currentTargetNode = null;
        }
    }

    @Override
    public void visitStringNode(Text node){
//System.out.println("TargetNodeExtractor.visitStringNode: "+node.getText());

        if(this.isDone()) return;
        
//        String text = Translate.decode(node.getText());
//        text = text.length() > 100 ? text.substring(0, 97)+"...":text;
//Logger.getLogger(this.getClass().getTaskName()).info(" Text: " +text);

        // Important
        super.visitStringNode(node);
    }

    @Override
    public void visitRemarkNode(Remark remark) {
//System.out.println("TargetNodeExtractor.visitRemarkNode: "+remark.getText());
        if(this.isDone()) return;
        super.visitRemarkNode(remark);
    }
    
    protected void extractData(Tag node) {

        this.doneCurrentTargetNode = false;

        if(!this.accept(node)) return;

        this.doExtractData(node);

        this.doneCurrentTargetNode = true;
    }

    protected boolean accept(Tag node) {
//Logger.getLogger(this.getClass().getTaskName()).log(Level.INFO, 
//"Within target node: {0}, done current: {1}", 
//new Object[]{this.withinTargetNode(), this.doneCurrentTargetNode});
        boolean accepted = (this.withinTargetNode() && 
                !this.doneCurrentTargetNode 
//                && this.isTargetNode(node)
                );
//System.out.println(this.getClass().getTaskName()+"#accept. Accepted: "+accepted);        
        return accepted;
    }
    
    protected void doExtractData(Tag node) {

        // Order of method call very important
        //
//System.out.println(this.getClass().getTaskName()+". About to commence extraction");
        
        Map data = null;
        try{
            data = this.getTargetNodeVisitor().extractData(node);
        }catch(ParserException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
        
//System.out.println(this.getClass().getTaskName()+". Extracted data: "+data);
        if(data == null || data.isEmpty()) return;

        Iterator iter = data.keySet().iterator();

        while(iter.hasNext()) {

            String key = (String)iter.next();
            Object val = (Object)data.get(key);

            this.add(key, val);
        }
    }

    protected boolean withinTargetNode() {
        return this.currentTargetNode != null;
    }
    
    protected boolean isNode(String key, Tag node) {
//        DefaultBoundsFilter filter = this.getTargetNodeFilter();
        if(filter == null) {
            filter = this.getTargetNodeFilter();
        }
        if(filter == null) return true;
//        NodeFilter childrenFilter = filter.getFilter(key);
//        if(childrenFilter == null) return true;
//        return childrenFilter.accept(node);
        return false;
    }
}//~END
