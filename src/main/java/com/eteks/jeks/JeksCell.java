/*
 * @(#)JeksCell.java   05/02/99
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

/**
 * Cell of a table. This class stores the row and the column of a cell.
 * 
 * @version 1.0
 * @author Emmanuel Puybaret
 * @since Jeks 1.0
 */
public class JeksCell
{
    private final String sheet;
    private final int row;
    private final int column;

    /**
     * Constructs a cell at coordinates (<code>row</code>,<code>column</code>).
     * 
     * @param row
     * @param column
     */
    public JeksCell(final int row, final int column)
    {
        this.sheet = null;
        this.row = row;
        this.column = column;
    }

    public JeksCell(final String sheet,
                    final int row, 
                    final int column)
    {
        this.sheet = sheet;
        this.row = row;
        this.column = column;
    }

    public String getSheet()
    {
        return sheet;
    }

    /**
     * Returns the row index of this table cell.
     * 
     * @return The row index.
     */
    public int getRow()
    {
        return row;
    }

    /**
     * Returns the column index of this table cell.
     * 
     * @return The column index.
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * Returns <code>true</code> if <code>object</code> represents the same cell
     * as this cell.
     * 
     * @param object
     *            an object.
     * @return <code>true</code> if this cell and <code>object</code> are equal.
     */
    @Override
    public boolean equals(final Object object)
    {
        if(object instanceof JeksCell)
        {
           JeksCell cell = (JeksCell) object;
           if(sheet == null)
           {
               return row == cell.row && column == cell.column && cell.sheet == null;
           }
           else
           {
               return row == cell.row && column == cell.column && sheet.equals(cell.sheet);
           }           
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        result = prime * result + ((sheet == null) ? 0 : sheet.hashCode());
        return result;
    }

    /**
     * Returns a string representation of this cell (row column).
     */
    @Override
    public String toString()
    {
        String sheetStr = sheet == null ? "" : sheet; 
        return sheetStr + "[" + row + "][" + column + "]";
    }
}
