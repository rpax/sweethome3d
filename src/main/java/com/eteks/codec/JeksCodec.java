package com.eteks.codec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.eteks.jeks.CircularityException;
import com.eteks.jeks.IllegalCellException;
import com.eteks.jeks.JeksCell;
import com.eteks.jeks.JeksExpression;
import com.eteks.jeks.JeksExpressionParser;
import com.eteks.jeks.JeksExpressionSyntax;
import com.eteks.jeks.JeksFunctionParser;
import com.eteks.jeks.JeksFunctionSyntax;
import com.eteks.jeks.JeksParameter;
import com.eteks.jeks.JeksTable;
import com.eteks.jeks.JeksTable.MODE;
import com.eteks.jeks.JeksTableModel;
import com.eteks.jeks.SharedState;
import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledFunction;

public class JeksCodec
{
    protected enum CLASS_CODES
    {
        STRING, DOUBLE, LONG, DATE, BOOLEAN
    }

    protected MODE sheetMode = MODE.SPREADSHEET;

    public static final String JEKS_1_0_HEADER = "@#JEKS10#@";
    public static final String JEKS_1_0_FOOTER = "@#END#@";

    protected static final Locale SAVED_LOCALE = new Locale("", "");

    protected final JeksFunctionSyntax savedFunctionSyntax = new JeksFunctionSyntax(
            SAVED_LOCALE);

    protected final JeksExpressionSyntax savedExpressionSyntax = new JeksExpressionSyntax(
            SAVED_LOCALE);

    // TODO: REMOVE?
    protected static final String STRING_CLASS_SHORT_CUT = "S";
    protected static final String DOUBLE_CLASS_SHORT_CUT = "N";
    protected static final String LONG_CLASS_SHORT_CUT = "L";
    protected static final String DATE_CLASS_SHORT_CUT = "D";
    protected static final String BOOLEAN_CLASS_SHORT_CUT = "B";

    protected JeksTable table;
    protected SharedState state;
    protected TableModel model;
    protected String fileName;
    protected Vector<JeksCell> cellSet;

    protected JeksExpressionParser expressionParser;
    protected JeksFunctionSyntax functionSyntax;
    protected JeksExpressionSyntax expressionSyntax;
    protected JeksFunctionParser savedFunctionParser;
    protected JeksExpressionParser savedExpressionParser;
    protected TranslationParser translationExpressionParser;
    protected TranslationParser translationFunctionParser;

    /*
     * This constructor is intended for use when encoding an existing
     * spreadsheet with the table <code>table</code>.
     */
    public JeksCodec(final JTable table, final SharedState state)
    {
        this.table = (JeksTable) table;
        this.model = table.getModel();
        this.state = state;
        this.fileName = ((JeksTableModel) model).name;
        if (table instanceof JeksTable)
        {
            this.savedFunctionParser = new JeksFunctionParser(
                    savedExpressionSyntax, state);
            this.expressionParser = ((JeksTable) table).getExpressionParser();
            this.functionSyntax = (JeksFunctionSyntax) expressionParser
                    .getFunctionParser().getSyntax();
            this.expressionSyntax = (JeksExpressionSyntax) expressionParser
                    .getSyntax();

            this.savedExpressionParser = new JeksExpressionParser(model, state, 
                    savedExpressionSyntax, new JeksParameter(
                            savedExpressionSyntax, null, model, state), null,
                    savedFunctionParser, null);
            
            this.translationFunctionParser = new TranslationParser(
                    (JeksFunctionSyntax) expressionParser.getFunctionParser()
                            .getSyntax(), null, model, state);
            
            

            this.translationExpressionParser = new TranslationParser(
                    (JeksExpressionSyntax) expressionParser.getSyntax(),
                    expressionParser.getExpressionParameter(), model, state);
        }

    }

    /*
     * This constructor is intended for use when decoding a file called
     * <code>fileName</code> and creating a new spreadsheet.
     */
    public JeksCodec(final String fileName, final SharedState state)
    {
        this.fileName = CodecUtils.removeExtension(fileName);
        this.state = state;
        this.savedFunctionParser = new JeksFunctionParser(savedExpressionSyntax, state);
        createModel();
        this.expressionParser = new JeksExpressionParser(this.model, state);
        this.functionSyntax = (JeksFunctionSyntax) expressionParser
                .getFunctionParser().getSyntax();
        this.expressionSyntax = (JeksExpressionSyntax) expressionParser
                .getSyntax();

        this.savedExpressionParser = new JeksExpressionParser(model, state,
                savedExpressionSyntax, new JeksParameter(savedExpressionSyntax,
                        null, model, state), null, savedFunctionParser, null);
        
        this.translationExpressionParser = new TranslationParser(
                savedExpressionSyntax,
                savedExpressionParser.getExpressionParameter(), model, state);
        
        this.translationFunctionParser = new TranslationParser(
                savedFunctionSyntax, null, model, state);        
        
    }

