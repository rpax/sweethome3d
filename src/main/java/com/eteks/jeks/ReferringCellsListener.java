/*
 * @(#)ReferringCellsListener.java   05/02/99
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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import com.eteks.type.UserTypeInstance;

/**
 * This class records the set of the links between the computed cells of a
 * table. The <code>geAlltReferringCells ()</code> method allows to list all the
 * cells which use a cell x in their formula, in the goal to optimize table
 * updates, when x is modified. Linked to a <code>TableModel</code> instance, it
 * listens to the changes of a table to update automatically all the computed
 * cells referring to a cell.
 * 
 * @version 1.0
 * @author Emmanuel Puybaret
 * @since Jeks 1.0
 */
public class ReferringCellsListener implements TableModelListener
{
    private Hashtable<JeksCell, Vector<JeksCell>> cellSet = new Hashtable<JeksCell, Vector<JeksCell>>();
    private Hashtable computedCellSet = new Hashtable();

    /**
     * Adds a referring link between cells <code>cell</code> and all the cells
     * of the parameters of <code>expression</code> in the formula of
     * <code>cell</code>.
     * 
     * @param cell
     *            a cell.
     * @param expression
     *            an expression using a set of cells as parameters in its
     *            formula.
     */
    public void addParametersReferringToCell(JeksCell cell,
            JeksExpression expression)
    {
        if (expression.getParameterCount() > 0)
            for (Enumeration e = expression.getParameters().elements(); e
                    .hasMoreElements();)
            {
                Object cellOrSet = e.nextElement();
                if (!cellOrSet.equals(IllegalCellException.class))
                    if (cellOrSet instanceof JeksCell)
                    {
                        JeksCell paramCell = (JeksCell) cellOrSet;
                        addReferringCell(cell, new JeksCell(paramCell.getSheet() == null ? cell.getSheet() : paramCell.getSheet(),
                                                            paramCell.getRow(),
                                                            paramCell.getColumn()));
                    }
                    else
                    {
                        addReferringCell(cell, (JeksCellSet) cellOrSet);
                    }
            }
    }

    /**
     * Adds a referring link between cells <code>cell</code> and
     * <code>referringCell</code>.
     * 
     * @param cell
     *            a cell.
     * @param referringCell
     *            a cell used in the formula of <code>cell</code>.
     */
    private void addReferringCell(JeksCell cell, JeksCell referringCell)
    {
        Vector<JeksCell> referringCells = cellSet.get(referringCell);
        if (referringCells == null)
            cellSet.put(referringCell, referringCells = new Vector(1));
        if (!referringCells.contains(cell))
            referringCells.addElement(cell);
    }

    /**
     * Adds a referring link between cells <code>cell</code> and all the cells
     * of <code>referringSet</code> set.
     * 
     * @param cell
     *            a cell.
     * @param referringSet
     *            a set of cells used in the formula of <code>cell</code>.
     */
    private void addReferringCell(JeksCell cell, JeksCellSet referringSet)
    {
        String sheet = referringSet.getSheet() == null ? cell.getSheet() : referringSet.getSheet();
        for (int row = referringSet.getFirstRow(); row <= referringSet
                .getLastRow(); row++)
            for (int column = referringSet.getFirstColumn(); column <= referringSet
                    .getLastColumn(); column++)
                addReferringCell(cell, new JeksCell(sheet, row, column));
    }

    /**
     * Removes all referring link to <code>cell</code>.
     * 
     * @param cell
     *            a cell.
     */
    public void removeReferringCells(JeksCell cell)
    {
        for (Enumeration<JeksCell> enumer = cellSet.keys(); enumer.hasMoreElements();)
        {
            JeksCell cellKey = enumer.nextElement();
            Vector<JeksCell> referringCells = cellSet.get(cellKey);
            for (int i = 0; i < referringCells.size();)
                if (referringCells.elementAt(i).equals(cell))
                    referringCells.removeElement(cell);
                else
                    i++;

            if (referringCells.size() == 0)
                cellSet.remove(cell);
        }
    }

