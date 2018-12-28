package com.eteks.functionsheet;

import com.eteks.jeks.JeksCellRenderer;
import com.eteks.jeks.JeksExpression;
import com.eteks.jeks.JeksExpressionSyntax;
import com.eteks.parser.Interpreter;

public class FunctionSheetCellRenderer extends JeksCellRenderer
{

    private boolean testMode = false;

    public FunctionSheetCellRenderer(JeksExpressionSyntax syntax,
            Interpreter interpreter)
    {
        super(syntax, interpreter);
    }

    @Override
    public Object getExpressionValue(JeksExpression expression)
    {
        if(testMode)
        {
            Object val = super.getExpressionValue(expression);
            return expression.getAestheticDefinition();
        }
        else
        {
            return super.getExpressionValue(expression);
        }
//        if (testMode)
//        {
//            return expression.getDefinition();
//        } else
//        {
//            return value;
//        }
    }

    public boolean getTestMode()
    {
        return testMode;
    }

    public void setTestMode(boolean mode)
    {
        testMode = mode;
    }

}
