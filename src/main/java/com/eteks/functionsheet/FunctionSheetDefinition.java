package com.eteks.functionsheet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.table.TableModel;

import com.eteks.jeks.JeksCell;
import com.eteks.jeks.JeksExpression;

public class FunctionSheetDefinition
{

    private String name;
    private TableModel parentModel;
    private LinkedHashMap<String, FunctionSheetParameter> parameters;
    private JeksCell output;

    public FunctionSheetDefinition(final TableModel model)
    {
        this.parentModel = model;
        this.parameters = new LinkedHashMap<String, FunctionSheetParameter>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void addParameter(final FunctionSheetParameter parameter)
    {
        parameters.put(parameter.getName(), parameter);
    }

    public JeksCell getParameterTestCell(final String parameter)
    {
        return parameters.get(parameter).getTestCell();
    }

    public boolean isParameter(final String parameterName)
    {
        return parameters.get(parameterName) != null;
    }

    public void resetParameters()
    {
        parameters = new LinkedHashMap<String, FunctionSheetParameter>();
    }

    public void setOutputCell(final JeksCell output)
    {
        this.output = output;
    }

    private String expandOutputCell()
    {
        JeksExpression outputExpression = (JeksExpression) parentModel
                .getValueAt(output.getRow(), output.getColumn());
        return "= " + expandExpression(outputExpression);
    }

    private String expandExpression(final JeksExpression expression)
    {
        Hashtable params = expression.getParameters();
        Enumeration keys = params.keys();
        String expressionDef = expression.getDefinition().substring(1);

        while (keys.hasMoreElements())
        {
            Object param = keys.nextElement();
            Object paramVal = params.get(param);
            String paramS = (String) param;
            if (parameters.get((String) param) != null)
            {

            } else if (paramVal instanceof JeksCell)
            {
                Object targetVal = parentModel.getValueAt(
                        ((JeksCell) paramVal).getRow(),
                        ((JeksCell) paramVal).getColumn());
                expressionDef = expressionDef.replace((String) param, "("
                        + expandObject(targetVal) + ")");
            }
        }
        return expressionDef;
    }

    private String expandObject(final Object obj)
    {
        if (obj instanceof JeksExpression)
        {
            return expandExpression((JeksExpression) obj);        
        }
        else
        {            
            return obj.toString();
        }
    }

    public String getFunctionString()
    {
        String functionSig = name + "(";
        Set<String> params = parameters.keySet();
        for (String param : params)
        {
            functionSig += (param + ",");
        }
        functionSig = functionSig.substring(0, functionSig.length() - 1);
        functionSig += ") ";
        return functionSig + expandOutputCell();
    }
    
    public String getMockFunctionString()
    {
        String functionSig = name + "(";
        Set<String> params = parameters.keySet();
        for (String param : params)
        {
            functionSig += (param + ",");
        }
        functionSig = functionSig.substring(0, functionSig.length() - 1);
        return functionSig += ") = 0";
    }

}
