package com.scrapper.formatter;

import com.bc.manager.Formatter;
import java.io.Serializable;

/**
 * @(#)CompanyNameFormatter.java   07-Mar-2013 01:27:05
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
public class CompanyNameFormatter implements Formatter<String>, Serializable {

    private String jobTitle;

    private String [] preWords;

    private String [] postWords;

    public CompanyNameFormatter() {
//        preWords = new String[]{" wanted at ", " vacancies at ", " needed at ", " at "};
//        postWords = new String[]{" vacancies ", " recruits ", " vacancy for "};
        preWords = new String[]{" wanted at ", " vacancies at ", " needed at ", " jobs at ", " at "};
        postWords = new String[]{" latest job vacancies ", " job vacancies ", " vacancies ", " recruits ", " vacancy for "};
    }

    public void reset() {
        jobTitle = null;
    }

    @Override
    public String format(String s) {

        String companyName = null;

        // E.g ABC Company Recruitment 2011 : Graduate Trainee
        //
        String [] parts = s.split(":");

        if(parts != null && parts.length > 1) {

            companyName = this.extractCompanyName(parts[0]);

            this.jobTitle = parts[1].trim();
        }

        // Drivers wanted at ABC Company, Cooks needed at XYZ Limited etc
        //
        if(companyName == null) {

            companyName = this.extractCompanyName(s);
        }

        if(companyName != null) {
            companyName = new ToTitleCase().format(companyName);
        }
//System.out.println(this.getClass().getName()+". Input: " + s+", Output: "+companyName);
        return companyName;
    }

    private String extractCompanyName(String s) {

        String companyName = this.extractCompanyName_0(s);

        // ABC Company Limited .... other title text
        //
        if(companyName == null) {

            companyName = this.extractCompanyName_1(s);

            if(companyName == null) {

                companyName = this.extractCompanyName_2(s);
            }
        }
//System.out.println(this.getClass().getName()+"#extractCompanyName. companyName: "+companyName);
        return companyName;
    }

    private String extractCompanyName_0(String s) {

        String sL = s.toLowerCase();

        // Notice the space before and after each array element
        //
        String [] parts = this.split(sL,
                preWords);
//System.out.println(this.getClass().getName()+"#extractCompanyName_0. Parts: " + Arrays.asList(parts));
        String companyName = null;

        if(parts != null) {

            this.jobTitle = parts[0].trim();

            companyName = parts[1].trim();

        }else{

            parts = this.split(sL, postWords);

            if(parts != null) {

                companyName = parts[0].trim();

                this.jobTitle = parts[1].trim();
            }
        }
//System.out.println(this.getClass().getName()+"#extractCompanyName_0. companyName:"+companyName);
        return companyName;
    }

    private String [] split(String src, String [] splits) {

        for(String split:splits) {

            String [] parts = this.split(src, split);

            if(parts != null) {
                return parts;
            }
        }
        return null;
    }

    private String [] split(String src, String split) {

        String [] parts = src.split(split);

        return (parts != null && parts.length > 1) ? parts : null;
    }

    private String extractCompanyName_1(String s) {

        // ABC Company Nigeria Limited
        //
        // use index of limiteds as end of companyName if it is found
        // otherwise use index of nigeria if it  is found, otherwise
        // use index of company if it is found, otherwise use only
        // the first part of String.split(" ")
        //
        String companyName = this.substring(s.toLowerCase(), 0,
                new String[]{"limited", "plc", "nigeria", "company"});
//System.out.println(this.getClass().getName()+"#extractCompanyName_1. companyName:"+companyName);
        return companyName;
    }

    private String substring(String src, int start, String [] ends) {

        for(int i=0; i<ends.length; i++) {

            String companyName = this.substring(src, start, ends[i]);

            if(companyName != null) {

                return companyName;
            }
        }

        return null;
    }

    private String substring(String src, int start, String endText) {

        int end = src.indexOf(endText);

        String output = end == -1 ? null : src.substring(start, end + endText.length());
//System.out.println(this.getClass().getName()+"#substring. Input: "+endText+", Output: "+output);
        return output;
    }

    private String extractCompanyName_2(String s) {

        String companyName = null;

        int end = s.indexOf(" ");

        if(end == -1) {
            companyName = s;
        }else{
            companyName = s.substring(0, end);
//            String [] parts = s.split(" ");
//            if(parts.length >= 4) {
//                companyName = parts[0] + " " + parts[1];
//            }else{
//                companyName = parts[0];
//            }
        }
//System.out.println(this.getClass().getName()+"#extractCompanyName_2. companyName:"+companyName);
        return companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String[] getPostWords() {
        return postWords;
    }

    public void setPostWords(String[] postWords) {
        this.postWords = postWords;
    }

    public String[] getPreWords() {
        return preWords;
    }

    public void setPreWords(String[] preWords) {
        this.preWords = preWords;
    }
}

