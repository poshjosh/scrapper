package com.scrapper.expression;

/**
 * @(#)ResolverException.java   10-Oct-2013 22:55:16
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
public class ResolverException extends RuntimeException {

    /**
     * Creates a new instance of <code>ResolverException</code> without detail message.
     */
    public ResolverException() {
    }

    /**
     * Constructs an instance of <code>ResolverException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ResolverException(String msg) {
        super(msg);
    }
}
