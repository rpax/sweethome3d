package com.eteks.type;

import com.eteks.jeks.JeksCell;
import com.eteks.jeks.JeksInterpreter;
import com.eteks.jeks.SharedState;
import com.eteks.parser.Syntax;
import com.eteks.parser.CompiledFunction;

public class TypeIntepreter extends JeksInterpreter
{
    
    @SuppressWarnings("serial")
    public static class UnimplementedTypeClassException extends IllegalArgumentException
    {

        public UnimplementedTypeClassException(String string)
        {
            super(string);
        }        
    }
    
    @SuppressWarnings("serial")
    public static class UnsupportedOperatorException extends IllegalArgumentException
    {
        public UnsupportedOperatorException(String string)
        {
            super(string);
        }       
    }
    
    public TypeIntepreter(SharedState state)
    {
        super(state);
    }
    
    private static UserType.TypeClass getRequiredTypeClass(final Object binaryOperatorKey)    
    {
        if(binaryOperatorKey.equals(Syntax.OPERATOR_EQUAL))
        {
            return UserType.TypeClass.Eq;
        }
        else if(binaryOperatorKey.equals(Syntax.OPERATOR_GREATER) ||
                binaryOperatorKey.equals(Syntax.OPERATOR_GREATER_OR_EQUAL) ||
                binaryOperatorKey.equals(Syntax.OPERATOR_LESS) ||
                binaryOperatorKey.equals(Syntax.OPERATOR_LESS_OR_EQUAL))
        {
            return UserType.TypeClass.Ord;                    
        }
        else
        {
            throw new UnsupportedOperatorException("Binary operator not supported for user defined types."
                    + " Operator code:" + binaryOperatorKey);
        }
       
    }
    
    private static Object processFunctionResults(final Object binaryOperatorKey,
                                                 final Object functionResult)
    {
        if (binaryOperatorKey.equals(Syntax.OPERATOR_GREATER))
        {
            return ((Number) functionResult).intValue() == 1 ? Boolean.TRUE
                    : Boolean.FALSE;
        } else if (binaryOperatorKey.equals(Syntax.OPERATOR_GREATER_OR_EQUAL))
        {
            return ((Number) functionResult).intValue() == -1 ? Boolean.FALSE
                    : Boolean.TRUE;
        } else if (binaryOperatorKey.equals(Syntax.OPERATOR_LESS))
        {
            return ((Number) functionResult).intValue() == -1 ? Boolean.TRUE
                    : Boolean.FALSE;
        } else if (binaryOperatorKey.equals(Syntax.OPERATOR_LESS_OR_EQUAL))
        {
            return ((Number) functionResult).intValue() == 1 ? Boolean.FALSE
                    : Boolean.TRUE;
        } else
        {
            return functionResult;
        }
    }
    
    @Override
    public Object getLiteralValue(Object literal)
    {
        if(literal instanceof UserTypeInstance.DefaultParam)
        {
            return literal;
        }
        return super.getLiteralValue(literal);
    }
    
    @Override
    public Object getBinaryOperatorValue(final Object binaryOperatorKey,
            Object param1, Object param2)
    {
        if (param1 instanceof UserTypeInstance &&
            param2 instanceof UserTypeInstance)
        {
            return getBinaryOperatorValue(binaryOperatorKey,
                                          (UserTypeInstance) param1,
                                          (UserTypeInstance) param2);
        }
        else if ((param1 instanceof UserTypeInstance
                || param2 instanceof UserTypeInstance) &&
                (binaryOperatorKey.equals(Syntax.OPERATOR_GREATER) ||
                binaryOperatorKey.equals(Syntax.OPERATOR_GREATER_OR_EQUAL) ||
                binaryOperatorKey.equals(Syntax.OPERATOR_LESS) ||
                binaryOperatorKey.equals(Syntax.OPERATOR_LESS_OR_EQUAL)))
        {
            return false;
        }
        return super.getBinaryOperatorValue(binaryOperatorKey, param1, param2);            
    }
    
    private Object getBinaryOperatorValue(final Object binaryOperatorKey,
            final UserTypeInstance type1, final UserTypeInstance type2)
    {        
        if (!type1.getDefiningType().equals(type2.getDefiningType()))
        {
            //TODO: Currently the types must be equal, if we add inheritance this should change.
            return false;
        }
        UserType.TypeClass typeClass = getRequiredTypeClass(binaryOperatorKey);
        if(!type1.getDefiningType().implementsTypeClass(typeClass))
        {
            throw new UnimplementedTypeClassException("This user type has not implemented type"
                    + " class " + typeClass);
        }
        CompiledFunction function = type1.getDefiningType()
                .getTypeClassFunction(typeClass);
        Object[] params = new Object[] { type1, type2 };
        Object result = function.computeFunction(this, params);
        return processFunctionResults(binaryOperatorKey, result);
    }      
}
