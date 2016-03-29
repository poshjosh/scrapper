package com.scrapper.extractor;

import com.bc.util.XLogger;
import com.scrapper.HasBounds;
import java.io.Serializable;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.visitors.NodeVisitor;

/**
 * @(#)CommentBoundsVisitor.java   16-Nov-2013 18:53:40
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
public class CommentBoundsVisitor extends NodeVisitor 
implements HasBounds, Serializable {
    
    private boolean doneAtNextNode;
    
    private boolean started;
    private boolean done;
    
    private String id;
    
    public CommentBoundsVisitor() { }
    
    public CommentBoundsVisitor(String id) { 
        this.id = id;
    }
    
    @Override
    public void reset() {
        this.doneAtNextNode = false;
        this.done = false;
        this.started = false;
    }
    
    @Override
    public void visitTag(Tag tag) {
        this.updateComment(tag);
    }
    
    @Override
    public void visitEndTag(Tag tag) {
        this.updateComment(tag);
    }
    
    @Override
    public void visitStringNode(Text string) { 
        updateComment(string);
    }

    @Override
    public void visitRemarkNode(Remark remark) { 
        updateComment(remark);
    }

    private void updateComment(Node node) {

        if(doneAtNextNode) {
            this.done = true;
            doneAtNextNode = false;
        }
        
        String text = node.getText();
        
        int n = text.indexOf("<!--");
        
        if(!started && n != -1) {
            
XLogger.getInstance().log(Level.FINER, "Entering Comment: {0}", 
        this.getClass(), node);                    

            started = true;
            
            if(text.indexOf("-->", n) != -1) {
                doneAtNextNode = true;
            }

        }else if(started && text.contains("-->")){
XLogger.getInstance().log(Level.FINER, "Exiting Comment: {0}", 
        this.getClass(), node);

            done = true;
        }
    }
    
    public boolean isComment(Node node) {
        if(node instanceof Text || node instanceof Remark) {
            String text = node.getText();
            int n = text.indexOf("<!--");
            return n != -1 && (text.indexOf("-->", n) != -1);
        }else{
            return false;
        }
    }
    
    public String getId() {
        return id;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}

