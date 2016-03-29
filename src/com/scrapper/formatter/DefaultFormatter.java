/**
 * @(#)DefaultFormatter.java   19-Apr-2011 15:21:36
 *
 * Copyright 2009 BC Enterprise, Inc. All rights reserved.
 * BCE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.scrapper.formatter;

import com.bc.currencyrateservice.Currencyrate;
import com.bc.currencyrateservice.CurrencyrateService;
import com.bc.currencyrateservice.YahooCurrencyrateService;
import com.bc.manager.Formatter;
import com.bc.util.XLogger;
import com.bc.jpa.fk.Keywords;
import com.scrapper.config.Config;
import com.scrapper.util.Translator;
import com.scrapper.expression.ArithmeticExpressionResolver;
import com.scrapper.expression.ConditionalExpressionResolver;
import com.scrapper.expression.ExpressionResolver;
import com.scrapper.util.Util;
import com.bc.json.config.JsonConfig;
import com.scrapper.context.CapturerContext;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author  chinomso bassey ikwuagwu
 * @version  0.3
 * @since   1.0
 */
public class DefaultFormatter implements Formatter<Map<String, Object>>, Serializable {

    public static final String APPLY_TEXT = "CLICK HERE TO APPLY";
    
    private CapturerContext context;
    
    private DateFormat inputDateformat;
    private DateFormat outputDateformat;
    
    private List jobRequestFields;

    /**
     * When extraction, default values take precedence over extracted values
     * When formatting, extracted values take precedence over default values
     */
    private Map defaultValues;
    
    public DefaultFormatter() { 
        
        SimpleDateFormat infmt = new SimpleDateFormat();
        // We use the date format for java.util.Date
        // Mon Jul 29 10:44:00 CAT 2013
        infmt.applyPattern("EEE MMM dd HH:mm:ss G yyyy");
        this.inputDateformat = infmt;
        
        SimpleDateFormat outfmt = new SimpleDateFormat();
        // 07 29 2013 10:44:00.0
        outfmt.applyPattern("MM dd yyyy HH:mm:ss.S");
        this.outputDateformat = outfmt;
    }

    public DefaultFormatter(CapturerContext context) {
        
        this.context = context;
        
        JsonConfig config = context.getConfig();
        
        Object [] arr = config.getArray(Config.Formatter.jobRequestFields);
        if(arr != null) {
            // We use a copy because we don't want to edit the original
            //
            jobRequestFields = new ArrayList<String>();
            jobRequestFields.addAll(Arrays.asList(arr));
        }    
        
        this.defaultValues = config.getMap(Config.Formatter.defaultValues);
        
        Object [] datePatterns = config.getArray(Config.Formatter.datePatterns);
        if(datePatterns == null || datePatterns.length == 0) {
            this.inputDateformat = new SimpleDateFormat();
        }else{
XLogger.getInstance().log(Level.FINER, "Config: {0}, Date patterns: {1}", 
this.getClass(), config.getName(), (datePatterns==null?null:Arrays.toString(datePatterns)));

            this.inputDateformat = new MyDateFormat(
            Arrays.copyOf(datePatterns, datePatterns.length, String[].class));
        }
        SimpleDateFormat outfmt = new SimpleDateFormat();
        // 07 29 2013 10:44:00.0
        outfmt.applyPattern("MM dd yyyy HH:mm:ss.S");
        this.outputDateformat = outfmt;
    }
    
    public String getTableName(Map<String, Object> parameters) {
        
        String defaultTableName = context.getConfig().getList(Config.Site.tables).get(0).toString();
        
        // The table name in the extract overrides the default
        //
        return Util.getTableValue(parameters, defaultTableName);
    }
    
    protected Map<String, Object> createCopy(Map<String, Object> parameters) {
        HashMap update = new HashMap();
        for(Entry<String, Object> en:parameters.entrySet()) {
            Object val = en.getValue();
            if(val != null) {
                val = val.toString().trim();
            }
            update.put(en.getKey(), val);
        }
        return update;
    }
    
