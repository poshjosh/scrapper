package com.scrapper.formatter;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author  chinomso bassey ikwuagwu
 * @version  0.3
 * @since   1.0
 */
public class MyDateFormat extends SimpleDateFormat {

    private Pattern validationPattern;
    
    // Default value
    //
    private String [] acceptedPatterns;

    public MyDateFormat() { 
        this.setLenient(false);
    }

    public MyDateFormat(String [] patterns){
        this.acceptedPatterns = patterns;
        this.setLenient(false);
    }

    @Override
    public Date parse(String text, ParsePosition pos) {

        Calendar cal = null;
        
        for(String pattern: acceptedPatterns) {

            this.applyPattern(pattern);

            Date date = super.parse(text, pos);

//System.out.println("Pattern: "+pattern+", date: "+date);

            if(date == null) {
                continue;
            }

            pattern = pattern.trim();
            
            if(!this.requiresValidation(pattern)) {
                
                return date;
            }
            
            if(cal == null) {
                cal = Calendar.getInstance();
            }
// Validation            
//
// Both dd MM yyyy and MM dd yyyy matches 26 10 2013            
// Also dd MM yyyy matches both 26 10 2013, 10 26 2013

// We decide which pattern to use by examining the date

// If the first 2 pairs of digits have a value greater than 12,
// then it is considered the 'dd' part

            
            if(this.isValid(text, pattern, date, cal)) {
                
                return date;
            }
        }

        return null;
    }
    
    private boolean requiresValidation(String pattern) {
        
        if(!this.isLenient()) {
            return false;
        }
        
        if(validationPattern == null) {
            validationPattern = Pattern.compile("(MM(-|\\s|/){1}dd)|(dd(-|\\s|/){1}MM)");
        }

        pattern = pattern.trim();

        Matcher m = validationPattern.matcher(pattern);

        return m.find() && m.start() == 0;
    }
    
    private boolean isValid(String input, String pattern, Date date, Calendar cal) {
        
//System.out.println("Checking validity. Input: "+input+", pattern: "+pattern+", date output: "+date);

        boolean isValid = true;

        cal.setTime(date);
        
        boolean a = pattern.contains("yyyy") && this.matchesYear(input, "yyyy", cal);
        boolean b = pattern.contains("yy") && this.matchesYear(input, "yy", cal);
//System.out.println("Matches long year: "+a+", short year: "+b);        

        boolean matchesYear = a || b;

        if(!matchesYear) {

            isValid = false;

        }else{

            if(pattern.startsWith("MM")) {

                boolean matchesMonth = this.matchesFirstField(2, input, cal, Calendar.MONTH);
//System.out.println("Matches month: "+matchesMonth);
                if(!matchesMonth) {

                    isValid = false;
                }
            }else if(pattern.startsWith("dd")) {

                boolean matchesDate = this.matchesFirstField(2, input, cal, Calendar.DATE);
//System.out.println("Matches date: "+matchesDate);
                if(!matchesDate) {

                    isValid = false;
                }
            }
        }
        
        return isValid;
    }
    
    private boolean matchesYear(String input, String patternPart, Calendar cal) {
        
        boolean contains;
        
        int field = cal.get(Calendar.YEAR);

        String sval = Integer.toString(field);

        // part = yy where as value = 2014
        if(patternPart.length() < sval.length()) {
            int diff = sval.length() - patternPart.length();
            sval = sval.substring(diff);
        }

        StringBuilder buff = new StringBuilder();
        buff.append(' ').append(sval).append(' ');

        if(!input.contains(buff)) {

            contains = false;

        }else{

            contains = true;
        }
        
        return contains;
    }
    
    private boolean matchesFirstField(int digitsInField, String input, Calendar cal, int calendarField) {
        
        String expected = input.trim().substring(0, digitsInField);

        int toAdd;
        
        switch(calendarField) {
            case Calendar.MONTH:
                toAdd = 1; break;
            case Calendar.DATE:
                toAdd = 0; break;
            default:
                throw new UnsupportedOperationException("Unexpected calendar field: "+calendarField);
        }

        int n = cal.get(calendarField) + toAdd;
        
        String prefix = n < 10 ? "0" : "";
        
        String found = prefix + n;
        
//System.out.println("Expected: "+expected+", found: "+found);        

        return expected.equals(found);
    }

    public String[] getAcceptedPatterns() {
        return acceptedPatterns;
    }

    public void setAcceptedPatterns(String[] acceptedPatterns) {
        this.acceptedPatterns = acceptedPatterns;
    }
}//~END
