package com.scrapper.formatter;

import com.bc.manager.Formatter;
import com.bc.util.XLogger;
import com.scrapper.util.Util;
import java.io.Serializable;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @(#)Aliexpressfashion_imageformatter.java   25-Mar-2013 23:40:11
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
public class Aliexpressfashion_imageformatter 
        implements Formatter<String>, Serializable {

    //Expected input format 
    // <div class="img-zoom-in"><span>See Larger Image:</span> <a href="http://www.aliexpress.com/item-img/New-arrival-2012-Long-Sleeve-Dresses-Women-Korean-fashion-Slim-Sexy-striped-mini-OL-dress-Skirt/671507204.html" id="lnk-enlarge-image" target="_blank" title="New arrival 2012 Long Sleeve Dresses Women Korean fashion Slim Sexy striped mini OL dress Skirt">New arrival 2012 Long Sleeve Dresses Women Korean fashion Slim Sexy striped mini OL dress Skirt Picture</a></div>
    
    @Override
    public String format(String html) {
XLogger.getInstance().log(Level.FINEST, "{0}. HTML: {1}", this.getClass(), html);        
        String link = null;

        try{
            
            Parser parser = new Parser(html);

            NodeList list = parser.parse(new TagNameFilter("A"));

//            list = parser.extractAllNodesThatMatch(new TagNameFilter("A"));

            for(int i=0; i<list.size(); i++) {

                Node node = list.elementAt(i);
XLogger.getInstance().log(Level.FINER, "{0}. Node: {1}", this.getClass(), Util.appendTag(node, null));
                if(!(node instanceof LinkTag)) continue;

                LinkTag linkTag = (LinkTag)node;

                link = linkTag.getLink();

                break;
            }
        }catch(ParserException e) {
            XLogger.getInstance().log(Level.WARNING, "", this.getClass(), e);
        }
XLogger.getInstance().log(Level.FINE, "{0}. Link; {1}", this.getClass(), link);        
        if(link == null) return null;
        
//Format         
//http://www.aliexpress.com/item-img/New-arrival-2012-Long-Sleeve-Dresses-Women-Korean-fashion-Slim-Sexy-striped-mini-OL-dress-Skirt/671507204.html        
//To        
//http://1.1.1.4/bmi/i00.i.aliimg.com/wsphoto/v0/671507204/New-arrival-2012-Long-Sleeve-Dresses-Women-Korean-fashion-Slim-Sexy-striped-mini-OL-dress-Skirt.jpg        
        int a = link.lastIndexOf("/");
        int b = link.lastIndexOf(".");
        String number = link.substring(a + 1, b);
        link = link.replace("http://www.aliexpress.com/item-img", 
                "http://1.1.1.4/bmi/i00.i.aliimg.com/wsphoto/v0/"+number);
        link = link.replace("/"+number+".html", ".jpg");
XLogger.getInstance().log(Level.FINE, "{0}. Formatted link: {1}", this.getClass(), link);        
        return link;
    }
}
