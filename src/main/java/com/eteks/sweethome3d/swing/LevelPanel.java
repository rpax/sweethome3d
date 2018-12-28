/*
 * LevelPanel.java  27 oct 2011
 *
 * Sweet Home 3D, Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
 */
package com.eteks.sweethome3d.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.LevelController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Level editing panel.
 * @author Emmanuel Puybaret
 */
public class LevelPanel extends JPanel implements DialogView {
  private final LevelController controller;
  private NullableCheckBox      viewableCheckBox;
  private JLabel                nameLabel;
  private JTextField            nameTextField;
  private JLabel                elevationLabel;
  private JSpinner              elevationSpinner;
  private JLabel                floorThicknessLabel;
  private JSpinner              floorThicknessSpinner;
  private JLabel                heightLabel;
  private JSpinner              heightSpinner;
  private JButton               increaseElevationIndexButton;
  private JButton               decreaseElevationIndexButton;
  private JLabel                levelsSummaryLabel;
  private JTable                levelsSummaryTable;
  private String                dialogTitle;

  /**
   * Creates a panel that displays home levels data according to the units
   * set in <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public LevelPanel(UserPreferences preferences,
                    LevelController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences, controller);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(final UserPreferences preferences,
                                final LevelController controller) {
    // Get unit name matching current unit
    String unitName = preferences.getLengthUnit().getName();

    if (controller.isPropertyEditable(LevelController.Property.VIEWABLE)) {
      // Create viewable check box bound to VIEWABLE controller property
      this.viewableCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
          LevelPanel.class, "viewableCheckBox.text"));
      this.viewableCheckBox.setNullable(controller.getViewable() == null);
      this.viewableCheckBox.setValue(controller.getViewable());
      this.viewableCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.setViewable(viewableCheckBox.getValue());
          }
        });
      controller.addPropertyChangeListener(LevelController.Property.VIEWABLE,
          new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                viewableCheckBox.setValue(controller.getViewable());
              }
            });
    }

    if (controller.isPropertyEditable(LevelController.Property.NAME)) {
      // Create name label and its text field bound to NAME controller property
      this.nameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, LevelPanel.class, "nameLabel.text"));
      this.nameTextField = new AutoCompleteTextField(controller.getName(), 15, preferences.getAutoCompletionStrings("LevelName"));
      if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
        SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
      }
      final PropertyChangeListener nameChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            nameTextField.setText(controller.getName());
          }
        };
      controller.addPropertyChangeListener(LevelController.Property.NAME, nameChangeListener);
      this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent ev) {
            controller.removePropertyChangeListener(LevelController.Property.NAME, nameChangeListener);
            String name = nameTextField.getText();
            if (name == null || name.trim().length() == 0) {
              controller.setName(null);
            } else {
              controller.setName(name);
            }
            controller.addPropertyChangeListener(LevelController.Property.NAME, nameChangeListener);
          }

          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }

          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
        });
    }

    final float maximumLength = preferences.getLengthUnit().getMaximumLength();
    if (controller.isPropertyEditable(LevelController.Property.ELEVATION)) {
      // Create elevation label and its spinner bound to ELEVATION controller property
      this.elevationLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          LevelPanel.class, "elevationLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel =
          new NullableSpinner.NullableSpinnerLengthModel(preferences, -1000f, preferences.getLengthUnit().getMaximumElevation());
      this.elevationSpinner = new NullableSpinner(elevationSpinnerModel);
      elevationSpinnerModel.setNullable(controller.getElevation() == null);
      elevationSpinnerModel.setLength(controller.getElevation());
      final PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            elevationSpinnerModel.setNullable(ev.getNewValue() == null);
            elevationSpinnerModel.setLength((Float)ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(LevelController.Property.ELEVATION,
          elevationChangeListener);
      elevationSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(LevelController.Property.ELEVATION,
                elevationChangeListener);
            controller.setElevation(elevationSpinnerModel.getLength());
            setFloorThicknessEnabled(controller);
            setElevationIndexButtonsEnabled(controller);
            controller.addPropertyChangeListener(LevelController.Property.ELEVATION,
                elevationChangeListener);
          }
        });
    }

    final float minimumLength = preferences.getLengthUnit().getMinimumLength();
    if (controller.isPropertyEditable(LevelController.Property.FLOOR_THICKNESS)) {
      // Create floor thickness label and its spinner bound to FLOOR_THICKNESS controller property
      this.floorThicknessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          LevelPanel.class, "floorThicknessLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel floorThicknessSpinnerModel =
          new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength / 10);
      this.floorThicknessSpinner = new NullableSpinner(floorThicknessSpinnerModel);
      final PropertyChangeListener floorThicknessChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float floorThickness = controller.getFloorThickness();
            floorThicknessSpinnerModel.setNullable(floorThickness == null);
            floorThicknessSpinnerModel.setLength(floorThickness);
          }
        };
      floorThicknessChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(LevelController.Property.FLOOR_THICKNESS, floorThicknessChangeListener);
      floorThicknessSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(LevelController.Property.FLOOR_THICKNESS, floorThicknessChangeListener);
            controller.setFloorThickness(floorThicknessSpinnerModel.getLength());
            controller.addPropertyChangeListener(LevelController.Property.FLOOR_THICKNESS, floorThicknessChangeListener);
          }
        });
      setFloorThicknessEnabled(controller);
    }

    if (controller.isPropertyEditable(LevelController.Property.HEIGHT)) {
      // Create floor thickness label and its spinner bound to HEIGHT controller property
      this.heightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          LevelPanel.class, "heightLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel =
          new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength);
      this.heightSpinner = new NullableSpinner(heightSpinnerModel);
      final PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float height = controller.getHeight();
            heightSpinnerModel.setNullable(height == null);
            heightSpinnerModel.setLength(height);
          }
        };
      heightChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(LevelController.Property.HEIGHT, heightChangeListener);
      heightSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(LevelController.Property.HEIGHT, heightChangeListener);
            controller.setHeight(heightSpinnerModel.getLength());
            controller.addPropertyChangeListener(LevelController.Property.HEIGHT, heightChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(LevelController.Property.ELEVATION_INDEX)) {
      this.increaseElevationIndexButton = new JButton(new ResourceAction(preferences, LevelPanel.class, "INCREASE_ELEVATION_INDEX") {
          @Override
          public void actionPerformed(ActionEvent ev) {
            controller.setElevationIndex(controller.getElevationIndex() + 1);
            setElevationIndexButtonsEnabled(controller);
          }
        });
      this.decreaseElevationIndexButton = new JButton(new ResourceAction(preferences, LevelPanel.class, "DECREASE_ELEVATION_INDEX") {
          @Override
          public void actionPerformed(ActionEvent ev) {
            controller.setElevationIndex(controller.getElevationIndex() - 1);
            setElevationIndexButtonsEnabled(controller);
          }
        });
      setElevationIndexButtonsEnabled(controller);
    }

    this.levelsSummaryLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        LevelPanel.class, "levelsSummaryLabel.text", unitName));
    final String [] columnNames = {
         SwingTools.getLocalizedLabelText(preferences, LevelPanel.class, "nameColumn"),
         SwingTools.getLocalizedLabelText(preferences, LevelPanel.class, "elevationColumn"),
         SwingTools.getLocalizedLabelText(preferences, LevelPanel.class, "floorThicknessColumn"),
         SwingTools.getLocalizedLabelText(preferences, LevelPanel.class, "heightColumn")};
    this.levelsSummaryTable = new JTable(new LevelsTableModel(controller, columnNames));
    float resolutionScale = SwingTools.getResolutionScale();
    if (resolutionScale != 1) {
      // Adapt row height to specified resolution scale
      this.levelsSummaryTable.setRowHeight(Math.round(this.levelsSummaryTable.getRowHeight() * resolutionScale));
    }
    // Display lengths according to the current length unit
    TableColumnModel columnModel = this.levelsSummaryTable.getColumnModel();
    columnModel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
          setEnabled((Boolean)table.getModel().getValueAt(row, 4));
          return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
      });
    TableCellRenderer lengthRenderer = new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
          if (value != null) {
            value = preferences.getLengthUnit().getFormat().format((Float)value);
            setHorizontalAlignment(JLabel.RIGHT);
          }
          setEnabled((Boolean)table.getModel().getValueAt(row, 4));
          return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
      };
    columnModel.getColumn(1).setCellRenderer(lengthRenderer);
    columnModel.getColumn(2).setCellRenderer(lengthRenderer);
    columnModel.getColumn(3).setCellRenderer(lengthRenderer);
    // Ensure only selected level is selected in the table
    this.levelsSummaryTable.setSelectionModel(new DefaultListSelectionModel() {
        {
          controller.addPropertyChangeListener(LevelController.Property.SELECT_LEVEL_INDEX, new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                Integer selectedLevelIndex = controller.getSelectedLevelIndex();
                if (selectedLevelIndex != null) {
                  // Levels are listed in the table in reverse order
                  int selectedRow = levelsSummaryTable.getRowCount() - selectedLevelIndex - 1;
                  fireValueChanged(selectedRow, selectedRow);
                  scrollToSelectedLevel(controller);
                }
              }
            });
        }

        public void setSelectionInterval(int index0, int index1) {
        }

        public void setLeadSelectionIndex(int index) {
        }

        public void setAnchorSelectionIndex(int index) {
        }

        public boolean isSelectionEmpty() {
          return controller.getSelectedLevelIndex() != null;
        }

        public boolean isSelectedIndex(int index) {
          // Levels are listed in the table in reverse order
          return controller.getSelectedLevelIndex() == levelsSummaryTable.getRowCount() - index - 1;
        }

        public void insertIndexInterval(int index, int length, boolean before) {
        }
      });
    this.levelsSummaryTable.setFocusable(false);
    this.levelsSummaryTable.addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent ev) {
          scrollToSelectedLevel(controller);
          ev.getComponent().removeAncestorListener(this);
        }

        public void ancestorRemoved(AncestorEvent ev) {
        }

        public void ancestorMoved(AncestorEvent ev) {
        }
      });

    this.dialogTitle = preferences.getLocalizedString(LevelPanel.class, "level.title");
  }

  private void setFloorThicknessEnabled(final LevelController controller) {
    Integer selectedLevelIndex = controller.getSelectedLevelIndex();
    if (this.floorThicknessSpinner != null && selectedLevelIndex != null) {
      Level [] levels = controller.getLevels();
      this.floorThicknessSpinner.setEnabled(levels [selectedLevelIndex].getElevation()
          != levels [0].getElevation());
    }
  }

  /**
   * Ensures selected level is visible.
   */
  private void scrollToSelectedLevel(final LevelController controller) {
    Integer selectedLevelIndex = controller.getSelectedLevelIndex();
    if (selectedLevelIndex != null) {
      // Levels are listed in the table in reverse order
      int selectedRow = levelsSummaryTable.getRowCount() - selectedLevelIndex - 1;
      this.levelsSummaryTable.scrollRectToVisible(this.levelsSummaryTable.getCellRect(selectedRow, 0, true));
    }
  }

