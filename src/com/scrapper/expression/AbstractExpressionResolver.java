package com.scrapper.expression;

import com.bc.util.XLogger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @(#)AbstractExpressionResolver.java   08-Oct-2013 22:13:35
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
public abstract class AbstractExpressionResolver<E> implements ExpressionResolver<E> {
    
    public AbstractExpressionResolver() { }
    
    protected abstract String getLeftOperandRegex();
    
    protected abstract String getRightOperandRegex();
    
    public Pattern getBasicPattern(String operator) {
        return Pattern.compile(this.getBasicRegex(operator).toString(), Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Resolves the input expression using all the operands returned by
     * {@linkplain #getOperators()}
     * @param input The expression to resolve. E.g <tt>4 - (3 - 6)</tt>
     * @return E.g for <tt>4 - (3 - 6)</tt> returns <tt>7</tt>
     */
    @Override
    public boolean isResolvable(String input) {
        for(String operator:this.getOperators()) {
            if(this.containsPattern(input, operator)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String resolve(String input) throws ResolverException{
        
        input = this.resolveBrackets(input);
        
        input = this.resolveNonBrackets(input);
        
        return input;
    }
    
    protected String resolveBrackets(String input) throws ResolverException{
        
XLogger.getInstance().log(Level.FINER, "@resolveBrackets Input: {0}", this.getClass(), input);

        if(input.indexOf("(") == -1) return input;
        
        Matcher matcher = Pattern.compile("\\(.+\\)").matcher(input);
        
        StringBuffer sb = new StringBuffer();
        
        while(matcher.find()) {
            
            String gp = matcher.group();
            // We remove the opending and closing brackets
            String expr = gp.trim().substring(1, gp.length()-1);

            // Resolve all brackets in the bracket
            while(expr.indexOf("(") != -1) {
                expr = resolveBrackets(expr);
            }
            
            // Resolve all expressions
            String replacement = resolveNonBrackets(expr);

            matcher.appendReplacement(sb, replacement);
        }
        
        matcher.appendTail(sb);
        
XLogger.getInstance().log(Level.FINER, "@resolveBrackets Output: {0}", this.getClass(), sb);
        
        return sb.toString();
    }
    
    protected String resolveNonBrackets(String input) {
        
        boolean resolved = false;
        
        for(int i=0; i<99; i++) {
            
            input = this.doNonBrackets(input);
            
            if(!this.isResolvable(input)) {
                resolved = true;
                break;
            }
        }
        
        if(!resolved) {
            throw new ResolverException("After 99 cycles, unable to resolve: "+input);
        }
        
        return input;
    }
    
    private String doNonBrackets(String input) {
        
XLogger.getInstance().log(Level.FINER, "@resolveNonBrackets Input: {0}", this.getClass(), input);
        
        for(String operator:this.getOperators()) {
            
            boolean resolved = false;
            int i = 0;
            do{
                input = this.resolve(input, operator).trim();

                resolved = !this.containsPattern(input, operator);
//XLogger.getInstance().log(Level.FINEST, "Cycle: {0} Operator: {1}, Output: {2}", 
//        this.getClass(), i, operator, input);

                if(resolved) { // move to the next operator
                    break;
                }
            }while(i < 99);
            
            if(!resolved) {
                throw new  ResolverException(operator+"  After 99 cycles, unable to resolve: "+input);
            }
        }

XLogger.getInstance().log(Level.FINER, "@resolveNonBrackets Output: {0}", this.getClass(), input);
        return input;
    }

    @Override
    public String resolve(String input, String operator) {

        if(!input.contains(operator)) {
XLogger.getInstance().log(Level.FINEST, "@resolve, Operator: {0} NOT FOUND in Input: {1}", 
        this.getClass(), operator, input);
            return input;
        }
        
XLogger.getInstance().log(Level.FINE, "@resolve, Operator: {0}, Input: {1}", 
        this.getClass(), operator, input);

        Pattern pattern = this.getBasicPattern(operator);
        
        Matcher matcher = pattern.matcher(input);
        
        StringBuffer update = new StringBuffer();
        
        if(matcher.find()) {  // Notice we use if
            
            String gp = matcher.group();
            
            String [] parts = this.getParts(gp, operator);
            
            String result = this.resolve(parts[0], operator, parts[1]);
            
            matcher.appendReplacement(update, result);
        }
        
        matcher.appendTail(update);
        
//XLogger.getInstance().log(Level.FINER, "@resolve, Operator: {0}, Output: {1}", 
//        this.getClass(), operator, sb);
        return update.toString();
    }
    
    protected String [] getParts(String input, String operator) {
        
XLogger.getInstance().log(Level.FINE, "@resolvePair, Operator: {0}, Input: {1}", 
        this.getClass(), operator, input);

        String [] parts = input.split("\\Q"+operator+"\\E");
        
XLogger.getInstance().log(Level.FINER, "@resolvePair, Operator: {0}, Output: {1}", 
        this.getClass(), operator, Arrays.toString(parts));
        
        if(parts.length != 2) {
            throw new IllegalArgumentException();
        }
        
        return parts;
    }
    
    private boolean containsPattern(String input, String operator) {
        return this.getBasicPattern(operator).matcher(input).find();
    }
    
    protected StringBuilder getBasicRegex(String operator) {
        StringBuilder regex = new StringBuilder();
        regex.append('('); // begin group 0
        regex.append(this.getLeftOperandRegex());
        regex.append('('); // begin operator group
        regex.append("\\Q").append(operator).append("\\E");
        regex.append(')'); // end operator group
        regex.append(this.getRightOperandRegex());
        regex.append(')'); // end group 0
        return regex;
    }
}
