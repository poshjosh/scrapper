/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrapper.expression;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class LogicExpressionResolverTest {
    
    public LogicExpressionResolverTest() {
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
     * Test of getLeftOperandRegex method, of class LogicExpressionResolver.
     */
    @Test
    public void testResolve_1args() {
        LogicExpressionResolver r =  new LogicExpressionResolver();
        StringBuilder errMsgs = new StringBuilder();
        this.doTestResolve_1args(r, "true||false", "true", errMsgs);
        this.doTestResolve_1args(r, "true|false", "true", errMsgs);
        this.doTestResolve_1args(r, "true&&false", "false", errMsgs);
        this.doTestResolve_1args(r, "true&false", "false", errMsgs);
        this.doTestResolve_1args(r, "true==false", "false", errMsgs);
        this.doTestResolve_1args(r, "true!=false", "true", errMsgs);
if(errMsgs.length() > 0) {
    throw new AssertionError(errMsgs.toString());
}else{
    System.out.println("SUCCESS!");
}        
    }
    private StringBuilder doTestResolve_1args(ExpressionResolver resolver, 
            String expression, String expResult, StringBuilder appendError) {
        try{
            String output = resolver.resolve(expression);
            boolean success = expResult.equals(output);
            return success ? null : appendError.append("Expected: "
            ).append(expResult).append(", output: ").append(output
            ).append(", expression: ").append(expression).append("\n");
        }catch(ResolverException e) {
//            e.printStackTrace();
            return appendError.append(e.toString()).append("\n");
        }
    }
    
    /**
     * Test of getLeftOperandRegex method, of class LogicExpressionResolver.
     */
    @Test
    public void testGetLeftOperandRegex() {
    }

    /**
     * Test of getRightOperandRegex method, of class LogicExpressionResolver.
     */
    @Test
    public void testGetRightOperandRegex() {
    }

    /**
     * Test of resolve method, of class LogicExpressionResolver.
     */
    @Test
    public void testResolve_3args_1() {
    }

    /**
     * Test of resolve method, of class LogicExpressionResolver.
     */
    @Test
    public void testResolve_3args_2() {
    }

    /**
     * Test of getOperators method, of class LogicExpressionResolver.
     */
    @Test
    public void testGetOperators() {
    }
}
