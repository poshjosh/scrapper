package com.scrapper.formatter;

import com.bc.webdatex.formatter.Formatter;
import java.io.Serializable;















public class CompanyNameFormatter
  implements Formatter<String>, Serializable
{
  private String jobTitle;
  private String[] preWords;
  private String[] postWords;
  
  public CompanyNameFormatter()
  {
    this.preWords = new String[] { " wanted at ", " vacancies at ", " needed at ", " jobs at ", " at " };
    this.postWords = new String[] { " latest job vacancies ", " job vacancies ", " vacancies ", " recruits ", " vacancy for " };
  }
  
  public void reset() {
    this.jobTitle = null;
  }
  

  public String format(String s)
  {
    String companyName = null;
    


    String[] parts = s.split(":");
    
    if ((parts != null) && (parts.length > 1))
    {
      companyName = extractCompanyName(parts[0]);
      
      this.jobTitle = parts[1].trim();
    }
    


    if (companyName == null)
    {
      companyName = extractCompanyName(s);
    }
    
    if (companyName != null) {
      companyName = new ToTitleCase().format(companyName);
    }
    
    return companyName;
  }
  
  private String extractCompanyName(String s)
  {
    String companyName = extractCompanyName_0(s);
    


    if (companyName == null)
    {
      companyName = extractCompanyName_1(s);
      
      if (companyName == null)
      {
        companyName = extractCompanyName_2(s);
      }
    }
    
    return companyName;
  }
  
  private String extractCompanyName_0(String s)
  {
    String sL = s.toLowerCase();
    


    String[] parts = split(sL, this.preWords);
    

    String companyName = null;
    
    if (parts != null)
    {
      this.jobTitle = parts[0].trim();
      
      companyName = parts[1].trim();
    }
    else
    {
      parts = split(sL, this.postWords);
      
      if (parts != null)
      {
        companyName = parts[0].trim();
        
        this.jobTitle = parts[1].trim();
      }
    }
    
    return companyName;
  }
  
  private String[] split(String src, String[] splits)
  {
    for (String split : splits)
    {
      String[] parts = split(src, split);
      
      if (parts != null) {
        return parts;
      }
    }
    return null;
  }
  
  private String[] split(String src, String split)
  {
    String[] parts = src.split(split);
    
    return (parts != null) && (parts.length > 1) ? parts : null;
  }
  







  private String extractCompanyName_1(String s)
  {
    String companyName = substring(s.toLowerCase(), 0, new String[] { "limited", "plc", "nigeria", "company" });
    

    return companyName;
  }
  
  private String substring(String src, int start, String[] ends)
  {
    for (int i = 0; i < ends.length; i++)
    {
      String companyName = substring(src, start, ends[i]);
      
      if (companyName != null)
      {
        return companyName;
      }
    }
    
    return null;
  }
  
  private String substring(String src, int start, String endText)
  {
    int end = src.indexOf(endText);
    
    String output = end == -1 ? null : src.substring(start, end + endText.length());
    
    return output;
  }
  
  private String extractCompanyName_2(String s)
  {
    String companyName = null;
    
    int end = s.indexOf(" ");
    
    if (end == -1) {
      companyName = s;
    } else {
      companyName = s.substring(0, end);
    }
    






    return companyName;
  }
  
  public String getJobTitle() {
    return this.jobTitle;
  }
  
  public String[] getPostWords() {
    return this.postWords;
  }
  
  public void setPostWords(String[] postWords) {
    this.postWords = postWords;
  }
  
  public String[] getPreWords() {
    return this.preWords;
  }
  
  public void setPreWords(String[] preWords) {
    this.preWords = preWords;
  }
}
