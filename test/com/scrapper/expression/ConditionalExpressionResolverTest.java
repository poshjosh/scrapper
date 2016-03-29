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
public class ConditionalExpressionResolverTest {
    
    public ConditionalExpressionResolverTest() {
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
     * Test of resolve method
     */
    @Test
    public void testResolve_1args() {
        ConditionalExpressionResolver r =  new ConditionalExpressionResolver();
        StringBuilder errMsgs = new StringBuilder();
        this.doTestResolve_1args(r, "0.1>1.0", "false", errMsgs);
        this.doTestResolve_1args(r, "12.1111>=12.1111", "true", errMsgs);
        this.doTestResolve_1args(r, "23<23.00", "false", errMsgs);
        this.doTestResolve_1args(r, "0.000<=0", "true", errMsgs);
        this.doTestResolve_1args(r, "123.456==123.4560", "true", errMsgs);
        this.doTestResolve_1args(r, "1.111!=1.1111", "true", errMsgs);
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
     * Test of resolve method, of class ConditionalExpressionResolver.
     */
    @Test
    public void testResolve() {
    }

    /**
     * Test of getOperators method, of class ConditionalExpressionResolver.
     */
    @Test
    public void testGetOperators() {
    }
}
