package com.scrapper.extractor;

import com.bc.util.XLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * @author  chinomso bassey ikwuagwu
 * @version  0.3
 * @since   1.0
 */
public class NodeListExtractor 
        extends NodeVisitor 
        implements NodeListExtractorIx {
    
    private boolean started;
    private boolean stopInitiated;
    private boolean stopped;

    private NodeList source;
    
    private Map extractedData;

    public NodeListExtractor() {
        this.extractedData = new HashMap();
    }
    
    @Override
    public void reset() {
        this.started = false;
        this.stopInitiated = false;
        this.stopped = false;
        this.source = null;
        // The previous reference may still be used else where
        // So we don't call clear
        this.extractedData = new HashMap();
    }
    
    @Override
    public Map extractData(NodeList nodeList) throws ParserException {
XLogger.getInstance().log(Level.FINE, "{0} process: {1}", this.getClass(), 
started?"Resuming":"Starting", this);
//System.out.println(started?"Resuming process":"Starting process "+this.getClass().getSimpleName());        
        
        this.started = true;
        this.stopInitiated = false;
        this.stopped = false;
        
        try{
            
            this.reset();
            nodeList.visitAllNodesWith(this);
            
        }finally{
            
            stopped = true;
            
XLogger.getInstance().log(Level.FINE, "{0} process: {1}", this.getClass(), 
stopInitiated?"Pausing":"Completed", this);
//System.out.println(stopInitiated?"Pausing process":"Completed process "+this.getClass().getSimpleName());        
        }
        
        return this.extractedData;
    }
        
    // Stoppable Task interface
    //
    @Override
    public void run() {
        try{
            this.extractData(source);
        }catch(ParserException | RuntimeException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
    }

    @Override
    public boolean isStopInitiated() {
        return stopInitiated;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void stop() {
        stopInitiated = true;
    }

    @Override
    public boolean isCompleted() {
        return started && stopped && !stopInitiated;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    // NodeList methods
    //
    @Override
    public void visitEndTag(Tag tag) {
        if(stopInitiated) {
            return;
        }
        super.visitEndTag(tag);
    }

    @Override
    public void visitRemarkNode(Remark remark) {
        if(stopInitiated) {
            return;
        }
        super.visitRemarkNode(remark);
    }

    @Override
    public void visitStringNode(Text string) {
        if(stopInitiated) {
            return;
        }
        super.visitStringNode(string);
    }

    @Override
    public void visitTag(Tag tag) {
        if(stopInitiated) {
            return;
        }
        super.visitTag(tag);
    }

    protected Map getExtractedData() {
        return extractedData;
    }

    @Override
    public NodeList getSource() {
        return source;
    }

    @Override
    public void setSource(NodeList source) {
        this.source = source;
    }

    @Override
    public String getTaskName() {
        return NodeListExtractor.class.getName();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.getTaskName());
        builder.append(", started: ").append(this.started);
        builder.append(", stopInitiated: ").append(this.stopInitiated);
        builder.append(", stopped: ").append(this.stopped);
        return builder.toString();
    }
}//~END
