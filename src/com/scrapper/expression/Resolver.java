package com.scrapper.expression;

/**
 * @(#)Resolver.java   10-Oct-2013 22:31:23
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
public interface Resolver {
    boolean isResolvable(String input);
    String resolve(String input) throws ResolverException;
}
