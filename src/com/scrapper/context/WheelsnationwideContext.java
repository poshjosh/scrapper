package com.scrapper.context;

import com.scrapper.HasImages;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import com.scrapper.extractor.NodeExtractorOld;
import com.scrapper.extractor.TargetNodeExtractor;
import com.scrapper.filter.DefaultBoundsFilter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.Translate;

/**
 * @(#)WheelsnationwideContext.java   23-Feb-2013 00:11:41
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
public class WheelsnationwideContext extends DefaultCapturerContext {
    
    public WheelsnationwideContext() { }
    
    public WheelsnationwideContext(JsonConfig config) { 
        super(config);
    }
    
    public static class WheelsnationwideExtractor extends 
            TargetNodeExtractor implements HasImages {

        private WheelsnationwideVisitor targetNodeVisitor;
        
        public WheelsnationwideExtractor(CapturerContext context) {
            super(context);
            this.targetNodeVisitor = new WheelsnationwideVisitor(context.getConfig());
        }

        @Override
        public void reset() {
            super.reset();
            this.targetNodeVisitor.reset();
        }
        
        @Override
        public Set<String> getImages() {
            return ((HasImages)this.targetNodeVisitor).getImages();
        }

    //    @Override
    //    protected String add(String key, Object val) {

    //        String col = super.add(key, val);

    //        if(key != null && col == null) {
    //            final String old = this.partSeparator;
    //            this.partSeparator = "";
    //            col = super.add("description", val);
    //            this.partSeparator = old;
    //        }

    //        return col;
    //    }

        @Override
        public NodeExtractorOld getTargetNodeVisitor() {
            return this.targetNodeVisitor;
        }

        @Override
        public NodeFilter getTargetNodeFilter() {
            return this.getCapturerContext().getFilter();
        }
    }

    public static class WheelsnationwideVisitor 
            extends NodeExtractorOld implements HasImages {

        private boolean inPropertyTop;
        private boolean inPlabPrice;
        private boolean inFeatures;
        private boolean inViewThumbs;
        private boolean inDetailsIntro;
        private boolean inDetailsFull;
        private boolean inAgentDetails;

        private Set<String> images;

        private String url;

        private Pattern datePattern = Pattern.compile("\\d{4}");

        public WheelsnationwideVisitor(JsonConfig site) {

            this.url = site.getString(Config.Site.url, "value");

            // Note we use a LinkedHashSet for images
            //
            this.images = new LinkedHashSet<String>();
        }

        @Override
        public void reset() {
            super.reset();
            this.inPropertyTop = false;
            this.inPlabPrice = false;
            this.inDetailsFull = false;
            this.inDetailsIntro = false;
            this.inFeatures = false;
            this.inViewThumbs = false;
            this.inAgentDetails = false;
    //        this.images = new LinkedHashSet<String>();  // No way! Images will be cleared after retrieval
        }

        // HasImages interface methods
        //
        @Override
        public Set<String> getImages() {
            return this.images;
        }

        @Override
        public void visitTag(Tag tag) {

            final String tagName = tag.getTagName();
    //System.out.println(this.getClass().getId()+"#visitTag. tag name: "+tagName);
            if(tagName.equals("IMG")) {
                if(this.inViewThumbs) {
                    this.extractImage(tag);
                }
            }else
            if(tagName.equals("DIV")) {

                final String id = tag.getAttribute("id");

                if(id != null) {
    //System.out.println(this.getClass().getId()+"#visitTag. id: "+id);

                    if(id.equals("propertytop")) {
                        this.inPropertyTop = true;
                    }else if(id.equals("plab_price")) {
                        this.inPlabPrice = true;
                    }else if(id.equals("plab_details_full")) {
                        this.inDetailsFull = true;
                    }else if(id.equals("plab_details_intro")) {
                        this.inDetailsIntro = true;
                    }else if(id.equals("plab_features")) {
                        this.inFeatures = true;
                    }else if(id.equals("plab_view_thumbs_inner")) {
                        this.inViewThumbs = true;
                    }else if(id.equals("agentdetails")) {
                        this.inAgentDetails = true;
                    }
                }
            }
        }

        @Override
        public void visitEndTag(Tag tag) {

            final String tagName = tag.getTagName();

            if(tagName.equals("DIV")) {

                if(this.inPropertyTop) {
                    if(this.inPlabPrice) {
                        this.inPlabPrice = false;
                    }else{
                        this.inPropertyTop = false;
                    }
                }else if(this.inDetailsFull) {
                    this.inDetailsFull = false;
                }else if(this.inDetailsIntro) {
                    this.inDetailsIntro = false;
                }else if(this.inFeatures) {
                    this.inFeatures = false;
                }else if(this.inViewThumbs) {
                    this.inViewThumbs = false;
                }else if(this.inAgentDetails) {
                    this.inAgentDetails = false;
                }
            }
        }

        @Override
        public void visitStringNode(Text node){

            String text = node.getText();
    //System.out.println(this.getClass().getId()+"#visitStringNode. " +
    //        "inDetailsFull:" +this.inDetailsFull+ ", inDetailsIntro:"+this.inDetailsIntro +
    //        ", inFeatures:"+this.inFeatures+", inViewThumbs:"+this.inViewThumbs+
    //        ",\ninLi:"+this.inLi+", imageTag:"+this.imageTag+", inAgentDetails: "+this.inAgentDetails+
    //        "\nText:"+text);

            // If you don't do this then all comparisons e.g .equals will fail
            // As & can't be compared to its character reference
            //
            text = Translate.decode(text);
            
            Map extractedData = this.getExtractedData();

            if (this.inPropertyTop) {
    //System.out.println("In propertytop string node. Text: "+text);
                if(this.inPlabPrice) {
    //System.out.println("In plab_price string node. Text: "+text);
                    if(!text.trim().equals("N")) {
                        if(extractedData.get("price") == null) {
                            extractedData.put("price", text);
                        }
                    }
                }else{

                    if(extractedData.get("model") == null) {
                        extractedData.put("model", text);
                    }

                    this.addDate(text);

                    if(extractedData.get("category") == null) {
                        String dom = (String)extractedData.get("dateOfManufacture");
                        extractedData.put("category", this.getCategory(dom));
                    }
                }
            }else if(this.inDetailsFull) {
                this.extractDetailsFull(text);
            }else if(this.inDetailsIntro) {
                this.extractDetailsIntro(text);
            }else if(this.inFeatures) {
                this.extractFeatures(text);
            }else if(this.inViewThumbs) {
                // Yep do nothing
            }else if(this.inAgentDetails) {
                text = text.replaceAll("dealer\\d+@wheelsnationwide\\.com", "");
                this.appendDescription(". ", text);
            }
        }

        private void addDate(String text) {

            if(this.getExtractedData().get("dateOfManufacture") != null) return;

            Matcher m = this.datePattern.matcher(text);

            while(m.find()) {

                String yearStr = m.group();

                //1700, 1789, 1823, 1904, 2001, 2010 etc
                if(yearStr.startsWith("1") || yearStr.startsWith("2")) {

                    this.getExtractedData().put("dateOfManufacture", yearStr);

                    break;
                }
            }
        }

        private String getCategory(String yearString) {
            if(yearString != null && (yearString.equals("2011") || yearString.equals("2010"))) {
                return "1";  // 1 = new
            }else{
                return "3";  // 3 = used
            }
        }

        private void extractImage(Tag imageTag) {
            String src = imageTag.getAttribute("src");
    //System.out.println(this.getClass().getId()+"#extractImage. Found:" + src);
    //src="/components/com_propertylab/images/330/standard/975_ducati-1.JPG"
            if(!src.startsWith("/")) return;

            if(!src.contains("components/com_propertylab/images")) return;

            src = src.replace("/thumbs/", "/standard/");

            final String imageUrl = src.startsWith(this.url) ? src : this.url + src;

    //System.out.println(this.getClass().getId()+"#extractImage. Adding: " + imageUrl);
            this.images.add(imageUrl);
        }
        private void extractDetailsFull(String text) {
            this.appendDescription(". ", text);
        }

        private void extractDetailsIntro(String text) {
            if(this.getExtractedData().get("keywords") == null) {
                this.getExtractedData().put("keywords", text);
            }
        }

        private void extractFeatures(String text) {
            String [] parts = text.split(":");
            if(parts == null) return;
            if(parts.length == 1) {
                this.appendDescription(", ", parts[0]);
                return;
            }
            String val = parts[1].trim();
            if(!val.isEmpty() && val.length() > 1) {
                this.getExtractedData().put(parts[0].trim(), val);
            }
        }

        private void appendDescription(String separator, String text) {
            String s = (String)this.getExtractedData().get("description");
            if(s  == null) {
                s = text;
            }else{
                s = s + separator + text;
            }
            this.getExtractedData().put("description", s);
        }
    }

    public static class WheelsnationwideNodeFilter extends DefaultBoundsFilter {

        public WheelsnationwideNodeFilter(JsonConfig site) throws IOException {

            super(site);

            TagNameFilter DIV = new TagNameFilter("DIV");

            HasAttributeFilter hasAttr4Imgs = new HasAttributeFilter();
            hasAttr4Imgs.setAttributeName("id");
            hasAttr4Imgs.setAttributeValue("plab_view_thumbs_inner");

            HasAttributeFilter a = new HasAttributeFilter();
            a.setAttributeName("id");
            a.setAttributeValue("plab_features");

            HasAttributeFilter b = new HasAttributeFilter();
            b.setAttributeName("id");
            b.setAttributeValue("plab_details_intro");

            HasAttributeFilter c = new HasAttributeFilter();
            c.setAttributeName("id");
            c.setAttributeValue("plab_details_full");

            HasAttributeFilter d = new HasAttributeFilter();
            d.setAttributeName("id");
            d.setAttributeValue("agentdetails");

    //<div id="propertytop"><div id="plab_price"><span class="nairo2">N</span>6,000,000</div>2008 mercedes  </div>

            HasAttributeFilter e = new HasAttributeFilter();
            e.setAttributeName("id");
            e.setAttributeValue("propertytop");

            HasAttributeFilter f = new HasAttributeFilter();
            f.setAttributeName("id");
            f.setAttributeValue("plab_price");

            OrFilter hasAttr = new OrFilter();
            hasAttr.setPredicates(new NodeFilter[]{hasAttr4Imgs, a, b, c, d, e, f});

            AndFilter targetFilter = new AndFilter();
            targetFilter.setPredicates(new NodeFilter[]{DIV, hasAttr});

            HasChildFilter hasChild = new HasChildFilter();
            hasChild.setRecursive(true);
            hasChild.setChildFilter(targetFilter);
            
            this.setParentFilter(hasChild);
        }
    }
}
