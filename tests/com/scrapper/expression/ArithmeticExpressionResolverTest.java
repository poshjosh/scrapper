/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrapper.expression;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class ArithmeticExpressionResolverTest {
    
    public ArithmeticExpressionResolverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getLeftOperandRegex method, of class ArithmeticExpressionResolver.
     */
    @Test
    public void testGetLeftOperandRegex() {
System.out.println("getLeftOperandRegex()");        
        ArithmeticExpressionResolver resolver = new ArithmeticExpressionResolver();
        String regex = resolver.getLeftOperandRegex();
        HashMap<String, Boolean> m = new HashMap<String, Boolean>();
        m.put("+23", Boolean.TRUE);
        m.put("23.001", Boolean.TRUE);
        m.put("222", Boolean.TRUE);
        m.put("0.123", Boolean.TRUE);
        this.doTestGetOperandRegex(Pattern.compile(regex), m);
    }

    /**
     * Test of getRightOperandRegex method, of class ArithmeticExpressionResolver.
     */
    @Test
    public void testGetRightOperandRegex() {
System.out.println("getLeftOperandRegex()");        
        ArithmeticExpressionResolver resolver = new ArithmeticExpressionResolver();
        String regex = resolver.getRightOperandRegex();
        HashMap<String, Boolean> m = new HashMap<String, Boolean>();
        m.put("23.001", Boolean.TRUE);
        m.put("222", Boolean.TRUE);
        m.put("0.123", Boolean.TRUE);
        this.doTestGetOperandRegex(Pattern.compile(regex), m);
    }

    private void doTestGetOperandRegex(Pattern pattern, Map<String, Boolean> m) {
System.out.println("getLeftOperandRegex()");        
        StringBuilder builder = new StringBuilder();
        boolean failed = false;
        for(String input:m.keySet()) {
            Boolean successExpected = m.get(input);
            boolean found = pattern.matcher(input).find();
            if(successExpected && !found) {
builder.append("FAILED. Could not find '").append(pattern.pattern()).append("' in '").append(input).append("'");                
                failed = true;
            }else if(!successExpected && found) {
builder.append("FAILED. Found '").append(pattern.pattern()).append("' in '").append(input).append("'");                
                failed = true;
            }
        }
        if(failed) {
fail(builder.toString());            
        }
    }

    /**
     * Test of getParts method, of class ArithmeticExpressionResolver.
     */
    @Test
    public void testGetParts() {
    }
    
    /**
     * Test of resolve(String) method, of class ArithmeticExpressionResolver.
     */
    @Test
    public void testResolve_1args() {
System.out.println("resolve(String)");
        ArithmeticExpressionResolver resolver = new ArithmeticExpressionResolver();
        StringBuilder builder = new StringBuilder();
        String expr = "23.109+14-86.111-2.3-(3-45)";
        StringBuilder errMsg = this.doTestResolve_1args(resolver, expr, 23.109+14-86.111-2.3-(3-45));
        if(errMsg != null) builder.append('\n').append(errMsg);
        expr = "0.109-10*3-2.0-1+12*6+(3-45)";
        errMsg = this.doTestResolve_1args(resolver, expr, 0.109-10*3-2.0-1+12*6+(3-45));
        if(errMsg != null) builder.append('\n').append(errMsg);
        expr = "(((2.12+7-(23-3)*2)-1.1)+0.1)/1";
        errMsg = this.doTestResolve_1args(resolver, expr, (((2.12+7-(23-3)*2)-1.1)+0.1)/1);
        if(errMsg != null) builder.append('\n').append(errMsg);
        expr = "(((0.9+7-(40.1-3)*2)-1.1)+0.1)%1";
        errMsg = this.doTestResolve_1args(resolver, expr, (((0.9+7-(40.1-3)*2)-1.1)+0.1)%1);
        if(errMsg != null) builder.append('\n').append(errMsg);
if(builder.length() > 0) {
    fail(builder.toString());
}         
    }
    
    private StringBuilder doTestResolve_1args(ExpressionResolver res, String expr, Double expResult) {
        try{
            String output = res.resolve(expr);
            boolean success = Double.parseDouble(output) == expResult;
            StringBuilder builder = new StringBuilder();
builder.append("Success: ").append(success).append(", Output: ").append(output).append(", expected: ").append(expResult);        
            return (success) ? null : builder;
        }catch(ResolverException e) {
//            e.printStackTrace();
            return new StringBuilder().append(e);
        }
    } 

    /**
     * Test of resolve method, of class ArithmeticExpressionResolver.
     */
    @Test
    public void testResolve_3args_1() {
System.out.println("resolve(String, String, String) will be tested via method resolve(String)");
    }

    /**
     * Test of resolve method, of class ArithmeticExpressionResolver.
     */
    @Test
    public void testResolve_3args_2() {
System.out.println("resolve(Double, String, Double) will be tested via method resolve(String)");
    }

    /**
     * Test of getOperators method, of class ArithmeticExpressionResolver.
     */
    @Test
    public void testGetOperators() {
System.out.println("getOperators");
        ArithmeticExpressionResolver instance = new ArithmeticExpressionResolver();
        String[] expResult = {"*", "/", "%", "+", "-"};
        String[] result = instance.getOperators();
        if(result == null) {
fail("getOperators cannot return null!");
        }else{
assertArrayEquals("If you have changed the operands then rewrite this class and other necessary codes", expResult, result);
        }
    }
}
