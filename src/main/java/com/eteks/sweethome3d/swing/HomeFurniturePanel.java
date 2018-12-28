/*
 * HomeFurniturePanel.java 16 mai 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.security.AccessControlException;

import javax.media.j3d.BranchGroup;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.ModelMaterialsController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home furniture editing panel.
 * @author Emmanuel Puybaret
 */
public class HomeFurniturePanel extends JPanel implements DialogView {
  private final HomeFurnitureController controller;
  private JLabel                  nameLabel;
  private JTextField              nameTextField;
  private JLabel                  descriptionLabel;
  private JTextField              descriptionTextField;
  private NullableCheckBox        nameVisibleCheckBox;
  private JLabel                  priceLabel;
  private JSpinner                priceSpinner;
  private JLabel                  valueAddedTaxPercentageLabel;
  private JSpinner                valueAddedTaxPercentageSpinner;
  private JLabel                  xLabel;
  private JSpinner                xSpinner;
  private JLabel                  yLabel;
  private JSpinner                ySpinner;
  private JLabel                  elevationLabel;
  private JSpinner                elevationSpinner;
  private NullableCheckBox        basePlanItemCheckBox;
  private JLabel                  angleLabel;
  private JSpinner                angleSpinner;
  private JRadioButton            rollRadioButton;
  private JSpinner                rollSpinner;
  private JRadioButton            pitchRadioButton;
  private JSpinner                pitchSpinner;
  private JLabel                  widthLabel;
  private JSpinner                widthSpinner;
  private JLabel                  depthLabel;
  private JSpinner                depthSpinner;
  private JLabel                  heightLabel;
  private JSpinner                heightSpinner;
  private JCheckBox               keepProportionsCheckBox;
  private NullableCheckBox        mirroredModelCheckBox;
  private JButton                 modelTransformationsButton;
  private JRadioButton            defaultColorAndTextureRadioButton;
  private JRadioButton            colorRadioButton;
  private ColorButton             colorButton;
  private JRadioButton            textureRadioButton;
  private JComponent              textureComponent;
  private JRadioButton            modelMaterialsRadioButton;
  private JComponent              modelMaterialsComponent;
  private JRadioButton            defaultShininessRadioButton;
  private JRadioButton            mattRadioButton;
  private JRadioButton            shinyRadioButton;
  private NullableCheckBox        visibleCheckBox;
  private JLabel                  lightPowerLabel;
  private JSpinner                lightPowerSpinner;
  private String                  dialogTitle;

