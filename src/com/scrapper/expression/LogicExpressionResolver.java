package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.logging.Level;












public class LogicExpressionResolver
  extends AbstractExpressionResolver<Boolean>
{
  private String[] operators = { "||", "|", "&&", "&", "==", "!=" };
  


  protected String getLeftOperandRegex()
  {
    return "(true|false)";
  }
  
  protected String getRightOperandRegex()
  {
    return "(true|false)";
  }
  

  public String resolve(String a, String operand, String b)
  {
    Boolean d1 = Boolean.valueOf(a);
    
    Boolean d2 = Boolean.valueOf(b);
    
    return "" + resolve(d1, operand, d2);
  }
  

  public Object resolve(Boolean a, String operator, Boolean b)
  {
    boolean output = false;
    


    if (operator.equals("&&")) {
      output = (a.booleanValue()) && (b.booleanValue());
    } else if (operator.equals("&")) {
      output = a.booleanValue() & b.booleanValue();
    } else if (operator.equals("||")) {
      output = (a.booleanValue()) || (b.booleanValue());
    } else if (operator.equals("|")) {
      output = a.booleanValue() | b.booleanValue();
    } else if (operator.equals("==")) {
      output = a.booleanValue() == b.booleanValue();
    } else if (operator.equals("!=")) {
      output = a.booleanValue() != b.booleanValue();
    } else {
      throw new IllegalArgumentException("Logic operator: " + operator);
    }
    
    XLogger.getInstance().log(Level.FINE, "@resolve {0} {1} {2} = {3}", getClass(), a, operator, b, Boolean.valueOf(output));
    
    return Boolean.valueOf(output);
  }
  
  public String[] getOperators()
  {
    return this.operators;
  }
}
