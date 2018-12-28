package com.eteks.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.eteks.jeks.JeksExpressionParser;
import com.eteks.parser.CompiledFunction;

import javax.swing.table.TableModel;

public class UserType
{

    private String name;
    private TableModel model;
    private Set<String> labels = new LinkedHashSet<String>();
    private ArrayList<String> defaults = new ArrayList<String>();
    private ArrayList<String> modifiers = new ArrayList<String>();
    private HashMap<TypeClass, CompiledFunction> implementedTypeClasses = new HashMap<TypeClass, CompiledFunction>();
    private JeksExpressionParser parser; 

    // TODO: Encapsulate
    public LinkedHashMap<String, FieldInfo> parameters = new LinkedHashMap<String, FieldInfo>();

    public enum TypeClass
    {
        Eq, Show, Ord
    }
    
    public enum ACCESS_MODIFIERS
    {
        PUBLIC, PRIVATE
    }

    public class FieldInfo
    {
        // TODO: Prevent setting the attributes twice.
        private String label;
        private ACCESS_MODIFIERS access = ACCESS_MODIFIERS.PUBLIC;
        private Object fieldDefault = null;

        public FieldInfo()
        {
        }

        public void addLabel(String label)
        {
            this.label = label;
        }

        public void addAccess(String access)
        {
            if (access.toUpperCase().equals("PRIVATE"))
            {
                this.access = ACCESS_MODIFIERS.PRIVATE;
            } else
            {
                this.access = ACCESS_MODIFIERS.PUBLIC;
            }
        }

        public void addAccess(ACCESS_MODIFIERS acess)
        {
            this.access = access;
        }

        public void addDefault(Object def)
        {
            this.fieldDefault = def;
        }

        public String getLabel()
        {
            return label;
        }

        public ACCESS_MODIFIERS getAccess()
        {
            return access;
        }

        public Object getDefault()
        {
            return fieldDefault;
        }

    }

    public UserType(String name, TableModel model, JeksExpressionParser parser)
    {
        this.name = name;
        this.model = model;
        this.parser = parser;
    }

    public String getName()
    {
        return name;
    }

    public TableModel getModel()
    {
        return model;
    }

    public Set<String> getLabels()
    {
        return labels;
    }

    public ArrayList<String> getDefaults()
    {
        return defaults;
    }

    public ArrayList<String> getModifers()
    {
        return modifiers;
    }
    
    public JeksExpressionParser getParser()
    {
        return this.parser;
    }

    public boolean addNewLabel(String label)
    {
        return labels.add(label);
    }

    public boolean addNewDefault(String def)
    {
        return defaults.add(def);
    }

    public void addParameter(FieldInfo param)
    {
        this.parameters.put(param.label, param);
    }

    public Collection<FieldInfo> getParams()
    {
        return Collections.unmodifiableCollection(parameters.values());
    }

    public boolean implementsTypeClass(final TypeClass typeClass)
    {
        return implementedTypeClasses.get(typeClass) == null ? false : true;
    }
    
    public CompiledFunction getTypeClassFunction(final TypeClass typeClass)
    {
        return implementedTypeClasses.get(typeClass);
    }
    
    public void addTypeClassImplementation(final TypeClass typeClass, final CompiledFunction function)
    {
        implementedTypeClasses.put(typeClass, function);
    }
    
    @Override
    public String toString()
    {
        return "USER-TYPE: " + name.toUpperCase();
    }

    public String getConstructorString()
    {
        boolean hasArgs = false;
        StringBuilder sb = new StringBuilder();
        sb.append(name + "(");

        for (FieldInfo param : parameters.values())
        {
            if (param.getAccess() == ACCESS_MODIFIERS.PUBLIC)
            {
                hasArgs = true;
                sb.append(param.getLabel() + ",");
            }
        }

        sb.replace(hasArgs ? sb.length() - 1 : sb.length(), sb.length(), ") = 0");
        return sb.toString();
    }

    public String getUnprotectedConstructorString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(name + "_unprotected(");

        for (FieldInfo param : parameters.values())
        {
            sb.append(param.getLabel() + ",");
        }
        sb.replace(sb.length() - 1, sb.length(), ") = 0");
        return sb.toString();
    }

    public boolean addNewAccessModifier(String value)
    {
        return modifiers.add(value);
    }
}