  /**
   * Creates a panel that displays home furniture data according to the units
   * set in <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public HomeFurniturePanel(UserPreferences preferences,
                            HomeFurnitureController controller) {
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
                                final HomeFurnitureController controller) {
    // Get unit name matching current unit
    String unitName = preferences.getLengthUnit().getName();

    if (controller.isPropertyEditable(HomeFurnitureController.Property.NAME)) {
      // Create name label and its text field bound to NAME controller property
      this.nameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class, "nameLabel.text"));
      this.nameTextField = new AutoCompleteTextField(controller.getName(), 15, preferences.getAutoCompletionStrings("HomePieceOfFurnitureName"));
      if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
        SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
      }
      final PropertyChangeListener nameChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            nameTextField.setText(controller.getName());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
      this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
            String name = nameTextField.getText();
            if (name == null || name.trim().length() == 0) {
              controller.setName(null);
            } else {
              controller.setName(name);
            }
            controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
          }

          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }

          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.NAME_VISIBLE)) {
      // Create name visible check box bound to NAME_VISIBLE controller property
      this.nameVisibleCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
          HomeFurniturePanel.class, "nameVisibleCheckBox.text"));
      this.nameVisibleCheckBox.setNullable(controller.getNameVisible() == null);
      this.nameVisibleCheckBox.setValue(controller.getNameVisible());
      final PropertyChangeListener nameVisibleChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            nameVisibleCheckBox.setNullable(ev.getNewValue() == null);
            nameVisibleCheckBox.setValue((Boolean)ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
      this.nameVisibleCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
            controller.setNameVisible(nameVisibleCheckBox.getValue());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.DESCRIPTION)) {
      // Create description label and its text field bound to DESCRIPTION controller property
      this.descriptionLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class, "descriptionLabel.text"));
      this.descriptionTextField = new AutoCompleteTextField(controller.getDescription(), 15, preferences.getAutoCompletionStrings("HomePieceOfFurnitureDescription"));
      if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
        SwingTools.addAutoSelectionOnFocusGain(this.descriptionTextField);
      }
      final PropertyChangeListener descriptionChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            descriptionTextField.setText(controller.getDescription());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.DESCRIPTION, descriptionChangeListener);
      this.descriptionTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.DESCRIPTION, descriptionChangeListener);
            String description = descriptionTextField.getText();
            if (description == null || description.trim().length() == 0) {
              controller.setDescription(null);
            } else {
              controller.setDescription(description);
            }
            controller.addPropertyChangeListener(HomeFurnitureController.Property.DESCRIPTION, descriptionChangeListener);
          }

          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }

          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.PRICE)) {
      // Create Price label and its spinner bound to PRICE controller property
      this.priceLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          HomeFurniturePanel.class, "priceLabel.text"));
      final NullableSpinner.NullableSpinnerNumberModel priceSpinnerModel =
          new NullableSpinner.NullableSpinnerNumberModel(BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1000000000"), BigDecimal.ONE);
      this.priceSpinner = new NullableSpinner(priceSpinnerModel);
      BigDecimal price = controller.getPrice();
      priceSpinnerModel.setNullable(true);
      priceSpinnerModel.setValue(price);
      final PropertyChangeListener priceChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            priceSpinnerModel.setNullable(ev.getNewValue() == null);
            priceSpinnerModel.setValue(ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.PRICE, priceChangeListener);
      priceSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.PRICE, priceChangeListener);
            controller.setPrice((BigDecimal)priceSpinnerModel.getNumber());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.PRICE, priceChangeListener);
          }
        });

      if (controller.isPropertyEditable(HomeFurnitureController.Property.VALUE_ADDED_TAX_PERCENTAGE)) {
        this.valueAddedTaxPercentageLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
            HomeFurniturePanel.class, "valueAddedTaxPercentageLabel.text"));
        final BigDecimal hundred = new BigDecimal("100");
        final NullableSpinner.NullableSpinnerNumberModel valueAddedTaxPercentageSpinnerModel = new NullableSpinner.NullableSpinnerNumberModel(
            BigDecimal.ZERO, BigDecimal.ZERO, hundred, new BigDecimal("0.5"));
        this.valueAddedTaxPercentageSpinner = new NullableSpinner(valueAddedTaxPercentageSpinnerModel);
        BigDecimal valueAddedTaxPercentage = controller.getValueAddedTaxPercentage();
        valueAddedTaxPercentageSpinnerModel.setNullable(true);
        if (valueAddedTaxPercentage != null) {
          valueAddedTaxPercentageSpinnerModel.setValue(valueAddedTaxPercentage.multiply(hundred));
        } else {
          valueAddedTaxPercentageSpinnerModel.setValue(null);
        }
        final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              priceSpinnerModel.setNullable(ev.getNewValue() == null);
              if (ev.getNewValue() != null) {
                valueAddedTaxPercentageSpinnerModel.setValue(((BigDecimal)ev.getNewValue()).multiply(hundred));
              } else {
                valueAddedTaxPercentageSpinnerModel.setValue(null);
              }
            }
          };
        valueAddedTaxPercentageSpinnerModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
              // Remove listener on controller to avoid being recalled since actual value is divided by 100
              controller.removePropertyChangeListener(HomeFurnitureController.Property.VALUE_ADDED_TAX_PERCENTAGE, propertyChangeListener);
              controller.setValueAddedTaxPercentage(valueAddedTaxPercentageSpinnerModel.getValue() != null
                  ? ((BigDecimal)valueAddedTaxPercentageSpinnerModel.getValue()).divide(hundred)
                  : null);
              controller.addPropertyChangeListener(HomeFurnitureController.Property.VALUE_ADDED_TAX_PERCENTAGE, propertyChangeListener);
            }
          });
        controller.addPropertyChangeListener(HomeFurnitureController.Property.VALUE_ADDED_TAX_PERCENTAGE,
            propertyChangeListener);
      }
    }

    final float maximumLength = preferences.getLengthUnit().getMaximumLength();

    if (controller.isPropertyEditable(HomeFurnitureController.Property.X)) {
      // Create X label and its spinner bound to X controller property
      this.xLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          HomeFurniturePanel.class, "xLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel xSpinnerModel =
          new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
      this.xSpinner = new NullableSpinner(xSpinnerModel);
      xSpinnerModel.setNullable(controller.getX() == null);
      xSpinnerModel.setLength(controller.getX());
      final PropertyChangeListener xChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            xSpinnerModel.setNullable(ev.getNewValue() == null);
            xSpinnerModel.setLength((Float)ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
      xSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
            controller.setX(xSpinnerModel.getLength());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.Y)) {
      // Create Y label and its spinner bound to Y controller property
      this.yLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class, "yLabel.text",
          unitName));
      final NullableSpinner.NullableSpinnerLengthModel ySpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
          preferences, -maximumLength, maximumLength);
      this.ySpinner = new NullableSpinner(ySpinnerModel);
      ySpinnerModel.setNullable(controller.getY() == null);
      ySpinnerModel.setLength(controller.getY());
      final PropertyChangeListener yChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            ySpinnerModel.setNullable(ev.getNewValue() == null);
            ySpinnerModel.setLength((Float) ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
      ySpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
            controller.setY(ySpinnerModel.getLength());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.ELEVATION)) {
      // Create elevation label and its spinner bound to ELEVATION controller property
      this.elevationLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "elevationLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
          preferences, 0f, preferences.getLengthUnit().getMaximumElevation());
      this.elevationSpinner = new NullableSpinner(elevationSpinnerModel);
      elevationSpinnerModel.setNullable(controller.getElevation() == null);
      elevationSpinnerModel.setLength(controller.getElevation());
      final PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            elevationSpinnerModel.setNullable(ev.getNewValue() == null);
            elevationSpinnerModel.setLength((Float) ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.ELEVATION, elevationChangeListener);
      elevationSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.ELEVATION, elevationChangeListener);
            controller.setElevation(elevationSpinnerModel.getLength());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.ELEVATION, elevationChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.ANGLE_IN_DEGREES)
        || controller.isPropertyEditable(HomeFurnitureController.Property.ANGLE)) {
      // Create angle label and its spinner bound to ANGLE controller property
      this.angleLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "angleLabel.text"));
      final NullableSpinner.NullableSpinnerNumberModel angleSpinnerModel = new NullableSpinner.NullableSpinnerModuloNumberModel(
          0f, 0f, 360f, 1f);
      this.angleSpinner = new NullableSpinner(angleSpinnerModel);
      Float angle = controller.getAngle();
      angleSpinnerModel.setNullable(angle == null);
      angleSpinnerModel.setValue(angle != null ? new Float((float)Math.toDegrees(angle)) : null);
      final PropertyChangeListener angleChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float newAngle = (Float)ev.getNewValue();
            angleSpinnerModel.setNullable(newAngle == null);
            angleSpinnerModel.setValue(newAngle != null ? new Float((float)Math.toDegrees(newAngle)) : null);
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.ANGLE, angleChangeListener);
      angleSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.ANGLE,
                angleChangeListener);
            Number value = (Number)angleSpinnerModel.getValue();
            if (value == null) {
              controller.setAngle(null);
            } else {
              controller.setAngle((float)Math.toRadians(value.doubleValue()));
            }
            controller.addPropertyChangeListener(HomeFurnitureController.Property.ANGLE, angleChangeListener);
          }
        });
    }

    if (!Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
      if (controller.isPropertyEditable(HomeFurnitureController.Property.ROLL)) {
        // Create roll label and its spinner bound to ROLL_IN_DEGREES controller property
        this.rollRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
            "rollRadioButton.text"));
        this.rollRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (rollRadioButton.isSelected()) {
              controller.setHorizontalAxis(HomeFurnitureController.FurnitureHorizontalAxis.ROLL);
            }
          }
        });

        final NullableSpinner.NullableSpinnerNumberModel rollSpinnerModel = new NullableSpinner.NullableSpinnerModuloNumberModel(
            0f, 0f, 360f, 1f);
        this.rollSpinner = new NullableSpinner(rollSpinnerModel);
        Float roll = controller.getRoll();
        rollSpinnerModel.setNullable(roll == null);
        rollSpinnerModel.setValue(roll != null ? new Float((float)Math.toDegrees(roll)) : null);
        final PropertyChangeListener rollChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              Float newRoll = (Float)ev.getNewValue();
              rollSpinnerModel.setNullable(newRoll == null);
              rollSpinnerModel.setValue(newRoll != null ? new Float((float)Math.toDegrees(newRoll)) : null);
            }
          };
        controller.addPropertyChangeListener(HomeFurnitureController.Property.ROLL, rollChangeListener);
        rollSpinnerModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
              controller.removePropertyChangeListener(HomeFurnitureController.Property.ROLL,
                  rollChangeListener);
              Number value = (Number)rollSpinnerModel.getValue();
              if (value == null) {
                controller.setRoll(null);
              } else {
                controller.setRoll((float)Math.toRadians(value.floatValue()));
              }
              controller.setHorizontalAxis(HomeFurnitureController.FurnitureHorizontalAxis.ROLL);
              controller.addPropertyChangeListener(HomeFurnitureController.Property.ROLL, rollChangeListener);
            }
          });
      }

      if (controller.isPropertyEditable(HomeFurnitureController.Property.PITCH)) {
        // Create pitch label and its spinner bound to PITCH_IN_DEGREES controller property
        this.pitchRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
            "pitchRadioButton.text"));
        this.pitchRadioButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
              if (pitchRadioButton.isSelected()) {
                controller.setHorizontalAxis(HomeFurnitureController.FurnitureHorizontalAxis.PITCH);
              }
            }
          });

        final NullableSpinner.NullableSpinnerNumberModel pitchSpinnerModel = new NullableSpinner.NullableSpinnerModuloNumberModel(
            0f, 0f, 360f, 1f);
        this.pitchSpinner = new NullableSpinner(pitchSpinnerModel);
        Float pitch = controller.getPitch();
        pitchSpinnerModel.setNullable(pitch == null);
        pitchSpinnerModel.setValue(pitch != null ? new Float((float)Math.toDegrees(pitch)) : null);
        final PropertyChangeListener pitchChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              Float newPitch = (Float)ev.getNewValue();
              pitchSpinnerModel.setNullable(newPitch == null);
              pitchSpinnerModel.setValue(newPitch != null ? new Float((float)Math.toDegrees(newPitch)) : null);
            }
          };
        controller.addPropertyChangeListener(HomeFurnitureController.Property.PITCH, pitchChangeListener);
        pitchSpinnerModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
              controller.removePropertyChangeListener(HomeFurnitureController.Property.PITCH,
                  pitchChangeListener);
              Number value = (Number)pitchSpinnerModel.getValue();
              if (value == null) {
                controller.setPitch(null);
              } else {
                controller.setPitch((float)Math.toRadians(value.floatValue()));
              }
              controller.setHorizontalAxis(HomeFurnitureController.FurnitureHorizontalAxis.PITCH);
              controller.addPropertyChangeListener(HomeFurnitureController.Property.PITCH, pitchChangeListener);
            }
          });
      }

      if (this.rollRadioButton != null && this.pitchRadioButton != null) {
        ButtonGroup group = new ButtonGroup();
        group.add(this.rollRadioButton);
        group.add(this.pitchRadioButton);
        updateHorizontalAxisRadioButtons(controller);
        controller.addPropertyChangeListener(HomeFurnitureController.Property.HORIZONTAL_AXIS, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              updateHorizontalAxisRadioButtons(controller);
            }
          });
      }
    }


    if (controller.isPropertyEditable(HomeFurnitureController.Property.BASE_PLAN_ITEM)) {
      // Create base plan item check box bound to BASE_PLAN_ITEM controller property
      this.basePlanItemCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
          HomeFurniturePanel.class, "basePlanItemCheckBox.text"));
      String basePlanItemToolTip = preferences.getLocalizedString(HomeFurniturePanel.class,
          "basePlanItemCheckBox.tooltip");
      if (basePlanItemToolTip.length() > 0) {
        this.basePlanItemCheckBox.setToolTipText(basePlanItemToolTip);
      }
      this.basePlanItemCheckBox.setNullable(controller.getBasePlanItem() == null);
      this.basePlanItemCheckBox.setValue(controller.getBasePlanItem());
      final PropertyChangeListener basePlanItemModelChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            basePlanItemCheckBox.setNullable(ev.getNewValue() == null);
            basePlanItemCheckBox.setValue((Boolean) ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.BASE_PLAN_ITEM,
          basePlanItemModelChangeListener);
      this.basePlanItemCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.BASE_PLAN_ITEM,
                basePlanItemModelChangeListener);
            controller.setBasePlanItem(basePlanItemCheckBox.getValue());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.BASE_PLAN_ITEM,
                basePlanItemModelChangeListener);
          }
        });
      this.basePlanItemCheckBox.setEnabled(controller.isBasePlanItemEnabled());
    }

    final float minimumLength = preferences.getLengthUnit().getMinimumLength();

    if (controller.isPropertyEditable(HomeFurnitureController.Property.WIDTH)) {
      // Create width label and its spinner bound to WIDTH controller property
      this.widthLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "widthLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel widthSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
          preferences, minimumLength, maximumLength);
      this.widthSpinner = new NullableSpinner(widthSpinnerModel);
      final PropertyChangeListener widthChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float width = controller.getWidth();
            widthSpinnerModel.setNullable(width == null);
            widthSpinnerModel.setLength(width);
            if (width != null) {
              widthSpinnerModel.setMinimumLength(Math.min(width, minimumLength));
            }
          }
        };
      widthChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
      widthSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
            controller.setWidth(widthSpinnerModel.getLength());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.DEPTH)) {
      // Create depth label and its spinner bound to DEPTH controller property
      this.depthLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "depthLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel depthSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
          preferences, minimumLength, maximumLength);
      this.depthSpinner = new NullableSpinner(depthSpinnerModel);
      final PropertyChangeListener depthChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float depth = controller.getDepth();
            depthSpinnerModel.setNullable(depth == null);
            depthSpinnerModel.setLength(depth);
            if (depth != null) {
              depthSpinnerModel.setMinimumLength(Math.min(depth, minimumLength));
            }
          }
        };
      depthChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
      depthSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
            controller.setDepth(depthSpinnerModel.getLength());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.HEIGHT)) {
      // Create height label and its spinner bound to HEIGHT controller property
      this.heightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "heightLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
          preferences, minimumLength, maximumLength);
      this.heightSpinner = new NullableSpinner(heightSpinnerModel);
      final PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float height = controller.getHeight();
            heightSpinnerModel.setNullable(height == null);
            heightSpinnerModel.setLength(height);
            if (height != null) {
              heightSpinnerModel.setMinimumLength(Math.min(height, minimumLength));
            }
          }
        };
      heightChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
      heightSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
            controller.setHeight(heightSpinnerModel.getLength());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.PROPORTIONAL)) {
      // Create keep proportions check box bound to PROPORTIONAL controller property
      this.keepProportionsCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          ImportedFurnitureWizardStepsPanel.class, "keepProportionsCheckBox.text"));
      this.keepProportionsCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setProportional(keepProportionsCheckBox.isSelected());
          }
        });
      this.keepProportionsCheckBox.setSelected(controller.isProportional()
          // Force proportional if selection is rotated around horizontal axis when no 3D is available
          || Boolean.getBoolean("com.eteks.sweethome3d.no3D")
             && (controller.getRoll() == null
                 || controller.getRoll() != 0
                 || controller.getPitch() == null
                 || controller.getPitch() != 0));
      controller.addPropertyChangeListener(HomeFurnitureController.Property.PROPORTIONAL, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If proportional property changes update keep proportions check box
            keepProportionsCheckBox.setSelected(controller.isProportional());
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.MODEL_MIRRORED)) {
      // Create mirror check box bound to MODEL_MIRRORED controller property
      this.mirroredModelCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
          HomeFurniturePanel.class, "mirroredModelCheckBox.text"));
      String mirroredModelToolTip = preferences.getLocalizedString(HomeFurniturePanel.class,
          "mirroredModelCheckBox.tooltip");
      if (mirroredModelToolTip.length() > 0) {
        this.mirroredModelCheckBox.setToolTipText(mirroredModelToolTip);
      }
      this.mirroredModelCheckBox.setNullable(controller.getModelMirrored() == null);
      this.mirroredModelCheckBox.setValue(controller.getModelMirrored());
      final PropertyChangeListener mirroredModelChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            mirroredModelCheckBox.setNullable(ev.getNewValue() == null);
            mirroredModelCheckBox.setValue((Boolean) ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED, mirroredModelChangeListener);
      this.mirroredModelCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED,
                mirroredModelChangeListener);
            controller.setModelMirrored(mirroredModelCheckBox.getValue());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED,
                mirroredModelChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.MODEL_TRANSFORMATIONS)) {
      try {
        if (!Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
          this.modelTransformationsButton = new JButton();
          this.modelTransformationsButton.setVisible(false);
          if (OperatingSystem.isMacOSX()) {
            this.modelTransformationsButton.putClientProperty("JButton.buttonType", "segmented");
            this.modelTransformationsButton.putClientProperty("JButton.segmentPosition", "only");
          }
          ModelMaterialsController modelMaterialsController = controller.getModelMaterialsController();
          if (modelMaterialsController != null && modelMaterialsController.getModel() != null) {
            ModelManager.getInstance().loadModel(modelMaterialsController.getModel(),
                new ModelManager.ModelObserver() {
                  public void modelUpdated(BranchGroup modelRoot) {
                    ModelManager modelManager = ModelManager.getInstance();
                    if (modelManager.containsDeformableNode(modelRoot)) {
                      // Make button visible only if model contains some deformable nodes
                      modelTransformationsButton.setText(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
                          modelManager.containsNode(modelRoot, ModelManager.MANNEQUIN_ABDOMEN_PREFIX)
                              ? "mannequinTransformationsButton.text"
                              : "modelTransformationsButton.text"));
                      modelTransformationsButton.setVisible(true);
                      modelTransformationsButton.addActionListener(new ActionListener() {
                          public void actionPerformed(ActionEvent ev) {
                            displayModelTransformationsView(preferences, controller);
                          }
                        });
                    }
                  }

                  public void modelError(Exception ex) {
                    // Ignore missing models
                  }
                });
          }
        }
      } catch (AccessControlException ex) {
        // com.eteks.sweethome3d.no3D property can't be read
      }
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.PAINT)) {
      ButtonGroup buttonGroup = new ButtonGroup();
      // Create radio buttons bound to COLOR and TEXTURE controller properties
      this.defaultColorAndTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          HomeFurniturePanel.class, "defaultColorAndTextureRadioButton.text"));
      buttonGroup.add(this.defaultColorAndTextureRadioButton);
      this.defaultColorAndTextureRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (defaultColorAndTextureRadioButton.isSelected()) {
              controller.setPaint(HomeFurnitureController.FurniturePaint.DEFAULT);
            }
          }
        });
      controller.addPropertyChangeListener(HomeFurnitureController.Property.PAINT, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updatePaintRadioButtons(controller);
          }
        });

      this.colorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "colorRadioButton.text"));
      buttonGroup.add(this.colorRadioButton);
      this.colorRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (colorRadioButton.isSelected()) {
              controller.setPaint(HomeFurnitureController.FurniturePaint.COLORED);
            }
          }
        });

      this.colorButton = new ColorButton(preferences);
      if (OperatingSystem.isMacOSX()) {
        this.colorButton.putClientProperty("JButton.buttonType", "segmented");
        this.colorButton.putClientProperty("JButton.segmentPosition", "only");
      }
      this.colorButton.setColorDialogTitle(preferences
          .getLocalizedString(HomeFurniturePanel.class, "colorDialog.title"));
      this.colorButton.setColor(controller.getColor());
      this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
            controller.setPaint(HomeFurnitureController.FurniturePaint.COLORED);
          }
        });
      controller.addPropertyChangeListener(HomeFurnitureController.Property.COLOR, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            colorButton.setColor(controller.getColor());
          }
        });

      TextureChoiceController textureController = controller.getTextureController();
      if (textureController != null) {
        this.textureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
            HomeFurniturePanel.class, "textureRadioButton.text"));
        this.textureRadioButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
              if (textureRadioButton.isSelected()) {
                controller.setPaint(HomeFurnitureController.FurniturePaint.TEXTURED);
              }
            }
          });
        this.textureComponent = (JComponent) textureController.getView();
        if (OperatingSystem.isMacOSX()) {
          this.textureComponent.putClientProperty("JButton.buttonType", "segmented");
          this.textureComponent.putClientProperty("JButton.segmentPosition", "only");
        }
        buttonGroup.add(this.textureRadioButton);
      }

      try {
        ModelMaterialsController modelMaterialsController = controller.getModelMaterialsController();
        if (modelMaterialsController != null
            && !Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
          this.modelMaterialsRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
              HomeFurniturePanel.class, "modelMaterialsRadioButton.text"));
          this.modelMaterialsRadioButton.addChangeListener(new ChangeListener() {
              public void stateChanged(ChangeEvent ev) {
                if (modelMaterialsRadioButton.isSelected()) {
                  controller.setPaint(HomeFurnitureController.FurniturePaint.MODEL_MATERIALS);
                }
              }
            });
          this.modelMaterialsComponent = (JComponent)modelMaterialsController.getView();
          if (OperatingSystem.isMacOSX()) {
            this.modelMaterialsComponent.putClientProperty("JButton.buttonType", "segmented");
            this.modelMaterialsComponent.putClientProperty("JButton.segmentPosition", "only");
          }
          buttonGroup.add(this.modelMaterialsRadioButton);
          boolean uniqueModel = modelMaterialsController.getModel() != null;
          this.modelMaterialsRadioButton.setEnabled(uniqueModel);
          this.modelMaterialsComponent.setEnabled(uniqueModel);
        }
      } catch (AccessControlException ex) {
        // com.eteks.sweethome3d.no3D property can't be read
      }
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.SHININESS)) {
      // Create radio buttons bound to SHININESS controller properties
      this.defaultShininessRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          HomeFurniturePanel.class, "defaultShininessRadioButton.text"));
      this.defaultShininessRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (defaultShininessRadioButton.isSelected()) {
              controller.setShininess(HomeFurnitureController.FurnitureShininess.DEFAULT);
            }
          }
        });
      controller.addPropertyChangeListener(HomeFurnitureController.Property.SHININESS, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateShininessRadioButtons(controller);
          }
        });
      this.mattRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "mattRadioButton.text"));
      this.mattRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (mattRadioButton.isSelected()) {
              controller.setShininess(HomeFurnitureController.FurnitureShininess.MATT);
            }
          }
        });
      this.shinyRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "shinyRadioButton.text"));
      this.shinyRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (shinyRadioButton.isSelected()) {
              controller.setShininess(HomeFurnitureController.FurnitureShininess.SHINY);
            }
          }
        });
      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(this.defaultShininessRadioButton);
      buttonGroup.add(this.mattRadioButton);
      buttonGroup.add(this.shinyRadioButton);
      updateShininessRadioButtons(controller);
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.PAINT)) {
      updatePaintRadioButtons(controller);
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.VISIBLE)) {
      // Create visible check box bound to VISIBLE controller property
      this.visibleCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
          HomeFurniturePanel.class, "visibleCheckBox.text"));
      this.visibleCheckBox.setNullable(controller.getVisible() == null);
      this.visibleCheckBox.setValue(controller.getVisible());
      final PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            visibleCheckBox.setNullable(ev.getNewValue() == null);
            visibleCheckBox.setValue((Boolean) ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
      this.visibleCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
            controller.setVisible(visibleCheckBox.getValue());
            controller.addPropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
          }
        });
    }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.LIGHT_POWER)) {
      // Create power label and its spinner bound to POWER controller property
      this.lightPowerLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class,
          "lightPowerLabel.text", unitName));
      final NullableSpinner.NullableSpinnerNumberModel lightPowerSpinnerModel = new NullableSpinner.NullableSpinnerNumberModel(
          0, 0, 100, 5);
      this.lightPowerSpinner = new NullableSpinner(lightPowerSpinnerModel);
      lightPowerSpinnerModel.setNullable(controller.getLightPower() == null);
      lightPowerSpinnerModel.setValue(controller.getLightPower() != null
          ? Math.round(controller.getLightPower() * 100)
          : null);
      final PropertyChangeListener lightPowerChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float lightPower = (Float) ev.getNewValue();
            lightPowerSpinnerModel.setNullable(lightPower == null);
            lightPowerSpinnerModel.setValue(lightPower != null
                ? Math.round((Float)ev.getNewValue() * 100)
                : null);
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.LIGHT_POWER, lightPowerChangeListener);
      lightPowerSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.LIGHT_POWER,
                lightPowerChangeListener);
            controller.setLightPower(((Number) lightPowerSpinnerModel.getValue()).floatValue() / 100f);
            controller
                .addPropertyChangeListener(HomeFurnitureController.Property.LIGHT_POWER, lightPowerChangeListener);
          }
        });
    }

    updateSizeComponents(controller);
    // Add a listener that enables / disables size fields depending on furniture resizable and deformable
    PropertyChangeListener sizeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateSizeComponents(controller);
        }
      };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.RESIZABLE, sizeListener);
    controller.addPropertyChangeListener(HomeFurnitureController.Property.DEFORMABLE, sizeListener);

    this.dialogTitle = preferences.getLocalizedString(HomeFurniturePanel.class, "homeFurniture.title");
  }

  /**
   * Updates roll and pitch radio buttons.
   */
  private void updateHorizontalAxisRadioButtons(HomeFurnitureController controller) {
    if (controller.getHorizontalAxis() == null) {
      SwingTools.deselectAllRadioButtons(this.rollRadioButton, this.pitchRadioButton);
    } else {
      switch (controller.getHorizontalAxis()) {
        case ROLL :
          this.rollRadioButton.setSelected(true);
          break;
        case PITCH :
          this.pitchRadioButton.setSelected(true);
          break;
      }
    }
  }

