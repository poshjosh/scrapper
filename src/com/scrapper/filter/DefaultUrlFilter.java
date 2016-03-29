package com.scrapper.filter;

import com.bc.manager.Filter;
import com.bc.util.XLogger;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @(#)DefaultUrlFilter.java   21-Feb-2013 23:36:31
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
public class DefaultUrlFilter implements Filter<String> {
    
    private String id;
    private Pattern requiredPattern;
    private Pattern unwantedPattern;
    private String [] required;
    private String [] unwanted;

    /**
     * Only works if urls passed through this filter have explicit date formats.
     * <p><b>Parseable dates:</b> 2014/06/13, 24/01/2016</p>
     * <p><b>Unparseable dates:</b> 14/06(Can't tell year), 14/09/11 (Can't tell month or day)</p> 
     */ 
//@update    
    private Calendar dateOffset;
    
    public DefaultUrlFilter() { }
    
    @Override
    public boolean accept(String url) {
XLogger.getInstance().log(Level.FINER, "Date Offset: {0}, URL: {1}", 
        this.getClass(), dateOffset==null?null:dateOffset.getTime(), url);
        
        url = this.format(url);

        boolean output = true;

        if(dateOffset != null) {
            Calendar date = parseDate(url);
            if(date != null) {
                if(!date.after(dateOffset)) {
                    output = false;
                    return false;
                }
            }
        }
        
        try{
            
            if(unwantedPattern != null) {
                
                boolean found = unwantedPattern.matcher(url).find();
XLogger.getInstance().log(Level.FINER, "{0}, Accepted: {1}, URL: {2}, unwanted regex: {3}", 
        this.getClass(), id, !found, url,unwantedPattern.pattern());        
                if(found) {
                    output = false;
                    return false;
                }
            }

            if(unwanted != null && unwanted.length > 0) {
                for(String s:unwanted) {
                    boolean contains = url.contains(s); 
XLogger.getInstance().log(Level.FINER, "{0}, Accepted: {1}, URL: {2}, unwanted text: {3}", 
        this.getClass(), id, !contains, url, s);        
                    if(contains) {
                        output = false;
                        return false;
                    }
                }
            }

            if(requiredPattern != null) {
                
                output = requiredPattern.matcher(url).find();
                
XLogger.getInstance().log(Level.FINER, "{0}, Accepted: {1}, URL: {2}, required regex: {3}", 
        this.getClass(), id, output, url, requiredPattern.pattern());        

                if(output) {
                    return true;
                }
            }else{
                output = true;
            }

            if(required != null && required.length > 0) {

                output = false;

                for(String s:required) {
                    output = url.contains(s);
XLogger.getInstance().log(Level.FINER, "{0}, Accepted: {1}, URL: {2}, required text: {3}", 
        this.getClass(), id, output, url, s);        
                    if(output) {
                        return true;
                    }
                }
            }

            return output;
            
        }finally{

// Usually far more URLs are filtered for capture than for scrapp operation
final Level level = (id != null && id.toLowerCase().contains("capture")) ?
        Level.FINER : Level.FINE;

XLogger.getInstance().log(level, "Accepted: {0}, URL: {1}", 
        this.getClass(), output, url);        

        }
    }
    
    private String format(String s) {
        s = s.toLowerCase();
        final String amp = "&amp;";
        if(s.length() >= amp.length()) {
            s = s.replace("&amp;", "&");
        }
        try{
            s = URLDecoder.decode(s, "UTF-8");
        }catch(UnsupportedEncodingException | RuntimeException ignored) { }
        return s;
    }

    private Calendar parseDate(String url) {
        Calendar date = this.parseDate(this.getPattern0(), url);
        if(date == null) {
            date = this.parseDate(this.getPattern1(), url);
        }
XLogger.getInstance().log(Level.FINER, "URL: {0}, date: {1}", this.getClass(), url, date==null?null:date.getTime());
        return date;
    }

    private Calendar parseDate(Pattern p, String url) {
        // Infer a date string from the url
        String pattern = p.pattern();
        boolean yearFirst = pattern.startsWith("/(\\d{4})/");
        boolean yearLast = pattern.endsWith("/(\\d{4})/");
        Matcher m = p.matcher(url);
        if(m.find()) {
            // If we have four digits then its the year
            Calendar cal;
            if(yearFirst) {
                int year = Integer.parseInt(m.group(1));
                int a = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));
                cal = this.getDate(year, a, b);
            }else if(yearLast){
                int a = Integer.parseInt(m.group(1));
                int b = Integer.parseInt(m.group(2));
                int year = Integer.parseInt(m.group(3));
                cal = this.getDate(year, a, b);
            }else{
                throw new IllegalArgumentException("Unexpected pattern: "+pattern);
            }    
            return cal;
        }
        return null;
    }
    
    private Calendar getDate(int year, int arg0, int arg1) {
        Calendar cal = this.getCalendar();
        cal.clear();
        cal.set(Calendar.YEAR, year);
        // if we have digits greater than 12 then its the day of month
        // and the other is the month
        // otherwise we can't infer the date correctly
        if(arg0 > 12) {
            cal.set(Calendar.MONTH, arg1);
            cal.set(Calendar.DAY_OF_MONTH, arg0);
        }else if(arg1 > 12) {
            cal.set(Calendar.MONTH, arg0);
            cal.set(Calendar.DAY_OF_MONTH, arg1);
        }else{
            cal = null;
        }
        return cal;
    }
    
    private Calendar _c;
    private Calendar getCalendar() {
        if(_c == null) {
            _c = Calendar.getInstance();
        }
        return _c;
    }
    
    private Pattern _p0;
    private Pattern getPattern0() {
        if(_p0 == null) {
            _p0 = Pattern.compile("/(\\d{4})/(\\d{1,2})/(\\d{1,2})/");
        }
        return _p0;
    }
    
    private Pattern _p1;
    private Pattern getPattern1() {
        if(_p1 == null) {
            _p1 = Pattern.compile("/(\\d{1,2})/(\\d{1,2})/(\\d{4})/");
        }
        return _p1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getRequired() {
        return required;
    }

    public void setRequired(String[] arr) {
        this.required = new String[arr.length];
        for(int i=0; i<arr.length; i++) {
            this.required[i] = this.format(arr[i]);
        }
    }

    public String[] getUnwanted() {
        return unwanted;
    }

    public void setUnwanted(String[] arr) {
        this.unwanted = new String[arr.length];
        for(int i=0; i<arr.length; i++) {
            this.unwanted[i] = this.format(arr[i]);
        }
    }

    public Pattern getRequiredPattern() {
        return requiredPattern;
    }

    public void setRequiredPattern(Pattern requiredPattern) {
        this.requiredPattern = requiredPattern;
    }

    public Pattern getUnwantedPattern() {
        return unwantedPattern;
    }

    public void setUnwantedPattern(Pattern unwantedPattern) {
        this.unwantedPattern = unwantedPattern;
    }

    /**
     * @param maxAgeInDays 
     * @see #dateOffset
     */
    public void setMaxAgeDays(int maxAgeInDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -maxAgeInDays);
        this.setDateOffset(cal);
    }

    /**
     * @see #dateOffset
     * @return 
     */
    public Calendar getDateOffset() {
        return dateOffset;
    }

    /**
     * @param dateOffset 
     * @see #dateOffset
     */
    public void setDateOffset(Calendar dateOffset) {
        this.dateOffset = dateOffset;
    }
}
/**
 * 
    private Set<String> getDateStrings(String regex, String url) {
        
        if(dateOffset == null) {
            return null;
        }
        
        Set<String> output = new HashSet(4);
        
        this.appendDateString(output, dateOffset, regex, true);
        this.appendDateString(output, dateOffset, regex, false);
        
        int amount = 1;
        try{
            dateOffset.add(Calendar.DAY_OF_MONTH, -amount); // subtract amount
            this.appendDateString(output, dateOffset, regex, true);
            this.appendDateString(output, dateOffset, regex, false);
        }finally{
            dateOffset.add(Calendar.DAY_OF_MONTH, amount); // add amount
        }
XLogger.getInstance().log(Level.INFO, "Date Strings: {0}", this.getClass(), output);
        return output;
    }
    
    private void appendDateString(Set<String> set, Calendar calendar, String regex, boolean addZeroToMonthsLessThanTen) {
        String s = this.getDateString(calendar, regex, addZeroToMonthsLessThanTen);
        if(s != null) {
            set.add(s);
        }
    }
    
    private String getDateString(Calendar calendar, String regex, boolean addZeroToMonthsLessThanTen) {
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; 
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); 

        if(regex.contains("/\\d{4}/\\d{1,2}/\\d{1,2}/") || regex.contains("/\\d{4}/\\d{2}/\\d{2}/")) {
            StringBuilder builder = this.getBuilder();
            builder.append('/').append(year).append('/');
            if(addZeroToMonthsLessThanTen && month < 10) {
                builder.append('0');
            }
            builder.append(month).append('/').append(dayOfMonth).append('/');
            return builder.toString();
        }else if(regex.contains("/\\d{4}/\\d{1,2}/") || regex.contains("/\\d{4}/\\d{2}/")) {
            StringBuilder builder = this.getBuilder();
            builder.append('/').append(year).append('/');
            if(addZeroToMonthsLessThanTen && month < 10) {
                builder.append('0');
            }
            builder.append(month).append('/');
            return builder.toString();
        }else if(regex.contains("/\\d{4}/")) {
            StringBuilder builder = this.getBuilder();
            builder.append('/').append(year).append('/');
            return builder.toString();
        }else{
            return null;
        }
    }
    
    private StringBuilder _b;
    private StringBuilder getBuilder() {
        if(_b == null) {
            _b = new StringBuilder(15);
        }else{
            _b.setLength(0);
        }
        return _b;
    }
    
    private boolean contains(String url, Set<String> set) {
        for(String s:set) {
            if(s != null && url.contains(s)) {
                return true;
            }
        }
        return false;
    }
    
 * 
 */