  /**
   * Updates elevation index buttons.
   */
  private void setElevationIndexButtonsEnabled(final LevelController controller) {
    Integer selectedLevelIndex = controller.getSelectedLevelIndex();
    if (selectedLevelIndex != null) {
      Level [] levels = controller.getLevels();
      this.increaseElevationIndexButton.setEnabled(selectedLevelIndex < levels.length - 1
          && levels [selectedLevelIndex].getElevation() == levels [selectedLevelIndex + 1].getElevation());
      this.decreaseElevationIndexButton.setEnabled(selectedLevelIndex > 0
          && levels [selectedLevelIndex].getElevation() == levels [selectedLevelIndex - 1].getElevation());
    } else {
      this.increaseElevationIndexButton.setEnabled(false);
      this.decreaseElevationIndexButton.setEnabled(false);
    }
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      if (this.viewableCheckBox != null) {
        this.viewableCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "viewableCheckBox.mnemonic")).getKeyCode());
      }
      if (this.nameLabel != null) {
        this.nameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "nameLabel.mnemonic")).getKeyCode());
        this.nameLabel.setLabelFor(this.nameTextField);
      }
      if (this.elevationLabel != null) {
        this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "elevationLabel.mnemonic")).getKeyCode());
        this.elevationLabel.setLabelFor(this.elevationSpinner);
      }
      if (this.floorThicknessLabel != null) {
        this.floorThicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "floorThicknessLabel.mnemonic")).getKeyCode());
        this.floorThicknessLabel.setLabelFor(this.floorThicknessSpinner);
      }
      if (this.heightLabel != null) {
        this.heightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "heightLabel.mnemonic")).getKeyCode());
        this.heightLabel.setLabelFor(this.heightSpinner);
      }
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents(UserPreferences preferences,
                                final LevelController controller) {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    Insets labelInsets = new Insets(0, 0, standardGap, standardGap);
    Insets rightComponentInsets = new Insets(0, 0, standardGap, 0);
    if (this.viewableCheckBox != null) {
      // First row
      add(this.viewableCheckBox, new GridBagConstraints(
          1, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, -4, standardGap, 0), 0, 0));
    }
    if (this.nameLabel != null) {
      // Second row
      add(this.nameLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.nameTextField, new GridBagConstraints(
          1, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.elevationLabel != null) {
      // Third row
      add(this.elevationLabel, new GridBagConstraints(
          0, 2, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.elevationSpinner, new GridBagConstraints(
          1, 2, 1, 1, 0.1, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
      add(new JLabel(), new GridBagConstraints(
          2, 2, 1, 1, 0.2, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.floorThicknessLabel != null) {
      // Forth row
      add(this.floorThicknessLabel, new GridBagConstraints(
          0, 3, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.floorThicknessSpinner, new GridBagConstraints(
          1, 3, 1, 1, 0.1, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
      add(new JLabel(), new GridBagConstraints(
          2, 3, 1, 1, 0.2, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.heightLabel != null) {
      // Fifth row
      add(this.heightLabel, new GridBagConstraints(
          0, 4, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
      add(this.heightSpinner, new GridBagConstraints(
          1, 4, 1, 1, 0.1, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), -15, 0));
      add(new JLabel(), new GridBagConstraints(
          2, 4, 1, 1, 0.2, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    add(new JSeparator(), new GridBagConstraints(
        0, 5, 4, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, standardGap, 0), 0, 0));
    add(this.levelsSummaryLabel, new GridBagConstraints(
        0, 6, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    JScrollPane levelsSummaryPane = new JScrollPane(this.levelsSummaryTable);
    levelsSummaryPane.setPreferredSize(new Dimension(Math.round(320 * SwingTools.getResolutionScale()),
        this.levelsSummaryTable.getTableHeader().getPreferredSize().height + this.levelsSummaryTable.getRowHeight() * 8 + 1));
    add(levelsSummaryPane, new GridBagConstraints(
        0, 7, 3, 2, 1, 1, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    // Add elevation index buttons at right of the levels table
    add(this.increaseElevationIndexButton, new GridBagConstraints(
        3, 7, 1, 1, 0, 0.5, GridBagConstraints.SOUTH,
        GridBagConstraints.NONE, new Insets(0, standardGap, 2, 0), 0, 0));
    add(this.decreaseElevationIndexButton, new GridBagConstraints(
        3, 8, 1, 1, 0, 0.5, GridBagConstraints.NORTH,
        GridBagConstraints.NONE, new Insets(2, standardGap, 0, 0), 0, 0));

    if (!OperatingSystem.isMacOSX()) {
      // Make buttons square
      Dimension preferredSize = this.increaseElevationIndexButton.getPreferredSize();
      preferredSize.width =
      preferredSize.height = preferredSize.height + 4;
      this.increaseElevationIndexButton.setPreferredSize(preferredSize);
      this.decreaseElevationIndexButton.setPreferredSize(preferredSize);
    }
  }

  /**
   * Displays this panel in a modal dialog box.
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.nameTextField) == JOptionPane.OK_OPTION) {
      this.controller.modifyLevels();
    }
  }

  /**
   * The model of the table used to show levels information.
   */
  private static final class LevelsTableModel extends AbstractTableModel {
    private Level  [] levels;
    private String [] columnNames;

    private LevelsTableModel(final LevelController controller, String [] columnNames) {
      this.levels = controller.getLevels();
      this.columnNames = columnNames;
      controller.addPropertyChangeListener(LevelController.Property.LEVELS, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            levels = controller.getLevels();
            fireTableDataChanged();
          }
        });
    }

    public int getRowCount() {
      return this.levels.length;
    }

    public int getColumnCount() {
      return 4;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      Level level = this.levels [this.levels.length - rowIndex - 1]; // List in reverse order
      switch (columnIndex) {
        case 0 : return level.getName();
        case 1 : return level.getElevation();
        case 2 :
          if (level.getElevation() == this.levels [0].getElevation()) {
            return null; // Don't display floor thickness of first levels
          } else {
            return level.getFloorThickness();
          }
        case 3 : return level.getHeight();
        case 4 :
          // Use not visible 5th column to retrieve viewable information
          return level.isViewable();
      }
      return null;
    }

    @Override
    public String getColumnName(int column) {
      return this.columnNames [column];
    }
  }
}