  /**
   * Updates color, texture and materials radio buttons.
   */
  private void updatePaintRadioButtons(HomeFurnitureController controller) {
    if (controller.getPaint() == null) {
      SwingTools.deselectAllRadioButtons(this.defaultColorAndTextureRadioButton,
          this.colorRadioButton, this.textureRadioButton, this.modelMaterialsRadioButton);
    } else {
      switch (controller.getPaint()) {
        case DEFAULT :
          this.defaultColorAndTextureRadioButton.setSelected(true);
          break;
        case COLORED :
          this.colorRadioButton.setSelected(true);
          break;
        case TEXTURED :
            this.textureRadioButton.setSelected(true);
          break;
        case MODEL_MATERIALS :
          if (this.modelMaterialsRadioButton != null) {
            this.modelMaterialsRadioButton.setSelected(true);
          }
          break;
      }
      updateShininessRadioButtons(controller);
    }
  }

  /**
   * Updates shininess radio buttons.
   */
  private void updateShininessRadioButtons(HomeFurnitureController controller) {
    if (controller.isPropertyEditable(HomeFurnitureController.Property.SHININESS)) {
      if (controller.getShininess() == HomeFurnitureController.FurnitureShininess.DEFAULT) {
        this.defaultShininessRadioButton.setSelected(true);
      } else if (controller.getShininess() == HomeFurnitureController.FurnitureShininess.MATT) {
        this.mattRadioButton.setSelected(true);
      } else if (controller.getShininess() == HomeFurnitureController.FurnitureShininess.SHINY) {
        this.shinyRadioButton.setSelected(true);
      } else { // null
        SwingTools.deselectAllRadioButtons(this.defaultShininessRadioButton, this.mattRadioButton, this.shinyRadioButton);
      }
      boolean shininessEnabled = controller.getPaint() != HomeFurnitureController.FurniturePaint.MODEL_MATERIALS;
      this.defaultShininessRadioButton.setEnabled(shininessEnabled);
      this.mattRadioButton.setEnabled(shininessEnabled);
      this.shinyRadioButton.setEnabled(shininessEnabled);
      if (!shininessEnabled) {
        SwingTools.deselectAllRadioButtons(this.defaultShininessRadioButton, this.mattRadioButton, this.shinyRadioButton);
      }
    }
  }