    @Override
    public Map<String, Object> format(Map<String, Object> parameters) {

XLogger.getInstance().log(Level.FINER, "BEFORE Params: {0}", 
        this.getClass(), parameters);

        String tableName = this.getTableName(parameters);

        // Order of method call important
        //
        Map<String, Object> copy = this.createCopy(parameters);

XLogger.getInstance().log(Level.FINER, "BEFORE: {0}", this.getClass(), copy);

        copy = this.resolveExpressions(copy);
        
XLogger.getInstance().log(Level.FINER, "AFTER: {0}", this.getClass(), copy);        
        
        copy = this.translate(copy);
        
        // We have to formatCurrencyTypes before we formatIntegerColumns because
        // currency is formatted here and it is an integer columns
        copy = this.formatCurrencyTypes(copy);
        
        // We have to format type and other integer columns first
        // to avoid some problems 
        //
        copy = this.formatIntegerColumns(tableName, copy);
        
        copy = this.formatDateTypes(copy);
        
        try{
            final int status = this.formatDateinAndExpiryDate(copy);
            copy.put("status", status);
        }catch(RuntimeException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
        
        copy = this.formatLinks(copy);
        
        copy = this.addDefaults(copy);

        if("jobs".equals(tableName)) {
            copy = this.updateHowToApply(copy);
        }
XLogger.getInstance().log(Level.FINER, "{0}. AFTER Params: {1}", 
        this.getClass(), copy);
        return copy;
    }
    
    protected Map<String, Object> translate(Map<String, Object> parameters) {
        Locale locale = null;
        String language = this.context.getConfig().getString(
                Config.Formatter.language);
        if(language != null) {
            Locale [] arr = Locale.getAvailableLocales();
            for(Locale e:arr) {
                if(language.equals(e.getISO3Language()) || 
                        language.equals(e.getLanguage())) {
                    if(e.getCountry() == null || e.getCountry().isEmpty()) {
                        locale = e;
                        break;
                    }
                }
            }
        }
XLogger.getInstance().log(Level.FINER, "Locale: {0}", this.getClass(), locale);        
        
        if(locale == null || locale.equals(Locale.getDefault())) {
            return parameters;
        }
        
        if(!Translator.isSupported(locale)) {
            throw new UnsupportedOperationException("Translation not supported for locale: "+locale);
        }
        
        Set<String> keys = parameters.keySet();
        for(String key:keys) {
            Object val = parameters.get(key);
            if(val == null) continue;
            val = Translator.translate(locale, Locale.getDefault(), val.toString());
            if(val != null) {
                parameters.put(key, val);
            }
        }
        return parameters;
    }
    
    protected Map<String, Object> formatIntegerColumns(
            String tableName, Map<String, Object> parameters) {

        if(context == null) {
            return parameters;
        }
        
        Keywords keyWords = context.getKeywords();
        
        if(keyWords == null) {
            return parameters;
        }
            
        HashMap<String, Object> updated = new HashMap<String, Object>();
        
        for(Entry<String, Object> entry:parameters.entrySet()) {
            
            String column = entry.getKey();
            Object value = entry.getValue();
            
XLogger.getInstance().log(Level.FINER, "#formtIntegerColumns. Column: {0}, Value: {1}", 
    this.getClass(), column, value);        
            
            if(value == null || value instanceof Integer) {
                continue;
            }
        
            Integer intVal = null;
            try{
                intVal = Integer.valueOf(value.toString());
            }catch(NumberFormatException e) {
                //
            }
        
            if(intVal != null) {
                continue;
            }
            
            keyWords.setTableName(tableName);
            
            intVal = keyWords.findMatchingKey(column, value.toString(), true);
            
XLogger.getInstance().log(Level.FINER, "#formatIntegerColumns. Column: {0}, original: {1}, replacement: {2}", 
    this.getClass(), column, value, intVal);        
            
            if(intVal != null) {
                // Prevent concurrent modification
                updated.put(column, intVal);
            }
        }

        if(!updated.isEmpty()) {
            // Replace the previous value
            parameters.putAll(updated);
        }
        
        return parameters;
    }

    /**
     * When extraction, default values take precedence over extracted values
     * When formatting, extracted values take precedence over default values
     * @param parameters
     * @return 
     */
    protected Map<String, Object> addDefaults(Map<String, Object> parameters) {
XLogger.getInstance().log(Level.FINER, "BEFORE adding defaults: {0}", 
        this.getClass(), parameters);
        if(defaultValues == null || defaultValues.isEmpty()) return parameters;
        Map<String, Object> addMe = new HashMap<String, Object>();
        Set<String> keys = defaultValues.keySet();
        for(String key:keys) {
            Object val = parameters.get(key);
            if(val == null) {
                addMe.put(key, defaultValues.get(key));
            }
        }
        parameters.putAll(addMe);
XLogger.getInstance().log(Level.FINER, "AFTER adding defaults: {0}", 
        this.getClass(), parameters);
        return parameters;
    }
    
    protected Map<String, Object> formatCurrencyTypes(Map<String, Object> parameters) {
XLogger.getInstance().log(Level.FINER, "@formatPrice, params: {0}", 
this.getClass(), parameters);        

        Object price = parameters.get("price");

        if(price == null) return parameters; 
        
        Object discount = parameters.get("discount");
        
        if(discount != null) {
            // Replace all none digits
            discount = this.preparePriceString(discount.toString());
            parameters.put("discount", discount);
        }
        
        // Replace all none digits
        price = this.preparePriceString(price.toString());
        parameters.put("price", price);

        Object oval = parameters.get("currency");
        
        if(oval == null) return parameters;
        
        String currCode = oval.toString().trim().toUpperCase();
        
        parameters.put("currency", currCode);

//@todo should be a property        
        final String DEFAULT_CURRENCY = "NGN";
        
        if(currCode.equals(DEFAULT_CURRENCY)) {
            return parameters;
        }
        
XLogger.getInstance().log(Level.FINER, "From: {0}, To: {1}", 
        this.getClass(), currCode, DEFAULT_CURRENCY);        

        CurrencyrateService currRateSvc = new YahooCurrencyrateService();
        
        Currencyrate currRate = currRateSvc.getRate(currCode, DEFAULT_CURRENCY);
        
        final float rate = currRate.getRate();

        if(rate == -1.0) {
            return parameters;
        }
        
        try{
            
            double convertedPrice = Double.parseDouble(price.toString()) * rate;
            parameters.put("price", convertedPrice);

            if(discount != null) {
                double convertedDiscount = Double.parseDouble(discount.toString()) * rate;  
                parameters.put("discount", convertedDiscount);
            }        

            parameters.put("currency", DEFAULT_CURRENCY);
            
XLogger.getInstance().log(Level.FINER, "{0} {1} updated to {2} {3}", 
this.getClass(), currCode, price, DEFAULT_CURRENCY, convertedPrice);        
        }catch(NumberFormatException e) {
            XLogger.getInstance().logSimple(Level.WARNING,  this.getClass(), e);
        }

        return parameters;
    }
    
    protected Map updateHowToApply(Map parameters) {
        Object howToApply = parameters.get("howToApply");
XLogger.getInstance().log(Level.FINER, "Before update. How to apply: {0}", 
        this.getClass(), howToApply);        
        if(howToApply == null) {
            howToApply = this.getMyHowToApply(parameters);
            parameters.put("howToApply", howToApply);
        }
XLogger.getInstance().log(Level.INFO, "How to apply: {0}", this.getClass(), howToApply);        
        return parameters;
    }    
    
    protected Map formatLinks(Map parameters) {
        
        String baseURL = this.context.getConfig().getString(Config.Site.url, "value"); 
        if(baseURL == null) {
            String s = this.context.getConfig().getString(Config.Site.url, Config.Site.start);
            if(s != null) {
                baseURL = Util.getBaseURL(s);
            }else{
                baseURL = s;
            }
        }

        HashMap update = new HashMap();
        
        Set<String> keys = parameters.keySet();
        
        for(String key:keys) {
            
            Object val = parameters.get(key);
            
            if(val == null) continue;
            
            String sval = val.toString();
            
            if(this.isHtmlLink(key, sval)) {
                val = formatHtmlLink(val.toString(), baseURL);
            }else if(this.isLink(key, sval)) {
                val = formatDirectLink(val.toString(), baseURL);
            }

            update.put(key, val);
        }
        parameters.putAll(update);
        return update;
    }
    
    private String replace(String tgt, Map replacements, boolean regex) {
        for(Object key:replacements.keySet()) {
            Object val = replacements.get(key);
XLogger.getInstance().log(Level.FINEST, "Replacing: {0} with: {1}", this.getClass(), key, val);            
            if(regex) {
                tgt = tgt.replaceAll(key.toString(), val.toString());
            }else{
                tgt = tgt.replace(key.toString(), val.toString());
            }
        }
        return tgt;
    }
    
    protected Map resolveExpressions(Map forUpdate) {

        JsonConfig config = this.context.getConfig();
        
        Set<Object[]> paths = config.getFullPaths(Config.Formatter.exression);
        
        if(paths == null || paths.isEmpty()) {
            return forUpdate;
        }
        
        for(Object [] path:paths) {
            
            if(path.length < 2) {
                continue;
            }
            
            String expression = null;
            Map replacements = null;
            if(path[1].toString().equals(Config.Formatter.replace.name()) ||
                    path[1].toString().equals(Config.Formatter.replaceRegex.name())) {
                replacements = config.getMap(path);
            }else{
                expression = config.getString(path);
            }

XLogger.getInstance().log(Level.FINER, "{0}={1}", 
this.getClass(), path==null?null:Arrays.toString(path), replacements==null?expression:replacements);            

            Config.Formatter type = Config.Formatter.valueOf(path[1].toString());
            
            Object col = path[2];
            
            boolean regex = false;
            
            Object result = null;
            switch(type) {
                case set:
                    result = this.resolve(forUpdate, expression, new ArithmeticExpressionResolver());
                    result = this.resolve(forUpdate, expression, new ConditionalExpressionResolver());
                    forUpdate.put(col, result);
XLogger.getInstance().log(Level.FINER, "EXPRESSION: {0}, RESULT: {1} SET TO {2}", 
this.getClass(), expression, col, result);                        
                    break;
                case replaceRegex:
                    regex = true;
                case replace:
                    Object val = forUpdate.get(col);
                    if(val != null) {
                        val = this.replace(val.toString(), replacements, regex);
                        forUpdate.put(col, val);
XLogger.getInstance().log(Level.FINER, "EXPRESSION: {0}, RESULT: {1} UPDATED FROM {2}, TO {3}", 
this.getClass(), expression, col, forUpdate.get(col), replacements.get(col));                        
                    }
                    break;
                case update:
                    this.updateExpression(forUpdate, path);
                    break;
                case accept:
                    this.acceptExpression(forUpdate, path);
                    break;
            }
        }
        
        return forUpdate;
    }
    
    private void updateExpression(Map parameters, Object[] path){
        Object column = path[2];
        String expression = context.getConfig().getString(path);
        if(expression == null) return;
        String result = this.resolve(parameters, expression, new ArithmeticExpressionResolver());
        if(!expression.equals(result)) {
            parameters.put(column, result);
XLogger.getInstance().log(Level.FINER, "EXPRESSION: {0}, RESULT: {1} UPDATED FROM {2} TO {3}", 
this.getClass(), expression, column);                        
        }else{
XLogger.getInstance().log(Level.WARNING, "Failed to resolve:: {0}={1}", 
this.getClass(), Arrays.toString(path), expression);                        
        }        
    }
    
    private void acceptExpression(Map parameters, Object[]path) {
        Object column = path[2];
        String expression = context.getConfig().getString(path);
        if(expression == null) return;
        String result = this.resolve(parameters, expression, new ConditionalExpressionResolver());
        if(!expression.equals(result)) {
            if(!Boolean.parseBoolean(result)) {
                if(column == null) {
                    parameters.clear();
XLogger.getInstance().log(Level.FINER, "Expression: {0}, Result: Rejected All", 
this.getClass(), expression);                        
                }else{
                    parameters.remove(column);
XLogger.getInstance().log(Level.FINER, "Expression: {0}, Result: Rejected Col: {1}", 
this.getClass(), expression, column);                        
                }
            }else{
XLogger.getInstance().log(Level.FINER, "Expression: {0}, Result: Accepted All", 
this.getClass(), expression);                        
            }
        }else{
XLogger.getInstance().log(Level.WARNING, "Failed to resolve:: {0}={1}", 
this.getClass(), Arrays.toString(path), expression);                        
        }        
    }
    
    private String resolve(Map parameters, String expression, ExpressionResolver resolver) {
        for(Object col:parameters.keySet()) {
            Object val = parameters.get(col);
            if(val == null) {
                val = "null";
            }
            expression = expression.replace(col.toString(), val.toString());
        }
        return resolver.resolve(expression);
    }    
    
    protected boolean isHtmlLink(String col, String val) {
        // We are looking for <a href ... etc
        val = val.trim().toLowerCase();
        if(val.startsWith("<a ")) {
            if(val.contains(" href=")) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isLink(String col, String val) {
        col = col.trim();
        return col.startsWith("image") ||
                col.equals("howToApply");
    }

    protected String formatHtmlLink(String input, String url) {
        NodeList list = null;
        try{
            Parser p = new Parser();
            p.setResource(input);
            list = p.parse(null);
            for(int i=0; i<list.size(); i++) {
                Node node = list.elementAt(i);
                if (node instanceof LinkTag) {
                    LinkTag tag = ((LinkTag)node);
                    String link = tag.getLink();
                    
                    if(link.toLowerCase().startsWith("file://")) continue;
                    
                    link = this.formatDirectLink(link, url);
                    
                    tag.setLink(link);
                    
                    tag.setAttribute("target", "_blank");
                }
            }
        }catch(ParserException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
        return (list == null) ? input : list.toHtml(false);
    }    
    
    protected String formatDirectLink(String link, String url) {
        
        if(link.toLowerCase().startsWith("http://")) {
            return link;
        }

        // ...abc.html becomes /abc.html
        // abc.html becomes /abc.html
        return Util.createURL(Util.getBaseURL(url), link);
    }
    
    protected String getMyHowToApply(Map parameters) {
        
        Object val = this.getUrl(parameters);
XLogger.getInstance().log(Level.FINER, "URL: {0}", 
        this.getClass(), val);        
        if(val == null) return getJobRequestHowToApply(parameters);
        
        String validUrl = this.formatUrl(val.toString());
        
        if(validUrl != null) {
            //<a href="url" target="_blank">CLICK HERE TO APPLY</a>
            StringBuilder builder = new StringBuilder("<a href=\"");
            builder.append(validUrl).append("\" target=\"_blank\">");
            builder.append(APPLY_TEXT).append("</a>");
            return builder.toString();
        }else{
            return getJobRequestHowToApply(parameters);
        }
    }
    
    protected String getUrl(Map parameters) {
        Object val = parameters.get("extraDetails");
XLogger.getInstance().log(Level.FINER, "Extra details: {0}", this.getClass(), val);        
        if(val == null) {
            return null;
        }
        Map extraDetails = Util.getParameters(val.toString(), "&");
        val = extraDetails.get("url");
XLogger.getInstance().log(Level.FINER, "URL: {0}", this.getClass(), val);        
        return val == null ? null : val.toString();
    }
    
    /**
     * @return A valid URL or null
     */
    private String formatUrl(final String url) {
        
        if(url.toLowerCase().startsWith("http://") || 
            url.toLowerCase().startsWith("file://")) return url;
        
        // ...abc.html becomes /abc.html
        // abc.html becomes /abc.html
        String fmtUrl = Util.prepareLink(url.toLowerCase());
        
        List<String> urls = Util.getBaseURLs(context.getConfig().getString(Config.Site.url, "value"));
        
        for(String base:urls) {

            String complete = base + fmtUrl;

            try{
        
                boolean isValid = isValidUrl(complete);
                
                if(isValid) return fmtUrl;
                
            }catch(IOException e) {
                XLogger.getInstance().log(Level.WARNING, 
                        "Failed to confirm validity of url: "+fmtUrl, 
                        this.getClass(), e);
            }
        }
        
        return null;
    }
    
    private boolean isValidUrl(String urlString) throws IOException {
        try{
            URL url = new URL(urlString);
            return com.bc.net.ConnectionManager.isValidUrl(url);
        }catch(MalformedURLException e) {
            XLogger.getInstance().logSimple(Level.WARNING,  this.getClass(), e);
            return false;
        }
    }
    
    protected String getJobRequestHowToApply(Map parameters) {
        if(this.jobRequestFields == null) {
            return null;
        }
        StringBuilder url = new StringBuilder("http://www.looseboxes.com/apply.jsp?pt=jobs");
        Iterator iter = parameters.keySet().iterator();
        while(iter.hasNext()) {
            Object key = iter.next();
            if(!this.jobRequestFields.contains(key.toString())) continue;
            try{
                url.append('&').append(key).append('=').append(URLEncoder.encode(parameters.get(key).toString(), "UTF-8"));
            }catch(UnsupportedEncodingException e) {
                XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            }catch(RuntimeException e) {
                XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            }
        }
        url.append("&itemtype=2"); // 2 == REQUEST
        StringBuilder updatedHowToApply = new StringBuilder();
        updatedHowToApply.append("<a href=\"").append(url).append("\">").append(APPLY_TEXT).append("</a>");
        return updatedHowToApply.toString();
    }

    protected Map<String, Object> formatDateTypes(Map<String, Object> parameters) {

        Iterator<String> iter = parameters.keySet().iterator();

        HashMap<String, Object> dateTypes = new HashMap<String, Object>(){
            @Override
            public Object put(String key, Object value) {
                if(key == null || value == null) throw new NullPointerException();
                return super.put(key, value);
            }
        };
        
        HashSet<String> failed = new HashSet<String>();
        
        while(iter.hasNext()) {

            String key = iter.next();

            if(this.isDateType(key, null)) {
                try{
                    
                    String temp = parameters.get(key).toString();
                    String dateStr = this.prepareDateString(temp);

XLogger.getInstance().log(Level.FINER, "Before: {0}, After: {1}",
this.getClass(), temp, dateStr);

                    Date date = inputDateformat.parse(dateStr);
                    
                    String fmtVal = this.outputDateformat.format(date);
                    
                    if(this.isDatetimeType(key)) {
                        if(!fmtVal.contains(":")) {
                            fmtVal = fmtVal + " 00:00:00.0";
                        }
                    }
                    dateTypes.put(key, fmtVal);
                    
XLogger.getInstance().log(Level.FINER, 
"{0}:: input String: {1}, parsed to Date: {2}, formatted to String: {3}", 
        this.getClass(), key, dateStr, date, fmtVal);            

                }catch(ParseException e) {
                    
                    failed.add(key);
                    
                    if(XLogger.getInstance().isLoggable(Level.FINE, this.getClass())) {
                        XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
                    }else{
                        XLogger.getInstance().log(Level.WARNING, e.toString(), this.getClass());
                    }
                }
            }
        }

        parameters.putAll(dateTypes);
        
        parameters.keySet().removeAll(failed);

        return parameters;
    }
    
    public int formatDateinAndExpiryDate(Map parameters) {
        
        Object expiryObj = parameters.get("expiryDate");

        Object dateinObj = parameters.get("datein");

        Date datein = null;

        if(dateinObj == null) {
            datein = new java.sql.Date(System.currentTimeMillis());
            dateinObj = this.outputDateformat.format(datein);
            parameters.put("datein", dateinObj);
        }else{
            try{
                datein = this.outputDateformat.parse(dateinObj.toString());
            }catch(ParseException e) {
                datein = new Date();
                XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            }
        }

        if(expiryObj == null) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(datein);
            cal.add(Calendar.DAY_OF_YEAR, 90);

            expiryObj = this.outputDateformat.format(cal.getTime());

            parameters.put("expiryDate", expiryObj);
        }
XLogger.getInstance().log(Level.FINER, "Datein: {0}, Expiry date: {1}", 
        this.getClass(), dateinObj, expiryObj);

        return this.getStatus(datein);
    }
    
    protected String preparePriceString(String str) {
        return this.removeNonePriceChars(str.trim());
    }

    /**
     * This method returns a string with only one white space between characters
     * e.g 14 10      2009 is changed to 12 10 2009
     */
    protected String prepareDateString(String str) {
        return this.condenseSpaces(this.removeNoneDateChars(str));
    }
    
    /**
     * Retains only digits and the characters ' ', '/', ':', ' ', '-', ',', '.'
     */
    private String removeNoneDateChars(String str) {
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            char ch = str.charAt(i); 
            if(Character.isLetterOrDigit(ch) || ch == ' ' || 
                ch == '-' || ch == ',' || ch == '/' || ch == ':' || ch == '.') {
                builder.append(ch);
            }
        }
        return builder.toString().trim();
    }

    /**
     * Retains only digits and the characters '.', ',', ':' if they are 
     * within digits.
     */
    private String removeNonePriceChars(String str) {
        int digits = 0;
        int afterDigits = 0;
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            char ch = str.charAt(i); 
            if(Character.isDigit(ch)) {
                ++digits;
                afterDigits = 0;
                builder.append(ch);
            }
            if(digits==0) {
                continue;
            }
            if(ch == '.' || ch == ',' || ch == ':') {
                ++afterDigits;
                builder.append(ch);
            }
        }
        
        builder.setLength(builder.length() - afterDigits);
        
        String output = builder.toString().trim();
        
XLogger.getInstance().log(Level.FINEST, "Price: {0}. After Format: {1}",
        this.getClass(), str, output);        

        return output;
    }
    
    /**
     * This method returns a string with only one white space between characters
     * e.g 14 10      2009 is changed to 12 10 2009
     */
    private String condenseSpaces(String str) {
        StringBuilder builder = new StringBuilder();
        boolean addWhiteSpace = true;
        for(int i=0; i<str.length(); i++) {
            char ch = str.charAt(i);
            if(Character.isWhitespace(ch)) {
                if(addWhiteSpace) {
                    builder.append(" ");
                    addWhiteSpace = false;
                }
            }else{
                builder.append(ch);
                addWhiteSpace = true;
            }
        }
        return builder.toString().trim();
    }

    protected boolean isDateType(String key, Object val) {
        return key.toLowerCase().contains("date");
    }

    protected boolean isDatetimeType(String col) {
        return col.equals("datein");
    }

    private int getStatus(Date date) {
        Calendar tgt = Calendar.getInstance();
        tgt.add(Calendar.DAY_OF_YEAR, -360); // becomes 1 year ago
        if(date.before(tgt.getTime())) { // more than 1 year
            return 2; // not available
        }else{
            tgt.add(Calendar.DAY_OF_YEAR, +240); // becomes 4 months ago
            if(date.before(tgt.getTime())) {     // 4 months - 1 year
                return 4;  // needs verification
            }else{                               // less than 4 months
                return 1;  // available
            }
        }
    }

    public DateFormat getInputDateformat() {
        return inputDateformat;
    }

    public void setInputDateformat(DateFormat inputDateformat) {
        this.inputDateformat = inputDateformat;
    }

    public DateFormat getOutputDateformat() {
        return outputDateformat;
    }

    public void setOutputDateformat(DateFormat outputDateformat) {
        this.outputDateformat = outputDateformat;
    }

    public List<String> getJobRequestFields() {
        return jobRequestFields;
    }

    public void setJobRequestFields(ArrayList<String> jobRequestFields) {
        this.jobRequestFields = jobRequestFields;
    }

    public Map getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(Map defaultValues) {
        this.defaultValues = defaultValues;
    }
}//~END
/**
 * 
    protected Map resolveArithmeticExpressions(Map extractedData) {

        if(!this.hasExpression()) {
            return extractedData;
        }
        
        if(extractedData.isEmpty()) return extractedData;
        
        ArrayList<Resolver> list = new ArrayList<Resolver>(3);
        
        // First resolve methods of class java.lang.String
        list.add(new StringMethodResolver());
        
        // then resolve arithmethic expressions
        list.add(new ArithmeticExpressionResolver());
        
        MultiResolver multiResolver = this.resolveExpressions(extractedData, list, site.getProperties(PropertiesType.formatter));
        
        for(String col:multiResolver.getVariableNames()) {
            Object val = multiResolver.getVariable(col);
            extractedData.setObject(col, val);
        }
        
        return extractedData;
    }
    
    protected Map resolveConditionalExpressions(Map extractedData) {
        
        if(!this.hasExpression()) {
            return extractedData;
        }
        
        ArrayList<Resolver> list = new ArrayList<Resolver>(3);
        
        // First resolve methods of class java.lang.String
        list.add(new StringMethodResolver());
        
        // then resolve arithmethic expressions
        list.add(new ConditionalExpressionResolver());
        
        MultiResolver multiResolver = this.resolveExpressions(extractedData, list, site.getProperties(PropertiesType.formatter));
        
        for(String col:multiResolver.getVariableNames()) {
            
            Object val = multiResolver.getVariable(col);
            
            // Reject values if any expression evaluates to false
            //
            if(!Boolean.valueOf(val.toString())) {
                extractedData.remove(col);
            }
        }
        
        return extractedData;
    }

    private MultiResolver resolveExpressions(Map extractedData, List<Resolver> resolvers, Properties cfg) {
        
        MultiResolver multiResolver = new MultiResolver(resolvers, null);
        
        // Multiple columns may be used as variable. E.g
        // description = description + howToApply
        // Therefore we first add all columns as variables
        //
        for(Object col:extractedData.keySet()) {
            
            Object val = extractedData.getObject(col);
            
            if(val == null) continue;
            
            multiResolver.addVariable(col.toString(), val);

        }
        
        int maxExpressionsPerColumn = 10;
        
        for(String col:multiResolver.getVariableNames()) {
            
            Object val = multiResolver.getVariable(col);
            
            if(val == null) continue;
            
            for(int i=0; i<maxExpressionsPerColumn; i++) {
                
                String expression = cfg.getString(col.toString()+".expression"+i);

                if(expression == null) continue;

                Object newVal = multiResolver.resolve(expression);
XLogger.getInstance().log(Level.INFO, "Expression: {0}, Column: {1}, Value: {2}, Result: {3}", 
        this.getClass(), expression, col, val, newVal);
                
                // Update the variable in the expression resolver
                multiResolver.addVariable(col.toString(), newVal);
            }
        }
        
        return multiResolver;
    }
    private boolean hasExpression() {
        
        Properties props = site.getProperties(PropertiesType.formatter);

        Set<String> propNames = props.stringPropertyNames();
        
        boolean hasExpr = false;
        for(String propName:propNames) {
            if(!propName.contains("expression")) {
                continue;
            }
            String [] parts = propName.split("\\.");
            if(parts.length != 2) {
                throw new AssertionError("Expected: name.expressionX, (x is any number incl 0). Found: "+propName);
            }
            String propValue = props.getString(propName);
            if(propValue != null && !propValue.isEmpty()) {
                hasExpr = true;
                break;
            }
        }
        
        return hasExpr;
    }
 * 
 */