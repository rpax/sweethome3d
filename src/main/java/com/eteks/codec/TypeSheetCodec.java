package com.eteks.codec;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.swing.JTable;

import com.eteks.codec.FunctionSheetCodec.FunctionParamHolder;
import com.eteks.codec.FunctionSheetCodec.TIER_ONE_HEADERS;
import com.eteks.codec.FunctionSheetCodec.TIER_TWO_HEADERS;
import com.eteks.functionsheet.FunctionSheetDefinition;
import com.eteks.functionsheet.FunctionSheetParameter;
import com.eteks.functionsheet.FunctionSheetTableModel;
import com.eteks.jeks.JeksTable.MODE;
import com.eteks.jeks.JeksCell;
import com.eteks.jeks.JeksTable;
import com.eteks.jeks.SharedState;
import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledFunction;
import com.eteks.type.UserType;
import com.eteks.type.UserType.FieldInfo;

public class TypeSheetCodec extends FunctionSheetCodec
{
    protected MODE sheetMode = MODE.TYPESHEET;
    protected static final ITIER_ONE_HEADERS output = TIER_ONE_HEADERS.OUTPUT;
    protected UserType type;
    protected FieldInfo parameterInfo;
    
    private boolean typeDefined = false;

    protected enum TIER_ONE_HEADERS implements ITIER_ONE_HEADERS
    {
        FIELDS, FUN_NAME, ARGS, DEFUN, OUTPUT
    }

    protected enum TIER_TWO_HEADERS implements ITIER_TWO_HEADERS
    {
        LABELS, DEFAULT, ACCESS, NAME, TEST_VALUE
    }

    public TypeSheetCodec(final JTable table, final SharedState state)
    {
        super(table, state);
    }

    public TypeSheetCodec(String fileName, SharedState state)
    {
        super(fileName, state);
        type = new UserType(CodecUtils.removeExtension(fileName), model, expressionParser);
        parameterInfo = type.new FieldInfo();
    }

    @Override
    protected JeksTable createTable()
    {
        return new JeksTable(model, expressionParser, true, state, fileName,
                sheetMode);
    }

    @Override
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

        type = new UserType(CodecUtils.removeExtension(fileName), model, expressionParser);
        parameterInfo = type.new FieldInfo();
        decode(in);
    }

    @Override
    protected void updateHeaders(final String cellAddress,
            final String cellContents) throws IOException
    {
        String header = cellContents.substring(1);
        if (CodecUtils.contains(TIER_ONE_HEADERS.class, header))
        {
            currentTierOneHeader = TIER_ONE_HEADERS.valueOf(header);
            columnHeaderMapping = new Hashtable<String, ITIER_TWO_HEADERS>();
        }
        else if (CodecUtils.contains(TIER_TWO_HEADERS.class, header))
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

    @Override
    protected void updateDefinition(final JeksCell cell,
            final String cellAddress, final String value)
    {
        if (currentTierOneHeader == TIER_ONE_HEADERS.FIELDS)
        {
            if (columnHeaderMapping.get(CodecUtils
                    .getColumnFromString(cellAddress)) == TIER_TWO_HEADERS.LABELS)
            {
                if (parameterInfo.getLabel() != null)
                {
                    type.addParameter(parameterInfo);
                    parameterInfo = type.new FieldInfo();
                }
                parameterInfo.addLabel(value);
            } else if (columnHeaderMapping.get(CodecUtils
                    .getColumnFromString(cellAddress)) == TIER_TWO_HEADERS.DEFAULT)
            {
                
                parameterInfo.addDefault(model.getValueAt(cell.getRow(), cell.getColumn()));
            } else if (columnHeaderMapping.get(CodecUtils
                    .getColumnFromString(cellAddress)) == TIER_TWO_HEADERS.ACCESS)
            {
                parameterInfo.addAccess(value);
            }

        } else if (currentTierOneHeader == TIER_ONE_HEADERS.FUN_NAME)
        {
            currentDefinition.setName(value);
            completeTypeDefinition();
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
            }
        }
    }

    @Override
    protected void decodeComplete()
    {
        completeTypeDefinition();
        super.decodeComplete();
    }
    
    @Override
    protected void completeDefiniton()
    {        
        try
        {
            try
            {
                expressionParser.removeUserFunction(recurInterim);
            }
            catch(NullPointerException e)
            {
                
            }
            String functionName = currentDefinition.getName();
            UserType.TypeClass typeClass = UserType.TypeClass.valueOf(functionName);
            String functionAsString = currentDefinition.getFunctionString();
            CompiledFunction compiledFunction = expressionParser
                    .getFunctionParser().compileFunction(functionAsString);
            type.addTypeClassImplementation(typeClass, compiledFunction);
            currentDefinition = new FunctionSheetDefinition(model);
            ((FunctionSheetTableModel) this.model)
                    .addDefinition(currentDefinition);
            paramHolder = new FunctionParamHolder();
        }
        catch(IllegalArgumentException e)
        {
            /*
             * If there is no TypeClass with this function
             * name, attempt to compile it like a normal
             * user defined function.
             */
            super.completeDefiniton();
        }
        catch (Exception e)
        {
            /*
             * If the compilation fails do nothing and
             * proceed.
             */
        }
        
    }
    
    protected void completeTypeDefinition()
    {
        if(typeDefined)
        {
            return;
        }
        typeDefined = true;
        
        if (parameterInfo.getLabel() != null)
        {
            type.addParameter(parameterInfo);
        }
        state.addType(fileName, type);
        String constructorString = type.getConstructorString();
        String unprotectedConstructorString = type
                .getUnprotectedConstructorString();
        try
        {
            if (!SAVED_LOCALE.equals(functionSyntax.getLocale()))
            {
                // The first compilation ensures that the read function is
                // correctly
                // written
                CompiledFunction compiledFunction = savedFunctionParser
                        .compileFunction(constructorString);
            }
            CompiledFunction compiledFunction = expressionParser
                    .getFunctionParser().compileFunction(constructorString);
            compiledFunction.setTypeFunction(true);
            savedExpressionParser.removeUserFunction(compiledFunction);
            expressionParser.addUserFunction(compiledFunction);
        } catch (CompilationException ex)
        {

        }
        try
        {
            if (!SAVED_LOCALE.equals(functionSyntax.getLocale()))
            {
                // The first compilation ensures that the read function is
                // correctly
                // written
                CompiledFunction compiledFunction = savedFunctionParser
                        .compileFunction(unprotectedConstructorString);
            }
            CompiledFunction compiledFunction = expressionParser
                    .getFunctionParser().compileFunction(
                            unprotectedConstructorString);
            compiledFunction.setTypeFunction(true);
            savedExpressionParser.removeUserFunction(compiledFunction);
            expressionParser.addUserFunction(compiledFunction);
        } catch (CompilationException ex)
        {

        }
    }
}
