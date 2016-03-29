package com.scrapper.expression;

import com.bc.util.XLogger;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;











public class StringMethodResolver
  implements Resolver
{
  private Method[] methods;
  
  public StringMethodResolver()
  {
    this.methods = String.class.getMethods();
  }
  
  public boolean isResolvable(String input)
  {
    boolean output = true;
    if (input.indexOf("(") == -1) {
      output = false;
    }
    if (output) {
      Pattern pattern = getMethodPattern();
      Matcher matcher = pattern.matcher(input);
      output = matcher.find();
    }
    return output;
  }
  
  public String resolve(String input)
    throws ResolverException
  {
    input = resolveSimpleConcatenation(input);
    
    String[] parts = input.split("\\+");
    
    StringBuilder output = new StringBuilder();
    
    for (int i = 0; i < parts.length; i++)
    {
      parts[i] = parts[i].trim();
      
      if (isResolvable(parts[i])) {
        parts[i] = resolveInnerMethods(parts[i]);
        parts[i] = resolveDirectMethods(parts[i]);

      }
      else if (((parts[i].startsWith("\"")) && (parts[i].endsWith("\""))) || ((parts[i].startsWith("'")) && (parts[i].endsWith("'"))))
      {
        parts[i] = parts[i].substring(1, parts[i].length() - 1);
      }
      

      output.append(parts[i]);
    }
    
    return output.toString();
  }
  
  public String resolveSimpleConcatenation(String input) throws ResolverException
  {
    if (!input.contains("+")) {
      return input;
    }
    
    XLogger.getInstance().log(Level.FINE, "BEFORE simple concatenation: {0}", getClass(), input);
    


    String concatRegex = "\"(.*?)\"(\\+)[\"']{0,1}([0-9.]*?)[\"']^\\.";
    
    Pattern pattern = Pattern.compile(concatRegex);
    
    Matcher matcher = pattern.matcher(input);
    
    StringBuffer sb = new StringBuffer();
    
    while (matcher.find()) {
      String whole = matcher.group(0);
      String left = matcher.group(1);
      String plus = matcher.group(2);
      String right = matcher.group(3);
      
      XLogger.getInstance().log(Level.FINEST, "Group0: {0}, gp1: {1}, gp2: {2}, gp3: {3}", getClass(), whole, left, plus, right);
      

      matcher.appendReplacement(sb, left + right);
    }
    
    matcher.appendTail(sb);
    
    String output = sb.toString();
    
    XLogger.getInstance().log(Level.FINE, "AFTER simple concatenation: {0}", getClass(), output);
    return output;
  }
  
  public String resolveInnerMethods(String input) throws ResolverException {
    XLogger.getInstance().log(Level.FINER, "BEFORE resolving inner methods input: {0}", getClass(), input);
    if (input.indexOf("(") == -1) { return input;
    }
    Pattern pattern = getMethodPattern();
    

    Matcher matcher = pattern.matcher(input);
    
    StringBuffer sb = new StringBuffer();
    
    while (matcher.find())
    {
      String whole = matcher.group(0);
      String stringObj = matcher.group(1);
      String methodName = matcher.group(2);
      String methodArgs = matcher.group(3);
      
      XLogger.getInstance().log(Level.FINEST, "Group0: {0}, gp1: {1}, gp2: {2}, gp3: {3}", getClass(), whole, stringObj, methodName, methodArgs);
      

      boolean hasInnerMethod = isResolvable(methodArgs);
      
      XLogger.getInstance().log(Level.FINE, "Has inner method: {0}, inner: {1}", getClass(), Boolean.valueOf(hasInnerMethod), methodArgs);
      

      String replacement = null;
      
      if (!hasInnerMethod)
      {
        replacement = resolveDirectMethods(whole);

      }
      else
      {
        methodArgs = resolveInnerMethods(methodArgs);
        
        String quote = getSuitableQuote(methodArgs);
        

        StringBuilder rep = new StringBuilder();
        rep.append('"').append(stringObj).append('"').append('.').append(methodName);
        replacement = '(' + quote + methodArgs + quote + ')';
      }
      
      XLogger.getInstance().log(Level.FINE, "Replacing: {0} with: {1}", getClass(), whole, replacement);
      

      matcher.appendReplacement(sb, replacement);
    }
    

    matcher.appendTail(sb);
    
    String output = sb.toString();
    XLogger.getInstance().log(Level.FINER, "AFTER resolving inner methods output: {0}", getClass(), output);
    

    return output;
  }
  




  protected String resolveDirectMethods(String input)
    throws ResolverException
  {
    XLogger.getInstance().log(Level.FINER, "@resolvePlainMethods, input: {0}", getClass(), input);
    

    for (int i = 0; i < this.methods.length; i++)
    {
      Method method = this.methods[i];
      
      Pattern pattern = getMethodPattern(method.getName());
      
      XLogger.getInstance().log(Level.FINEST, "Method: {0}, regex: {1}", getClass(), method.getName(), pattern.pattern());
      

      Matcher matcher = pattern.matcher(input);
      
      StringBuffer update = new StringBuffer();
      
      while (matcher.find())
      {
        String whole = matcher.group(0);
        String stringObj = matcher.group(1);
        String methodName = matcher.group(2);
        String methodArgs = matcher.group(3);
        
        XLogger.getInstance().log(Level.FINEST, "Group0: {0}, gp1: {1}, gp2: {2}, gp3: {3}", getClass(), whole, stringObj, methodName, methodArgs);
        

        if (!methodName.equals(method.getName()))
        {
          throw new AssertionError("Expected: " + method.getName() + ", Found: " + methodName);
        }
        
        Object[] params = null;
        
        if ((methodArgs != null) && (!methodArgs.isEmpty()))
        {
          String[] parts = methodArgs.split("\\,");
          
          Class<?>[] paramTypes = method.getParameterTypes();
          
          if (paramTypes.length != parts.length)
          {

            if (!isSameNameMethodAvailable(methodName, i))
            {

              throw new ResolverException("Wrong number of method arguments, expected " + paramTypes.length + " arguments found " + parts.length + " arguments for method: " + method);
            }
          }
          else {
            Class<?>[] paramClasses = new Class[parts.length];
            
            for (int j = 0; j < parts.length; j++) {
              Parameter param = getParameter(parts[j]);
              parts[j] = param.value;
              paramClasses[j] = param.valueClass;
            }
            XLogger.getInstance().log(Level.FINE, "Args: {0}, ActualTypes: {1}, MyGuess: {2}", getClass(), Arrays.toString(parts), Arrays.asList(paramTypes), Arrays.asList(paramClasses));
            


            if (suitable(paramTypes, paramClasses))
            {


              params = getParameters(method.getParameterTypes(), parts);
              XLogger.getInstance().log(Level.FINER, "Params: {0}", getClass(), params);
            }
          }
        } else {
          try {
            Object result = method.invoke(stringObj, params);
            
            XLogger.getInstance().log(Level.FINE, "Result: {0}, object: {1}, method: {2}, args: {3}", getClass(), result, stringObj, methodName, Arrays.toString(params));
            

            matcher.appendReplacement(update, result.toString());
          }
          catch (Exception e) {
            ResolverException re = new ResolverException();
            re.initCause(e);
            throw re;
          }
        }
      }
      matcher.appendTail(update);
      
      if ((XLogger.getInstance().isLoggable(Level.FINER, getClass())) && (!input.equals(update.toString())))
      {
        XLogger.getInstance().log(Level.FINER, "After resolving: {0}, updated: {1}, to: {2}", getClass(), method.getName(), input, update);
      }
      

      input = update.toString();
    }
    
    return input;
  }
  


  public String getMethodRegex()
  {
    return "[a-zA-Z]{4,19}";
  }
  
  public String getMethodRegex(String method)
  {
    StringBuilder regex = new StringBuilder();
    regex.append("\\\"(.*?)\\\"\\.(");
    regex.append(method);
    regex.append(")\\((.*)\\)");
    return regex.toString();
  }
  
  private Pattern getMethodPattern() {
    return getMethodPattern(getMethodRegex());
  }
  
  private Pattern getMethodPattern(String method) {
    String regex = getMethodRegex(method);
    return Pattern.compile(regex);
  }
  
  private boolean isSameNameMethodAvailable(String methodName, int offset) {
    for (int i = offset; i < this.methods.length; i++) {
      if (methodName.equals(this.methods[i].getName())) return true;
    }
    return false;
  }
  
  private boolean suitable(Class<?>[] actual, Class<?>[] found) {
    for (int i = 0; i < actual.length; i++) {
      if (!suitable(actual[i], found[i])) {
        return false;
      }
    }
    return true;
  }
  
  private boolean suitable(Class actual, Class found) {
    if (found == String.class) {
      return (actual == String.class) || (actual == CharSequence.class) || (actual == StringBuffer.class) || (actual == Charset.class) || (actual == Object.class);
    }
    


    if (found == Long.TYPE) {
      return (actual == Integer.TYPE) || (actual == Long.TYPE);
    }
    if (found == Character.TYPE) {
      return actual == Character.TYPE;
    }
    return false;
  }
  
  private Parameter getParameter(String input) throws ResolverException
  {
    input = input.trim();
    boolean isChar = input.charAt(0) == '\'';
    boolean isString = input.charAt(0) == '"';
    boolean isNumber = false;
    if ((isChar) || (isString)) {
      input = input.substring(1, input.length() - 1);
    } else {
      try {
        Long.parseLong(input);
        isNumber = true;
      }
      catch (NumberFormatException e) {}
    }
    
    Class aClass = null;
    if (isChar) {
      aClass = Character.TYPE;
    } else if (isString) {
      aClass = String.class;
    } else if (isNumber) {
      aClass = Long.TYPE;
    } else {
      throw new ResolverException("Unable to determine type of method parameter: " + input);
    }
    XLogger.getInstance().log(Level.FINEST, "Input: {0} Suitable class: {1}", getClass(), input, aClass);
    
    return new Parameter(input, aClass);
  }
  
  private static class Parameter {
    private String value;
    private Class valueClass;
    
    Parameter(String val, Class valClass) { if ((val == null) || (valClass == null)) throw new NullPointerException();
      this.value = val;
      this.valueClass = valClass;
    }
  }
  
  private String getSuitableQuote(String input) {
    Class aClass = getSuitableClass(input);
    return aClass == Character.TYPE ? "'" : aClass == Long.TYPE ? "" : "\"";
  }
  
  private Class getSuitableClass(String input) {
    boolean isNumber = false;
    try {
      Long.parseLong(input);
      isNumber = true;
    }
    catch (NumberFormatException e) {}
    
    boolean isChar = input.length() == 1;
    return isChar ? Character.TYPE : isNumber ? Long.TYPE : String.class;
  }
  
  private Object[] getParameters(Class<?>[] paramTypes, String[] parts) throws ResolverException {
    if (parts == null) throw new NullPointerException();
    Object[] params = new Object[parts.length];
    for (int i = 0; i < parts.length; i++) {
      params[i] = convertTo(paramTypes[i], parts[i]);
    }
    return params;
  }
  
  private Object convertTo(Class aClass, String s) throws ResolverException {
    if (aClass == String.class)
      return s;
    if (aClass == CharSequence.class)
      return s;
    if (aClass == Character.class)
      return Character.valueOf(s.charAt(0));
    if (aClass == Integer.TYPE)
      return Integer.valueOf(Integer.parseInt(s));
    if (aClass == Object.class)
      return s;
    if (aClass == StringBuffer.class)
      return new StringBuffer(s);
    if (aClass == Charset.class)
      return Charset.forName(s);
    if (aClass == Long.TYPE)
      return Long.valueOf(Long.parseLong(s));
    if (aClass == Locale.class)
      throw new UnsupportedOperationException();
    if (aClass == byte[].class) {
      throw new UnsupportedOperationException();
    }
    throw new ResolverException("" + aClass + " is not a known method argument for any of the methods of class java.lang.String");
  }
}
