package com.scrapper.util;

import com.bc.process.StoppableTask;
import java.util.List;

/**
 * @(#)TaskCache.java   05-Nov-2014 13:02:18
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public abstract class TaskCache<T extends StoppableTask> extends DefaultLimitedCache<T> {

    public TaskCache(String category, List<String> names) {
        
        super(category, names);
    }
    
    @Override
    public int getLimit() {
        return this.getNames().length;
    }

    @Override
    public boolean isDone(T type) {
        return type.isStopped() || type.isCompleted();
    }
}
