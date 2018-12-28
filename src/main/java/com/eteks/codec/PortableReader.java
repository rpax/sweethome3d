package com.eteks.codec;

import java.io.BufferedReader;
import java.io.IOException;

public class PortableReader extends BufferedReader
{
    public PortableReader(final BufferedReader reader)
    {
        super(reader);
    }

    public String readLine() throws IOException
    {
        String s = super.readLine();
        if (s.indexOf('\\') >= 0)
        {
            StringBuffer buffer = new StringBuffer(s.length());
            for (int i = 0; i < s.length();)
            {
                char c = s.charAt(i++);
                if (c == '\\')
                    switch (s.charAt(i++))
                    {
                        case '\\':
                            buffer.append('\\');
                            break;
                        case 't':
                            buffer.append('\t');
                            break;
                        case 'n':
                            buffer.append('\n');
                            break;
                        case 'r':
                            buffer.append('\r');
                            break;
                        case 'f':
                            buffer.append('\f');
                            break;
                        case 'u':
                            buffer.append((char) Integer.parseInt(
                                    s.substring(i, i += 4), 16));
                            break;
                    }
                else
                    buffer.append(c);
            }
            s = buffer.toString();
        }
        return s;
    }
}
