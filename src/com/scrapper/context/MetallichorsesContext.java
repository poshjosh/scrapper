package com.scrapper.context;

import com.bc.manager.Formatter;
import com.scrapper.HasImages;
import com.bc.json.config.JsonConfig;
import com.scrapper.extractor.NodeExtractorOld;
import com.scrapper.extractor.TargetNodeExtractor;
import com.scrapper.filter.DefaultBoundsFilter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

/**
 * @(#)MetallichorsesContext.java   22-Feb-2013 23:30:23
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
public class MetallichorsesContext extends DefaultCapturerContext {
    
    public MetallichorsesContext() { }
    
    public MetallichorsesContext(JsonConfig config) { 
        super(config);
    }
    
    public static class MetallichorsesUrlFormatter implements Formatter<String>{
        @Override
        public String format(String url) {
            int x = url.indexOf("&amp;osCsid=");
            if(x == -1) {
                x = url.indexOf("&osCsid=");
            }
            if(x == -1) return url;
            return url.substring(0, x);
        }
    }

    public static class MetallichorsesExtractor extends TargetNodeExtractor
            implements HasImages {

        private MetallichorsesVisitor targetNodeVisitor;
        
        public MetallichorsesExtractor(CapturerContext context) {

            super(context);

            this.targetNodeVisitor = new MetallichorsesVisitor();
        }

        @Override
        public void reset() {
            super.reset();
            this.targetNodeVisitor.reset();
        }
        
        @Override
        public Set<String> getImages() {
            return ((HasImages)targetNodeVisitor).getImages();
        }

        @Override
        public NodeExtractorOld getTargetNodeVisitor() {
            return this.targetNodeVisitor;
        }

        @Override
        public NodeFilter getTargetNodeFilter() {
            return this.getCapturerContext().getFilter();
        }
    }//~END

    public static class MetallichorsesVisitor 
            extends NodeExtractorOld
            implements HasImages {

        private boolean done;

        private Set<String> images;

        public MetallichorsesVisitor() {
            images = new LinkedHashSet<String>();
        }

        @Override
        public void reset() {
            super.reset();
            done = false;
        }

        @Override
        public Set<String> getImages() {
            return images;
        }

        @Override
        public void visitTag(Tag tag) {
    //System.out.println("...............Tag: "+tag.getTagName()+", Done: "+done);
            if(done) return;

            if(tag.getTagName().equals("FORM")) {

    //System.out.println(this.getClass().getId()+". Tag:\n"+tag.toHtml(false));

                NodeList children = tag.getChildren();

                this.extractHeaders((Tag)children.elementAt(0));

                this.extractDetails((Tag)children.elementAt(1));

                this.getExtractedData().put("type", 2);

                this.done = true;
            }
        }

        private void extractHeaders(Tag tag) {
            NodeList children = tag.getChildren();
            Node headerNode = children.elementAt(0);
    //System.out.println(this.getClass().getId()+". Header Node:\n"+headerNode.toHtml(false));
            String priceText = headerNode.toPlainTextString();
    //System.out.println(this.getClass().getId()+". Price text: "+priceText);
            final String NGN = "NGN";
            int start = priceText.indexOf(NGN);
            if(start != -1) {
                priceText = priceText.substring(start+NGN.length());
            }
            // Should be a number
            try{
                double d = Double.parseDouble(priceText.replace(",", ""));
                this.getExtractedData().put("price", d);
            }catch(NumberFormatException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "", e);
            }
            this.getExtractedData().put("make", children.elementAt(1).toPlainTextString());
        }

        private void extractDetails(Tag tag) {

            NodeList children = tag.getChildren();
    //System.out.println(this.getClass().getId()+". \n\n"+children.toHtml(false));
            HasAttributeFilter hasPiGal = new HasAttributeFilter();
            hasPiGal.setAttributeName("id");
            hasPiGal.setAttributeValue("piGal");

            NodeList list = children.extractAllNodesThatMatch(hasPiGal, true);
            list = list.extractAllNodesThatMatch(new TagNameFilter("A"), true);
    //System.out.println(this.getClass().getId()+". \n\n"+children.toHtml(false));
            for(int i=0; i<list.size(); i++) {
                Node node = list.elementAt(i);
                if(node instanceof LinkTag) {

                    String link = ((LinkTag)node).getLink();

                    link = formatLink(link);

                    this.images.add(link);
                }
            }

            this.extractDetails(children.asString());
        }

        private String formatLink(String link) {
    //../store/images/ARMOUR SHORTS3.JPG?osCsid=418dc2842523ce23a84aa1b9bcdd32c5
            if(link.startsWith("..")) {
                link = "http://metallichorses.com" + link.substring(2);
            }
            int index = link.indexOf("?osCsid=");
            if(index != -1) {
                link = link.substring(0, index);
            }
            index = link.indexOf("&amp;osCsid=");
            if(index != -1) {
                link = link.substring(0, index);
            }
            index = link.indexOf("&osCsid=");
            if(index != -1) {
                link = link.substring(0, index);
            }
            return link;
        }

        private void extractDetails(String text) {
    //System.out.println(this.getClass().getId()+". Text: "+text);
            this.getExtractedData().put("description", text.replace("\n", "<br/>"));
            String [] parts = text.split("\n");
            if(parts == null || parts.length < 2) {
                return;
            }
            for(int i=0; i<parts.length; i++) {
                String [] subParts = parts[i].split(":");
                if(subParts != null && subParts.length == 2) {
                    this.getExtractedData().put(parts[0], parts[1]);
                }
            }
        }
    }

    public static class MetallichorsesNodeFilter extends DefaultBoundsFilter {

        public MetallichorsesNodeFilter(JsonConfig site) throws IOException {

            super(site);

            NodeFilter isForm = new TagNameFilter("FORM");

            HasAttributeFilter hasName = new HasAttributeFilter();
            hasName.setAttributeName("name");
            hasName.setAttributeValue("cart_quantity");

            AndFilter targetFilter = new AndFilter();
            targetFilter.setPredicates(new NodeFilter[]{isForm, hasName});

            // Replace the default
            HasChildFilter hasChild = new HasChildFilter();
            hasChild.setRecursive(true);
            hasChild.setChildFilter(targetFilter);
            
            
            this.setParentFilter(hasChild);

            HasAttributeFilter hasClass = new HasAttributeFilter();
            hasClass.setAttributeName("class");
            hasClass.setAttributeValue("buttonSet");

            NotFilter notClass = new NotFilter();
            notClass.setPredicate(hasClass);

            HasParentFilter hasParent = new HasParentFilter();
            hasParent.setRecursive(true);
            hasParent.setParentFilter(targetFilter);

            AndFilter childrenFilter = new AndFilter();
            childrenFilter.setPredicates(new NodeFilter[]{hasParent, notClass});

            // Replace the default
//            this.getBoundsMarker().setFilter(FilterFactory.CHILD, childrenFilter);
        }
    }
}
