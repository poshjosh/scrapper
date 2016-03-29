package com.scrapper.util;

import com.bc.util.XLogger;
import com.scrapper.tag.Link;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;


/**
 * @(#)PageNodeListImpl.java   29-Nov-2014 12:46:29
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class PageNodesImpl implements PageNodes {

    private String url;
    private String formattedUrl;
    private MetaTag robots;
    private MetaTag keywords;
    private MetaTag description;
    private Link ico;
    private Link icon;
    private TitleTag title;
    private BodyTag body;
    private NodeList nodes;

    public PageNodesImpl(String url, NodeList nodes) {
        this(url, url, nodes);
    }
    
    public PageNodesImpl(String url, String formattedUrl, NodeList nodes) {
        this.url = url;
        this.formattedUrl = formattedUrl;
        this.nodes = nodes;
        init(nodes);
    }
    
    private void init(NodeList nodes) {
        
        NodeList metas = nodes.extractAllNodesThatMatch(new TagNameFilter("META"), true);
        
        for(Node node:metas) {

            MetaTag metaTag = (MetaTag)node;
//System.out.println(this.getClass().getName()+". "+metaTag.toHtml());                
            final String name = metaTag.getAttribute("name");
            if(robots == null && "robots".equals(name)) {
                robots = metaTag;
            }else if(keywords == null && "keywords".equals(name)) {
                keywords = metaTag;
            }else if(description == null && "description".equals(name)) {
                description = metaTag;
            }
        }
        
        NodeList titles = nodes.extractAllNodesThatMatch(new TagNameFilter("TITLE"), true);
        
        if(titles != null && !titles.isEmpty()) {
            title = (TitleTag)titles.get(0);
        }
        
        NodeList links = nodes.extractAllNodesThatMatch(new TagNameFilter("LINK"), true);
        for(Node node:links) {
            
            Link link = ((Link)node);
            String rel = link.getAttribute("rel");
            if(rel == null) {
                continue;
            }
            String lower = rel.toLowerCase().trim();
            if(ico == null && "shortcut icon".equals(lower)) {
                ico = link;
            }else if(icon == null && "icon".equals(lower)) {
                icon = link;
            }
            
            if(ico != null && icon != null) {
                break;
            }
        }
        
//System.out.println(this.getClass().getName()+". "+title.toHtml());                

        NodeList bodies = nodes.extractAllNodesThatMatch(new TagNameFilter("BODY"), true);

        if(bodies == null || bodies.isEmpty()) {
XLogger.getInstance().log(Level.WARNING, "No HTML body found for content at: {0}", this.getClass(), this.getURL());
        }else{
            body = (BodyTag)bodies.get(0);
        }
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public void setURL(String url) {
        this.url = url;
    }

    @Override
    public String getFormattedURL() {
        return formattedUrl;
    }

    @Override
    public void setFormattedURL(String formattedUrl) {
        this.formattedUrl = formattedUrl;
    }

    @Override
    public MetaTag getRobots() {
        return robots;
    }

    @Override
    public void setRobots(MetaTag robots) {
        this.robots = robots;
    }

    @Override
    public MetaTag getKeywords() {
        return keywords;
    }

    @Override
    public void setKeywords(MetaTag keywords) {
        this.keywords = keywords;
    }

    @Override
    public MetaTag getDescription() {
        return description;
    }

    @Override
    public void setDescription(MetaTag description) {
        this.description = description;
    }

    @Override
    public Link getIco() {
        return ico;
    }

    @Override
    public void setIco(Link ico) {
        this.ico = ico;
    }

    @Override
    public Link getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Link icon) {
        this.icon = icon;
    }

    @Override
    public TitleTag getTitle() {
        return title;
    }

    @Override
    public void setTitle(TitleTag title) {
        this.title = title;
    }

    @Override
    public BodyTag getBody() {
        return body;
    }

    @Override
    public void setBody(BodyTag body) {
        this.body = body;
    }

    @Override
    public NodeList getNodeList() {
        return nodes;
    }

    @Override
    public void setNodeList(NodeList nodes) {
        this.nodes = nodes;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append(". URL: ").append(this.url);
        builder.append(", Nodes: ").append(nodes==null?null:nodes.size());
        return builder.toString();
    }
}
