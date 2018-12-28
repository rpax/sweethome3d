/*
 * @(#)JeksTableModel.java   05/02/99
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

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import java.util.Hashtable;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * A table model storing its values in a hashtable with keys of
 * <code>JeksCell</code> class. This allows to have very big tables with a lot
 * of <code>null</code> values.
 * 
 * @version 1.0
 * @author Emmanuel Puybaret
 * @since Jeks 1.0
 */
public class JeksTableModel extends AbstractTableModel
{
    private int rowCount;
    private int columnCount;
    private SharedState state;
    public String name;

    private Hashtable<JeksCell, Object> cellValues;

    /**
     * Creates a table model with <code>Short.MAX_VALUE</code> rows and columns.
     */
    public JeksTableModel(String name, SharedState state)
    {
        this(name, state, Short.MAX_VALUE, Short.MAX_VALUE);
    }

    /**
     * Creates a table model with <code>rowCount</code> rows and
     * <code>columnCount</code> columns.
     */
    public JeksTableModel(String name, SharedState state, int rowCount,
            int columnCount)
    {
        this.name = name;
        this.state = state;
        this.rowCount = rowCount;
        this.columnCount = columnCount;

        cellValues = new Hashtable<JeksCell, Object>();
    }

    public SharedState getState()
    {
        return state;
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public int getColumnCount()
    {
        return columnCount;
    }

    public Object getValueAt(int row, int column)
    {
        // row and column index are checked but storing in a Hashtable
        // won't cause real problems
        if (row >= getRowCount())
            throw new ArrayIndexOutOfBoundsException(row);
        if (column >= getColumnCount())
            throw new ArrayIndexOutOfBoundsException(column);
        return cellValues.get(new JeksCell(name, row, column));
    }

    public boolean isCellEditable(int row, int column)
    {
        return true;
    }

    public void setValueAt(Object value, int row, int column)
    {
        // row and column index are checked but storing in a Hashtable
        // won't cause real problems
        if (row >= getRowCount())
            throw new ArrayIndexOutOfBoundsException(row);
        if (column >= getColumnCount())
            throw new ArrayIndexOutOfBoundsException(column);
        JeksCell cell = new JeksCell(name, row, column);
        if (value == null || "".equals(value))
            cellValues.remove(cell);
        else
            cellValues.put(cell, value);

        fireTableChanged(new TableModelEvent(this, row, row, column));
    }
}
