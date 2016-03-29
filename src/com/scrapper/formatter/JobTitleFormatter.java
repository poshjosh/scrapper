package com.scrapper.formatter;












public class JobTitleFormatter
  extends CompanyNameFormatter
{
  public String format(String s)
  {
    super.format(s);
    return getJobTitle();
  }
}
