package com.eteks.codec;

import java.io.BufferedWriter;
import java.io.IOException;

public class PortableWriter extends BufferedWriter
{
    public PortableWriter(final BufferedWriter writer)
    {
        super(writer);
    }

    public void writeLine(String s) throws IOException
    {
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            switch (c)
            {
                case '\\':
                    super.write("\\\\");
                    break;
                case '\t':
                    super.write("\\t");
                    break;
                case '\n':
                    super.write("\\n");
                    break;
                case '\r':
                    super.write("\\r");
                    break;
                case '\f':
                    super.write("\\f");
                    break;
                default:
                    if ((c < 0x0020) || (c > 0x007E))
                    {
                        String hexChar = Integer.toHexString(c);
                        super.write("\\u");
                        super.write(Integer.toHexString((c >> 12) & 0xF));
                        super.write(Integer.toHexString((c >> 8) & 0xF));
                        super.write(Integer.toHexString((c >> 4) & 0xF));
                        super.write(Integer.toHexString(c & 0xF));
                    } else
                        write(c);
            }
        }
        newLine();
    }
}
