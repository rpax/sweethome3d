package com.eteks.codec;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CodecUtils
{
    private CodecUtils()
    {
        // This is a utility class, we don't need to instantiate it.
        throw new AssertionError();
    }
    
    public static String getColumnFromString(final String cellAddress)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cellAddress.length(); i++)
        {
            if (Character.isLetter(cellAddress.charAt(i)))
            {
                sb.append(cellAddress.charAt(i));
            } else
            {
                break;
            }
        }
        return sb.toString();
    }

    public static String getRowFromString(final String cellAddress)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = cellAddress.length() - 1; i >= 0; i--)
        {
            if (Character.isDigit(cellAddress.charAt(i)))
            {
                sb.append(cellAddress.charAt(i));
            } else
            {
                break;
            }
        }
        return sb.reverse().toString();
    }

    public static String removeExtension(final String s)
    {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1)
        {
            filename = s;
        } else
        {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
        {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }

    @SuppressWarnings("unchecked")
    public static boolean contains(final Class clazz, final String value)
    {
        try
        {
            Enum.valueOf(clazz, value);
            return true;
            // yes
        } catch (IllegalArgumentException ex)
        {
            return false;
        }
    }

    public static String getColumnFromHeader(
            Set<Map.Entry<String, ITIER_TWO_HEADERS>> mappings,
            ITIER_TWO_HEADERS header)
    {
        Iterator<Map.Entry<String, ITIER_TWO_HEADERS>> it = mappings.iterator();
        while (it.hasNext())
        {
            Map.Entry<String, ITIER_TWO_HEADERS> me = it.next();
            if (me.getValue() == header)
            {
                return me.getKey();
            }
        }
        return null;
    }

}
