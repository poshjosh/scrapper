package com.scrapper;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.url.ConfigURLList;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.junit.Test;

/**
 * @(#)BasicTests.java   28-Sep-2013 03:32:34
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
public class BasicTests {
    
    public BasicTests() throws Exception {
        CapturerApp.getInstance().init(true);
    }
    
    @Test
    public void testCloneNode() {
        try{

            Div parent = new Div();
            parent.setAttribute("id", "parent");
            
            NodeList siblings = new NodeList();
            parent.setChildren(siblings);
            
            Span sibling = new Span();
            sibling.setAttribute("id", "sibling");
            siblings.add(sibling);
            sibling.setParent(parent);
            
            Div tag = new Div();
            tag.setStartPosition(0);
            tag.setEndPosition(1000);
            tag.setAttribute("id", "target");
            siblings.add(tag);
            tag.setParent(parent);
            
            NodeList children = new NodeList();
            tag.setChildren(children);
            
            LinkTag linkTag = new LinkTag();
            linkTag.setLink("http://www.looseboxes.com");
            linkTag.setAttribute("id", "child_0");
            children.add(linkTag);
            linkTag.setParent(tag);
            
            Span child_1 = new Span();
            child_1.setAttribute("id", "child_1");
            children.add(child_1);
            child_1.setParent(tag);
            
            Span child_2 = new Span();
            child_2.setAttribute("id", "child_2");
            children.add(child_2);
            child_2.setParent(tag);
            
            NodeList grandChildren = new NodeList();
            child_2.setChildren(grandChildren);
            
            TextNode grandChild = new TextNode("This is some text");
            grandChildren.add(grandChild);
            grandChild.setParent(child_2);
            
XLogger.getInstance().log(Level.INFO, "Before clone1, Tag: {0}, Children: {1}", 
    BasicTests.class, tag.toTagHtml(), tag.getChildren()==null?null:tag.getChildren().size());            
            
//            Tag clone = (Tag)tag.clone();
            Tag clone;
            try{
                clone = (Tag)com.scrapper.util.Util.deepClone(tag);
            }catch(CloneNotSupportedException e) {
                return;
            }
            
XLogger.getInstance().log(Level.INFO, "After clone1, Tag: {0}, Children: {1}", 
    BasicTests.class, clone.toTagHtml(), clone.getChildren()==null?null:clone.getChildren().size());            

            boolean a = tag.getParent().getChildren().contains(tag);
            boolean b = clone.getParent().getChildren().contains(clone);

XLogger.getInstance().log(Level.INFO, "Consistency test on Tag passed: {0}, on clone passed: {1}", 
    BasicTests.class, a, b);            

//XLogger.getInstance().log(Level.INFO, "Tag structure: {0}", BasicTests.class, new TagStructure().build(tag));            

//XLogger.getInstance().log(Level.INFO, "Clone structure: {0}", BasicTests.class, new TagStructure().build(tag));            
            
            clone.getChildren().removeAll();

XLogger.getInstance().log(Level.INFO, "After removing clone children, Tag: {0}, Children: {1}", 
    BasicTests.class, tag.toTagHtml(), tag.getChildren()==null?null:tag.getChildren().size());            

        }catch(RuntimeException e) {

            XLogger.getInstance().log(Level.WARNING, "", BasicTests.class, e);
        }
    }
    
    @Test
    public void testMultiURLList() throws Exception {
        
        ScrapperConfigFactory factory = CapturerApp.getInstance().getConfigFactory();
        
        Set<String> names = factory.getSitenames();
    
        for(String name:names) {
            
            JsonConfig config = factory.getConfig(name);
            
//            if(ConfigURLPartList.getSerialPart(config, "counter") == null) {
//                continue;
//            }
            
            Object oval = config.getObject("url", "counter");
            
            if(oval == null) {
                continue;
            }
            
            testMultiURLList(config);
        }
    }
    
    private void testMultiURLList(JsonConfig config) {
        ConfigURLList list = new ConfigURLList();
        list.update(config, "counter");
System.out.println("List size: "+(list==null?null:list.size()));        
        if(list == null || list.isEmpty()) {
            return;
        }
StringBuilder builder = new StringBuilder("PRINTING LIST FOR: "+config.getName());        
        for(String url:list) {
builder.append('\n').append(url);
        }
System.out.println(builder.toString());        
    }
    
    @Test
    public void testJobbermanFormatterPatterns() {
        
            Pattern expiryPattern = Pattern.compile("([0-9]{1,3})\\s(month|week|day)[s]{0,1}\\s[from now]", Pattern.CASE_INSENSITIVE);
            Pattern jobIdPattern = Pattern.compile("\\/([0-9]+)");
            
            String [] inputs = {"1 day from now", "1 day from no", "1 days from now", "39 week from now", "100 month from now"};
            
            for(String input:inputs) {
                
                Matcher matcher = expiryPattern.matcher(input);
                
                if(matcher.find()) {
System.out.println("Found: "+matcher.group(1)+", and: "+matcher.group(2)+", in: "+input);                    
                }else{
System.err.println("Match failed for: "+input);                    
                }
            }

            inputs = new String[]{"http://www.jobberman.com/job/270/", "www.jobberman.com/job/270111/",
            "http://www.jobberman.com/job/270558/cheif-executive-officer-in-lagos/"};
            
            for(String input:inputs) {
                
                Matcher matcher = jobIdPattern.matcher(input);
                
                if(matcher.find()) {
System.out.println("Found: "+matcher.group(1)+", in: "+input);                    
                }else{
System.err.println("Match failed for: "+input);                    
                }
            }
    }
    
    @Test
    public void regexTest() throws MalformedURLException {
        //[/item/].+[-dress].+[\d]+\.html            
        String regex = "[/item/].+[-dress].+[\\d]+\\.html";            
        String [] urls = {            
        "http://www.aliexpress.com/wholesale?g=y&SearchText=dress&CatId=200003482&maxPrice=10&shipCountry=ng&page=1",
        "http://www.aliexpress.com/item/Without-Belt-Korean-Women-Summer-New-Fashion-Chiffon-Dress-Short-sleeve-Dots-Polka-Waist-Mini-Beige/629131530.html",
        "http://www.aliexpress.com/item/New-Arrival-Summer-Women-s-Dress-Crew-Neck-Chiffon-Sleeveless-Causal-Tunic-Sundress-4-colors-Free/704934446.html",
        "http://www.aliexpress.com/item/Fashion-Sleeveless-Dress-with-belt-121870/537932957.html"
        };
        Pattern pattern = Pattern.compile(regex);
        for(int i=0; i<urls.length; i++) {
            System.out.println(pattern.matcher(urls[i]).find());
        }
    }
    
    @Test
    public void regexTest_1() {
        String regex = "[^\\(]{0,1}\"(.*?)\"[^[\\)\\.]]{0,1}";
        regex = "\"(.*?)\"";
        Pattern p = Pattern.compile(regex);
        StringBuilder errMsgs = new StringBuilder();
        this.regexTest(p, "\"abc\"", true, errMsgs);
        this.regexTest(p, "(\"abc\")", true, errMsgs);
        this.regexTest(p, "\"abc\".", true, errMsgs);
if(errMsgs.length() > 0) {
    throw new AssertionError(errMsgs.toString());
}else{
    System.out.println("SUCCESS!");
}        
    }
    
    private void regexTest(Pattern p, String input, boolean expected, StringBuilder errMsgs) {
        boolean result = p.matcher(input).find();
        if(result != expected) {
            errMsgs.append("Expected: ").append(expected).append(", result: ");
            errMsgs.append(result).append(", input: ").append(input);
        }
    }
}