    /**
     * Removes all referring link between cells <code>cell</code> and all the
     * cells of the parameters of <code>expression</code> in the formula of
     * <code>cell</code>.
     * 
     * @param cell
     *            a cell.
     * @param expression
     *            an expression using a set of cells as parameters in its
     *            formula.
     */
    public void removeParametersReferringToCell(JeksCell cell,
            JeksExpression expression)
    {
        if (expression.getParameterCount() > 0)
            for (Enumeration e = expression.getParameters().elements(); e
                    .hasMoreElements();)
            {
                Object cellOrSet = e.nextElement();
                if (!cellOrSet.equals(IllegalCellException.class))
                    if (cellOrSet instanceof JeksCell)
                        removeParameterReferringToCell(cell,
                                (JeksCell) cellOrSet);
                    else
                        removeParameterReferringToCell(cell,
                                (JeksCellSet) cellOrSet);
            }
    }

    /**
     * Removes a referring link between cells <code>cell</code> and
     * <code>parameter</code>.
     * 
     * @param cell
     *            a cell.
     * @param parameter
     *            a cell used in the formula of <code>cell</code>.
     */
    private void removeParameterReferringToCell(JeksCell cell,
            JeksCell parameter)
    {
        Vector<JeksCell> referringCells = cellSet.get(parameter);
        if (referringCells != null)
        {
            referringCells.removeElement(cell);
            if (referringCells.size() == 0)
                cellSet.remove(parameter);
        }
    }

    /**
     * Removes a referring link between cells <code>cell</code> and all the
     * cells of <code>referringSet</code> set.
     * 
     * @param cell
     *            a cell.
     * @param referringSet
     *            a set of cells used in the formula of <code>cell</code>.
     */
    private void removeParameterReferringToCell(JeksCell cell,
            JeksCellSet referringSet)
    {
        String sheet = referringSet.getSheet() == null ? cell.getSheet() : referringSet.getSheet();
        for (int row = referringSet.getFirstRow(); row <= referringSet
                .getLastRow(); row++)
            for (int column = referringSet.getFirstColumn(); column <= referringSet
                    .getLastColumn(); column++)
                removeParameterReferringToCell(cell, new JeksCell(sheet, row, column));
    }

    /**
     * Returns the list of all the cells referring <code>cell</code> as a
     * parameter in their formula directly.
     * 
     * @param cell
     *            a cell.
     */
    public Vector getReferringCells(JeksCell cell)
    {
        // TODO use something faster than a Vector with access that doesn't
        // oblige
        // to create
        // a new JeksCell instance
        return (Vector) cellSet.get(cell);
    }

    /**
     * Returns the full list of all the cells referring <code>cell</code> as a
     * parameter in their formula directly or indirectly. This method is mostly
     * used to get all the cells to update after a change of <code>cell</code>.
     * 
     * @param cell
     *            a cell.
     */
    public Vector<JeksCell> getAllReferringCells(JeksCell cell)
    {
        Vector<JeksCell> referringCells = cellSet.get(cell);
        Vector<JeksCell> cellsToUpdate = null;
        if (referringCells != null)
        {
            cellsToUpdate = new Vector<JeksCell>(
                    computedCellSet.size() < 100 ? computedCellSet.size() / 2
                            : 50);
            getAllReferringCells(cell, cellsToUpdate, referringCells);
        }
        return cellsToUpdate;
    }

    /**
     * This method allows recursive calls to retrieve in
     * <code>cellsToUpdate</code> the full list of referring cells of
     * <code>referringCells</code> set.
     * 
     * @param startCell
     * @param cellsToUpdate
     * @param referringCells
     */
    private void getAllReferringCells(JeksCell startCell, Vector<JeksCell> cellsToUpdate,
            Vector<JeksCell> referringCells)
    {
        for (int i = 0; i < referringCells.size(); i++)
        {
            JeksCell referringCell = referringCells.elementAt(i);
            // If referringCell is the same as startCell, stop seeking other
            // cells
            if (startCell.equals(referringCell))
                return;

            int index = cellsToUpdate.indexOf(referringCell);
            if (index >= 0)
            {
                // If referringCell is already in the list, put it at the end
                if (index != cellsToUpdate.size() - 1)
                {
                    cellsToUpdate.removeElementAt(index);
                    cellsToUpdate.addElement(referringCell);
                }
            } else
            {
                cellsToUpdate.addElement(referringCell);

                // Add to the list all the referring cells of startCell
                Vector<JeksCell> subReferringCells = cellSet.get(referringCell);
                if (subReferringCells != null)
                    getAllReferringCells(startCell, cellsToUpdate,
                            subReferringCells);
            }
        }
    }

