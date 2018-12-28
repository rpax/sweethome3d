package com.eteks.jeks;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import com.eteks.type.UserType;

public enum SharedState
{
    
    INSTANCE;    
    
    private final Hashtable<String, TableModel> sharedModels = new Hashtable<String, TableModel>();
    private final Hashtable<String, UserType> userTypes = new Hashtable<String, UserType>();
    private final Hashtable<String, HashSet<JeksCell>> typeInstances = new Hashtable<String, HashSet<JeksCell>>();
    private final Hashtable<TableModel, ReferringCellsListener> typeInstancesListeners = new Hashtable<TableModel, ReferringCellsListener>();

    public void addModel(final String name, final TableModel model)
    {
        sharedModels.put(name, model);
    }

    public TableModel getModel(final String name)
    {
        return sharedModels.get(name);
    }

    public TableModel getModel(final JeksCell cell)
    {
        return sharedModels.get(cell.getSheet());
    }

    public void addType(final String name, final UserType type)
    {
        userTypes.put(name, type);
    }

    public UserType getType(final String name)
    {
        return userTypes.get(name);
    }

    //TODO: MOVE THIS
    public String getTypeConstructor(final String expression)
    {
        Pattern p = Pattern.compile("=([a-zA-Z]+)(.*)$");
        Matcher matches = p.matcher(expression);
        try
        {
            matches.matches();
            if (matches.groupCount() > 1)
            {
                String type = matches.group(1);
                return userTypes.get(type) == null ? null : type;
            }
        } catch (Exception e)
        {
            return null;
        }
        return null;
    }

    public Set<JeksCell> getTypeInstances(final String type)
    {
        return Collections.unmodifiableSet(typeInstances.get(type));
    }

    public synchronized void addTypeInstanceRef(final ReferringCellsListener listener,
                                                final String type, 
                                                final JeksCell cell)
    {
        if (typeInstances.get(type) == null)
        {
            typeInstances.put(type, new HashSet<JeksCell>());
        }
        typeInstances.get(type).add(cell);
    }

    public synchronized void removeTypeInstanceRef(final String type, final JeksCell cell)
    {
        if (typeInstances.get(type) != null)
        {
            typeInstances.get(type).remove(cell);
        }
    }

    public synchronized void removeTypeInstance(final JeksCell cell)
    {
        Enumeration<String> keys = typeInstances.keys();
        while (keys.hasMoreElements())
        {
            typeInstances.get(keys.nextElement()).remove(cell);
        }
    }
}
