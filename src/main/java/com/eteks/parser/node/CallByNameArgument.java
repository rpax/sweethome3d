package com.eteks.parser.node;

import com.eteks.parser.Interpreter;

public class CallByNameArgument
{
    private final Object[] closure;
    private final ExpressionNode expression;
    
    public CallByNameArgument(final Object[] closure, final ExpressionNode expression)
    {
        this.closure = closure;
        this.expression = expression;
    }
    
    public Object getValue(final Interpreter interpreter)
    {
        return expression.computeExpression(interpreter, closure);
    }
}
