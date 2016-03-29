package com.scrapper.util;

import com.bc.webdatex.locator.TransverseList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class TransverseListTest {
    
    public TransverseListTest() {
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

    @Test
    public void testAll() {
        
        String transverse = "[\"<HTML>\",\"<BODY>\",\"<div id=\"Main\">\"]";
        
        transverse = "[\"<HTML>\",\"<BODY>\",\"<div id=\"Main\">\",\"<div id=\"Contents\">\",\"<div id=\"OverviewPanel\" class=\"tabPanel active\">\",\"<div id=\"CarPicture\"> <div id=\"Price\"> <br /> <div id=\"ContactInfo\">\"]";
                
        TransverseList tList = new TransverseList();
        
        tList.setTransverse(transverse);
        
        for(List<String> list:tList) {
            for(String s:list) {
                if(!s.startsWith("<") || !s.endsWith(">")) {
                    throw new AssertionError("Test Failed");
                }
System.out.print(s); System.out.print(' ');                
            }
System.out.println();
        }
    }

    /**
     * Test of get method, of class TransverseList.
     */
    @Test
    public void testGet() {
    }

    /**
     * Test of size method, of class TransverseList.
     */
    @Test
    public void testSize() {
    }

    /**
     * Test of setTransverse method, of class TransverseList.
     */
    @Test
    public void testSetTransverse_ObjectArr() {
    }

    /**
     * Test of setTransverse method, of class TransverseList.
     */
    @Test
    public void testSetTransverse_String() {
    }
}
