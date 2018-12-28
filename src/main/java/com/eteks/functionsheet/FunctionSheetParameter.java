package com.eteks.functionsheet;

import com.eteks.jeks.JeksCell;

public class FunctionSheetParameter
{

    private final String name;
    private final JeksCell testCell;

    public FunctionSheetParameter(final String name, final JeksCell testCell)
    {
        this.name = name;
        this.testCell = testCell;        
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return name;
    }

    public JeksCell getTestCell()
    {
        return testCell;
    }
}
