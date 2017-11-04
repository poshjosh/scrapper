package com.scrapper.formatter;












public class JobTitleFormatter
  extends CompanyNameFormatter
{
  public String apply(String s)
  {
    super.apply(s);
    return getJobTitle();
  }
}
