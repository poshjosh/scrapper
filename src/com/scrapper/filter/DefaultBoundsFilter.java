package com.scrapper.filter;

import com.bc.util.XLogger;
import com.bc.json.config.JsonConfig;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;

/**
 * @author Josh
 */
////////////////////////////////////////////////////////////////////////////////
//
// !!!!!!!!!! IMPORTANT NOTICES !!!!!!!!!!
//
// * Unlike Visitors, NodeFilters parse thourgh the whole tag as a unit.
// Visitors on the other hand parse through startTag, body, then endTag.
//   
// * If a tag is not accepted then it and all of its children have 
// automatically been rejected. So generally accept all parent tags 
// of any target tag
////////////////////////////////////////////////////////////////////////////////
public class DefaultBoundsFilter extends ParentNodeBoundsFilter {
    
    private boolean withinHtml;
    private boolean withinHead;
    private boolean withinTitle;
    private boolean withinBody;
    
    public DefaultBoundsFilter(JsonConfig site) { 

        super(site, false);
    }
    
    @Override
    public void reset() {
        super.reset();
        this.withinBody = false;
        this.withinHead = false;
        this.withinHtml = false;
        this.withinTitle = false;
    }
    
    @Override
    public boolean accept(Node node) {
        
        boolean accept = super.accept(node);
        
        if(accept) {
            accept = this.isValidContent(node);
        }
        
        return accept;
    }
    
    @Override
    protected boolean isDefaultAccept(Node node) {
        
        if ( !(node instanceof Tag) ) {
            return false;
        }
        
        Tag tag = (Tag)node;
        
        // The pages encountered usually has the
        // <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
        // tag as the first node and the whole html page
        // <html><head><title></title></head><body></body></html>
        // as the second node. There may be other nodes (probably comments) after these
        //

        // Note that in class org.htmlparser.util.NodeList, a node can only be
        // tested for acceptance if its parent has already been accepted.
        // Hence we have to accept HTML and BODY tags
        // We also want to capture the head and title of the document.
        //
        // html,body,head,title
        //

        final String tagName = tag.getTagName();

        boolean defaultAccept = 
           (tagName.equals("HTML") || 
                tagName.equals("HEAD") || 
                tagName.equals("TITLE"));

if(defaultAccept) {        
XLogger.getInstance().log(Level.FINER, "DEFAULT Accept: {0}", 
this.getClass(), tagName);        
}
        
        return defaultAccept;
    }

    // We cannot really tell start, end tags apart.
    // We can only infer those comments whose start and end tags
    // are in the same text node
    //
    private boolean isComment(Node node) {
        if(node instanceof Text || node instanceof Remark) {
            String text = node.getText();
            int n = text.indexOf("<!--");
            return n != -1 && (text.indexOf("-->", n) != -1);
        }else{
            return false;
        }
    }
    
    private boolean isValidContent(Node node) {

        if(this.isDefaultAccept(node)) {
            return true;
        }
        
        Tag tag;
        
        if(node instanceof Tag) { // Only Tags a checked
            tag = ((Tag)node);
        }else{
            return !this.isComment(node);
        }
        
// Since this is a filter, we don't have the NodeVisitor luxury of
// differentiated start and end tags... We only know when we encounter
// the tag. Therefore, the various withinXXX attributes are not accurate.
// Except for withinTitle which is set to false immediately we encounter
// the next tag after the title Tag. This should be accurate since
// title tags should not contain tags.
        if(withinTitle) {
            this.withinTitle = false;
        }
        
        if(!this.withinHtml && tag.getTagName().equals("HTML")) {
            this.withinHtml = true;
        }else if(!this.withinHead && tag.getTagName().equals("HEAD")) {
            this.withinHead = true;
        }else if(!this.withinTitle && tag.getTagName().equals("TITLE")) {
            this.withinTitle = true;
        }else if(!this.withinBody && tag.getTagName().equals("BODY")) {
            this.withinTitle = false;
            this.withinHead = false;
            this.withinBody = true;
        }

        // Default accept accepts head already, so we can do this
        //
        boolean accept = this.withinTitle || this.withinBody;
        
XLogger.getInstance().log(Level.FINER, "Within title: {0}, body: {1}, Node: {2}", 
        this.getClass(), withinTitle, withinBody, tag);
        
        return accept;
    }
}//~END
