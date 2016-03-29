package com.scrapper.filter;

import com.bc.util.XLogger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;

/**
 * @author Josh
 */
public class HasAttributesRegexFilter extends HasAttributeFilter{

    private Pattern pattern;
    
    public HasAttributesRegexFilter (String attribute, String regex){
        super(attribute, regex);
        if(regex != null) {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
    }

    @Override
    public boolean accept (Node node){

        if( !(node instanceof Tag) ) return false;

        Attribute attribute = ((Tag)node).getAttributeEx(mAttribute);

        boolean accept = false;
        
        if (attribute != null && pattern != null) {
            accept = pattern.matcher(attribute.getValue()).find();
        }

//if("IMG".equals(((Tag)node).getTagName()))
XLogger.getInstance().log(Level.FINEST, "{0}={1}, Accept: {2}, Node: {3}", 
this.getClass(), mAttribute, pattern.pattern(), accept, node);
        return accept;
    }
}