  /**
   * Updates size components depending on the fact that furniture is resizable or not.
   */
  private void updateSizeComponents(final HomeFurnitureController controller) {
    boolean editableSize = controller.isResizable();
    this.widthLabel.setEnabled(editableSize);
    this.widthSpinner.setEnabled(editableSize);
    this.depthLabel.setEnabled(editableSize);
    this.depthSpinner.setEnabled(editableSize);
    this.heightLabel.setEnabled(editableSize);
    this.heightSpinner.setEnabled(editableSize);
    this.keepProportionsCheckBox.setEnabled(editableSize && controller.isDeformable()
        // Disable proportional if selection is rotated around horizontal axis when no 3D is available
        && (!Boolean.getBoolean("com.eteks.sweethome3d.no3D")
            || controller.getRoll() != null
                && controller.getRoll() == 0
                && controller.getPitch() != null
                && controller.getPitch() == 0));
    this.mirroredModelCheckBox.setEnabled(editableSize);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      if (this.nameLabel != null) {
        this.nameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            HomeFurniturePanel.class, "nameLabel.mnemonic")).getKeyCode());
        this.nameLabel.setLabelFor(this.nameTextField);
      }
      if (this.nameVisibleCheckBox != null) {
        this.nameVisibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            HomeFurniturePanel.class, "nameVisibleCheckBox.mnemonic")).getKeyCode());
      }
      if (this.descriptionLabel != null) {
        this.descriptionLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            HomeFurniturePanel.class, "descriptionLabel.mnemonic")).getKeyCode());
        this.descriptionLabel.setLabelFor(this.descriptionTextField);
      }
      if (this.priceLabel != null) {
        this.priceLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "priceLabel.mnemonic")).getKeyCode());
        this.priceLabel.setLabelFor(this.priceSpinner);
      }
      if (this.valueAddedTaxPercentageLabel != null) {
        this.valueAddedTaxPercentageLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "valueAddedTaxPercentageLabel.mnemonic")).getKeyCode());
        this.valueAddedTaxPercentageLabel.setLabelFor(this.valueAddedTaxPercentageSpinner);
      }
      if (this.xLabel != null) {
        this.xLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "xLabel.mnemonic")).getKeyCode());
        this.xLabel.setLabelFor(this.xSpinner);
      }
      if (this.yLabel != null) {
        this.yLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "yLabel.mnemonic")).getKeyCode());
        this.yLabel.setLabelFor(this.ySpinner);
      }
      if (this.elevationLabel != null) {
        this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "elevationLabel.mnemonic")).getKeyCode());
        this.elevationLabel.setLabelFor(this.elevationSpinner);
      }
      if (this.angleLabel != null) {
        this.angleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "angleLabel.mnemonic")).getKeyCode());
        this.angleLabel.setLabelFor(this.angleSpinner);
      }
      if (this.rollRadioButton != null) {
        this.rollRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "rollRadioButton.mnemonic")).getKeyCode());
      }
      if (this.pitchRadioButton != null) {
        this.pitchRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "pitchRadioButton.mnemonic")).getKeyCode());
      }
      if (this.keepProportionsCheckBox != null) {
        this.keepProportionsCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "keepProportionsCheckBox.mnemonic")).getKeyCode());
      }
      if (this.widthLabel != null) {
        this.widthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "widthLabel.mnemonic")).getKeyCode());
        this.widthLabel.setLabelFor(this.widthSpinner);
      }
      if (this.depthLabel != null) {
        this.depthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "depthLabel.mnemonic")).getKeyCode());
        this.depthLabel.setLabelFor(this.depthSpinner);
      }
      if (this.heightLabel != null) {
        this.heightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "heightLabel.mnemonic")).getKeyCode());
        this.heightLabel.setLabelFor(this.heightSpinner);
      }
      if (this.modelTransformationsButton != null) {
        this.modelTransformationsButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "modelTransformationsButton.mnemonic")).getKeyCode());
      }
      if (this.basePlanItemCheckBox != null) {
        this.basePlanItemCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "basePlanItemCheckBox.mnemonic")).getKeyCode());
      }
      if (this.mirroredModelCheckBox != null) {
        this.mirroredModelCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "mirroredModelCheckBox.mnemonic")).getKeyCode());
      }
      if (this.defaultColorAndTextureRadioButton != null) {
        this.defaultColorAndTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "defaultColorAndTextureRadioButton.mnemonic"))
            .getKeyCode());
        this.colorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "colorRadioButton.mnemonic")).getKeyCode());
        this.textureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "textureRadioButton.mnemonic")).getKeyCode());
      }
      if (this.defaultShininessRadioButton != null) {
        this.defaultShininessRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "defaultShininessRadioButton.mnemonic"))
            .getKeyCode());
        this.mattRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "mattRadioButton.mnemonic")).getKeyCode());
        this.shinyRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "shinyRadioButton.mnemonic")).getKeyCode());
      }
      if (this.visibleCheckBox != null) {
        this.visibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(HomeFurniturePanel.class, "visibleCheckBox.mnemonic")).getKeyCode());
      }
      if (this.lightPowerLabel != null) {
        this.lightPowerLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            HomeFurniturePanel.class, "lightPowerLabel.mnemonic")).getKeyCode());
        this.lightPowerLabel.setLabelFor(this.lightPowerSpinner);
      }
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents(UserPreferences preferences,
                                final HomeFurnitureController controller) {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    boolean priceDisplayed = this.priceLabel != null;
    boolean orientationPanelDisplayed = this.angleLabel != null
        && (this.rollRadioButton != null || this.pitchRadioButton != null);
    JPanel namePanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        HomeFurniturePanel.class, priceDisplayed  ?  "nameAndPricePanel.title"  : "namePanel.title"));
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    int rowGap = OperatingSystem.isMacOSXLeopardOrSuperior() ? 0 : standardGap;
    if (this.nameLabel != null) {
      namePanel.add(this.nameLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          new Insets(0, 0, 0, standardGap), 0, 0));
      namePanel.add(this.nameTextField, new GridBagConstraints(
          1, 0, 3, 1, 1, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }
    if (this.nameVisibleCheckBox != null) {
      namePanel.add(this.nameVisibleCheckBox, new GridBagConstraints(
          4, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
    }
    if (this.descriptionLabel != null) {
      namePanel.add(this.descriptionLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          new Insets(standardGap, 0, 0, standardGap), 0, 0));
      namePanel.add(this.descriptionTextField, new GridBagConstraints(
          1, 1, priceDisplayed ? 1 : 3, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, priceDisplayed  ? 10  : 0), 0, 0));
    }
    if (priceDisplayed) {
      namePanel.add(this.priceLabel, new GridBagConstraints(
          this.descriptionLabel != null ? 2 : 0, 1, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          new Insets(standardGap, 0, 0, standardGap), 0, 0));
      namePanel.add(this.priceSpinner, new GridBagConstraints(
          this.descriptionLabel != null ? 3 : 1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), OperatingSystem.isMacOSX() ? -60 : -30, 0));
      if (this.valueAddedTaxPercentageLabel != null) {
        namePanel.add(this.valueAddedTaxPercentageLabel, new GridBagConstraints(
            this.descriptionLabel != null ? 4 : 2, 1, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
            new Insets(standardGap, 10, 0, standardGap), 0, 0));
        namePanel.add(this.valueAddedTaxPercentageSpinner, new GridBagConstraints(
            this.descriptionLabel != null ? 5 : 3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(standardGap, 0, 0, 0), 0, 0));
      }
    }
    if (namePanel.getComponentCount() > 0) {
      add(namePanel, new GridBagConstraints(0, 0, orientationPanelDisplayed ? 4 : 3, 1, 0, 0, labelAlignment,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, rowGap, 0), 0, 0));
    }
    // Location panel
    JPanel locationPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        HomeFurniturePanel.class, "locationPanel.title"));
    Insets labelInsets = new Insets(0, 0, standardGap, standardGap);
    Insets rightComponentInsets = new Insets(0, 0, standardGap, 0);
    if (this.xLabel != null) {
      locationPanel.add(this.xLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          labelInsets, 0, 0));
      locationPanel.add(this.xSpinner, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    }
    if (this.yLabel != null) {
      locationPanel.add(this.yLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          labelInsets, 0, 0));
      locationPanel.add(this.ySpinner, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    }
    if (this.elevationLabel != null) {
      locationPanel.add(this.elevationLabel, new GridBagConstraints(
          0, 2, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      locationPanel.add(this.elevationSpinner, new GridBagConstraints(
          1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    }
    if (this.angleLabel != null
        && !orientationPanelDisplayed) {
      locationPanel.add(this.angleLabel, new GridBagConstraints(
          0, 3, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      locationPanel.add(this.angleSpinner, new GridBagConstraints(
          1, 3, 1, 1, 0, 1, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    }
    if (this.mirroredModelCheckBox != null) {
      locationPanel.add(this.mirroredModelCheckBox, new GridBagConstraints(
          0, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, OperatingSystem.isMacOSX() ? 5 : 2, 0), 0, 0));
    }
    if (this.basePlanItemCheckBox != null) {
      locationPanel.add(this.basePlanItemCheckBox, new GridBagConstraints(
          0, 5, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
    if (locationPanel.getComponentCount() > 0) {
      locationPanel.add(new JLabel(), new GridBagConstraints(
          0, 100, 2, 1, 0, 1, GridBagConstraints.LINE_START,
          GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      add(locationPanel, new GridBagConstraints(
          0, 1, 1, 1, 1, 1, labelAlignment, GridBagConstraints.BOTH, new Insets(
          0, 0, rowGap, 0), 0, 0));
    }
    if (orientationPanelDisplayed) {
      // Orientation panel
      JPanel orientationPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
          HomeFurniturePanel.class, "orientationPanel.title"));
      JLabel verticalRotationLabel = new JLabel(preferences.getLocalizedString(
          HomeFurniturePanel.class, "verticalRotationLabel.text"));
      JLabel horizontalRotationLabel = new JLabel(preferences.getLocalizedString(
          HomeFurniturePanel.class, "horizontalRotationLabel.text"));
      JLabel orientationLabel = new JLabel(preferences.getLocalizedString(
          HomeFurniturePanel.class, "orientationLabel.text"));
      // There are two possible layout depending whether horizontal and vertical rotation label are defined or not
      boolean layoutWithHorizontalVerticalLabels = verticalRotationLabel.getText().length() > 0
          && horizontalRotationLabel.getText().length() > 0;
      if (this.angleLabel != null) {
        // Row 0 may contain verticalRotationLabel
        orientationPanel.add(this.angleLabel, new GridBagConstraints(
            0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE,
            new Insets(0, new JRadioButton().getPreferredSize().width, OperatingSystem.isMacOSX() || layoutWithHorizontalVerticalLabels ? standardGap : 2, standardGap), 0, 0));
        orientationPanel.add(this.angleSpinner, new GridBagConstraints(
            1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.HORIZONTAL, rightComponentInsets, 10, 0));
      }
      Insets secondRadioButtonInsets = new Insets(0, 0, layoutWithHorizontalVerticalLabels ? 2 : (OperatingSystem.isMacOSX() ? 5 : 1), standardGap);
      Insets secondSpinnerInsets = new Insets(0, 0, layoutWithHorizontalVerticalLabels ? 2 : standardGap, 0);
      Insets thirdRadioButtonInsets = new Insets(0, 0, 0, standardGap);
      Insets thirdSpinnerinsets = new Insets(0, 0, 0, 0);
      if (this.pitchRadioButton != null) {
        // Row 2 may contain horizontalRotationLabel
        orientationPanel.add(this.pitchRadioButton, new GridBagConstraints(
            0, layoutWithHorizontalVerticalLabels ? 3 : 4, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, layoutWithHorizontalVerticalLabels ? secondRadioButtonInsets : thirdRadioButtonInsets, 0, 0));
        orientationPanel.add(this.pitchSpinner, new GridBagConstraints(
            1, layoutWithHorizontalVerticalLabels ? 3 : 4, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.HORIZONTAL, layoutWithHorizontalVerticalLabels ? secondSpinnerInsets : thirdSpinnerinsets, 10, 0));
      }
      if (this.rollRadioButton != null) {
        orientationPanel.add(this.rollRadioButton, new GridBagConstraints(
            0, layoutWithHorizontalVerticalLabels ? 4 : 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, layoutWithHorizontalVerticalLabels ? thirdRadioButtonInsets : secondRadioButtonInsets, 0, 0));
        orientationPanel.add(this.rollSpinner, new GridBagConstraints(
            1, layoutWithHorizontalVerticalLabels ? 4 : 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.HORIZONTAL, layoutWithHorizontalVerticalLabels ? thirdSpinnerinsets : secondSpinnerInsets, 10, 0));
      }
      if (this.rollRadioButton != null
          && this.pitchRadioButton != null) {
        if (controller.isTexturable()) {
          // Do not display orientation label information for not texturable furniture to keep a balanced dialog box
          orientationPanel.add(new JLabel(new ImageIcon(getClass().getResource("resources/furnitureOrientation.png"))), new GridBagConstraints(
              0, 6, 2, 1, 1, layoutWithHorizontalVerticalLabels ? 1 : 0, GridBagConstraints.CENTER,
              GridBagConstraints.BOTH, new Insets(10, 0, standardGap, 0), 0, 0));
        }
        if (layoutWithHorizontalVerticalLabels) {
          // Add information labels about axes adjusting insets to align with components in other panels
          orientationPanel.add(verticalRotationLabel, new GridBagConstraints(
              0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
              GridBagConstraints.NONE, new Insets(OperatingSystem.isMacOSX() ? 5 : 3, 0, OperatingSystem.isMacOSX() ? 10 : 8, 0), 0, 0));
          orientationPanel.add(horizontalRotationLabel, new GridBagConstraints(
              0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START,
              GridBagConstraints.NONE, new Insets(OperatingSystem.isMacOSX() ? 5 : 3, 0, OperatingSystem.isMacOSX() ? 9 : 8, 0), 0, 0));
        } else {
          // Use same font for label as tooltips
          orientationLabel.setFont(UIManager.getFont("ToolTip.font"));
          orientationPanel.add(orientationLabel, new GridBagConstraints(
              0, 7, 2, 1, 1, 1, GridBagConstraints.NORTH,
              GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
        }
      }
      if (orientationPanel.getComponentCount() > 0) {
        add(orientationPanel, new GridBagConstraints(
            1, 1, 1, 2, 1, 0, labelAlignment, GridBagConstraints.BOTH, new Insets(
            0, 0, rowGap, 0), 0, 0));
      }
    }
    // Size panel
    JPanel sizePanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        HomeFurniturePanel.class, "sizePanel.title"));
    if (this.widthLabel != null) {
      sizePanel.add(this.widthLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      sizePanel.add(this.widthSpinner, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, rightComponentInsets, -10, 0));
    }
    if (this.depthLabel != null) {
      sizePanel.add(this.depthLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      sizePanel.add(this.depthSpinner, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, rightComponentInsets, -10, 0));
    }
    if (this.heightLabel != null) {
      sizePanel.add(this.heightLabel, new GridBagConstraints(
          0, 2, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      sizePanel.add(this.heightSpinner, new GridBagConstraints(
          1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, rightComponentInsets, -10, 0));
    }
    if (this.keepProportionsCheckBox != null) {
      sizePanel.add(this.keepProportionsCheckBox, new GridBagConstraints(
          0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, OperatingSystem.isMacOSX() ? 5 : 2, 0), 0, 0));
    }
    if (this.modelTransformationsButton != null) {
      sizePanel.add(this.modelTransformationsButton, new GridBagConstraints(
          0, 4, 2, 1, 0, 1, GridBagConstraints.NORTH,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
    if (sizePanel.getComponentCount() > 0) {
      sizePanel.add(new JLabel(), new GridBagConstraints(
          0, 100, 2, 1, 0, 1, GridBagConstraints.LINE_START,
          GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      add(sizePanel, new GridBagConstraints(
          orientationPanelDisplayed ? 2 : 1, 1, 2, 1, 1, 1, labelAlignment,
          GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));
    }
    final JPanel paintPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        HomeFurniturePanel.class, "colorAndTexturePanel.title"));
    if (this.defaultColorAndTextureRadioButton != null) {
      int buttonPadY;
      int buttonsBottomInset;
      if (OperatingSystem.isMacOSXLeopardOrSuperior()
          && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
        // Ensure the top and bottom of segmented buttons are correctly drawn
        buttonPadY = 4;
        buttonsBottomInset = -4;
      } else {
        buttonPadY = 0;
        buttonsBottomInset = 0;
      }
      // Color and Texture panel
      paintPanel.add(this.defaultColorAndTextureRadioButton, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      paintPanel.add(this.colorRadioButton, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
      paintPanel.add(this.colorButton, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, buttonsBottomInset, 0), 0, buttonPadY));
      if (this.textureComponent != null) {
        paintPanel.add(this.textureRadioButton, new GridBagConstraints(
            0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(standardGap, 0, 0, standardGap), 0, 0));
        paintPanel.add(this.textureComponent, new GridBagConstraints(
            1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, buttonsBottomInset, 0), 0, buttonPadY));
      }
      if (this.modelMaterialsComponent != null) {
        paintPanel.add(this.modelMaterialsRadioButton, new GridBagConstraints(
            0, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(standardGap, 0, 0, standardGap), 0, 0));
        paintPanel.add(this.modelMaterialsComponent, new GridBagConstraints(
            1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, buttonsBottomInset, 0), 0, buttonPadY));
      }
      add(paintPanel, new GridBagConstraints(
          0, 2, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));

      controller.addPropertyChangeListener(HomeFurnitureController.Property.TEXTURABLE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              paintPanel.setVisible(controller.isTexturable());
            }
          });
      paintPanel.setVisible(controller.isTexturable());
    }
    if (this.defaultShininessRadioButton != null) {
      // Shininess panel
      final JPanel shininessPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
          HomeFurniturePanel.class, "shininessPanel.title"));
      shininessPanel.add(this.defaultShininessRadioButton, new GridBagConstraints(
          0, 0, 1, 1, 0, 1, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
      shininessPanel.add(this.mattRadioButton, new GridBagConstraints(
          0, 1, 1, 1, 0, 1, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      shininessPanel.add(this.shinyRadioButton, new GridBagConstraints(
          0, 2, 1, 1, 0, 1, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(standardGap, 0, 0, 0), 0, 0));
      if (paintPanel.getComponentCount() == 7) {
        shininessPanel.add(new JLabel(), new GridBagConstraints(
            0, 3, 1, 1, 0, 1, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(standardGap, 0, 0, 0), 0, 0));
      }
      add(shininessPanel, new GridBagConstraints(
          orientationPanelDisplayed ? 2 : 1, 2, 2, 1, 0, 0, labelAlignment,
          GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));

      controller.addPropertyChangeListener(HomeFurnitureController.Property.TEXTURABLE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              shininessPanel.setVisible(controller.isTexturable());
            }
          });
      shininessPanel.setVisible(controller.isTexturable());
    }
    // Last row
    if (this.visibleCheckBox != null) {
      add(this.visibleCheckBox, new GridBagConstraints(
          0, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
    }
    if (this.lightPowerLabel != null) {
      add(this.lightPowerLabel, new GridBagConstraints(
          orientationPanelDisplayed ? 2 : 1, 3, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, new Insets(0, 10, 0, standardGap), 0, 0));
      add(this.lightPowerSpinner, new GridBagConstraints(
          orientationPanelDisplayed ? 3 : 2, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    }
  }

  /**
   * Displays this panel in a modal dialog box.
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.nameTextField) == JOptionPane.OK_OPTION) {
      this.controller.modifyFurniture();
    }
  }

  /**
   * Displays a panel which lets the user modify the transformations applied to the edited model.
   */
  private void displayModelTransformationsView(UserPreferences preferences, HomeFurnitureController controller) {
    new ModelTransformationsPanel(preferences, controller).displayView(this);
  }

  /**
   * A panel that displays a preview of a model to let the user change transformations applied on it.
   */
  private static class ModelTransformationsPanel extends JPanel {
    private ModelPreviewComponent   previewComponent;
    private JLabel                  transformationsLabel;
    private JButton                 resetTransformationsButton;
    private JButton                 viewFromFrontButton;
    private JButton                 viewFromSideButton;
    private JButton                 viewFromTopButton;
    private String                  dialogTitle;
    private HomeFurnitureController controller;

    public ModelTransformationsPanel(UserPreferences preferences,
                                     HomeFurnitureController controller) {
      super(new GridBagLayout());
      this.controller = controller;
      createComponents(preferences, controller);
      setMnemonics(preferences);
      layoutComponents();
    }

    /**
     * Creates and initializes components.
     */
    private void createComponents(final UserPreferences preferences,
                                  final HomeFurnitureController controller) {
      ModelMaterialsController modelMaterialsController = controller.getModelMaterialsController();
      this.previewComponent = new ModelPreviewComponent(true, true, true, true);
      this.previewComponent.setFocusable(false);
      float resolutionScale = SwingTools.getResolutionScale();
      this.previewComponent.setPreferredSize(new Dimension((int)(400 * resolutionScale), (int)(400 * resolutionScale)));
      this.previewComponent.setModel(modelMaterialsController.getModel(), modelMaterialsController.isBackFaceShown(), modelMaterialsController.getModelRotation(),
          modelMaterialsController.getModelWidth(), modelMaterialsController.getModelDepth(), modelMaterialsController.getModelHeight());
      this.previewComponent.setModelMaterials(modelMaterialsController.getMaterials());
      this.previewComponent.setModelTranformations(controller.getModelTransformations());
      this.previewComponent.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseReleased(MouseEvent ev) {
            updateComponents(controller);
          }
        });

      this.transformationsLabel = new JLabel(preferences.getLocalizedString(ModelTransformationsPanel.class, "transformationsLabel.text"));

      this.resetTransformationsButton = new JButton(new AbstractAction(SwingTools.getLocalizedLabelText(preferences,
              ModelTransformationsPanel.class, "resetTransformationsButton.text")) {
          public void actionPerformed(ActionEvent ev) {
            previewComponent.resetModelTranformations();
            updateComponents(controller);
          }
        });
      updateComponents(controller);
      this.viewFromFrontButton = new JButton(new AbstractAction(SwingTools.getLocalizedLabelText(preferences,
              ModelTransformationsPanel.class, "viewFromFrontButton.text")) {
          public void actionPerformed(ActionEvent ev) {
            previewComponent.setViewYaw(0);
            previewComponent.setViewPitch(0);
          }
        });
      this.viewFromSideButton = new JButton(new AbstractAction(SwingTools.getLocalizedLabelText(preferences,
            ModelTransformationsPanel.class, "viewFromSideButton.text")) {
          public void actionPerformed(ActionEvent ev) {
            previewComponent.setViewYaw((float)(Math.PI / 2));
            previewComponent.setViewPitch(0);
          }
        });
      this.viewFromTopButton = new JButton(new AbstractAction(SwingTools.getLocalizedLabelText(preferences,
            ModelTransformationsPanel.class, "viewFromTopButton.text")) {
          public void actionPerformed(ActionEvent ev) {
            previewComponent.setViewYaw(0);
            previewComponent.setViewPitch(-(float)(Math.PI / 2));
          }
        });

      this.dialogTitle = preferences.getLocalizedString(ModelTransformationsPanel.class, "modelTransformations.title");
    }

    private void updateComponents(HomeFurnitureController controller) {
      this.resetTransformationsButton.setEnabled(this.previewComponent.getModelTransformations() != null);
    }

    /**
     * Sets components mnemonics and label / component associations.
     */
    private void setMnemonics(UserPreferences preferences) {
      if (!OperatingSystem.isMacOSX()) {
        this.resetTransformationsButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(ModelTransformationsPanel.class, "resetTransformationsButton.mnemonic")).getKeyCode());
        this.viewFromFrontButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(ModelTransformationsPanel.class, "viewFromFrontButton.mnemonic")).getKeyCode());
        this.viewFromSideButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(ModelTransformationsPanel.class, "viewFromSideButton.mnemonic")).getKeyCode());
        this.viewFromTopButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(ModelTransformationsPanel.class, "viewFromTopButton.mnemonic")).getKeyCode());
      }
    }

    /**
     * Layouts components in panel with their labels.
     */
    private void layoutComponents() {
      int standardGap = Math.round(5 * SwingTools.getResolutionScale());
      // Preview
      add(this.transformationsLabel, new GridBagConstraints(
          0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, standardGap, 10), 0, 0));
      this.previewComponent.setPreferredSize(new Dimension(400, 400));
      add(this.previewComponent, new GridBagConstraints(
          0, 1, 1, 10, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.NONE, new Insets(2, 0, 0, 15), 0, 0));
      add(this.resetTransformationsButton, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.BOTH, new Insets(2, 0, standardGap, 0), 0, 0));
      add(this.viewFromFrontButton, new GridBagConstraints(
          1, 2, 1, 1, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.BOTH, new Insets(2, 0, standardGap, 0), 0, 0));
      add(this.viewFromSideButton, new GridBagConstraints(
          1, 3, 1, 1, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.BOTH, new Insets(2, 0, standardGap, 0), 0, 0));
      add(this.viewFromTopButton, new GridBagConstraints(
          1, 4, 1, 1, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.BOTH, new Insets(2, 0, standardGap, 0), 0, 0));
    }

    private void updateLocationAndSize() {
      float modelX = this.previewComponent.getModelX();
      float modelY = this.previewComponent.getModelY();
      float pieceX = (float)(this.controller.getX()
          + modelX * Math.cos(this.controller.getAngle()) - modelY * Math.sin(this.controller.getAngle()));
      float pieceY = (float)(this.controller.getY()
          + modelX * Math.sin(this.controller.getAngle()) + modelY * Math.cos(this.controller.getAngle()));
      float pieceElevation = this.controller.getElevation()
          + this.previewComponent.getModelElevation() + this.controller.getHeight() / 2;
      Transformation [] modelTransformations = this.previewComponent.getModelTransformations();
      this.controller.setModelTransformations(modelTransformations != null ? modelTransformations : new Transformation [0],
          pieceX, pieceY, pieceElevation,
          this.previewComponent.getModelWidth(),
          this.previewComponent.getModelDepth(),
          this.previewComponent.getModelHeight());
    }

    /**
     * Displays this panel in a modal dialog box.
     */
    public void displayView(View parent) {
      JComponent parentComponent = SwingUtilities.getRootPane((JComponent)parent);
      if (SwingTools.showConfirmDialog(parentComponent, this, this.dialogTitle, null) == JOptionPane.OK_OPTION) {
        updateLocationAndSize();
      }
    }
  }
}
