package com.scrapper.context;

import com.bc.json.config.JsonConfig;
import com.scrapper.util.Util;
import com.scrapper.extractor.NodeExtractorOld;
import com.scrapper.extractor.TargetNodeExtractor;
import com.scrapper.filter.HasAttributesRegexFilter;
import com.scrapper.filter.DefaultBoundsFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.Translate;
import com.bc.jpa.fk.Keywords;
import com.scrapper.config.Config;
import java.util.List;

/**
 * @(#)CareersnigeriaContext.java   22-Feb-2013 23:00:10
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
public class CareersnigeriaContext extends DefaultCapturerContext {
    
    public CareersnigeriaContext() { }
    
    public CareersnigeriaContext(JsonConfig config) { 
        super(config);
    }
    
    public class CareersnigeriaExtractor extends TargetNodeExtractor {

        private CareersNigeriaVisitor targetNodeVisitor;

        public CareersnigeriaExtractor(CapturerContext context) {
            super(context);
            this.targetNodeVisitor = new CareersNigeriaVisitor();
        }

        @Override
        public void reset() {
            super.reset();
            this.targetNodeVisitor.reset();
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
    
    private class CareersNigeriaVisitor 
            extends NodeExtractorOld {

        private boolean inH2;
        private boolean inJobsDiv;
        private boolean inLI;
        private boolean inP;
        private boolean inUL;
        private boolean inStrong;

        private StringBuilder h2Text = new StringBuilder();
        private StringBuilder remainderText = new StringBuilder();
        private StringBuilder strongText = new StringBuilder();
        private StringBuilder liText = new StringBuilder();

        private boolean done;

        private boolean doneUrl;
        private String howToApplyUrl;

        private String email;

        private Pattern emailPattern;
        private Pattern datePostedPattern;
        
        public CareersNigeriaVisitor() {
            this.emailPattern = Util.getEmailPattern();
            this.datePostedPattern = Pattern.compile("\\b\\d{1,2}/\\d{1,2}/\\d{4}\\b");
        }

        @Override
        public void reset() {
            super.reset();
            inH2 = false;
            inJobsDiv = false;
            inLI = false;
            inP = false;
            inUL = false;
            inStrong = false;
            h2Text = new StringBuilder();
            remainderText = new StringBuilder();
            strongText = new StringBuilder();
            liText = new StringBuilder();
            done = false;
            doneUrl = false;
            howToApplyUrl = null;
            email = null;
        }

        @Override
        public void finishedParsing() {
            // Update any extracted data not yet updated
            this.updateExtractedData();

            if(email == null && howToApplyUrl == null) return;

            Object howToApply = Util.findValueWithMatchingKey(
                    this.getExtractedData(), "apply");
    //System.out.println(this.getClass().getId()+"#getExtractedData. How to apply: "+howToApply);
            if(howToApply == null) {


                if(howToApplyUrl != null) {
                    howToApply = howToApplyUrl;
                }

                if(email != null) {
                    if(howToApply == null) {
                        howToApply = email;
                    }else{
                        howToApply = howToApply + " " + email;
                    }
                }

                if(howToApply != null) {
                    this.getExtractedData().put("apply", howToApply);
                }
            }
        }

        @Override
        public void visitTag(Tag tag) {

            if(this.done) return;

            String tagName = tag.getTagName();
    //System.out.println(this.getClass().getId()+"#visitTag. " + tagName);
            if(!doneUrl) {
                this.doneUrl = true;
                howToApplyUrl = tag.getPage().getUrl();
            }

            if(tag instanceof LinkTag) {

                LinkTag linkTag = ((LinkTag)tag);

                this.updateLink(linkTag);
            }

            if(tagName.equals("H2")) {
                inH2 = true;
            }else
            if(tagName.equals("DIV")) {
                String val = tag.getAttribute("id");
                if(val != null && val.equals("jobs_rel")){
                    this.inJobsDiv = true;
                }
            }else
            if(tagName.equals("LI")) {

                this.inLI = true;

            }else
            if(tagName.equals("UL"))  {

                this.inUL = true;

            }else
            if(tagName.equals("P")) {

                this.inP = true;

            }else
            if(tagName.equals("STRONG")) {
                this.inStrong = true;
            }
        }

        @Override
        public void visitEndTag(Tag tag) {

            if(this.done) return;

            String tagName = tag.getTagName();
    //System.out.println(this.getClass().getId()+"#visitEngTag. " + tagName);
            if(tagName.equals("H2")) {
                inH2 = false;
            }else
            if(tagName.equals("DIV")) {
                if(this.inJobsDiv) {
                    this.inJobsDiv = false;
                }
            }else
            if(tagName.equals("LI")) {

                this.inLI = false;

    //System.out.println(this.getClass().getId()+"#visitEndTag. Text: "+liText);
                // Note this
                //
                this.addCompanyName();

                this.addDatein();

                liText = new StringBuilder();

            }else
            if(tagName.equals("UL")) {

                this.inUL = false;

            }else
            if(tagName.equals("P")) {
                this.inP = false;
            }else
            if(tagName.equals("STRONG")) {
                this.inStrong = false;
            }
        }

        private void updateExtractedData() {

            if(strongText.length() > 0 && remainderText.length() > 0) {
    //System.out.println(this.getClass().getId()+"#updateExtractedData. Adding: ["+strongText+"="+this.remainderText+"]");
                this.getExtractedData().put(this.strongText.toString(), this.remainderText.toString());
                strongText = new StringBuilder();
                remainderText = new StringBuilder();
            }

            if(!doneH2() && h2Text.length() > 0) {
                this.addJobTitle();
                this.addType();
                h2Text = null;
            }
        }

        private void addJobTitle() {
    //System.out.println(this.getClass().getId()+"#addJobTitle. Adding: "+h2Text);
            this.getExtractedData().put("jobTitle", h2Text.toString());
        }

        private void addType() {
            
            Keywords keyWords = CareersnigeriaContext.this.getKeywords();
            
            if(keyWords == null) {
                return;
            }
            
            List tables = CareersnigeriaContext.this.getConfig().getList(Config.Site.tables);
            
            keyWords.setTableName(tables.get(0).toString());
            
    //System.out.println(this.getClass().getId()+"#addType. Input: "+h2Text);
            Integer val = keyWords.findMatchingKey("type", this.h2Text.toString(), true);
            
            if(val != null) {
    //System.out.println(this.getClass().getId()+"#addType. Adding: "+val);
                this.getExtractedData().put("type", val);
            }
        }

        private boolean doneH2() {
            return h2Text == null;
        }

        private void addCompanyName() {
            if(liText.length() == 0) return;
            String str = liText.toString().toLowerCase();
            if(!str.startsWith("industry:")) return;
            String companyName = liText.substring("industry:".length()).trim();
    //System.out.println(this.getClass().getId()+"#addCompanyName. Adding: "+companyName);
            this.getExtractedData().put("companyName", companyName);
        }

        private void addDatein() {

            if(liText.length() == 0) return;

            String str = liText.toString().toLowerCase();

            if(!str.startsWith("posted:")) return;

            Matcher m = this.datePostedPattern.matcher(liText);

            if(m.find()) {
                this.getExtractedData().put("datein", m.group());
            }
        }

        private void updateLink(LinkTag linkTag) {

            String link = linkTag.getLink();

            String linkText = linkTag.getLinkText();

            if(linkText.toLowerCase().contains("apply") ||
                    linkText.toLowerCase().contains("application")) {

                this.howToApplyUrl = "Visit: " + link;

            }

            this.updateText(linkText + " " + link, true);

            return;
        }

        @Override
        public void visitStringNode(Text node){

            if(this.done) return;

            this.updateText(node.getText(), false);
        }

        private void updateText(String text, boolean link){

    //System.out.println(this.getClass().getId()+"#updateText. " +
    //        "Link:" +link+ ", InJobsDiv:"+this.inJobsDiv +
    //        ", inLi:"+this.inLI+", inUL:"+this.inUL+
    //        ", inH2:"+this.inH2+", inP:"+this.inP+", inStrong:"+
    //        this.inStrong+"\nText:"+text);

            // If you don't do this then all comparisons e.g .equals will fail
            // As & can't be compared to its character reference
            //
            text = Translate.decode(text);

            if(!doneH2() && inH2) {
                h2Text.append(text);
                h2Text.append(" ");
                return;
            }

            if(this.inJobsDiv && this.inLI) {
                if(!link) {
                    this.liText.append(text);
                    this.liText.append(" ");
                }
                return;
            }

            if(inP) {

                if(inStrong || this.isUpperCase(text)) {

                    // Note this
                    //
                    this.updateExtractedData();

                    this.strongText.append(text);
                    this.strongText.append(" ");

                    return;
                }
            }

            if(this.inP || this.inLI) {

                if(!text.toLowerCase().startsWith("tags:")) {

                    if(this.hasEmail(text)) {
                        // Only the last email is required
                        email = text;
                    }

                    this.remainderText.append(text);
                    this.remainderText.append(" ");

                }else{
                    this.done = true;
                }
            }
        }

        private boolean hasEmail(String str) {
            return this.emailPattern.matcher(str).find();
        }

        private boolean isUpperCase(String str) {
            for(int i=0; i<str.length(); i++) {
                char ch = str.charAt(i);
                if(Character.isLowerCase(ch)) {
                    return false;
                }
            }
            return true;
        }
    }

    //<div class="post-12178 post hentry category-accounting-jobs-in-nigeria category-jobs tag-cima tag-cost-accountant tag-ican" id="post-12178">
    //  <h2>Cost Accountant (ACA, ACCA, CIMA)</h2>
    //  <div id="jobs_rel">
    //    <ul>
    //      <li>Category: <a href="http://www.careersnigeria.com/category/accounting-jobs-in-nigeria" title="View all posts in Accounting Jobs Nigeria" rel="category tag">Accounting Jobs Nigeria</a>,  <a href="http://www.careersnigeria.com/category/jobs" title="View all posts in Jobs" rel="category tag">Jobs</a></li>
    //      <li>Industry: <a href="http://www.careersnigeria.com/tag/cima" rel="tag">CIMA</a>, <a href="http://www.careersnigeria.com/tag/cost-accountant" rel="tag">Cost Accountant</a>, <a href="http://www.careersnigeria.com/tag/ican" rel="tag">ICAN</a></li>
    //      <li>Posted:  20/2/2011</li>
    //    </ul>
    //  </div>
    //  <div class="clear"></div>
    //  <div class="entry">
    //    <p>We are currently searching for an highly experienced <strong>Cost Accountant</strong> for a leading FMCG company in Nigeria.</p>
    //    <p><strong>RESPONSIBILITIES</strong></p>
    //    <ul>
    //      <li>Act as the “costing expert” and custodian of the costing system within the organization.</li>
    //      <li>Maintain accurate standard costs across all aspects of the production process.</li>
    //    </ul>
    //  </div>
    //</div>
    public static class CareersnigeriaNodeFilter extends DefaultBoundsFilter {

        public CareersnigeriaNodeFilter(JsonConfig site) throws IOException {

            super(site);

            TagNameFilter DIV = new TagNameFilter("DIV");
            TagNameFilter H2 = new TagNameFilter("H2");

            HasAttributesRegexFilter hasMyId = new HasAttributesRegexFilter("id", "\\bpost-[\\d]+\\b");

            HasChildFilter hasH2 = new HasChildFilter();
            hasH2.setRecursive(false);
            hasH2.setChildFilter(H2);

            HasChildFilter hasDiv = new HasChildFilter();
            hasDiv.setRecursive(false);
            hasDiv.setChildFilter(DIV);

            AndFilter targetFilter = new AndFilter();
            targetFilter.setPredicates(new NodeFilter[]{DIV, hasMyId, hasH2, hasDiv});

            // Replace the default
            HasChildFilter hasChild = new HasChildFilter();
            hasChild.setRecursive(true);
            hasChild.setChildFilter(targetFilter);
            
            this.setParentFilter(hasChild);
        }
    }//~END
}
