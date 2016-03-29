package com.scrapper.context;

import com.bc.jpa.fk.Keywords;
import com.scrapper.HasImages;
import com.bc.json.config.JsonConfig;
import com.scrapper.config.Config;
import com.scrapper.extractor.PageExtractor;
import com.scrapper.formatter.DefaultFormatter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;

/**
 * @(#)TaafooContext.java   23-Feb-2013 00:04:54
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
public class TaafooContext extends DefaultCapturerContext {
    
    public TaafooContext() { }
    
    public TaafooContext(JsonConfig config) { 
        super(config);
    }
    
    public class TaafooExtractor extends PageExtractor implements HasImages {

        private List<String> knownKeys;

        private static final int KEY = 0;
        private static final int VALUE = 1;
        private static final int IMAGE = 2;

        private int tagType = -1;
        private String lastKey;

        private Set<String> images;

        public TaafooExtractor(CapturerContext context) {
            
            super(context);
            
            this.knownKeys = Arrays.asList(new String[]{"Actual Price:", "Current Price:", 
                "Savings:", "Quantity Left:", "Condition:"});
        }

        @Override
        public void reset() {
            super.reset();
            this.tagType = -1;
            this.lastKey = null;
            this.images = new HashSet<String>();
        }

        @Override
        public void finishedParsing() {
            super.finishedParsing(); // Important
    //System.out.println("TaafooExtractor#getExtractedData. Description: "+m.get("description")+", KeywordsImpl: "+m.get("keywords"));
            String pageTitle = this.getPageTitle();
            if(pageTitle != null && !pageTitle.trim().isEmpty()) {
                String description = (String)this.getExtractedData().get("description");
                if(description != null) {
                    description = (pageTitle+"<br/>"+description);
                }else{
                    description = pageTitle;
                }
                this.getExtractedData().put("description", description);
                if(this.getExtractedData().get("keywords") == null) {
                    this.getExtractedData().put("keywords", pageTitle);
                }
            }
        }

        @Override
        protected String getTitle(Text node) {

            String title = super.getTitle(node);
    //System.out.println("TaafooExtractor#getTitle. Title: "+title);

            if(title != null) {
                int end = title.lastIndexOf("-");
    //System.out.println("TaafooExtractor#getTitle. Index: "+end);            
                if(end != -1) {
                    title = title.substring(0, end);
    //System.out.println("TaafooExtractor#getTitle. Title: "+title);
                }else{
                    // on taafoo.com. Unbelievable... WAS FORMATTED TO on com. Unbelievable... 
                    end = title.toLowerCase().indexOf("unbelievable");
                    if(end != -1) {
                        end = title.indexOf("on com.");
                        if(end != -1) {
                            title = title.substring(0, end);
                        }
                    }
                }    
            }
            return title;
        }

        private int getTagType(Tag tag) {
    //        <img src="http://imageserver.taafoo.com/write/Items/square_d41269a295554964.jpg" onmouseover="movepic('ImPhoto1','http://imageserver.taafoo.com/write/Items/square_d41269a295554964.jpg')" onmouseout = "movepic('ImPhoto1','http://imageserver.taafoo.com/write/Items/square_d41269a295554964.jpg')" style="width:65px;height:65px;border:solid 1px gray;" />
            final String TAG_NAME = tag.getTagName();
            if(TAG_NAME.equals("IMG")) {
                if(tag.getAttribute("onmouseover") != null && 
                   tag.getAttribute("onmouseout") != null && 
                   tag.getAttribute("src").startsWith("http://imageserver.taafoo.com/write/Items")) {
                    return IMAGE;    
                }
                return -1;
            }
            if(TAG_NAME.equals("SPAN")) {
                final String ID = tag.getAttribute("id");
                if(ID != null) {
                    if(ID.equals("ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lbStoreLb")) 
                        return KEY;
                    if(ID.equals("ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lbStore")) 
                        return VALUE;
                    if(ID.equals("ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lbInternetLb"))
                        return KEY;
                    if(ID.equals("ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lbInternet"))
                        return VALUE;
                    if(ID.equals("ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lbSavings"))
                        return KEY;
                    if(ID.equals("ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lbSave"))
                        return VALUE;
                    if(ID.startsWith("ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_ucAttr_dlProd_ct")) {
        //ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_ucAttr_dlProd_ctXXX_lbName    XXX == 101,102 etc                
        //ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_ucAttr_dlProd_ctXXX_lbValue                
                        if(ID.endsWith("Name")) {
                            return KEY;
                        }else if(ID.endsWith("Value")) {
                            return VALUE;
                        }else{
                            return -1;
                        }
                    }
                }
            }
            return -1;
        }

        private void addImageLink(Tag tag) {
            this.images.add(tag.getAttribute("src"));
        }

        @Override
        public void visitTag(Tag tag) {
            if(this.isDone()) return;
            super.visitTag(tag);
            tagType = this.getTagType(tag);
    //        if(tagType != -1) {
    //System.out.println(this.getClass().getTaskName()+"#visitTag. Tag: "+tag.getTagName()+", TYPE: "+tagType);        
    //        }
            if(tagType == IMAGE) {
                this.addImageLink(tag);
            }
    //System.out.println(this.getClass().getTaskName()+"..................... Tag: "+tag.getTagName()+", id: "+tag.getAttribute("id")+", class: "+tag.getAttribute("class"));        
    //logger.log(Level.FINE, "Tag: {0}, id: {1}, class: {2}", 
    //        new Object[]{tag.getTagName(), tag.getAttribute("id"), tag.getAttribute("class")});
        }

        @Override
        public void visitEndTag(Tag tag) {
            if(this.isDone()) return;
            super.visitEndTag(tag);
            this.tagType = -1;
        }

        @Override
        public void visitStringNode(Text node) {

            if(this.isDone()) return;

            super.visitStringNode(node);

            String text = node.getText();

            if(text.contains("&nbsp;&nbsp;")) return;

            if(this.tagType == -1) {
                if(this.knownKeys.contains(text)) {
    //System.out.println("TaafooExtractor#visitStringNode. Found known key: "+text);                
                    this.lastKey = text; return;
                }else{
                    this.add(text);
                }
                return;
            }

            switch(tagType) {
                case KEY:
    //System.out.println("TaafooExtractor#visitStringNode. Found key: "+text);                
                    lastKey = text; return;
                case VALUE:
                    add(text);
                default: return;    
            }
        }

        private void add(String text) {
            if(lastKey == null) return;
            text = format(text);
            this.add(lastKey, text); 
    //System.out.println("TaafooExtractor#visitStringNode. Added pair: ["+lastKey+","+text+"]");                                    
            this.tagType = -1;
            lastKey = null;
            return;
        }

        private String format(String text) {
            if(this.lastKey == null) return text;
            String sval = this.lastKey.toLowerCase().trim();
            if(sval.startsWith("actual price")) {
                text = ("Actual Price: " + text);
            }else if(sval.startsWith("savings")) {
                text = ("Savings: " + text);
            }
            return text;
        }

        @Override
        public void visitRemarkNode(Remark remark) {
            if(this.isDone()) return;
            super.visitRemarkNode(remark);
        }

        @Override
        public Set<String> getImages() {
            return images;
        }
    }//~END

    public class TaafooFormatter extends DefaultFormatter {

        public TaafooFormatter(CapturerContext context) throws IOException {
            super(context);
        }

        @Override
        public Map<String, Object> format(Map<String, Object> parameters) {
    //Logger.getLogger(this.getClass().getTaskName()).info(this.getClass().getTaskName()+"BEFORE Params: "+parameters);

            Object oval = parameters.remove("status");
            if(oval != null) {
                try{
                    if(oval.toString().trim().equalsIgnoreCase("only 1 left")) {
                        parameters.put("status", 2); // Needs verification
                    }else
                    if(Integer.parseInt(oval.toString()) > 0) {
                        parameters.put("status", 1); // Available
                    }else{
                        parameters.put("status", 4); // Taken
                    }
                }catch(NumberFormatException e) {
                    parameters.put("status", 4);
    //                Logger.getLogger(this.getClass().getTaskName()).log(Level.WARNING, "", e);
                }
            }
            oval = parameters.remove("category");
            Integer category = null;
            if(oval != null) {
                Keywords keyWords = TaafooContext.this.getKeywords();
                if(keyWords != null) {
                    List tables = TaafooContext.this.getConfig().getList(Config.Site.tables);
                    keyWords.setTableName(tables.get(0).toString());
                    category = keyWords.findMatchingKey("category", oval.toString(), true);
                }
            }
            if(category == null || category == -1) {
                parameters.put("category", 1);
            }else{
                parameters.put("category", category);
            }

            parameters.put("type", getType(parameters));

            return super.format(parameters);
        }
        public int getType(Map parameters) {
            return getType(parameters.toString());
        }    
        public int getType(String input) {
            int type = 126;
            input = input.toLowerCase();
            if(this.contains(input, new String[]{"feminine", "women", "female", "woman", "ladies", "her",
            "dress", "skirt", "blouse", "lingerie", "bra", "top", "necklace", "hand bag", "earrings", "earings", "purse"})) {
                if(this.contains(input, new String[]{"shirt", "polo", "dress", "skirt", "blouse", "lingerie", "bra", "top"})) {
                    type = 1;
                }else if(this.contains(input, new String[]{"shoe", "platform"})) {
                    type = 2;
                }else if(this.contains(input, new String[]{"watch", "belt", "stud", 
                    "glass", "bag", "jewelry", "necklace", "sun shade", "earrings", "earings", "purse",
                    "mascara", "nails polish", "lipstick", "eyes shadow"})) {
                    type = 3;
                }
            }else if(this.contains(input, new String[]{"masculine", "men", "male", "man", "him"})) {

                if(this.contains(input, new String[]{"shirt", "polo"})) {
                    type = 4;
                }else if(this.contains(input, new String[]{"shoe"})) {
                    type = 5;
                }else if(this.contains(input, new String[]{"watch", "belt", "stud", "glass", "sun shade", "cuff link"})) {
                    type = 6;
                }
            }else if(this.contains(input, new String[]{"junior", "kid", "toy", "girl", "boy", "jsp", "nickelodeon", "cinderella", "lego"})) {
                if(this.contains(input, new String[]{"shirt", "polo", "dress", "skirt", "blouse"})) {
                    type = 7;
                }else if(this.contains(input, new String[]{"shoe"})) {
                    type = 8;
                }else if(this.contains(input, new String[]{"watch", "belt", "glass", "necklace", "sun shade"})) {
                    type= 9;
                }
            }else if(this.contains(input, new String[]{"baby"})) {
                if(this.contains(input, new String[]{"shoe"})) {
                    type = 11;
                }else{
                    type= 10;
                }
            }
    //System.out.println("TaafooFormatter. Type: "+type+", Input: "+input);        
            return type;
        }

        private boolean contains(String s, String [] arr) {
            for(String e:arr) {
                if(s.contains(e)) return true;
            }
            return false;
        }
    }//~END
}
