package com.eteks.codec;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.eteks.functionsheet.FunctionSheetDefinition;
import com.eteks.functionsheet.FunctionSheetParameter;
import com.eteks.functionsheet.FunctionSheetTableModel;
import com.eteks.jeks.JeksCell;
import com.eteks.jeks.JeksExpression;
import com.eteks.jeks.JeksExpressionSyntax;
import com.eteks.jeks.JeksFunctionSyntax;
import com.eteks.jeks.JeksTable;
import com.eteks.jeks.JeksTableModel;
import com.eteks.jeks.SharedState;
import com.eteks.jeks.JeksTable.MODE;
import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledFunction;

public class FunctionSheetCodec extends JeksCodec
{
    protected MODE sheetMode = MODE.FUNCTIONSHEET;

    protected static final char HEADER_CHAR = '#';
    protected static final ITIER_ONE_HEADERS output = TIER_ONE_HEADERS.OUTPUT;
    
    protected CompiledFunction recurInterim;

    protected enum TIER_ONE_HEADERS implements ITIER_ONE_HEADERS
    {
        FUN_NAME, ARGS, DEFUN, OUTPUT
    }

    protected enum TIER_TWO_HEADERS implements ITIER_TWO_HEADERS
    {
        NAME, TEST_VALUE
    }

    protected static class FunctionParamHolder
    {
        public String name = null;
        public JeksCell testCell = null;

        public boolean isFullyDefined()
        {
            return !(name == null || testCell == null);
        }

    }

    protected FunctionSheetDefinition currentDefinition;
    protected ITIER_ONE_HEADERS currentTierOneHeader;
    protected ITIER_TWO_HEADERS currentTierTwoHeader;
    protected Hashtable<String, ITIER_TWO_HEADERS> columnHeaderMapping;
    protected FunctionParamHolder paramHolder = new FunctionParamHolder();

    public FunctionSheetCodec(final JTable table, final SharedState state)
    {
        super(table, state);
    }

    public FunctionSheetCodec(final String fileName, final SharedState state)
    {
        super(fileName, state);
        currentDefinition = new FunctionSheetDefinition(model);

        // Add the current definition to the model so that we can
        // correctly evaluate expressions that use the parameters
        // in the function definition.
        ((FunctionSheetTableModel) this.model).addDefinition(currentDefinition);
    }

    @Override
    protected void createModel()
    {
        this.model = new FunctionSheetTableModel(this.fileName, this.state,
                500, 200);
    }

    @Override
    protected JeksTable createTable()
    {
        return new JeksTable(model, expressionParser, true, state, fileName,
                sheetMode);
    }

    protected ITIER_ONE_HEADERS getOutput()
    {
        return output;
    }

    @Override
    public void update(final InputStream in) throws IOException
    {
        for (CompiledFunction def : ((FunctionSheetTableModel) model)
                .getCompiledDefinitions())
        {
            expressionParser.removeUserFunction(def);
        }
        ((FunctionSheetTableModel) model).clearDefinitions();
        currentDefinition = new FunctionSheetDefinition(model);

        // Add the current definition to the model so that we can
        // correctly evaluate expressions that use the parameters
        // in the function definition.
        ((FunctionSheetTableModel) this.model).addDefinition(currentDefinition);
        decode(in);
    }

    @Override
    protected void decodeCellValue(final String line) throws IOException
    {
        JeksCell cell;
        String address;
        String value;
        try
        {
            Object[] lineDetailsTuple = extractLineDetails(line);
            cell = (JeksCell) lineDetailsTuple[0];
            address = (String) lineDetailsTuple[1];
            value = (String) lineDetailsTuple[2];

            // Is this cell an expression or literal?
            if (value.startsWith(savedExpressionParser.getSyntax()
                    .getAssignmentOperator()))
            {
                cellSet.addElement(cell);
                JeksExpression computedCell = computeExpressionValue(cell,
                        value);
                model.setValueAt(computedCell, cell.getRow(), cell.getColumn());

                // If the current top header is OUTPUT then the next expression
                // is the
                // output of the current function definition. Attempt to compile
                // the
                // function and reset the definition incase there are further
                // functions.
                if (currentTierOneHeader == getOutput())
                {
                    currentDefinition.setOutputCell(cell);
                    completeDefiniton();
                }
            
            } else
            {
                Object cellValue = getCellLiteral(value);
                model.setValueAt(cellValue, cell.getRow(), cell.getColumn());
                additionalValueProcessing(cell, address, value);
            }
        } catch (IndexOutOfBoundsException ex)
        {
        } // Should warn ?
    }

