package com.scrapper.filter;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

/**
 * @author Josh
 */
public class UppercaseFilter implements NodeFilter {

    /**
     * @param node
     * @return True of the Node contains only upper case text
     */
    @Override
    public boolean accept(Node node) {
        String text = node.getText();
        return text.toUpperCase().equals(text);
    }
}
