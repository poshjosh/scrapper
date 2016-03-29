package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.logging.Level;












public class ConditionalExpressionResolver
  extends ArithmeticExpressionResolver
{
  private String[] operators = { ">", ">=", "<", "<=", "==", "!=" };
  



  public Object resolve(Double a, String operator, Double b)
  {
    boolean output = false;
    


    if (operator.equals(">")) {
      output = a.doubleValue() > b.doubleValue();
    } else if (operator.equals(">=")) {
      output = a.doubleValue() >= b.doubleValue();
    } else if (operator.equals("<")) {
      output = a.doubleValue() < b.doubleValue();
    } else if (operator.equals("<=")) {
      output = a.doubleValue() <= b.doubleValue();
    } else if (operator.equals("=="))
    {
      output = a.doubleValue() == b.doubleValue();
    } else if (operator.equals("!="))
    {
      output = a.doubleValue() != b.doubleValue();
    } else {
      throw new IllegalArgumentException("Conditional operator: " + operator);
    }
    
    XLogger.getInstance().log(Level.FINE, "@resolve {0} {1} {2} = {3}", getClass(), a, operator, b, Boolean.valueOf(output));
    
    return Boolean.valueOf(output);
  }
  
  public String[] getOperators()
  {
    return this.operators;
  }
}
