/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrapper.expression;

import java.util.Set;
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
public class VariableExpressionManagerTest {
    
    public VariableExpressionManagerTest() {
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
     * Test of reset method, of class MultiResolver.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        MultiResolver instance = new MultiResolver();
        instance.reset();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resolve method, of class MultiResolver.
     */
    @Test
    public void testResolve() {
    }

    /**
     * Test of getVariableNames method, of class MultiResolver.
     */
    @Test
    public void testGetVariableNames() {
        System.out.println("getVariableNames");
        MultiResolver instance = new MultiResolver();
        Set expResult = null;
        Set result = instance.getVariableNames();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVariable method, of class MultiResolver.
     */
    @Test
    public void testGetVariable() {
        System.out.println("getVariable");
        String name = "";
        MultiResolver instance = new MultiResolver();
        Object expResult = null;
        Object result = instance.getVariable(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addVariable method, of class MultiResolver.
     */
    @Test
    public void testAddVariable() {
        System.out.println("addVariable");
        String name = "";
        Object value = null;
        MultiResolver instance = new MultiResolver();
        instance.addVariable(name, value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeVariable method, of class MultiResolver.
     */
    @Test
    public void testRemoveVariable() {
        System.out.println("removeVariable");
        String name = "";
        MultiResolver instance = new MultiResolver();
        Object expResult = null;
        Object result = instance.removeVariable(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getResolver method, of class MultiResolver.
     */
    @Test
    public void testGetResolver() {
    }

    /**
     * Test of setResolver method, of class MultiResolver.
     */
    @Test
    public void testSetResolver() {
    }
}
