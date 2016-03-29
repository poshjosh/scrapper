package com.scrapper.context;

import com.bc.jpa.fk.Keywords;
import com.scrapper.HasImages;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.Sitenames;
import com.scrapper.extractor.NodeExtractorOld;
import com.scrapper.extractor.TargetNodeExtractor;
import com.scrapper.filter.DefaultBoundsFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.Translate;

/**
 * @(#)NigeriandriverContext.java   22-Feb-2013 23:56:35
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
public class NigeriandriverContext extends DefaultCapturerContext {
    
    public NigeriandriverContext() { }
    
    public NigeriandriverContext(JsonConfig config) { 
        super(config);
    }

    public class NigeriandriverExtractor extends 
            TargetNodeExtractor implements HasImages {

        private NigeriandriverVisitor targetNodeVisitor;
        
        public NigeriandriverExtractor(CapturerContext context) {

            super(context);

            this.targetNodeVisitor = new NigeriandriverVisitor(context.getConfig());
        }
        
        @Override
        public void reset() {
            super.reset();
            this.targetNodeVisitor.reset();
        }
        
        @Override
        public void finishedParsing() {

            super.finishedParsing(); // Important
            
            Keywords keyWords = NigeriandriverContext.this.getKeywords();
            
            if(keyWords == null) {
                return;
            }
            
            List tables = NigeriandriverContext.this.getConfig().getList(Config.Site.tables);

            keyWords.setTableName(tables.get(0).toString());
            
            // Note we remove the type
            // We also do this before date because date will be used
            // to update status
            //
            String typeStr = (String)getExtractedData().remove("type");
    //System.out.println("Type String: "+typeStr);
            if(typeStr != null) {

                Integer category = keyWords.findMatchingKey("category", typeStr, true);
    //System.out.println("Category: "+category);
                if(category != null) {
                    this.getExtractedData().put("category", category);
                }
            }

            String brandStr = (String)getExtractedData().get("brand");
    //System.out.println("Brand String: "+brandStr);
            if(brandStr != null) {

                Integer type = keyWords.findMatchingKey("type", brandStr, true);
    //System.out.println("Type: "+type);
                if(type != null) {
                    this.getExtractedData().put("type", type);
                }
            }
        }
        
        @Override
        public Set<String> getImages() {
            return this.targetNodeVisitor.getImages();
        }

    //    @Override
    //    protected String add(String key, Object val) {

    //        if(key == null || val == null) return null;

    //        String col = Util.findValueWithMatchingKey(keys, key);
    //System.out.println(this.getClass().getId()+"#add. Key: "+key+", Matching column: "+col);
    //        if(col == null) {
    //            return null;
    //        }else if(col.equals("description")) {
    //            final boolean oldVal = this.append;
    //            this.setAppend(true);
    //            col = super.add("description", val);
    //            this.setAppend(oldVal);
    //        }else{
    //            col = super.add(key, val);
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

        //@todo
        private void addCategory(String statusStr) {

        }
    }//~END

    public static class NigeriandriverVisitor 
            extends NodeExtractorOld
            implements HasImages {

        private static final String HOLDER = "uXuXuXuXuX";

        private boolean ready;

        private boolean inKey;
        private boolean inVal;

        private int keyDept;
        private int valDept;

        private NodeFilter keyNodeFilter;
        private NodeFilter valNodeFilter;

        private Pattern imagePattern;

        private int image = 0;

        private Set<String> images;

        private String url;

        public NigeriandriverVisitor(JsonConfig site) {

            if(!Sitenames.NIGERIANDRIVER.equals(site.getName())) {
                throw new IllegalArgumentException("Expected: "+
                Sitenames.NIGERIANDRIVER+", Found: "+site.getName());
            }
            
            url = site.getString(Config.Site.url, "value");

            HasAttributeFilter hasAttr1 = new HasAttributeFilter();
            hasAttr1.setAttributeName("class");
            hasAttr1.setAttributeValue("smallBold");

            this.keyNodeFilter = hasAttr1;

            HasAttributeFilter hasAttr2 = new HasAttributeFilter();
            hasAttr2.setAttributeName("class");
            hasAttr2.setAttributeValue("main_text");

            this.valNodeFilter = hasAttr2;

    //onclick="popup('pop_car.php?img=1299267120DSC09102.JPG');" or
    //onclick="popup('pop_car.php?img=2323454349DSC-IMG21098273.jpg');"
            this.imagePattern = Pattern.compile("pop_car\\.php\\?img\\=.+\\.JPG", Pattern.CASE_INSENSITIVE);

            // Note we use a LinkedHashSet
            this.images = new LinkedHashSet<String>();
        }

        @Override
        public void reset() {
            super.reset();
            this.inKey = false;
            this.inVal = false;
            this.keyDept = 0;
            this.valDept = 0;
            this.image = 0;
        }
        
        @Override
        public void finishedParsing() {

            super.finishedParsing(); // Important

            List unwanted = new ArrayList();
            unwanted.add(HOLDER);
            getExtractedData().values().removeAll(unwanted);
        }

        /**
         * This method should only be called once after an extraction operation.
         * After calling this method once, subsequent calls return null.
         * @return
         */
        @Override
        public Set<String> getImages() {
            return this.images;
        }

        @Override
        public void visitTag(Tag tag) {

            String tagName = tag.getTagName();

            if(tagName.equals("IMG")) {
                this.addImage(tag);
                return;
            }

            if(!ready) return;

    //System.out.println(this.getClass().getId()+"#visitTag.\nTag: "+tagName+
    //        ",   keyDept: "+this.keyDept+",   valDept: "+this.valDept+
    //        ",   inKey: "+this.inKey+",   inVal: "+this.inVal+",   ready: "+ready);

            if(tagName.equals("TD")) {

                String className = tag.getAttribute("class");

                if(className != null) {
                    if(className.equals("smallBold")) {
    //System.out.println(this.getClass().getId()+"#visitTag. Found TD Tag with class=smallBold");
                        this.inKey = true;
                        ++this.keyDept;
                    }else
                    if(className.equals("main_text")) {
                        this.inVal = true;
                        ++this.valDept;
                    }
                }
            }
        }

        private void addImage(Tag tag) {
    //System.out.println(this.getClass().getId()+"#addImage. Found image tag");
    //onclick="popup('pop_car.php?img=1299267120DSC09102.JPG');" // This didn't work well
    //<img id="BigImage" src="upload/images/cars/large/1301335892DSC09233.JPG" />
    //        String id = tag.getAttribute("id");
    //System.out.println(this.getClass().getId()+"#addImage. id="+id);        

            String src = tag.getAttribute("src");
    //System.out.println(this.getClass().getId()+"#addImage. src="+src);

            if(!src.contains("upload/images/cars/small/"))return;

            src = src.replace("small", "large");

            String imageUrl = url + "/" + src;
    System.out.println(this.getClass().getName()+"#addImage. Adding: " + imageUrl);
            this.images.add(imageUrl);
        }

        @Override
        public void visitEndTag(Tag tag) {

            if(!ready) return;

            String tagName = tag.getTagName();

    //System.out.println(this.getClass().getId()+"#visitEndTag.\nTag: "+tagName+
    //        ",   keyDept: "+this.keyDept+",   valDept: "+this.valDept+
    //        ",   inKey: "+this.inKey+",   inVal: "+this.inVal+",   ready: "+ready);

            if(tagName.equals("TD")) {

                if(this.inKey) {
                    this.inKey = false;
                    --this.keyDept;
                }else
                if(this.inVal) {
                    this.inVal = false;
                    --this.valDept;
                }
            }
        }

        @Override
        public void visitStringNode(Text node) {

            // Doing this ensures all comparisons etc are accurate
            // For example &nbsp; is changed to space char
            //
            String text = Translate.decode(node.getText());

            text = Pattern.compile("\\s").matcher(text).replaceAll(" ").trim();

            if(text.length() == 0 || text.equals(" ")) return;

            String lower = text.toLowerCase();

            if(lower.contains("owner's name")) {
                this.ready = true;
                this.inKey = true;
                this.inVal = false;
                this.keyDept = 1;
                this.valDept = 0;
            }

            if(!this.ready) return;

            if(this.keyDept == 0 && this.valDept == 0) return;

    //System.out.println(this.getClass().getId()+"#visitStringNode.\nText: "+text+
    //        ",   keyDept: "+this.keyDept+",   valDept: "+this.valDept+
    //        ",   inKey: "+this.inKey+",   inVal: "+this.inVal+",   ready: "+ready);


            // We do this because though car location is withing the required
            // tag it is not really a key for which a value could be added.
            // Rather it is a heading for 2 keys i.e State and City
            //
            if(lower.contains("car location") || lower.contains(":")) {
                return;
            }

            if(this.inKey) {
    //System.out.println(this.getClass().getId()+"#visitStringNode, Adding: ["+text+"="+HOLDER+"]");

                this.getExtractedData().put(text, HOLDER);

            }else
            if(this.inVal) {
                Iterator iter = this.getExtractedData().keySet().iterator();
                while(iter.hasNext()) {
                    Object key = iter.next();
                    String val = (String)this.getExtractedData().get(key);
                    if(HOLDER.equals(val)) {
                        if(this.accept(key) && this.accept(text)) {
    //System.out.println(this.getClass().getId()+"#visitStringNode. Adding: ["+key+"="+text+"]");
                            this.getExtractedData().put(key, text);
                        }
                        break;
                    }
                }
            }
        }

        private boolean accept(Object obj) {
            // We use toString.trim.length > 1 because some strings are '?' instead of empty string
            return obj != null && obj.toString().trim().length() > 1;
        }
    }

    public static class NigeriandriverNodeFilter extends DefaultBoundsFilter {

        public NigeriandriverNodeFilter(JsonConfig site) throws IOException {

            super(site);

            NodeFilter isTr = new TagNameFilter("TR");

            NodeFilter isTd = new TagNameFilter("TD");

            HasAttributeFilter hasAttr1 = new HasAttributeFilter();
            hasAttr1.setAttributeName("class");
            hasAttr1.setAttributeValue("smallBold");

            AndFilter isTd1 = new AndFilter();
            isTd1.setPredicates(new NodeFilter[]{isTd, hasAttr1});

            HasAttributeFilter hasAttr2 = new HasAttributeFilter();
            hasAttr2.setAttributeName("class");
            hasAttr2.setAttributeValue("main_text");

            AndFilter isTd2 = new AndFilter();
            isTd2.setPredicates(new NodeFilter[]{isTd, hasAttr2});

            HasChildFilter hasTd1 = new HasChildFilter();
            hasTd1.setRecursive(true);
            hasTd1.setChildFilter(isTd1);

            HasChildFilter hasTd2 = new HasChildFilter();
            hasTd2.setRecursive(true);
            hasTd2.setChildFilter(isTd2);

            AndFilter targetFilter = new AndFilter();
            targetFilter.setPredicates(new NodeFilter[]{isTr, hasTd1, hasTd2});

            // Replace the default
            HasChildFilter hasChild = new HasChildFilter();
            hasChild.setRecursive(true);
            hasChild.setChildFilter(targetFilter);
            
            this.setParentFilter(hasChild);
        }
    }//~END
}
