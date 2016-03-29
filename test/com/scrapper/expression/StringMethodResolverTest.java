package com.scrapper.expression;

import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class StringMethodResolverTest {
    
    public StringMethodResolverTest() { }

    @BeforeClass
    public static void setUpClass() throws Exception { }

    @AfterClass
    public static void tearDownClass() throws Exception { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    @Test
    public void testResolve_1args() {
        StringMethodResolver r =  new StringMethodResolver();
        StringBuilder errMsgs = new StringBuilder();
        this.doTestResolve_1args(r, "\"abcde\".length()", "5", errMsgs);
        this.doTestResolve_1args(r, "\"abcde\".substring(0,1)", "a", errMsgs);
        this.doTestResolve_1args(r, "\"abcde\".charAt(2)", "c", errMsgs);
        this.doTestResolve_1args(r, "\"abcde\".equals(\"abcde\")", "true", errMsgs);
        this.doTestResolve_1args(r, "\"abcde\".indexOf(\"cd\")", "2", errMsgs);
        this.doTestResolve_1args(r, "\"abcde\".toUpperCase()", "ABCDE", errMsgs);
        this.doTestResolve_1args(r, "\"cde\".equals(\"abcde\".substring(2))", "true", errMsgs);
        this.doTestResolve_1args(r, "\"BC\"+\"abcde\".length()+\"BC\"", "BC5BC", errMsgs);
        this.doTestResolve_1args(r, "\"BC\"+\"DE\"+\"abcde\".length()+\"BC\"", "BCDE5BC", errMsgs);
if(errMsgs.length() > 0) {
    System.err.println(errMsgs);
    throw new AssertionError(errMsgs.toString());
}else{
    System.out.println("SUCCESS!");
}        
    }
    private StringBuilder doTestResolve_1args(Resolver resolver, String expression, String expResult, StringBuilder appendError) {
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
    
    @Test
    public void testGetMethodRegex_1args() {
        // shortest methods of String class trim, wait = 4 chars 
        // longest method of String class compareToIgnoreCase = 19 chars
        String regex = new StringMethodResolver().getMethodRegex("[a-zA-Z]{4,19}"); 
        Pattern p = Pattern.compile(regex);
        StringBuilder errMsgs = new StringBuilder();
        this.doTestGetMethodRegex_1args(p, "\"abc\".length()", true, errMsgs);
        this.doTestGetMethodRegex_1args(p, "\"abcde\".substring(0,1)", true, errMsgs);
        this.doTestGetMethodRegex_1args(p, "\"abcde\".charAt(2)", true, errMsgs);
        this.doTestGetMethodRegex_1args(p, "\"abcde\".equals(\"abcde\")", true, errMsgs);
        this.doTestGetMethodRegex_1args(p, "\"abcde\".indexOf(\"cd\")", true, errMsgs);
    }
    private void doTestGetMethodRegex_1args(Pattern p, String input, boolean expected, StringBuilder errMsgs) {
        boolean result = p.matcher(input).find();
        if(result != expected) {
            errMsgs.append("Expected: ").append(expected).append(", result: ");
            errMsgs.append(result).append(", input: ").append(input);
        }
    }

    /**
     * Test of resolve method, of class StringMethodResolver.
     */
    @Test
    public void testResolve() throws Exception { }

    /**
     * Test of resolvePlainMethods method, of class StringMethodResolver.
     */
    @Test
    public void testResolvePlainMethods() throws Exception { }
}
