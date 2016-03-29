package com.scrapper.util;

import com.scrapper.tag.Link;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

/**
 * @(#)PageNodeList.java   25-Aug-2013 21:22:30
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
public interface PageNodes {
    void setURL(String url);
    String getURL();
    void setFormattedURL(String url);
    String getFormattedURL();
    void setNodeList(NodeList nodes);
    NodeList getNodeList();
    void setRobots(MetaTag robots);
    MetaTag getRobots();
    void setKeywords(MetaTag keywords);
    MetaTag getKeywords();
    void setDescription(MetaTag description);
    MetaTag getDescription();
    Link getIco();
    void setIco(Link ico);
    Link getIcon();
    void setIcon(Link icon);
    void setTitle(TitleTag title);
    TitleTag getTitle();
    void setBody(BodyTag body);
    BodyTag getBody();
}
