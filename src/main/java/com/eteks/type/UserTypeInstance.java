package com.eteks.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.plaf.ListUI;
import javax.swing.table.TableModel;

import com.eteks.jeks.IllegalCellException;
import com.eteks.jeks.JeksTableModel;
import com.eteks.parser.CompiledFunction;
import com.eteks.type.UserType.ACCESS_MODIFIERS;
import com.eteks.type.UserType.FieldInfo;
import com.eteks.type.UserType.TypeClass;

public class UserTypeInstance
{    
    private final UserType definition;
    private LinkedHashMap<String, Object> values;

    public static class DefaultParam
    {
    }
    
    public UserTypeInstance(UserType definition, Object[] args)
    {
        this.definition = definition;
        this.values = new LinkedHashMap<String, Object>();
        int argsCount = args.length;
        setValues(args, !(definition.getParams().size() == argsCount));
    }

    private void setValues(Object[] args, boolean publicOnly)
    {
        int counter = 0;
        for (FieldInfo param : definition.getParams())
        {
            if (publicOnly && param.getAccess() == ACCESS_MODIFIERS.PRIVATE)
            {
                continue;
            }
            if(args[counter] instanceof DefaultParam)
            {
                values.put(param.getLabel(), param.getDefault());
            }
            else
            {
                values.put(param.getLabel(), args[counter]);
            }
            counter++;
        }
    }

    public String getTypeName()
    {
        return definition.getName();
    }

    public Object getValue(String key)
    {
        return values.get(key);
    }

    public UserType getDefiningType()
    {
        return definition;
    }
    
    public int getIndexOfKey(String key)
    {
        Iterator<String> keys = values.keySet().iterator();
        int counter = -1;
        while (keys.hasNext())
        {
            counter++;
            String curKey = keys.next();
            if (curKey.equals(key))
                break;
        }
        return counter;
    }

    public String toString()
    {   
        if(getDefiningType().implementsTypeClass(TypeClass.Show))
        {
            try
            {
                CompiledFunction showFunction = 
                        getDefiningType().getTypeClassFunction(TypeClass.Show);
            
                return showFunction.computeFunction(getDefiningType().getParser().getInterpreter(),
                                                    new Object[] { this }).toString();
            }
            catch(Exception e)
            {
                return "#Error in 'Show' function for type: " 
                       + getDefiningType().getName();
            }
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(definition.getName() + " - ");
            for (FieldInfo param : definition.getParams())
            {
                if (param.getAccess() == ACCESS_MODIFIERS.PRIVATE)
                {
                    continue;
                }
                sb.append(param.getLabel() + ": "
                        + values.get(param.getLabel()) + ", ");
            }
            return sb.toString().substring(0, sb.length() - 2);
        }
    }

    // TODO: Handle copying of fields better here. We shouldn't assume it is the
    // same
    // reference.

    /// TODO: Change from recursive to while-loop (Java doesn't support tail call 
    ///       optimization :(
    public Object deref(String sheet, ArrayList<String> fields)
    {        
        ArrayList<String> copyOfFields = (ArrayList<String>) fields.clone();
        try
        {
            String field = copyOfFields.get(0);
            if (definition.parameters.get(field).getAccess() == ACCESS_MODIFIERS.PRIVATE
                && !sheet.equals(definition.getName()))
            {
                throw new IllegalCellException(
                        "Attempt to access private field.");
            }
        } catch (IndexOutOfBoundsException e)
        {
            // Do Nothing
        }

        Object value = getValue(copyOfFields.get(0));
        if (value == null)
        {
            if (definition.parameters.containsKey(copyOfFields.get(0)))
            {
                throw new IllegalCellException("Field is not set");
            }
            throw new IllegalCellException("Field does not exist.");
        }

        copyOfFields.remove(0);
        if (copyOfFields.size() == 0)
            return value;
        if (value instanceof UserTypeInstance)
            return ((UserTypeInstance) value).deref(sheet, copyOfFields);
        else
            throw new IllegalCellException("Field does not exist.");
    }

}