    protected void additionalValueProcessing(final JeksCell cell,
            final String address, final String value) throws IOException
    {
        int spaceIndex = value.indexOf(" ");
        if (spaceIndex == -1)
            throw new IOException(
                    functionSyntax
                            .getMessage(JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));

        String cellContents = value.substring(spaceIndex + 1);
        if (cellContents.charAt(0) == HEADER_CHAR)
        {
            updateHeaders(address, cellContents);
        } else
        {
            updateDefinition(cell, address, cellContents);
        }
    }

    protected void updateHeaders(final String cellAddress,
            final String cellContents) throws IOException
    {
        String header = cellContents.substring(1);
        if (CodecUtils.contains(TIER_ONE_HEADERS.class, header))
        {
            currentTierOneHeader = TIER_ONE_HEADERS.valueOf(header);
            columnHeaderMapping = new Hashtable<String, ITIER_TWO_HEADERS>();
        } else if (CodecUtils.contains(TIER_TWO_HEADERS.class, header))
        {
            currentTierTwoHeader = TIER_TWO_HEADERS.valueOf(header);
            columnHeaderMapping.put(
                    CodecUtils.getColumnFromString(cellAddress),
                    currentTierTwoHeader);
        } else
        {
            throw new IOException("Unrecognised header: " + header);
        }
        if(currentTierOneHeader.equals(TIER_ONE_HEADERS.DEFUN))
        { 
            try
            {
                String def = currentDefinition.getMockFunctionString();               
                recurInterim = expressionParser
                        .getFunctionParser().compileFunction(def);                

                expressionParser.addUserFunction(recurInterim);

            } catch (CompilationException ex)
            {
                // Do some logging here, we should let the sheet be opened
                // even if the function does not compile.
            }
        }
    }

    protected void updateDefinition(final JeksCell cell,
            final String cellAddress, final String value)
    {
        if (currentTierOneHeader == TIER_ONE_HEADERS.FUN_NAME)
        {
            currentDefinition.setName(value);
        } else if (currentTierOneHeader == TIER_ONE_HEADERS.ARGS)
        {
            if (columnHeaderMapping.get(CodecUtils
                    .getColumnFromString(cellAddress)) == TIER_TWO_HEADERS.NAME)
            {
                paramHolder.name = value;
                String col = CodecUtils.getColumnFromHeader(
                        columnHeaderMapping.entrySet(),
                        TIER_TWO_HEADERS.TEST_VALUE);
                if (col != null)
                {
                    String testAddress = col
                            + CodecUtils.getRowFromString(cellAddress);
                    paramHolder.testCell = savedExpressionSyntax
                            .getCellAt(testAddress);
                    currentDefinition.addParameter(new FunctionSheetParameter(
                            paramHolder.name, paramHolder.testCell));
                    paramHolder = new FunctionParamHolder();
                }
                /*
                 * if(paramHolder.isFullyDefined()) {
                 * currentDefinition.addParameter(new
                 * FunctionSheetParameter(paramHolder.name,
                 * paramHolder.testCell)); paramHolder = new
                 * FunctionParamHolder(); }
                 */
            }
            /*
             * else
             * if(columnHeaderMapping.get(CodecUtils.getColumnFromString(cellAddress
             * )) == TIER_TWO_HEADERS.TEST_VALUE) { paramHolder.testCell = cell;
             * if(paramHolder.isFullyDefined()) {
             * currentDefinition.addParameter(new
             * FunctionSheetParameter(paramHolder.name, paramHolder.testCell));
             * paramHolder = new FunctionParamHolder(); } }
             */
        }
    }

    protected void completeDefiniton()
    {
        try
        {
            // Compile the functions
            try
            {
                expressionParser.removeUserFunction(recurInterim);
            }
            catch(NullPointerException e)
            {
                
            }
            String functionAsString = currentDefinition.getFunctionString();            
            CompiledFunction compiledFunction = expressionParser
                    .getFunctionParser().compileFunction(functionAsString);
            compiledFunction.setTypeFunction(false);

            // Add them to the model and parser.
            ((FunctionSheetTableModel) this.model)
                    .addCompiledDefinition(compiledFunction);
            expressionParser.addUserFunction(compiledFunction);

        } catch (CompilationException ex)
        {
            // Do some logging here, we should let the sheet be opened
            // even if the function does not compile.
        } finally
        {
            currentDefinition = new FunctionSheetDefinition(model);
            ((FunctionSheetTableModel) this.model)
                    .addDefinition(currentDefinition);
            paramHolder = new FunctionParamHolder();
        }
    }
}
