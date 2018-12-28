/*
 * @(#)JeksFunctionParser.java   08/26/99
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

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import com.eteks.parser.CompilationException;
import com.eteks.parser.FunctionParser;
import com.eteks.parser.CompiledFunction;

/**
 * Parser for functions entered by user.
 * 
 * @version 1.0
 * @author Emmanuel Puybaret
 * @since Jeks 1.0
 */
public class JeksFunctionParser extends FunctionParser implements AestheticTransformationLayer
{
    private SharedState state;    
    
    /**
     * Creates a function parser using an instance
     * <code>JeksFunctionSyntax</code> as syntax.
     */
    public JeksFunctionParser(SharedState state)
    {
        this(new JeksFunctionSyntax(), state);
    }

    /**
     * Creates a function parser that uses the function syntax
     * <code>syntax</code> .
     */
    public JeksFunctionParser(JeksFunctionSyntax syntax, SharedState state)
    {
        super(syntax);
        this.state = state;
    }

    public CompiledFunction compileFunction(TableModel model, String functionDefinition)
            throws CompilationException
    {
        String transformedDefinition = toImplementationSyntax(model, functionDefinition);
        CompiledFunction function = super.compileFunction(transformedDefinition);
        // TODO : Should forbid user functions that use a common function name
        // in
        // different Locales (like SUM, SOMME)
        return function;
    }

    @Override
    public String toImplementationSyntax(TableModel model, String definition)
    {
        String newDefinition = definition;        
        StringTokenizer tokens = new StringTokenizer(newDefinition,
                getSyntax().getDelimiters());
        
        String token;
        String cellID = null;
        String fields = null;
        
        String modelName = model instanceof JeksTableModel ? ((JeksTableModel) model).name : "";
        
        while (tokens.hasMoreTokens())
        {
            token = tokens.nextToken();            
            Pattern pattern = Pattern.compile("(([^!]+)!)?([^!]+)!(.+)");
            Matcher matcher = pattern.matcher(token);
            if(matcher.matches())
            {      
                String match1 = matcher.group(2);
                String match2 = matcher.group(3);
                String match3 = matcher.group(4);
                
                if(match1 != null && state.getModel(match1) != null)
                {
                    cellID = match1 + "!"  + match2;
                    fields = match3;
                }
                else if(match1 != null)
                {
                    cellID = match1;
                    fields = match2 + "!"  + match3;
                }
                else if(state.getModel(match2) == null)
                {
                    cellID = match2;
                    fields = match3;
                }
                if(cellID != null)
                {
                    try
                    {
                        newDefinition = newDefinition.replace(token, "DEREF(\"" + modelName + "\","
                                + cellID + ",\"" + fields + "\")");
                    } catch (IndexOutOfBoundsException e)
                    {
                        // throw new CompilationException();
                    }
                }
            }
            cellID = null;
            fields = null;
        }
        return newDefinition;
    }

    @Override
    public String toAestheticSyntax(String definition)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
