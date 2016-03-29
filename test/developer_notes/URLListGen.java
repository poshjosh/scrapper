package developer_notes;

import com.bc.io.CharFileIO;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @(#)URLListGen.java   03-Dec-2012 15:15:08
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * Generates a List for a File containing a html 
 * structured URL List of the sort:
 * <code>
 * <ul>
 *   <li>http://www.abc.com</li>
 *   <li>http://www.abc.com/folder</li>
 * </ul>
 * </code>
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class URLListGen implements Serializable {

    private URLListGen() { }
    
    public static List<String> generate(String pathToURLList,
            String separator) throws IOException {
        CharFileIO fileIO = new CharFileIO();
        CharSequence contents = fileIO.readChars(pathToURLList);
        if(contents == null) return null;
        String [] parts = contents.toString().split(separator);
        return Arrays.asList(parts);
    }
    
    public static List<String> generate(String pathToURLList,
            String parentNodeName, String childNodeName) throws ParserException {
        
        Parser parser = new Parser(pathToURLList);

        NodeList list = parser.parse(null);
        
        parser.extractAllNodesThatMatch(new TagNameFilter(parentNodeName));

        ArrayList<String> urlList = new ArrayList<String>();
        
        for(int i=0; i<list.size(); i++) {
            
            Node node = list.elementAt(i);
            
            if(!(node instanceof Tag)) continue;
            
            Tag tag = (Tag)node;
            
            if(!tag.getTagName().equals(parentNodeName.toUpperCase())) continue;
            
            NodeList children = tag.getChildren();
            
            for(int j=(children.size()-1); j>=0; j--) {
                
                Node child = children.elementAt(j);
                if(!(child instanceof Tag)) continue;
                
                Tag childTag = (Tag)child;
                if(!childTag.getTagName().equals(childNodeName.toUpperCase())) continue;
                
                NodeList subChildren = childTag.getChildren();
                
                for(int k=0; k<subChildren.size(); k++) {
                    
                    Node subChild = subChildren.elementAt(k);
                    
                    if(subChild instanceof LinkTag) {
                        
                        LinkTag linkTag = (LinkTag)subChild;
                        
                        String link = linkTag.getLink();
                        
                        urlList.add(link);
                    }
                }
            }
        }
        
        return urlList;
    }
}
