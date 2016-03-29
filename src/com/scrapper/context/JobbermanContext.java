package com.scrapper.context;

import com.bc.util.XLogger;
import com.bc.json.config.JsonConfig;
import com.scrapper.formatter.DefaultFormatter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @(#)JobbermanContext.java   19-Oct-2013 03:01:30
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
public class JobbermanContext extends DefaultCapturerContext {
    
    public JobbermanContext() { }
    
    public JobbermanContext(JsonConfig config) { 
        super(config);
    }
    
    public static class JobbermanFormatter extends DefaultFormatter {
        private Pattern expiryPattern;
        private Pattern jobIdPattern;
        public JobbermanFormatter(CapturerContext context) {
            super(context);
            expiryPattern = Pattern.compile("([0-9]{1,3})\\s(month|week|day)[s]{0,1}\\s[from now]", Pattern.CASE_INSENSITIVE);
            jobIdPattern = Pattern.compile("\\/([0-9]+)");
        }
        @Override
        public Map<String, Object> format(Map<String, Object> parameters) {

            this.formatHowToApply(parameters);
            
            this.formatDates(parameters);
            
            return super.format(parameters);
        }
        
        private void formatHowToApply(Map<String, Object> parameters) {
            Object oval = parameters.get("howToApply");
            if(oval == null) return;
            String sval = oval.toString().trim();
            String target = "You can also ";
            int start = sval.indexOf(target);
            if(start > -1) {
                start = start + target.length();
                if(start < sval.length()) {
                    parameters.put("howToApply", sval.substring(start));
                }
            }
        }
        
        private void formatDates(Map<String, Object> parameters) {
            
            Object dateinObj = parameters.get("datein");
            
            if(dateinObj == null || dateinObj.toString().isEmpty()) {
                String url = this.getUrl(parameters);
                dateinObj = this.guessDateinFromUrl(url);
                if(dateinObj != null) {
XLogger.getInstance().log(Level.FINEST, "Datein: {0} was guessed from URL: {1}",
        this.getClass(), dateinObj, url);                    
                    // This map does not accept null values HashMapNoNulls
                    parameters.put("datein", dateinObj);
                }else{
XLogger.getInstance().log(Level.WARNING, "Unable to guess datein from URL: {0}. Expected URL containing pattern: {1}", 
        this.getClass(), url, jobIdPattern.pattern());                    
                }
            }
            
            if(dateinObj != null) {
                
                int daysToAdd = this.getDaysToAdd(parameters);
                
                if(daysToAdd > 0) {
                    Date date = this.parseDate(dateinObj.toString());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.DAY_OF_YEAR, daysToAdd);
                    if(cal.before(Calendar.getInstance())) {
                        parameters.put("status", 4); // taken/sold
                    }
                    
                    parameters.put("expiryDate", this.formatDate(cal.getTime()));
                }
            }
        }
        private int getDaysToAdd(Map parameters) {
            int daysToAdd = 0;
            String [] cols = {"expiryDate", "description", "keywords", 
                "minExperience", "minQualification"};
            for(String col:cols) {
                Object val = parameters.get(col);
                if(val == null) {
                    continue;
                }
                daysToAdd = this.getDaysToAdd(val.toString());    
                if(daysToAdd > 0) {
                    break;
                }    
            }
            return daysToAdd;
        }   
        private int getDaysToAdd(String data) {     
            data = data.toLowerCase();
            int daysToAdd = 0;
            Matcher matcher = expiryPattern.matcher(data);
            if(matcher.find()) {
                String num = matcher.group(1);
                String typ = matcher.group(2);
                try{
                    int n = Integer.parseInt(num.trim());
                    daysToAdd = n * this.getDaysInType(typ);
                }catch(NumberFormatException e) { }
            }
            return daysToAdd;
        }
        private int getDaysInType(String type) {
            int multiple = 0;
            if(type.startsWith("month")) {
                multiple = 30;
            }else if(type.startsWith("week")) {
                multiple = 7;
            }else if(type.startsWith("day")) {
                multiple = 1;
            }
            return multiple;
        }
        private String formatDate(Date date) {
            if(this.getInputDateformat() == null) {
                return date.toString();
            }
            return this.getInputDateformat().format(date);
        }
        private Date parseDate(String str) {
            Date datein = null;
            try{
                datein = this.getOutputDateformat().parse(str);
            }catch(ParseException e) {
                try{
                    datein = this.getInputDateformat().parse(str);
                }catch(ParseException e1) { }    
            }
            return datein;
        }
        
        private String guessDateinFromUrl(String url) {
            
            Matcher matcher = jobIdPattern.matcher(url);
            
            if(!matcher.find()) {
                return null;
            }
            
            String numStr = matcher.group(1);
            
            int n = -1;
            try{
                n = Integer.parseInt(numStr);
            }catch(NumberFormatException e) { }
            
            if(n == -1) {
                return null;
            }
            
            // 270750 = 5 Oct 2013
            
            int diff = n - 270750;
            
            int days = diff/40;
            
XLogger.getInstance().log(Level.FINEST, "Diff: {0}, Days: {1}", 
        this.getClass(), diff, days);            
            Calendar datein = Calendar.getInstance();
            
            datein.set(2013, 9, 5);  // 5 Oct 2013
            
            datein.add(Calendar.DAY_OF_YEAR, days);

            return this.formatDate(datein.getTime());
        }
    }
}

