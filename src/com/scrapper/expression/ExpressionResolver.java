package com.scrapper.expression;

/**
 * @(#)ExpressionResolver.java   08-Oct-2013 21:57:40
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
public interface ExpressionResolver<E> extends Resolver {

    String[] getOperators();

    /**
     * Resolves the input expression using the specified operand.
     * @param input The expression to resolve. E.g <tt>4 + 3 - 6</tt>
     * @param operator The operand to use in resolving the expression
     * @return String E.g for <tt>4 + 3 - 6</tt> and operand '+' returns <tt>7 - 6</tt>
     * @throws IllegalArgumentException if operand an element in the array returned
     * by the method {@linkplain #getOperators()}
     */
    String resolve(String input, String operator) throws ResolverException;
    
    /**
     * @throws IllegalArgumentException if operand an element in the array returned
     * by the method {@linkplain #getOperators()}
     */
    String resolve(String a, String operator, String b) throws ResolverException;

    Object resolve(E a, String operator, E b) throws ResolverException;
}
