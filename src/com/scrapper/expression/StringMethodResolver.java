package com.scrapper.expression;

import com.bc.util.XLogger;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @(#)StringMethodResolver.java   10-Oct-2013 22:15:00
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
public class StringMethodResolver implements Resolver {
    
    private Method [] methods;
    
    public StringMethodResolver() {
        methods = String.class.getMethods();
    }
    
    @Override
    public boolean isResolvable(String input) {
        boolean output = true;
        if(input.indexOf("(") == -1) {
            output = false;
        }
        if(output) {
            Pattern pattern = this.getMethodPattern();
            Matcher matcher = pattern.matcher(input);
            output = matcher.find();
        }
        return output;
    }
    
    @Override
    public String resolve(String input) throws ResolverException{

        input = this.resolveSimpleConcatenation(input);
        
        String [] parts = input.split("\\+");
        
        StringBuilder output = new StringBuilder();
        
        for(int i=0; i<parts.length; i++) {
            
            parts[i] = parts[i].trim();
            
            if(this.isResolvable(parts[i])) {
                parts[i] = this.resolveInnerMethods(parts[i]);
                parts[i] = this.resolveDirectMethods(parts[i]);
            }else{
                // remove leading and trailing ' or " if necessary
                if( (parts[i].startsWith("\"") && parts[i].endsWith("\"")) ||
                ((parts[i].startsWith("'") && parts[i].endsWith("'"))) ) {
                    parts[i] = parts[i].substring(1, parts[i].length()-1);
                }
            }
            
            output.append(parts[i]);
        }
        
        return output.toString();
    }

    public String resolveSimpleConcatenation(String input) throws ResolverException {
        
        if(!input.contains("+")) {
            return input;
        }
        
XLogger.getInstance().log(Level.FINE, "BEFORE simple concatenation: {0}", this.getClass(), input);        
        
        // "abc"+"5",  "abc"+'5', "abc"+5 BUT NOT '5'+"abc",  5+"abc"
        // NOT "abc"+"5".length()
        String concatRegex = "\"(.*?)\"(\\+)[\"']{0,1}([0-9.]*?)[\"']^\\.";
        
        Pattern pattern = Pattern.compile(concatRegex);
        
        Matcher matcher = pattern.matcher(input);
        
        StringBuffer sb = new StringBuffer();
        
        while(matcher.find()) {
            String whole = matcher.group(0);
            String left = matcher.group(1);
            String plus = matcher.group(2);
            String right = matcher.group(3);
            
XLogger.getInstance().log(Level.FINEST, "Group0: {0}, gp1: {1}, gp2: {2}, gp3: {3}", 
this.getClass(), whole, left, plus, right);            
            
            matcher.appendReplacement(sb, left + right);
        }
        
        matcher.appendTail(sb);
        
        String output = sb.toString();
        
XLogger.getInstance().log(Level.FINE, "AFTER simple concatenation: {0}", this.getClass(), output);        
        return output;
    }
    
    public String resolveInnerMethods(String input) throws ResolverException{
XLogger.getInstance().log(Level.FINER, "BEFORE resolving inner methods input: {0}", this.getClass(), input);        
        if(input.indexOf("(") == -1) return input;
        
        Pattern pattern = this.getMethodPattern();
        
        // resolve brackets
        Matcher matcher = pattern.matcher(input);

        StringBuffer sb = new StringBuffer();
        
        while(matcher.find()) {
            
            String whole = matcher.group(0);
            String stringObj = matcher.group(1);
            String methodName = matcher.group(2);
            String methodArgs = matcher.group(3);

XLogger.getInstance().log(Level.FINEST, "Group0: {0}, gp1: {1}, gp2: {2}, gp3: {3}", 
this.getClass(), whole, stringObj, methodName, methodArgs);                

            boolean hasInnerMethod = this.isResolvable(methodArgs);
            
XLogger.getInstance().log(Level.FINE, "Has inner method: {0}, inner: {1}", 
this.getClass(), hasInnerMethod, methodArgs);

            String replacement = null;
            
            if(!hasInnerMethod) { // x.abc(x.abc(0,1),x.abc(2,3))
                
                replacement = this.resolveDirectMethods(whole);
                
            }else{
                
                // found method within method brakets
                methodArgs = this.resolveInnerMethods(methodArgs);

                final String quote = this.getSuitableQuote(methodArgs);

                // reconstruct the method
                StringBuilder rep = new StringBuilder();
                rep.append('"').append(stringObj).append('"').append('.').append(methodName);
                replacement = rep.append('(').append(quote).append(methodArgs).append(quote).append(')').toString();
            }
            
XLogger.getInstance().log(Level.FINE, "Replacing: {0} with: {1}", 
this.getClass(), whole, replacement);

            matcher.appendReplacement(sb, replacement);

        }
        
        matcher.appendTail(sb);
        
        String output = sb.toString(); //this.resolveDirectMethods(sb.toString());
XLogger.getInstance().log(Level.FINER, "AFTER resolving inner methods output: {0}", 
this.getClass(), output);

        return output;
    }

    /**
     * Plain methods are methods with no nested methods as parameters
     * @param input
     * @return
     * @throws ResolverException 
     */
    protected String resolveDirectMethods(String input) throws ResolverException {
XLogger.getInstance().log(Level.FINER, "@resolvePlainMethods, input: {0}", 
this.getClass(), input);

        for(int i=0; i<methods.length; i++) {
            
            Method method = methods[i];
            
            Pattern pattern = this.getMethodPattern(method.getName());

XLogger.getInstance().log(Level.FINEST, "Method: {0}, regex: {1}", 
this.getClass(), method.getName(), pattern.pattern());
            
            Matcher matcher = pattern.matcher(input);
            
            StringBuffer update = new StringBuffer();
            
            while(matcher.find()) {

                String whole = matcher.group(0);
                String stringObj = matcher.group(1);
                String methodName = matcher.group(2);
                String methodArgs = matcher.group(3);

XLogger.getInstance().log(Level.FINEST, "Group0: {0}, gp1: {1}, gp2: {2}, gp3: {3}", 
this.getClass(), whole, stringObj, methodName, methodArgs);                

                if(!methodName.equals(method.getName())) {

                    throw new AssertionError("Expected: "+method.getName()+", Found: "+methodName);
                }

                Object [] params = null;
                
                if(methodArgs != null && !methodArgs.isEmpty()) {
                    
                    String [] parts = methodArgs.split("\\,");
                    
                    Class<?> [] paramTypes = method.getParameterTypes();

                    if(paramTypes.length != parts.length) {

                        // NOTE: The ++ in i++ above has taken effect 
                        if(this.isSameNameMethodAvailable(methodName, i)) {
                            continue;
                        }else{
                            throw new ResolverException("Wrong number of method arguments, expected "+paramTypes.length+" arguments found "+parts.length+" arguments for method: "+method);
                        }    
                    }
                    
                    Class<?> [] paramClasses = new Class<?>[parts.length];
                    
                    for(int j=0; j<parts.length; j++) {
                        Parameter param = this.getParameter(parts[j]);
                        parts[j] = param.value;
                        paramClasses[j] = param.valueClass;
                    }
XLogger.getInstance().log(Level.FINE, "Args: {0}, ActualTypes: {1}, MyGuess: {2}", 
this.getClass(), Arrays.toString(parts), 
Arrays.asList(paramTypes), Arrays.asList(paramClasses));
                    
                    if(!this.suitable(paramTypes, paramClasses)) {
                        continue;
                    }

                    params = this.getParameters(method.getParameterTypes(), parts);
XLogger.getInstance().log(Level.FINER, "Params: {0}", this.getClass(), params);                    
                }
                
                try{
                    
                    Object result = method.invoke(stringObj, params);
                    
XLogger.getInstance().log(Level.FINE, "Result: {0}, object: {1}, method: {2}, args: {3}", 
this.getClass(), result, stringObj, methodName, Arrays.toString(params));

                    matcher.appendReplacement(update, result.toString());
                    
                }catch(Exception e) {
                    ResolverException re = new ResolverException();
                    re.initCause(e);
                    throw re;
                }
            }

            matcher.appendTail(update);

if(XLogger.getInstance().isLoggable(Level.FINER, this.getClass()) 
        && !input.equals(update.toString())) {
XLogger.getInstance().log(Level.FINER, "After resolving: {0}, updated: {1}, to: {2}", 
this.getClass(), method.getName(), input, update);
}
            
            input = update.toString();
        }

        return input;
    }
    
    public String getMethodRegex() {
        // String class methods contain only letters
        // shortest methods of String class trim, wait = 4 chars 
        // longest method of String class compareToIgnoreCase = 19 chars
        return "[a-zA-Z]{4,19}"; 
    }
    
    public String getMethodRegex(String method) {
//"(.*?)"\\.(stringMethodName)\\((.*)\\)
        StringBuilder regex = new StringBuilder();
        regex.append("\\\"(.*?)\\\"\\.(");
        regex.append(method);
        regex.append(")\\((.*)\\)");
        return regex.toString();
    }
    
    private Pattern getMethodPattern() {
        return this.getMethodPattern(this.getMethodRegex());
    }
    
    private Pattern getMethodPattern(String method) {
        String regex = this.getMethodRegex(method);
        return Pattern.compile(regex);
    }

    private boolean isSameNameMethodAvailable(String methodName, int offset) {
        for(int i=offset; i<methods.length; i++) {
            if(methodName.equals(methods[i].getName())) return true;
        }
        return false;
    }
    
    private boolean suitable(Class<?> [] actual, Class<?> [] found) {
        for(int i=0; i<actual.length; i++) {
            if(!this.suitable(actual[i], found[i])) {
                return false;
            }
        }
        return true;
    }
    
    private boolean suitable(Class actual, Class found) {
        if(found == String.class) {
            return actual == String.class ||
                    actual == CharSequence.class || 
                    actual == StringBuffer.class || 
                    actual == Charset.class ||
                    actual == Object.class;
        }else if(found == long.class) {
            return actual == int.class || 
                    actual == long.class;
        }else if(found == char.class){
            return actual == char.class;
        }else{
            return false;
        }
    }
    
    private Parameter getParameter(String input) throws ResolverException{
        input = input.trim();
        boolean isChar = input.charAt(0) == '\'';
        boolean isString = input.charAt(0) == '"';
        boolean isNumber = false;
        if(isChar || isString) {
            input = input.substring(1, input.length()-1);
        }else{
            try{
                Long.parseLong(input);
                isNumber = true;
            }catch(NumberFormatException e) {
                //
            }
        }
        Class aClass = null;
        if(isChar) {
            aClass = char.class;
        }else if(isString) {
            aClass = String.class;
        }else if(isNumber) {
            aClass = long.class;
        }else{
            throw new ResolverException("Unable to determine type of method parameter: "+input);
        }
XLogger.getInstance().log(Level.FINEST, "Input: {0} Suitable class: {1}", 
this.getClass(), input, aClass);
        return new Parameter(input, aClass);
    }
    
    private static class Parameter{
        private String value;
        private Class valueClass;
        Parameter(String val, Class valClass) {
            if(val == null || valClass == null) throw new NullPointerException();
            this.value = val;
            this.valueClass = valClass;
        }
    }
    
    private String getSuitableQuote(String input) {
        Class aClass = this.getSuitableClass(input);
        return (aClass == long.class) ? "" : (aClass == char.class) ? "'" : "\""; 
    }
    
    private Class getSuitableClass(String input) {
        boolean isNumber = false;
        try{
            Long.parseLong(input);
            isNumber = true;
        }catch(NumberFormatException e) {
            // ignore
        }
        boolean isChar = input.length() == 1;
        return (isNumber) ? long.class : (isChar) ? char.class : String.class; 
    }
    
    private Object [] getParameters(Class<?> [] paramTypes, String [] parts) throws ResolverException {
        if(parts == null) throw new NullPointerException();
        Object [] params = new Object[parts.length];
        for(int i=0; i<parts.length; i++) {
            params[i] = convertTo(paramTypes[i], parts[i]);
        }
        return params;
    }
    
    private Object convertTo(Class aClass, String s) throws ResolverException {
        if(aClass == String.class) {
            return s;
        }else if(aClass == CharSequence.class) {
            return s;
        }else if(aClass == Character.class) {    
            return s.charAt(0);
        }else if(aClass == int.class) {
            return Integer.parseInt(s);
        }else if(aClass == Object.class) {
            return s;
        }else if(aClass == StringBuffer.class){
            return new StringBuffer(s);
        }else if(aClass == Charset.class) {
            return Charset.forName(s);
        }else if(aClass == long.class) {
            return Long.parseLong(s);
        }else if(aClass == Locale.class) {
            throw new UnsupportedOperationException();
        }else if(aClass == byte[].class) {
            throw new UnsupportedOperationException();
        }else{
            throw new ResolverException(""+aClass+" is not a known method argument for any of the methods of class java.lang.String");
        }
    }
}
