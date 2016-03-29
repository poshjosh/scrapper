package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.logging.Level;

/**
 * @(#)LogicExpressionResolver.java   08-Oct-2013 20:35:41
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
public class LogicExpressionResolver extends AbstractExpressionResolver<Boolean> {
    
    private String [] operators = {"||", "|", "&&", "&", "==", "!="};

    public LogicExpressionResolver() { }
    
    @Override
    protected String getLeftOperandRegex() {
        return "(true|false)";
    }

    @Override
    protected String getRightOperandRegex() {
        return "(true|false)";
    }
    
    @Override
    public String resolve(String a, String operand, String b) {

        Boolean d1 = Boolean.valueOf(a);
        
        Boolean d2 = Boolean.valueOf(b);
        
        return "" + resolve(d1, operand, d2);
    }

    @Override
    public Object resolve(Boolean a, String operator, Boolean b) {
        
        boolean output = false;
        
        // "||", "|", "&&", "&", "==", "!="

        if(operator.equals("&&")) {
            output = a && b;
        }else if(operator.equals("&")) {
            output = a & b;
        }else if(operator.equals("||")) {
            output = a || b;
        }else if(operator.equals("|")) {
            output = a | b;
        }else if(operator.equals("==")) {
            output = a.booleanValue() == b.booleanValue();
        }else if(operator.equals("!=")) {
            output = a.booleanValue() != b.booleanValue();
        }else{
            throw new IllegalArgumentException("Logic operator: "+operator);
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