    public void update(final InputStream in) throws IOException
    {

    }

    protected void createModel()
    {
        this.model = new JeksTableModel(this.fileName, this.state, 500, 200);
    }

    // TODO: THINK ABOUT WRITING SIMPLE USER DEFINED FUNCTIONS
    public void encode(final OutputStream out, final JTable table)
            throws IOException
    {
        PortableWriter writer = new PortableWriter(new BufferedWriter(
                new OutputStreamWriter(out)));

        writer.writeLine(JEKS_1_0_HEADER);
        writer.writeLine("#+MODE:" + ((JeksTable) table).getMode());
        writer.newLine();
        encodeCells(writer);
        writer.writeLine(JEKS_1_0_FOOTER);
        writer.flush();
    }

    protected void encodeCells(final PortableWriter writer) throws IOException
    {
        for (int row = 0; row < model.getRowCount(); row++)
        {
            for (int column = 0; column < model.getColumnCount(); column++)
            {
                StringBuffer buffer = new StringBuffer();
                Object value = model.getValueAt(row, column);
                if (value == null)
                {
                    continue;
                }
                encodeCell(buffer, value, row, column);
                writer.writeLine(buffer.toString());
            }
        }
    }

    protected void encodeCell(final StringBuffer buffer, final Object value,
            final int row, final int column)
    {
        buffer.append(savedExpressionSyntax.getColumnName(column));
        buffer.append(savedExpressionSyntax.getRowName(row));
        buffer.append(" ");
        if (value instanceof JeksExpression)
        {
            encodeExpression(buffer, (JeksExpression) value);
        } else
        {
            encodeLiteral(buffer, value);
            buffer.append(" ");
            buffer.append(String.valueOf(value));
        }
    }

    protected void encodeExpression(final StringBuffer buffer,
            final JeksExpression expression)
    {
        if (expressionParser == null
                || SAVED_LOCALE.equals(((JeksExpressionSyntax) expressionParser
                        .getSyntax()).getLocale()))
        {
            buffer.append(expression.getDefinition());
        } else
        {
            buffer.append(translationExpressionParser
                    .translateExpressionToSyntax(expression,
                            savedExpressionSyntax));
        }
    }

    protected void encodeLiteral(final StringBuffer buffer, Object value)
    {
        Class valueClass = value.getClass();
        if (valueClass.equals(String.class))
        {
            buffer.append(CLASS_CODES.STRING);
        } else if (valueClass.equals(Double.class))
        {
            buffer.append(CLASS_CODES.DOUBLE);
        } else if (valueClass.equals(Long.class))
        {
            buffer.append(CLASS_CODES.LONG);
        } else if (valueClass.equals(Date.class))
        {
            buffer.append(CLASS_CODES.DATE);
        } else if (valueClass.equals(Boolean.class))
        {
            buffer.append(CLASS_CODES.BOOLEAN);
        } else
        {
            buffer.append(valueClass.getName());
        }
    }

    protected JeksTable createTable()
    {
        return new JeksTable(model, expressionParser, true, state, fileName,
                sheetMode);
    }

    public JTable decode(final InputStream in) throws IOException
    {
        String line;
        BufferedReader reader = new PortableReader(new BufferedReader(
                new InputStreamReader(in)));
        cellSet = new Vector<JeksCell>();

        if (table == null)
        {
            table = createTable();
        }

        reader.readLine(); // Skip Header
        reader.readLine(); // Skip Mode line
        try
        {
            decodeUserFunctions(reader);
            while ((line = reader.readLine()) != null
                    && !line.equals(JEKS_1_0_FOOTER))
            {
                decodeCellValue(line);
            }
        } catch (IOException e)
        {
            throw e;
        } finally
        {
            reader.close();
        }
        decodeComplete();        
        return table;
    }

    protected void decodeUserFunctions(final BufferedReader reader)
            throws IOException
    {
        String line;
        while ((line = reader.readLine()) != null && line.length() > 0)
        {
            try
            {
                if (!SAVED_LOCALE.equals(functionSyntax.getLocale()))
                {
                    // The first compilation ensures that the read function is
                    // correctly
                    // written
                    CompiledFunction compiledFunction = savedFunctionParser
                            .compileFunction(line);

                    savedExpressionParser.addUserFunction(compiledFunction);

                    // Translate the function
                    line = translationFunctionParser.translateFunctionToSyntax(
                            compiledFunction, functionSyntax);
                }
                CompiledFunction compiledFunction = expressionParser
                        .getFunctionParser().compileFunction(line);

                expressionParser.addUserFunction(compiledFunction);
            } catch (CompilationException ex)
            {
                // Shouldn't happen
                throw new IOException(
                        functionSyntax
                                .getMessage(JeksFunctionSyntax.MESSAGE_INVALID_JEKS_FILE));
            }
        }
    }

