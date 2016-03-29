package com.scrapper.util;

import com.bc.task.StoppableTask;
import java.util.List;












public abstract class TaskCache<T extends StoppableTask>
  extends DefaultLimitedCache<T>
{
  public TaskCache(String category, List<String> names)
  {
    super(category, names);
  }
  
  public int getLimit()
  {
    return getNames().length;
  }
  
  public boolean isDone(T type)
  {
    return (type.isStopped()) || (type.isCompleted());
  }
}