    /**
     * <code>TableModelListener</code> implementation
     */
    public void tableChanged(final TableModelEvent event)
    {
        if (event.getType() == TableModelEvent.UPDATE)
            // Launch the update of the table in a different thread to free AWT
            // thread
            new Thread()
            {
                public void run()
                {
                    // Synchronize to ensure one update at a time
                    synchronized (ReferringCellsListener.this)
                    {
                        TableModel model = (TableModel) event.getSource();

                        // Remove this listener from the model to avoid useless
                        // callbacks
                        // and circularity problems
                        model.removeTableModelListener(ReferringCellsListener.this);

                        tableUpdated(model, event.getFirstRow(),
                                event.getLastRow(), event.getColumn());

                        // Add this listener to the model to get callbacks
                        model.addTableModelListener(ReferringCellsListener.this);
                    }
                }
                // TODO }.start ();
            }.run();

        // TODO need to process INSERT and DELETE events ???
    }

    private void tableUpdated(TableModel model, int firstRow, int lastRow,
            int column)
    {

        String name = ((JeksTableModel) model).name;
        if (firstRow == lastRow)
        {
            Object val = model.getValueAt(firstRow, column);
            ((JeksTableModel) model).getState().removeTypeInstance(
                    new JeksCell(name, firstRow, column));
            if (val instanceof JeksExpression)
            {
                String typeName = ((JeksTableModel) model).getState()
                        .getTypeConstructor(
                                ((JeksExpression) val).getDefinition());
                if (typeName != null)
                {
                    ((JeksTableModel) model).getState().addTypeInstanceRef(
                            this, typeName,
                            new JeksCell(name, firstRow, column));
                }
            }
        }

        for (int row = firstRow; row <= lastRow; row++)
            if (row != TableModelEvent.HEADER_ROW)
                updateCellParameters(model, new JeksCell(name, row, column));

        // Get the full list of all the cells referring the updated cells
        Vector<JeksCell> referringCells = null;
        for (int row = firstRow; row <= lastRow; row++)
            if (row != TableModelEvent.HEADER_ROW)
                referringCells = addReferringCellsToSet(new JeksCell(name, row,
                        column), referringCells);

        if (referringCells != null)
            // Keep in referringCells only cells that need to be updated
            for (int row = firstRow; row <= lastRow; row++)
                if (row != TableModelEvent.HEADER_ROW)
                {
                    int index = referringCells
                            .indexOf(new JeksCell(name, row, column));
                    if (index >= 0)
                        // If updatedCell is in the list, remove it (it's
                        // already updated)
                        referringCells.removeElementAt(index);
                }

        // Update all the cells using these cells
        invalidateReferringCells(model, referringCells);
    }

    public void tableUpdated(TableModel model, Vector updatedSet)
    {

        String name = ((JeksTableModel) model).name;
        for (int i = 0; i < updatedSet.size(); i++)
        {
            Object cellOrSet = updatedSet.elementAt(i);
            if (cellOrSet instanceof JeksCell)
                updateCellParameters(model, (JeksCell) cellOrSet);
            else if (cellOrSet instanceof JeksCellSet)
            {
                JeksCellSet cellSet = (JeksCellSet) cellOrSet;
                for (int row = cellSet.getFirstRow(); row <= cellSet
                        .getLastRow(); row++)
                    for (int column = cellSet.getFirstColumn(); column <= cellSet
                            .getLastColumn(); column++)
                        updateCellParameters(model, new JeksCell(name, row, column));
            }
        }

        // Get the full list of all the cells referring the updated cells
        Vector<JeksCell> referringCells = null;
        for (int i = 0; i < updatedSet.size(); i++)
        {
            Object cellOrSet = updatedSet.elementAt(i);
            if (cellOrSet instanceof JeksCell)
                referringCells = addReferringCellsToSet((JeksCell) cellOrSet,
                        referringCells);
            else if (cellOrSet instanceof JeksCellSet)
            {
                JeksCellSet cellSet = (JeksCellSet) cellOrSet;
                for (int row = cellSet.getFirstRow(); row <= cellSet
                        .getLastRow(); row++)
                    for (int column = cellSet.getFirstColumn(); column <= cellSet
                            .getLastColumn(); column++)
                        referringCells = addReferringCellsToSet(new JeksCell(
                                name, row, column), referringCells);
            }
        }

        if (referringCells != null)
            // Keep in referringCells only cells that need to be updated
            for (int i = 0; i < updatedSet.size(); i++)
            {
                Object cellOrSet = updatedSet.elementAt(i);
                if (cellOrSet instanceof JeksCell)
                {
                    int index = referringCells.indexOf((JeksCell) cellOrSet);
                    if (index >= 0)
                        // If updatedCell is in the list, remove it (it's
                        // already updated)
                        referringCells.removeElementAt(index);
                } else if (cellOrSet instanceof JeksCellSet)
                {
                    JeksCellSet cellSet = (JeksCellSet) cellOrSet;
                    for (int row = cellSet.getFirstRow(); row <= cellSet
                            .getLastRow(); row++)
                        for (int column = cellSet.getFirstColumn(); column <= cellSet
                                .getLastColumn(); column++)
                        {
                            int index = referringCells.indexOf(new JeksCell(name,
                                    row, column));
                            if (index >= 0)
                                // If updatedCell is in the list, remove it
                                // (it's already
                                // updated)
                                referringCells.removeElementAt(index);
                        }
                }
            }

        // Update all the cells using these cells
        invalidateReferringCells(model, referringCells);
    }

