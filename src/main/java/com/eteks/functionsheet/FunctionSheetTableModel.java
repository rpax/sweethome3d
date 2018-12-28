package com.eteks.functionsheet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import com.eteks.jeks.JeksCell;
import com.eteks.jeks.JeksExpression;
import com.eteks.jeks.JeksTableModel;
import com.eteks.jeks.SharedState;
import com.eteks.parser.CompiledFunction;
import com.eteks.type.UserTypeInstance;

public class FunctionSheetTableModel extends JeksTableModel
{

    private HashSet<FunctionSheetDefinition> definitions;
    private HashSet<CompiledFunction> compiledDefinitons;

    public FunctionSheetTableModel(String name, SharedState state)
    {
        this(name, state, Short.MAX_VALUE, Short.MAX_VALUE);
    }

    public FunctionSheetTableModel(String name, SharedState state,
            int rowCount, int columnCount)
    {
        super(name, state, rowCount, columnCount);
        definitions = new HashSet<FunctionSheetDefinition>();
        compiledDefinitons = new HashSet<CompiledFunction>();
    }

    public void addDefinition(FunctionSheetDefinition def)
    {
        definitions.add(def);
    }

    public void addCompiledDefinition(CompiledFunction def)
    {
        compiledDefinitons.add(def);
    }

    public HashSet<FunctionSheetDefinition> getDefinitions()
    {
        return definitions;
    }

    public HashSet<CompiledFunction> getCompiledDefinitions()
    {
        return compiledDefinitons;
    }

    public void clearDefinitions()
    {
        definitions.clear();
        compiledDefinitons.clear();
    }

}
