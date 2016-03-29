package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * @(#)ArithmeticExpressionResolver.java   08-Oct-2013 22:35:54
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
public class ArithmeticExpressionResolver extends AbstractExpressionResolver<Double> {
    
    private String [] operators = {"*", "/", "%", "+", "-"};
    
    public ArithmeticExpressionResolver() { }
    
    @Override
    protected String getLeftOperandRegex() {
        // 3 or 2345.6789 etc or starting with +/- at the beginning of a line
        return "(^[+-]{0,1}[0-9.]+|[0-9.]+)";
    }

    @Override
    protected String getRightOperandRegex() {
        // 3 or 2345.6789 or -3 or -2345.6789
        return "([+-]{0,1}[0-9.]+)";
    }
    
    @Override
    protected String [] getParts(String input, String operator) {
XLogger.getInstance().log(Level.FINE, "@resolvePair, Operator: {0}, Input: {1}", 
        this.getClass(), operator, input);

        // Format '-4-3' to '4-3' so that split works well 
        //    
        boolean appendOperatorToFirstPart = false;
        if(input.startsWith(operator)) {
            input = input.substring(operator.length());
            appendOperatorToFirstPart = true;
        }

//        // Format '4--3' to '4-3' so that split works well
        boolean appendOperatorToSecondPart = false;
        String doub = operator + operator;
        if(input.contains(doub)) {
            input = input.replace(doub, operator);
            appendOperatorToSecondPart = true;
        }
        
        String [] parts = super.getParts(input, operator);
        
        if(appendOperatorToFirstPart) {
            parts[0] = operator + parts[0];
        }
        
        if(appendOperatorToSecondPart) {
            parts[1] = operator + parts[1];
        }
        
XLogger.getInstance().log(Level.FINER, "@resolvePair, Operator: {0}, Output: {1}", 
        this.getClass(), operator, Arrays.toString(parts));

        return parts;
    }
    
    @Override
    public String resolve(String a, String operator, String b) {

        Double d1 = Double.valueOf(a);
        
        Double d2 = Double.valueOf(b);
        
        return "" + resolve(d1, operator, d2);
    }

    @Override
    public Object resolve(Double a, String operator, Double b) {
        
        Double output = null;
        
        // "*", "/", "%", "+", "-"

        if(operator.equals("*")) {
            output = a * b;
        }else if(operator.equals("/")) {
            output = a / b;
        }else if(operator.equals("%")) {
            output = a % b;
        }else if(operator.equals("+")) {
            output = a + b;
        }else if(operator.equals("-")) {
            output = a - b;
        }else{
            throw new IllegalArgumentException("Arithmetic operator: "+operator);
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
