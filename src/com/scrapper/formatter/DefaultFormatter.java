package com.scrapper.formatter;

import com.bc.currencyrateservice.Currencyrate;
import com.bc.currencyrateservice.CurrencyrateService;
import com.bc.currencyrateservice.YahooCurrencyrateService;
import com.bc.jpa.fk.Keywords;
import com.bc.json.config.JsonConfig;
import com.bc.net.ConnectionManager;
import com.bc.util.XLogger;
import com.scrapper.Formatter;
import com.scrapper.config.Config;
import static com.scrapper.config.Config.Formatter.replaceRegex;
import static com.scrapper.config.Config.Formatter.set;
import static com.scrapper.config.Config.Formatter.update;
import com.scrapper.context.CapturerContext;
import com.scrapper.expression.ArithmeticExpressionResolver;
import com.scrapper.expression.ConditionalExpressionResolver;
import com.scrapper.expression.ExpressionResolver;
import com.scrapper.util.Translator;
import com.scrapper.util.Util;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class DefaultFormatter
  implements Formatter<Map<String, Object>>, Serializable {
    
  public static final String APPLY_TEXT = "CLICK HERE TO APPLY";
  private CapturerContext context;
  private DateFormat inputDateformat;
  private DateFormat outputDateformat;
  private List jobRequestFields;
  private Map defaultValues;
  
  public DefaultFormatter() {
      
    SimpleDateFormat infmt = new SimpleDateFormat();
    
    infmt.applyPattern("EEE MMM dd HH:mm:ss G yyyy");
    this.inputDateformat = infmt;
    
    SimpleDateFormat outfmt = new SimpleDateFormat();
    
    outfmt.applyPattern("MM dd yyyy HH:mm:ss.S");
    this.outputDateformat = outfmt;
  }
  
  public DefaultFormatter(CapturerContext context) {
      
    this.context = context;
    
    JsonConfig config = context.getConfig();
    
    Object[] arr = config.getArray(new Object[] { Config.Formatter.jobRequestFields });
    if (arr != null) {
      this.jobRequestFields = new ArrayList();
      this.jobRequestFields.addAll(Arrays.asList(arr));
    }
    
    this.defaultValues = config.getMap(new Object[] { Config.Formatter.defaultValues });
    
    Object[] datePatterns = config.getArray(new Object[] { Config.Formatter.datePatterns });
    
    if ((datePatterns == null) || (datePatterns.length == 0)) {
      this.inputDateformat = new SimpleDateFormat();
    } else {
      final String [] datePatternsStrArr = (String[])Arrays.copyOf(datePatterns, datePatterns.length, String[].class); 
      XLogger.getInstance().log(Level.FINER, "Config: {0}, Date patterns: {1}", 
              getClass(), config.getName(), Arrays.toString(datePatternsStrArr));
      
      this.inputDateformat = new MyDateFormat(datePatternsStrArr);
    }
    
    SimpleDateFormat outfmt = new SimpleDateFormat();
    
    outfmt.applyPattern("MM dd yyyy HH:mm:ss.S");
    this.outputDateformat = outfmt;
  }
  
  public String getTableName(Map<String, Object> parameters)
  {
    String defaultTableName = this.context.getConfig().getList(new Object[] { Config.Site.tables }).get(0).toString();
    


    return Util.getTableValue(parameters, defaultTableName);
  }
  
  protected Map<String, Object> createCopy(Map<String, Object> parameters) {
    HashMap update = new HashMap();
    for (Map.Entry<String, Object> en : parameters.entrySet()) {
      Object val = en.getValue();
      if (val != null) {
        val = val.toString().trim();
      }
      update.put(en.getKey(), val);
    }
    return update;
  }
  

  public Map<String, Object> format(Map<String, Object> parameters)
  {
    XLogger.getInstance().log(Level.FINER, "BEFORE Params: {0}", getClass(), parameters);
    

    String tableName = getTableName(parameters);
    


    Map<String, Object> copy = createCopy(parameters);
    
    XLogger.getInstance().log(Level.FINER, "BEFORE: {0}", getClass(), copy);
    
    copy = resolveExpressions(copy);
    
    XLogger.getInstance().log(Level.FINER, "AFTER: {0}", getClass(), copy);
    
    copy = translate(copy);
    


    copy = formatCurrencyTypes(copy);
    



    copy = formatIntegerColumns(tableName, copy);
    
    copy = formatDateTypes(copy);
    try
    {
      int status = formatDateinAndExpiryDate(copy);
      copy.put("status", Integer.valueOf(status));
    } catch (RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    
    copy = formatLinks(copy);
    
    copy = addDefaults(copy);
    
    if ("jobs".equals(tableName)) {
      copy = updateHowToApply(copy);
    }
    XLogger.getInstance().log(Level.FINER, "{0}. AFTER Params: {1}", getClass(), copy);
    
    return copy;
  }
  
  protected Map<String, Object> translate(Map<String, Object> parameters) {
    Locale locale = null;
    String language = this.context.getConfig().getString(new Object[] { Config.Formatter.language });
    
    if (language != null) {
      Locale[] arr = Locale.getAvailableLocales();
      for (Locale e : arr) {
        if ((language.equals(e.getISO3Language())) || (language.equals(e.getLanguage())))
        {
          if ((e.getCountry() == null) || (e.getCountry().isEmpty())) {
            locale = e;
            break;
          }
        }
      }
    }
    XLogger.getInstance().log(Level.FINER, "Locale: {0}", getClass(), locale);
    
    if ((locale == null) || (locale.equals(Locale.getDefault()))) {
      return parameters;
    }
    
    if (!Translator.isSupported(locale)) {
      throw new UnsupportedOperationException("Translation not supported for locale: " + locale);
    }
    
    Set<String> keys = parameters.keySet();
    for (String key : keys) {
      Object val = parameters.get(key);
      if (val != null) {
        val = Translator.translate(locale, Locale.getDefault(), val.toString());
        if (val != null)
          parameters.put(key, val);
      }
    }
    return parameters;
  }
  

  protected Map<String, Object> formatIntegerColumns(String tableName, Map<String, Object> parameters)
  {
    if (this.context == null) {
      return parameters;
    }
    
    Keywords keyWords = this.context.getKeywords();
    
    if (keyWords == null) {
      return parameters;
    }
    
    HashMap<String, Object> updated = new HashMap();
    
    for (Map.Entry<String, Object> entry : parameters.entrySet())
    {
      String column = (String)entry.getKey();
      Object value = entry.getValue();
      
      XLogger.getInstance().log(Level.FINER, "#formtIntegerColumns. Column: {0}, Value: {1}", getClass(), column, value);
      

      if ((value != null) && (!(value instanceof Integer)))
      {


        Integer intVal = null;
        try {
          intVal = Integer.valueOf(value.toString());
        }
        catch (NumberFormatException e) {}
        

        if (intVal == null)
        {


          keyWords.setTableName(tableName);
          
          intVal = keyWords.findMatchingKey(column, value.toString(), true);
          
          XLogger.getInstance().log(Level.FINER, "#formatIntegerColumns. Column: {0}, original: {1}, replacement: {2}", getClass(), column, value, intVal);
          

          if (intVal != null)
          {
            updated.put(column, intVal); }
        }
      }
    }
    if (!updated.isEmpty())
    {
      parameters.putAll(updated);
    }
    
    return parameters;
  }
  





  protected Map<String, Object> addDefaults(Map<String, Object> parameters)
  {
    XLogger.getInstance().log(Level.FINER, "BEFORE adding defaults: {0}", getClass(), parameters);
    
    if ((this.defaultValues == null) || (this.defaultValues.isEmpty())) return parameters;
    Map<String, Object> addMe = new HashMap();
    Set<String> keys = this.defaultValues.keySet();
    for (String key : keys) {
      Object val = parameters.get(key);
      if (val == null) {
        addMe.put(key, this.defaultValues.get(key));
      }
    }
    parameters.putAll(addMe);
    XLogger.getInstance().log(Level.FINER, "AFTER adding defaults: {0}", getClass(), parameters);
    
    return parameters;
  }
  
  protected Map<String, Object> formatCurrencyTypes(Map<String, Object> parameters) {
    XLogger.getInstance().log(Level.FINER, "@formatPrice, params: {0}", getClass(), parameters);
    

    Object price = parameters.get("price");
    
    if (price == null) { return parameters;
    }
    Object discount = parameters.get("discount");
    
    if (discount != null)
    {
      discount = preparePriceString(discount.toString());
      parameters.put("discount", discount);
    }
    

    price = preparePriceString(price.toString());
    parameters.put("price", price);
    
    Object oval = parameters.get("currency");
    
    if (oval == null) { return parameters;
    }
    String currCode = oval.toString().trim().toUpperCase();
    
    parameters.put("currency", currCode);
    

    String DEFAULT_CURRENCY = "NGN";
    
    if (currCode.equals("NGN")) {
      return parameters;
    }
    
    XLogger.getInstance().log(Level.FINER, "From: {0}, To: {1}", getClass(), currCode, "NGN");
    

    CurrencyrateService currRateSvc = new YahooCurrencyrateService();
    
    Currencyrate currRate = currRateSvc.getRate(currCode, "NGN");
    
    float rate = currRate.getRate();
    
    if (rate == -1.0D) {
      return parameters;
    }
    
    try
    {
      double convertedPrice = Double.parseDouble(price.toString()) * rate;
      parameters.put("price", Double.valueOf(convertedPrice));
      
      if (discount != null) {
        double convertedDiscount = Double.parseDouble(discount.toString()) * rate;
        parameters.put("discount", Double.valueOf(convertedDiscount));
      }
      
      parameters.put("currency", "NGN");
      
      XLogger.getInstance().log(Level.FINER, "{0} {1} updated to {2} {3}", getClass(), currCode, price, "NGN", Double.valueOf(convertedPrice));
    }
    catch (NumberFormatException e) {
      XLogger.getInstance().logSimple(Level.WARNING, getClass(), e);
    }
    
    return parameters;
  }
  
  protected Map updateHowToApply(Map parameters) {
    Object howToApply = parameters.get("howToApply");
    XLogger.getInstance().log(Level.FINER, "Before update. How to apply: {0}", getClass(), howToApply);
    
    if (howToApply == null) {
      howToApply = getMyHowToApply(parameters);
      parameters.put("howToApply", howToApply);
    }
    XLogger.getInstance().log(Level.INFO, "How to apply: {0}", getClass(), howToApply);
    return parameters;
  }
  
  protected Map formatLinks(Map parameters)
  {
    String baseURL = this.context.getConfig().getString(new Object[] { Config.Site.url, "value" });
    if (baseURL == null) {
      String s = this.context.getConfig().getString(new Object[] { Config.Site.url, Config.Site.start });
      if (s != null) {
        baseURL = com.bc.util.Util.getBaseURL(s);
      } else {
        baseURL = s;
      }
    }
    
    HashMap update = new HashMap();
    
    Set<String> keys = parameters.keySet();
    
    for (String key : keys)
    {
      Object val = parameters.get(key);
      
      if (val != null)
      {
        String sval = val.toString();
        
        if (isHtmlLink(key, sval)) {
          val = formatHtmlLink(val.toString(), baseURL);
        } else if (isLink(key, sval)) {
          val = formatDirectLink(val.toString(), baseURL);
        }
        
        update.put(key, val);
      } }
    parameters.putAll(update);
    return update;
  }
  
  private String replace(String tgt, Map replacements, boolean regex) {
    for (Object key : replacements.keySet()) {
      Object val = replacements.get(key);
      XLogger.getInstance().log(Level.FINEST, "Replacing: {0} with: {1}", getClass(), key, val);
      if (regex) {
        tgt = tgt.replaceAll(key.toString(), val.toString());
      } else {
        tgt = tgt.replace(key.toString(), val.toString());
      }
    }
    return tgt;
  }
  
  protected Map resolveExpressions(Map forUpdate)
  {
    JsonConfig config = this.context.getConfig();
    
    Set<Object[]> paths = config.getFullPaths(new Object[] { Config.Formatter.exression });
    
    if ((paths == null) || (paths.isEmpty())) {
      return forUpdate;
    }
    
    for (Object[] path : paths)
    {
      if (path.length >= 2)
      {


        String expression = null;
        Map replacements = null;
        if ((path[1].toString().equals(Config.Formatter.replace.name())) || (path[1].toString().equals(Config.Formatter.replaceRegex.name())))
        {
          replacements = config.getMap(path);
        } else {
          expression = config.getString(path);
        }
        
        XLogger.getInstance().log(Level.FINER, "{0}={1}", getClass(), path == null ? null : Arrays.toString(path), replacements == null ? expression : replacements);
        

        Config.Formatter type = Config.Formatter.valueOf(path[1].toString());
        
        Object col = path[2];
        
        boolean regex = false;
        
        Object result = null;
        switch (type) {
        case set: 
          result = resolve(forUpdate, expression, new ArithmeticExpressionResolver());
          result = resolve(forUpdate, expression, new ConditionalExpressionResolver());
          forUpdate.put(col, result);
          XLogger.getInstance().log(Level.FINER, "EXPRESSION: {0}, RESULT: {1} SET TO {2}", getClass(), expression, col, result);
          
          break;
        case replaceRegex: 
          regex = true;
        case replace: 
          Object val = forUpdate.get(col);
          if (val != null) {
            val = replace(val.toString(), replacements, regex);
            forUpdate.put(col, val);
            XLogger.getInstance().log(Level.FINER, "EXPRESSION: {0}, RESULT: {1} UPDATED FROM {2}, TO {3}", getClass(), expression, col, forUpdate.get(col), replacements.get(col));
          }
          
          break;
        case update: 
          updateExpression(forUpdate, path);
          break;
        case accept: 
          acceptExpression(forUpdate, path);
        }
        
      }
    }
    return forUpdate;
  }
  
  private void updateExpression(Map parameters, Object[] path) {
    Object column = path[2];
    String expression = this.context.getConfig().getString(path);
    if (expression == null) return;
    String result = resolve(parameters, expression, new ArithmeticExpressionResolver());
    if (!expression.equals(result)) {
      parameters.put(column, result);
      XLogger.getInstance().log(Level.FINER, "EXPRESSION: {0}, RESULT: {1} UPDATED FROM {2} TO {3}", getClass(), expression, column);
    }
    else {
      XLogger.getInstance().log(Level.WARNING, "Failed to resolve:: {0}={1}", getClass(), Arrays.toString(path), expression);
    }
  }
  
  private void acceptExpression(Map parameters, Object[] path)
  {
    Object column = path[2];
    String expression = this.context.getConfig().getString(path);
    if (expression == null) return;
    String result = resolve(parameters, expression, new ConditionalExpressionResolver());
    if (!expression.equals(result)) {
      if (!Boolean.parseBoolean(result)) {
        if (column == null) {
          parameters.clear();
          XLogger.getInstance().log(Level.FINER, "Expression: {0}, Result: Rejected All", getClass(), expression);
        }
        else {
          parameters.remove(column);
          XLogger.getInstance().log(Level.FINER, "Expression: {0}, Result: Rejected Col: {1}", getClass(), expression, column);
        }
      }
      else {
        XLogger.getInstance().log(Level.FINER, "Expression: {0}, Result: Accepted All", getClass(), expression);
      }
    }
    else {
      XLogger.getInstance().log(Level.WARNING, "Failed to resolve:: {0}={1}", getClass(), Arrays.toString(path), expression);
    }
  }
  
  private String resolve(Map parameters, String expression, ExpressionResolver resolver)
  {
    for (Object col : parameters.keySet()) {
      Object val = parameters.get(col);
      if (val == null) {
        val = "null";
      }
      expression = expression.replace(col.toString(), val.toString());
    }
    return resolver.resolve(expression);
  }
  
  protected boolean isHtmlLink(String col, String val)
  {
    val = val.trim().toLowerCase();
    if ((val.startsWith("<a ")) && 
      (val.contains(" href="))) {
      return true;
    }
    
    return false;
  }
  
  protected boolean isLink(String col, String val) {
    col = col.trim();
    return (col.startsWith("image")) || (col.equals("howToApply"));
  }
  
  protected String formatHtmlLink(String input, String baseUrl)
  {
    NodeList list = null;
    try {
      Parser p = new Parser();
      p.setResource(input);
      list = p.parse(null);
      for (int i = 0; i < list.size(); i++) {
        Node node = list.elementAt(i);
        if ((node instanceof LinkTag)) {
          LinkTag tag = (LinkTag)node;
          String link = tag.getLink();
          
          if (!link.toLowerCase().startsWith("file://"))
          {
            link = formatDirectLink(link, baseUrl);
            
            tag.setLink(link);
            
            tag.setAttribute("target", "_blank");
          }
        }
      }
    } catch (ParserException e) { XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    return list == null ? input : list.toHtml(false);
  }
  
  protected String formatDirectLink(String link, String baseUrl)
  {
    if (link.toLowerCase().startsWith("http://")) {
      return link;
    }
    
    return Util.createURL(baseUrl, link);
  }
  
  protected String getMyHowToApply(Map parameters)
  {
    Object val = getUrl(parameters);
    XLogger.getInstance().log(Level.FINER, "URL: {0}", getClass(), val);
    
    if (val == null) { return getJobRequestHowToApply(parameters);
    }
    String validUrl = formatUrl(val.toString());
    
    if (validUrl != null)
    {
      StringBuilder builder = new StringBuilder("<a href=\"");
      builder.append(validUrl).append("\" target=\"_blank\">");
      builder.append("CLICK HERE TO APPLY").append("</a>");
      return builder.toString();
    }
    return getJobRequestHowToApply(parameters);
  }
  
  protected String getUrl(Map parameters)
  {
    Object val = parameters.get("extraDetails");
    XLogger.getInstance().log(Level.FINER, "Extra details: {0}", getClass(), val);
    if (val == null) {
      return null;
    }
    Map extraDetails = Util.getParameters(val.toString(), "&");
    val = extraDetails.get("url");
    XLogger.getInstance().log(Level.FINER, "URL: {0}", getClass(), val);
    return val == null ? null : val.toString();
  }
  



  private String formatUrl(String url)
  {
    if ((url.toLowerCase().startsWith("http://")) || (url.toLowerCase().startsWith("file://"))) {
      return url;
    }
    

    String fmtUrl = Util.prepareLink(url.toLowerCase());
    
    List<String> urls = Util.getBaseURLs(this.context.getConfig().getString(new Object[] { Config.Site.url, "value" }));
    
    for (String base : urls)
    {
      String complete = base + fmtUrl;
      
      try
      {
        boolean isValid = isValidUrl(complete);
        
        if (isValid) return fmtUrl;
      }
      catch (IOException e) {
        XLogger.getInstance().log(Level.WARNING, "Failed to confirm validity of url: " + fmtUrl, getClass(), e);
      }
    }
    


    return null;
  }
  
  private boolean isValidUrl(String urlString) throws IOException {
    try {
      URL url = new URL(urlString);
      return ConnectionManager.isValidUrl(url);
    } catch (MalformedURLException e) {
      XLogger.getInstance().logSimple(Level.WARNING, getClass(), e); }
    return false;
  }
  
  protected String getJobRequestHowToApply(Map parameters)
  {
    if (this.jobRequestFields == null) {
      return null;
    }
    StringBuilder url = new StringBuilder("http://www.looseboxes.com/apply.jsp?pt=jobs");
    Iterator iter = parameters.keySet().iterator();
    while (iter.hasNext()) {
      Object key = iter.next();
      if (this.jobRequestFields.contains(key.toString()))
        try {
          url.append('&').append(key).append('=').append(URLEncoder.encode(parameters.get(key).toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
        } catch (RuntimeException e) {
          XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
        }
    }
    url.append("&itemtype=2");
    StringBuilder updatedHowToApply = new StringBuilder();
    updatedHowToApply.append("<a href=\"").append(url).append("\">").append("CLICK HERE TO APPLY").append("</a>");
    return updatedHowToApply.toString();
  }
  
  protected Map<String, Object> formatDateTypes(Map<String, Object> parameters)
  {
    Iterator<String> iter = parameters.keySet().iterator();
    
    HashMap<String, Object> dateTypes = new HashMap()
    {
      public Object put(String key, Object value) {
        if ((key == null) || (value == null)) throw new NullPointerException();
        return super.put(key, value);
      }
      
    };
    HashSet<String> failed = new HashSet();
    
    while (iter.hasNext())
    {
      String key = (String)iter.next();
      
      if (isDateType(key, null)) {
        try
        {
          String temp = parameters.get(key).toString();
          String dateStr = prepareDateString(temp);
          
          XLogger.getInstance().log(Level.FINER, "Before: {0}, After: {1}", getClass(), temp, dateStr);
System.out.println(this.getClass().getName()+" = o = o = o = o = o = o = o = o = o: "+key+", before: "+temp+", after: "+dateStr);          

          java.util.Date date = this.inputDateformat.parse(dateStr);
          
          String fmtVal = this.outputDateformat.format(date);
          
          if ((isDatetimeType(key)) && 
            (!fmtVal.contains(":"))) {
            fmtVal = fmtVal + " 00:00:00.0";
          }
          
          dateTypes.put(key, fmtVal);
          
          XLogger.getInstance().log(Level.FINER, "{0}:: input String: {1}, parsed to Date: {2}, formatted to String: {3}", getClass(), key, dateStr, date, fmtVal);

        }
        catch (ParseException e)
        {

          failed.add(key);
          
          if (XLogger.getInstance().isLoggable(Level.FINE, getClass())) {
            XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
          } else {
            XLogger.getInstance().log(Level.WARNING, e.toString(), getClass());
          }
        }
      }
    }
    
    parameters.putAll(dateTypes);
    
    parameters.keySet().removeAll(failed);
    
    return parameters;
  }
  
  public int formatDateinAndExpiryDate(Map parameters)
  {
    Object expiryObj = parameters.get("expiryDate");
    
    Object dateinObj = parameters.get("datein");
    
    java.util.Date datein = null;
    
    if (dateinObj == null) {
      datein = new java.sql.Date(System.currentTimeMillis());
      dateinObj = this.outputDateformat.format(datein);
      parameters.put("datein", dateinObj);
    } else {
      try {
        datein = this.outputDateformat.parse(dateinObj.toString());
      } catch (ParseException e) {
        datein = new java.util.Date();
        XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
      }
    }
    
    if (expiryObj == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(datein);
      cal.add(6, 90);
      
      expiryObj = this.outputDateformat.format(cal.getTime());
      
      parameters.put("expiryDate", expiryObj);
    }
    XLogger.getInstance().log(Level.FINER, "Datein: {0}, Expiry date: {1}", getClass(), dateinObj, expiryObj);
    

    return getStatus(datein);
  }
  
  protected String preparePriceString(String str) {
    return removeNonePriceChars(str.trim());
  }
  



  protected String prepareDateString(String str)
  {
    return condenseSpaces(removeNoneDateChars(str));
  }
  


  private String removeNoneDateChars(String str)
  {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if ((Character.isLetterOrDigit(ch)) || (ch == ' ') || (ch == '-') || (ch == ',') || (ch == '/') || (ch == ':') || (ch == '.'))
      {
        builder.append(ch);
      }
    }
    return builder.toString().trim();
  }
  



  private String removeNonePriceChars(String str)
  {
    int digits = 0;
    int afterDigits = 0;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (Character.isDigit(ch)) {
        digits++;
        afterDigits = 0;
        builder.append(ch);
      }
      if (digits != 0)
      {

        if ((ch == '.') || (ch == ',') || (ch == ':')) {
          afterDigits++;
          builder.append(ch);
        }
      }
    }
    builder.setLength(builder.length() - afterDigits);
    
    String output = builder.toString().trim();
    
    XLogger.getInstance().log(Level.FINEST, "Price: {0}. After Format: {1}", getClass(), str, output);
    

    return output;
  }
  



  private String condenseSpaces(String str)
  {
    StringBuilder builder = new StringBuilder();
    boolean addWhiteSpace = true;
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (Character.isWhitespace(ch)) {
        if (addWhiteSpace) {
          builder.append(" ");
          addWhiteSpace = false;
        }
      } else {
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
  
  private int getStatus(java.util.Date date) {
    Calendar tgt = Calendar.getInstance();
    tgt.add(6, 65176);
    if (date.before(tgt.getTime())) {
      return 2;
    }
    tgt.add(6, 240);
    if (date.before(tgt.getTime())) {
      return 4;
    }
    return 1;
  }
  

  public DateFormat getInputDateformat()
  {
    return this.inputDateformat;
  }
  
  public void setInputDateformat(DateFormat inputDateformat) {
    this.inputDateformat = inputDateformat;
  }
  
  public DateFormat getOutputDateformat() {
    return this.outputDateformat;
  }
  
  public void setOutputDateformat(DateFormat outputDateformat) {
    this.outputDateformat = outputDateformat;
  }
  
  public List<String> getJobRequestFields() {
    return this.jobRequestFields;
  }
  
  public void setJobRequestFields(ArrayList<String> jobRequestFields) {
    this.jobRequestFields = jobRequestFields;
  }
  
  public Map getDefaultValues() {
    return this.defaultValues;
  }
  
  public void setDefaultValues(Map defaultValues) {
    this.defaultValues = defaultValues;
  }
}
