package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;













public abstract class AbstractExpressionResolver<E>
  implements ExpressionResolver<E>
{
  protected abstract String getLeftOperandRegex();
  
  protected abstract String getRightOperandRegex();
  
  public Pattern getBasicPattern(String operator)
  {
    return Pattern.compile(getBasicRegex(operator).toString(), 2);
  }
  






  public boolean isResolvable(String input)
  {
    for (String operator : getOperators()) {
      if (containsPattern(input, operator)) {
        return true;
      }
    }
    return false;
  }
  
  public String resolve(String input)
    throws ResolverException
  {
    input = resolveBrackets(input);
    
    input = resolveNonBrackets(input);
    
    return input;
  }
  
  protected String resolveBrackets(String input) throws ResolverException
  {
    XLogger.getInstance().log(Level.FINER, "@resolveBrackets Input: {0}", getClass(), input);
    
    if (input.indexOf("(") == -1) { return input;
    }
    Matcher matcher = Pattern.compile("\\(.+\\)").matcher(input);
    
    StringBuffer sb = new StringBuffer();
    
    while (matcher.find())
    {
      String gp = matcher.group();
      
      String expr = gp.trim().substring(1, gp.length() - 1);
      

      while (expr.indexOf("(") != -1) {
        expr = resolveBrackets(expr);
      }
      

      String replacement = resolveNonBrackets(expr);
      
      matcher.appendReplacement(sb, replacement);
    }
    
    matcher.appendTail(sb);
    
    XLogger.getInstance().log(Level.FINER, "@resolveBrackets Output: {0}", getClass(), sb);
    
    return sb.toString();
  }
  
  protected String resolveNonBrackets(String input)
  {
    boolean resolved = false;
    
    for (int i = 0; i < 99; i++)
    {
      input = doNonBrackets(input);
      
      if (!isResolvable(input)) {
        resolved = true;
        break;
      }
    }
    
    if (!resolved) {
      throw new ResolverException("After 99 cycles, unable to resolve: " + input);
    }
    
    return input;
  }
  
  private String doNonBrackets(String input)
  {
    XLogger.getInstance().log(Level.FINER, "@resolveNonBrackets Input: {0}", getClass(), input);
    
    for (String operator : getOperators())
    {
      boolean resolved = false;
      int i = 0;
      do {
        input = resolve(input, operator).trim();
        
        resolved = !containsPattern(input, operator);


      }
      while ((!resolved) && 
      

        (i < 99));
      
      if (!resolved) {
        throw new ResolverException(operator + "  After 99 cycles, unable to resolve: " + input);
      }
    }
    
    XLogger.getInstance().log(Level.FINER, "@resolveNonBrackets Output: {0}", getClass(), input);
    return input;
  }
  

  public String resolve(String input, String operator)
  {
    if (!input.contains(operator)) {
      XLogger.getInstance().log(Level.FINEST, "@resolve, Operator: {0} NOT FOUND in Input: {1}", getClass(), operator, input);
      
      return input;
    }
    
    XLogger.getInstance().log(Level.FINE, "@resolve, Operator: {0}, Input: {1}", getClass(), operator, input);
    

    Pattern pattern = getBasicPattern(operator);
    
    Matcher matcher = pattern.matcher(input);
    
    StringBuffer update = new StringBuffer();
    
    if (matcher.find())
    {
      String gp = matcher.group();
      
      String[] parts = getParts(gp, operator);
      
      String result = resolve(parts[0], operator, parts[1]);
      
      matcher.appendReplacement(update, result);
    }
    
    matcher.appendTail(update);
    


    return update.toString();
  }
  
  protected String[] getParts(String input, String operator)
  {
    XLogger.getInstance().log(Level.FINE, "@resolvePair, Operator: {0}, Input: {1}", getClass(), operator, input);
    

    String[] parts = input.split("\\Q" + operator + "\\E");
    
    XLogger.getInstance().log(Level.FINER, "@resolvePair, Operator: {0}, Output: {1}", getClass(), operator, Arrays.toString(parts));
    

    if (parts.length != 2) {
      throw new IllegalArgumentException();
    }
    
    return parts;
  }
  
  private boolean containsPattern(String input, String operator) {
    return getBasicPattern(operator).matcher(input).find();
  }
  
  protected StringBuilder getBasicRegex(String operator) {
    StringBuilder regex = new StringBuilder();
    regex.append('(');
    regex.append(getLeftOperandRegex());
    regex.append('(');
    regex.append("\\Q").append(operator).append("\\E");
    regex.append(')');
    regex.append(getRightOperandRegex());
    regex.append(')');
    return regex;
  }
}
