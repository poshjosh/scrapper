package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.logging.Level;

/**
 * @(#)ConditionalExpressionResolver.java   08-Oct-2013 21:29:56
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
public class ConditionalExpressionResolver extends ArithmeticExpressionResolver {
    
    private String [] operators = {">", ">=", "<", "<=", "==", "!="};
    
    public ConditionalExpressionResolver() { }
    
    @Override
    public Object resolve(Double a, String operator, Double b) {
        
        boolean output = false;
        
        // ">", ">=", "<", "<=", "==", "!="

        if(operator.equals(">")) {
            output = a > b;
        }else if(operator.equals(">=")) {
            output = a >= b;
        }else if(operator.equals("<")) {
            output = a < b;
        }else if(operator.equals("<=")) {
            output = a <= b;
        }else if(operator.equals("==")) {
            // For equality we have to use the underlying double value
            output = a.doubleValue() == b.doubleValue();
        }else if(operator.equals("!=")) {
            // For equality we have to use the underlying double value
            output = a.doubleValue() != b.doubleValue();
        }else{
            throw new IllegalArgumentException("Conditional operator: "+operator);
        }
        
XLogger.getInstance().log(Level.FINE, "@resolve {0} {1} {2} = {3}", 
        this.getClass(), a, operator, b, output);
        return output;
    }

    @Override
    public String [] getOperators() {
        return operators;
    }
}