    private void updateCellParameters(TableModel model, JeksCell cell)
    {
        JeksExpression computedCell = (JeksExpression) computedCellSet
                .get(cell);
        Object value = model.getValueAt(cell.getRow(), cell.getColumn());

        if (value == null || !value.equals(computedCell))
        {
            if (computedCell != null)
            {
                // Remove this cell from all the cells referring it
                removeParametersReferringToCell(cell, computedCell);
                computedCellSet.remove(cell);
            }

            if (value instanceof JeksExpression)
            {
                // Add this cell to all the parameters referring it
                addParametersReferringToCell(cell, (JeksExpression) value);
                computedCellSet.put(cell, value);
            }
        }
    }

    private Vector<JeksCell> addReferringCellsToSet(JeksCell cell, Vector<JeksCell> referringCells)
    {
        if (referringCells == null)
            referringCells = getAllReferringCells(cell);
        else
        {
            Vector<JeksCell> refCells = cellSet.get(cell);
            if (refCells != null)
                getAllReferringCells(cell, referringCells, refCells);
        }
        return referringCells;
    }

    private void invalidateReferringCells(TableModel model,
            Vector<JeksCell> referringCells)
    {
        TableModel m;
        JeksTableModel jeksModel = (JeksTableModel) model;
        SharedState state = null;
        if (jeksModel != null)
        {
            state = jeksModel.getState();
        }
        if (referringCells != null)
        {
            // TODO optimize code
            // System.out.println ("Invalidate " + referringCells.size() +
            // " cells at " + System.currentTimeMillis());
            // First invalidate value of all the referring cells
            for (int i = 0; i < referringCells.size(); i++)
            {
                JeksCell referringCell = (JeksCell) referringCells.elementAt(i);
                Object cellValue = null;
                if(referringCell.getSheet() != null)
                {                    
                    m = state.getModel(referringCell);
                    cellValue = m.getValueAt(referringCell.getRow(),
                            referringCell.getColumn());
                }
                else
                {
                   cellValue = model.getValueAt(referringCell.getRow(),
                            referringCell.getColumn());
                }
                
                if (cellValue instanceof JeksExpression)
                {
                    JeksExpression computedCell = (JeksExpression) cellValue;
                    if (computedCell != null) // TODO shouldn't happen
                        try
                        {
                            computedCell.invalidateValue();
                            // Check again circularity problems
                            computedCell.checkCircularity(state, model,
                                    referringCell);
                        } catch (CircularityException ex)
                        {
                            computedCell.invalidateValue(ex);
                        }
                }
            }

            for (int i = 0; i < referringCells.size(); i++)
            {
                // Fire event to update values on screen
                JeksCell referringCell = (JeksCell) referringCells.elementAt(i);
                if(referringCell.getSheet() != null)
                {                    
                    m = state.getModel(referringCell);
                    m.setValueAt(
                                m.getValueAt(referringCell.getRow(),
                                    referringCell.getColumn()),
                            referringCell.getRow(), referringCell.getColumn());
                }
                else
                {
                    model.setValueAt(
                            model.getValueAt(referringCell.getRow(),
                                    referringCell.getColumn()),
                            referringCell.getRow(), referringCell.getColumn());
                }
                
            }
        }
    }
}
