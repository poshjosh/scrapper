package com.scrapper.extractor;















public class ScriptExtractor
{
  private String parse(String input, int offset)
  {
    int indents = 0;
    
    int start = input.indexOf("function", offset);
    int end = -1;
    
    if (start == -1) {
      return null;
    }
    
    boolean started = true;
    
    offset = start + "function".length();
    
    for (int i = offset; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (ch == '{') {
        indents++;
      } else if (ch == '}') {
        indents--;
      }
      if (indents > 0) {
        started = true;
      }
      if ((started) && (indents == 0)) {
        int nextStart = input.indexOf("function", i);
        if (nextStart != -1) {
          parse(input, i);
          break;
        }
      }
      if (started) {
        int nextStart = input.indexOf("function", i);
        if (nextStart != -1) {
          parse(input, i);
          break;
        }
      }
    }
    
    throw new UnsupportedOperationException();
  }
}