    protected void decodeCellValue(final String line) throws IOException
    {
        JeksCell cell;
        String value;
        try
        {
            Object[] cellValTuple = extractLineDetails(line);
            cell = (JeksCell) cellValTuple[0];
            value = (String) cellValTuple[2];

            // Is this cell an expression or literal?
            if (value.startsWith(savedExpressionParser.getSyntax()
                    .getAssignmentOperator()))
            {
                cellSet.addElement(cell);
                JeksExpression computedCell = computeExpressionValue(cell,
                        value);
                model.setValueAt(computedCell, cell.getRow(), cell.getColumn());
            } else
            {
                Object cellValue = getCellLiteral(value);
                model.setValueAt(cellValue, cell.getRow(), cell.getColumn());
            }
        } catch (IndexOutOfBoundsException ex)
        {
        } // Should warn ?
    }

    protected void decodeComplete()
    {
        table.getReferringCellsListener().tableUpdated(model, cellSet);
    }
    
    protected Object[] extractLineDetails(final String line) throws IOException
    {
        // First space separates the cell address from the rest of the line.
        // E.g. "B13 LONG 3"
        int firstSpaceIndex = line.indexOf(" ");
        Object[] lineDetails = new Object[3];

        if (firstSpaceIndex == -1)
        {
            throw new IOException(
                    functionSyntax
                            .getMessage(JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
        }
        try
        {
            lineDetails[0] = savedExpressionSyntax.getCellAt(line.substring(0,
                    firstSpaceIndex));
            lineDetails[1] = (line.substring(0, firstSpaceIndex));
            lineDetails[2] = line.substring(firstSpaceIndex + 1);
        } catch (RuntimeException e)
        {
            throw new IOException(
                    functionSyntax
                            .getMessage(JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
        }
        return lineDetails;
    }

    protected JeksExpression computeExpressionValue(final JeksCell cell,
            final String expression) throws IOException
    {
        JeksExpression computedCell = null;
        String translatedExpression = expression;
        try
        {
            if (!SAVED_LOCALE.equals(expressionSyntax.getLocale()))
            {
                // The first compilation ensures that the read expression is
                // correctly written
                computedCell = (JeksExpression) savedExpressionParser
                        .compileExpression(expression);

                translatedExpression = translationExpressionParser
                        .translateExpressionToSyntax(computedCell,
                                expressionSyntax);
            }
            computedCell = (JeksExpression) expressionParser
                    .compileExpression(translatedExpression);
            computedCell.checkCircularity(state, model, cell);
        }
        /*
         * Handle Exceptions
         */
        catch (CompilationException ex)
        {
            // Shouldn't happen
            throw new IOException(
                    functionSyntax
                            .getMessage(JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
        } catch (CircularityException ex)
        {
            // Required to avoid infinite loops
            computedCell.invalidateValue(ex);
        } catch (IndexOutOfBoundsException ex)
        {
        } catch (IllegalCellException ex)
        {
            computedCell.invalidateValue();
        }
        return computedCell;
    }

    protected Object getCellLiteral(final String value) throws IOException
    {
        Class valueClass;
        Class[] ctorArgs1 = new Class[1];
        ctorArgs1[0] = String.class;
        int spaceIndex = value.indexOf(" ");

        if (spaceIndex == -1)
        {
            throw new IOException(
                    functionSyntax
                            .getMessage(JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
        }
        try
        {
            // Read the class name of the value and the value
            CLASS_CODES code = CLASS_CODES.valueOf(value.substring(0,
                    spaceIndex).toUpperCase());
            switch (code)
            {
                case STRING:
                    valueClass = String.class;
                    break;
                case DOUBLE:
                    valueClass = Double.class;
                    break;
                case LONG:
                    valueClass = Long.class;
                    break;
                case DATE:
                    valueClass = Date.class;
                    break;
                case BOOLEAN:
                    valueClass = Boolean.class;
                    break;
                default:
                    valueClass = Class.forName(value.substring(0, spaceIndex));
                    break;
            }
            Constructor strCtor = valueClass.getConstructor(ctorArgs1);
            Object cellValue = strCtor.newInstance(value
                    .substring(spaceIndex + 1));
            return cellValue;
        } catch (Exception e)
        {
            throw new IOException(
                    functionSyntax
                            .getMessage(JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
        }
    }
}
