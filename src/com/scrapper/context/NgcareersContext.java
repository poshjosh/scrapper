package com.scrapper.context;

import com.bc.manager.Formatter;
import com.bc.util.XLogger;
import com.scrapper.config.Config;
import com.bc.json.config.JsonConfig;
import com.scrapper.formatter.DefaultFormatter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

/**
 * @(#)NgcareersContext.java   07-Dec-2013 19:51:34
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
public class NgcareersContext extends DefaultCapturerContext {
    
    public NgcareersContext() { }
    
    public NgcareersContext(JsonConfig config) { 
        super(config);
    }
    
    @Override
    public Formatter<Map<String, Object>> getFormatter(CapturerContext context) {
        return new NgcareersFormatter(context);
    }

    @Override
    public Formatter<String> getUrlFormatter(JsonConfig config) {
        return new NgcareersUrlFormatter(config);
    }
    
    public class NgcareersFormatter extends DefaultFormatter {

        public NgcareersFormatter(CapturerContext context) {
            super(context);
        }

        @Override
        protected Map updateHowToApply(Map parameters) {
            Object howToApply = parameters.get("howToApply");
    XLogger.getInstance().log(Level.FINER, "Before update. How to apply: {0}", 
        this.getClass(), howToApply);        
            if(howToApply == null) {
                Object oval = parameters.get("description");
    XLogger.getInstance().log(Level.FINEST, 
        "Description: {1}", this.getClass(), oval);        
                if(oval == null) {
                    howToApply = this.getMyHowToApply(parameters);
                }else{
                    String sval = oval.toString().toLowerCase();
                    if(!sval.contains("apply") && !sval.contains("application")) {
                        howToApply = this.getMyHowToApply(parameters);
                    }else{
                        howToApply = "see description";
                    }
                }
                parameters.put("howToApply", howToApply);
            }
            String s = parameters.get("howToApply").toString();
            if(s != null) {
                s = s.toLowerCase();
            }
            if(s != null && (s.contains("has expired") ||
                     s.contains("is expired"))) {
    XLogger.getInstance().log(Level.INFO, "Status is set to: Not Available", this.getClass());        
                parameters.put("status", 4); // 4 = Taken

                //@todo
                // change the date, it should be before expiry date
                updateExpiryDate(parameters);
            } 
    XLogger.getInstance().log(Level.INFO, "How to apply: {0}", this.getClass(), howToApply);        
            return parameters;
        }    

        private void updateExpiryDate(Map parameters) {

            Object expObj = parameters.get("expiryDate");

            if(expObj == null) return;

            Date expDate = null;
            try{// Has the expiryDate already been formatted for output?
                expDate = this.getOutputDateformat().parse(expObj.toString());
            }catch(ParseException e) {
                System.out.println("0... "+e);
                try{
                    expDate = this.getInputDateformat().parse(expObj.toString());
                }catch(ParseException e1) {
                    System.out.println("1... "+e);
                }
            }
    XLogger.getInstance().log(Level.FINER, "ExpiryDate: {0}", this.getClass(), expDate);                
            if(expDate != null && expDate.before(new Date())) {

                Calendar cal = Calendar.getInstance();
                cal.setTime(expDate);
                cal.add(Calendar.MONTH, -1);

                Date datein = cal.getTime();

                String dateinStr = this.getOutputDateformat().format(datein);
                parameters.put("datein", dateinStr);
    XLogger.getInstance().log(Level.FINER, "Updating datein to : {0}", this.getClass(), dateinStr);                
            }
        }
    }
    
    public static class NgcareersUrlFormatter implements Formatter<String> {

        private String currentYear;
        private String previousYear;

        private JsonConfig config;

        public NgcareersUrlFormatter(JsonConfig config) {

            this.config = config;

            Calendar c = Calendar.getInstance();
            currentYear = "/" + c.get(Calendar.YEAR);
            c.add(Calendar.YEAR, -1);
            previousYear = "/" + c.get(Calendar.YEAR);
        }

        @Override
        public String format(String link) {
XLogger.getInstance().log(Level.FINE, "Before format: {0}", this.getClass(), link);            

            if(link.startsWith(config.getString(Config.Site.url, "value"))) return link;

            int index = link.indexOf(previousYear);
            if(index == -1) {
                index = link.indexOf(currentYear);
            }
            if(index != -1) {

                link = link.substring(index);

                link = config.getString(Config.Site.url, "value") +link;
            }
XLogger.getInstance().log(Level.FINE, "After format: {0}", this.getClass(), link);            
            return link;
        }
    }
}
