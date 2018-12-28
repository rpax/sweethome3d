/*
 * @(#)JeksInterpreter.java   05/02/99
 *
 * Copyright (c) 1998-2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Visit eTeks web site for up-to-date versions of this file and other
 * Java tools and tutorials : http://www.eteks.com/
 */
package com.eteks.jeks;

import com.eteks.parser.WrapperInterpreter;
import com.eteks.type.UserType;
import com.eteks.type.UserTypeInstance;

import java.util.Date;

/**
 * Interpreter used to compute cell values. This interpreter interprets
 * <code>null</code> cell values as 0.
 * 
 * @version 1.0
 * @author Emmanuel Puybaret
 * @since Jeks 1.0
 */
public class JeksInterpreter extends WrapperInterpreter
{
    public static final Object NULL_VALUE = new Long(0);
    public SharedState state;

    /**
     * Creates an interpreter.
     */
    public JeksInterpreter(SharedState state)
    {
        super(true, false, false);
        this.state = state;
    }

    /**
     * Returns <code>true</code> if <code>param</code> is an instance of
     * <code>Number</code> different of 0, or if <code>param</code> is a true
     * <code>Boolean</code> object. If <code>param</code> is <code>null</code>,
     * this method returns <code>false</code>.
     */
    public boolean isTrue(Object param)
    {
        if (param instanceof Number)
            return ((Number) param).doubleValue() != 0.;
        else if (param instanceof Boolean)
            return ((Boolean) param).booleanValue();
        else if (param == null)
            return false;
        else
            throw new IllegalArgumentException();
    }

    /**
     * If <code>param</code> is null then convert to the Jeks defined NULL value;
     * @param param
     * @return <code>param</code> if <code>param</code> was non-null, otherwise
     *         <code>JeksInterpreter.NULL_VALUE</code>.
     */
    private Object convertIfNull(Object param)
    {
        return param == null ? NULL_VALUE : param;
    }

    /**
     * Returns <code>parameter</code> if it's <code>null</code> an instance of
     * <code>Number</code>, <code>String</code>, <code>Character</code>,
     * <code>Boolean</code>, <code>Date</code> or <code>Object [][]</code>.
     * 
     * @param parameter
     *            the computed parameter to evaluate.
     * @exception IllegalArgumentException
     *                if <code>parameter</code> class isn't allowed.
     */
    public Object getParameterValue(Object parameter)
    {
        if (parameter == null || parameter instanceof Number
                || parameter instanceof String
                || parameter instanceof Character
                || parameter instanceof Boolean || parameter instanceof Date
                || parameter instanceof Object[][]
                || parameter instanceof UserTypeInstance)
            return parameter;
        else
            throw new IllegalArgumentException(
                    "Parameter "
                    + parameter + " of type " + parameter.getClass()
                    + " not an instance of Number, String, Character, Boolean or Date");
    }

    public Object getUnaryOperatorValue(Object unaryOperatorKey, Object param)
    {
        // Only functions may take a cell set as parameter
        if (param instanceof JeksCellSet)
            throw new IllegalArgumentException();
        else
            return super.getUnaryOperatorValue(unaryOperatorKey,
                    convertIfNull(param));
    }

    public Object getBinaryOperatorValue(Object binaryOperatorKey,
            Object param1, Object param2)
    {
        // Only functions may take a cell set as parameter
        if (param1 instanceof JeksCellSet || param2 instanceof JeksCellSet)
        {
            throw new IllegalArgumentException();
        }
        else
        {
            return super.getBinaryOperatorValue(binaryOperatorKey,
                    convertIfNull(param1), convertIfNull(param2));
        }
    }

    public Object getCommonFunctionValue(Object commonFunctionKey, Object param)
    {
        // Only functions may take a cell set as parameter
        if (param instanceof JeksCellSet)
            throw new IllegalArgumentException();
        else
            return super.getCommonFunctionValue(commonFunctionKey,
                    convertIfNull(param));
    }
}
