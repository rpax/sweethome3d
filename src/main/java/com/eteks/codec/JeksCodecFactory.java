package com.eteks.codec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JTable;

import com.eteks.jeks.JeksTable;
import com.eteks.jeks.SharedState;
import com.eteks.jeks.JeksTable.MODE;

public class JeksCodecFactory
{

    private static final String SPREADSHEET_MODE_LINE = "#+MODE:SPREADSHEET";
    private static final String TYPESHEET_MODE_LINE = "#+MODE:TYPESHEET";
    private static final String FUNCTIONSHEET_MODE_LINE = "#+MODE:FUNCTIONSHEET";

    private JeksCodecFactory()
    {
        // This is a utility class, we don't need to instantiate it.
        throw new AssertionError();
    }
    
    public static JeksCodec getCodecInstance(final JTable table,
            final SharedState state)
    {
        if (((JeksTable) table).getMode() == MODE.SPREADSHEET)
        {
            return new JeksCodec(table, state);
        } else if (((JeksTable) table).getMode() == MODE.FUNCTIONSHEET)
        {
            return new FunctionSheetCodec(table, state);
        } else
        {
            return new TypeSheetCodec(table, state);
        }
    }

    /**
     * Factory method that returns a <code>JeksCodec</code> whos underlying
     * implementation depends on the mode line of the file read from the stream.
     */
    public static JeksCodec getCodecInstance(final InputStream in,
            final String fileName, final SharedState state) throws IOException
    {
        BufferedReader reader = new PortableReader(new BufferedReader(
                new InputStreamReader(in)));

        try
        {
            if (!reader.readLine().equals(JeksCodec.JEKS_1_0_HEADER))
            {
                throw new IOException();
            }

            String line = reader.readLine();
            if (!line.startsWith("#+MODE:"))
            {
                throw new IOException("No Mode Header Specified");
            }

            if (line.equals(SPREADSHEET_MODE_LINE))
            {
                reader.close();
                return new JeksCodec(fileName, state);
            } else if (line.equals(TYPESHEET_MODE_LINE))
            {
                reader.close();
                return (JeksCodec) new TypeSheetCodec(fileName, state);
            } else if (line.equals(FUNCTIONSHEET_MODE_LINE))
            {
                reader.close();
                return (JeksCodec) new FunctionSheetCodec(fileName, state);
            } else
            {
                throw new IOException("Mode unrecognised");
            }
        } catch (IOException e)
        {
            throw e;
        } finally
        {
            reader.close();
        }
    }
}
