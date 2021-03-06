package com.scrapper.formatter;

import com.bc.manager.Formatter;
import java.io.Serializable;

/**
 * @author Josh
 */
public class ToTitleCase implements Formatter<String>, Serializable {

    @Override
    public String format(String input) {

        String [] parts = input.split(" ");

        if(parts == null) return input;

        StringBuilder output = new StringBuilder();

        for(int i=0; i<parts.length; i++) {

            char [] chars = parts[i].toCharArray();

            for(int j=0; j<chars.length; j++) {
                char ch = chars[j];
                // Convert only the first char to title case
                if(j==0) {
                    ch = Character.toTitleCase(ch);
                }
                output.append(ch);
            }

            if(i < parts.length -1) {
                // Append spaces between words
                output.append(' ');
            }
        }

        return output.toString();
    }
}
