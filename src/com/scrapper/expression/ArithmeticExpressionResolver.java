package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.Arrays;
import java.util.logging.Level;












public class ArithmeticExpressionResolver
  extends AbstractExpressionResolver<Double>
{
  private String[] operators = { "*", "/", "%", "+", "-" };
  



  protected String getLeftOperandRegex()
  {
    return "(^[+-]{0,1}[0-9.]+|[0-9.]+)";
  }
  

  protected String getRightOperandRegex()
  {
    return "([+-]{0,1}[0-9.]+)";
  }
  
  protected String[] getParts(String input, String operator)
  {
    XLogger.getInstance().log(Level.FINE, "@resolvePair, Operator: {0}, Input: {1}", getClass(), operator, input);
    



    boolean appendOperatorToFirstPart = false;
    if (input.startsWith(operator)) {
      input = input.substring(operator.length());
      appendOperatorToFirstPart = true;
    }
    

    boolean appendOperatorToSecondPart = false;
    String doub = operator + operator;
    if (input.contains(doub)) {
      input = input.replace(doub, operator);
      appendOperatorToSecondPart = true;
    }
    
    String[] parts = super.getParts(input, operator);
    
    if (appendOperatorToFirstPart) {
      parts[0] = (operator + parts[0]);
    }
    
    if (appendOperatorToSecondPart) {
      parts[1] = (operator + parts[1]);
    }
    
    XLogger.getInstance().log(Level.FINER, "@resolvePair, Operator: {0}, Output: {1}", getClass(), operator, Arrays.toString(parts));
    

    return parts;
  }
  

  public String resolve(String a, String operator, String b)
  {
    Double d1 = Double.valueOf(a);
    
    Double d2 = Double.valueOf(b);
    
    return "" + resolve(d1, operator, d2);
  }
  

  public Object resolve(Double a, String operator, Double b)
  {
    Double output = null;
    


    if (operator.equals("*")) {
      output = Double.valueOf(a.doubleValue() * b.doubleValue());
    } else if (operator.equals("/")) {
      output = Double.valueOf(a.doubleValue() / b.doubleValue());
    } else if (operator.equals("%")) {
      output = Double.valueOf(a.doubleValue() % b.doubleValue());
    } else if (operator.equals("+")) {
      output = Double.valueOf(a.doubleValue() + b.doubleValue());
    } else if (operator.equals("-")) {
      output = Double.valueOf(a.doubleValue() - b.doubleValue());
    } else {
      throw new IllegalArgumentException("Arithmetic operator: " + operator);
    }
    
    XLogger.getInstance().log(Level.FINE, "@resolve {0} {1} {2} = {3}", getClass(), a, operator, b, output);
    
    return output;
  }
  
  public String[] getOperators()
  {
    return this.operators;
  }
}
