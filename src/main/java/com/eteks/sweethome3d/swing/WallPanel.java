/*
 * WallPanel.java 29 mai 07
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.viewcontroller.BaseboardChoiceController;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.WallController;

/**
 * Wall editing panel.
 * @author Emmanuel Puybaret
 */
public class WallPanel extends JPanel implements DialogView {
  private final WallController controller;
  private JLabel               xStartLabel;
  private JSpinner             xStartSpinner;
  private JLabel               yStartLabel;
  private JSpinner             yStartSpinner;
  private JLabel               xEndLabel;
  private JSpinner             xEndSpinner;
  private JLabel               yEndLabel;
  private JSpinner             yEndSpinner;
  private JLabel               distanceToEndPointLabel;
  private JSpinner             distanceToEndPointSpinner;
  private JRadioButton         leftSideColorRadioButton;
  private ColorButton          leftSideColorButton;
  private JRadioButton         leftSideTextureRadioButton;
  private JComponent           leftSideTextureComponent;
  private JRadioButton         leftSideMattRadioButton;
  private JButton              leftSideBaseboardButton;
  private JRadioButton         leftSideShinyRadioButton;
  private JRadioButton         rightSideColorRadioButton;
  private ColorButton          rightSideColorButton;
  private JRadioButton         rightSideTextureRadioButton;
  private JComponent           rightSideTextureComponent;
  private JRadioButton         rightSideMattRadioButton;
  private JRadioButton         rightSideShinyRadioButton;
  private JButton              rightSideBaseboardButton;
  private JLabel               patternLabel;
  private JComboBox            patternComboBox;
  private JLabel               topColorLabel;
  private JRadioButton         topDefaultColorRadioButton;
  private JRadioButton         topColorRadioButton;
  private ColorButton          topColorButton;
  private JRadioButton         rectangularWallRadioButton;
  private JLabel               rectangularWallHeightLabel;
  private JSpinner             rectangularWallHeightSpinner;
  private JRadioButton         slopingWallRadioButton;
  private JLabel               slopingWallHeightAtStartLabel;
  private JSpinner             slopingWallHeightAtStartSpinner;
  private JLabel               slopingWallHeightAtEndLabel;
  private JSpinner             slopingWallHeightAtEndSpinner;
  private JLabel               thicknessLabel;
  private JSpinner             thicknessSpinner;
  private JLabel               arcExtentLabel;
  private JSpinner             arcExtentSpinner;
  private JLabel               wallOrientationLabel;
  private String               dialogTitle;

  /**
   * Creates a panel that displays wall data according to the units set in
   * <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public WallPanel(UserPreferences preferences,
                   WallController controller) {
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
                                final WallController controller) {
    // Get unit name matching current unit
    String unitName = preferences.getLengthUnit().getName();

    // Create X start label and its spinner bound to X_START controller property
    this.xStartLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "xLabel.text", unitName));
    final float maximumLength = preferences.getLengthUnit().getMaximumLength();
    final NullableSpinner.NullableSpinnerLengthModel xStartSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xStartSpinner = new NullableSpinner(xStartSpinnerModel);
    xStartSpinnerModel.setNullable(controller.getXStart() == null);
    xStartSpinnerModel.setLength(controller.getXStart());
    final PropertyChangeListener xStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xStartSpinnerModel.setNullable(ev.getNewValue() == null);
          xStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
    xStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
          controller.setXStart(xStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
        }
      });

    // Create Y start label and its spinner bound to Y_START controller property
    this.yStartLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel yStartSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.yStartSpinner = new NullableSpinner(yStartSpinnerModel);
    yStartSpinnerModel.setNullable(controller.getYStart() == null);
    yStartSpinnerModel.setLength(controller.getYStart());
    final PropertyChangeListener yStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yStartSpinnerModel.setNullable(ev.getNewValue() == null);
          yStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
    yStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
          controller.setYStart(yStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
        }
      });

    // Create X end label and its spinner bound to X_END controller property
    this.xEndLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "xLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel xEndSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xEndSpinner = new NullableSpinner(xEndSpinnerModel);
    xEndSpinnerModel.setNullable(controller.getXEnd() == null);
    xEndSpinnerModel.setLength(controller.getXEnd());
    final PropertyChangeListener xEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xEndSpinnerModel.setNullable(ev.getNewValue() == null);
          xEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
    xEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
          controller.setXEnd(xEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
        }
      });

    // Create Y end label and its spinner bound to Y_END controller property
    this.yEndLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel yEndSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.yEndSpinner = new NullableSpinner(yEndSpinnerModel);
    yEndSpinnerModel.setNullable(controller.getYEnd() == null);
    yEndSpinnerModel.setLength(controller.getYEnd());
    final PropertyChangeListener yEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yEndSpinnerModel.setNullable(ev.getNewValue() == null);
          yEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
    yEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
          controller.setYEnd(yEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
        }
      });

    // Create distance to end point label and its spinner bound to DISTANCE_TO_END_POINT controller property
    this.distanceToEndPointLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "distanceToEndPointLabel.text", unitName));
    final float minimumLength = preferences.getLengthUnit().getMinimumLength();
    final NullableSpinner.NullableSpinnerLengthModel distanceToEndPointSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, 2 * maximumLength * (float)Math.sqrt(2));
    this.distanceToEndPointSpinner = new NullableSpinner(distanceToEndPointSpinnerModel);
    distanceToEndPointSpinnerModel.setNullable(controller.getLength() == null);
    distanceToEndPointSpinnerModel.setLength(controller.getDistanceToEndPoint());
    final PropertyChangeListener distanceToEndPointChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          distanceToEndPointSpinnerModel.setNullable(ev.getNewValue() == null);
          distanceToEndPointSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.DISTANCE_TO_END_POINT,
        distanceToEndPointChangeListener);
    distanceToEndPointSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.DISTANCE_TO_END_POINT,
              distanceToEndPointChangeListener);
          controller.setDistanceToEndPoint(distanceToEndPointSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.DISTANCE_TO_END_POINT,
              distanceToEndPointChangeListener);
        }
      });

    // Left side color and texture buttons bound to left side controller properties
    this.leftSideColorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "leftSideColorRadioButton.text"));
    this.leftSideColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideColorRadioButton.isSelected()) {
            controller.setLeftSidePaint(WallController.WallPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_PAINT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateLeftSideColorRadioButtons(controller);
          }
        });

    this.leftSideColorButton = new ColorButton(preferences);
    this.leftSideColorButton.setColorDialogTitle(preferences.getLocalizedString(
        WallPanel.class, "leftSideColorDialog.title"));
    this.leftSideColorButton.setColor(controller.getLeftSideColor());
    this.leftSideColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setLeftSideColor(leftSideColorButton.getColor());
            controller.setLeftSidePaint(WallController.WallPaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_COLOR,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            leftSideColorButton.setColor(controller.getLeftSideColor());
          }
        });

    this.leftSideTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "leftSideTextureRadioButton.text"));
    this.leftSideTextureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideTextureRadioButton.isSelected()) {
            controller.setLeftSidePaint(WallController.WallPaint.TEXTURED);
          }
        }
      });

    this.leftSideTextureComponent = (JComponent)controller.getLeftSideTextureController().getView();

    ButtonGroup leftSideColorButtonGroup = new ButtonGroup();
    leftSideColorButtonGroup.add(this.leftSideColorRadioButton);
    leftSideColorButtonGroup.add(this.leftSideTextureRadioButton);
    updateLeftSideColorRadioButtons(controller);

    // Left side shininess radio buttons bound to LEFT_SIDE_SHININESS controller property
    this.leftSideMattRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "leftSideMattRadioButton.text"));
    this.leftSideMattRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideMattRadioButton.isSelected()) {
            controller.setLeftSideShininess(0f);
          }
        }
      });
    PropertyChangeListener leftSideShininessListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateLeftSideShininessRadioButtons(controller);
        }
      };
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_SHININESS,
        leftSideShininessListener);

    this.leftSideShinyRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "leftSideShinyRadioButton.text"));
    this.leftSideShinyRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideShinyRadioButton.isSelected()) {
            controller.setLeftSideShininess(0.25f);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_SHININESS,
        leftSideShininessListener);

    ButtonGroup leftSideShininessButtonGroup = new ButtonGroup();
    leftSideShininessButtonGroup.add(this.leftSideMattRadioButton);
    leftSideShininessButtonGroup.add(this.leftSideShinyRadioButton);
    updateLeftSideShininessRadioButtons(controller);

    this.leftSideBaseboardButton = new JButton(new ResourceAction.ButtonAction(
        new ResourceAction(preferences, WallPanel.class, "MODIFY_LEFT_SIDE_BASEBOARD", true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            editBaseboard((JComponent)ev.getSource(),
                preferences.getLocalizedString(WallPanel.class, "leftSideBaseboardDialog.title"),
                controller.getLeftSideBaseboardController());
          }
        }));

    // Right side color and texture buttons bound to right side controller properties
    this.rightSideColorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "rightSideColorRadioButton.text"));
    this.rightSideColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (rightSideColorRadioButton.isSelected()) {
            controller.setRightSidePaint(WallController.WallPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_PAINT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateRightSideColorRadioButtons(controller);
          }
        });

    this.rightSideColorButton = new ColorButton(preferences);
    this.rightSideColorButton.setColor(controller.getRightSideColor());
    this.rightSideColorButton.setColorDialogTitle(preferences.getLocalizedString(
        WallPanel.class, "rightSideColorDialog.title"));
    this.rightSideColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setRightSideColor(rightSideColorButton.getColor());
            controller.setRightSidePaint(WallController.WallPaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_COLOR,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            rightSideColorButton.setColor(controller.getRightSideColor());
          }
        });

    this.rightSideTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "rightSideTextureRadioButton.text"));
    this.rightSideTextureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (rightSideTextureRadioButton.isSelected()) {
            controller.setRightSidePaint(WallController.WallPaint.TEXTURED);
          }
        }
      });

    this.rightSideTextureComponent = (JComponent)controller.getRightSideTextureController().getView();

    ButtonGroup rightSideColorButtonGroup = new ButtonGroup();
    rightSideColorButtonGroup.add(this.rightSideColorRadioButton);
    rightSideColorButtonGroup.add(this.rightSideTextureRadioButton);
    updateRightSideColorRadioButtons(controller);

    // Right side shininess radio buttons bound to LEFT_SIDE_SHININESS controller property
    this.rightSideMattRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "rightSideMattRadioButton.text"));
    this.rightSideMattRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (rightSideMattRadioButton.isSelected()) {
            controller.setRightSideShininess(0f);
          }
        }
      });
    PropertyChangeListener rightSideShininessListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateRightSideShininessRadioButtons(controller);
        }
      };
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_SHININESS,
        rightSideShininessListener);

    this.rightSideShinyRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "rightSideShinyRadioButton.text"));
    this.rightSideShinyRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (rightSideShinyRadioButton.isSelected()) {
            controller.setRightSideShininess(0.25f);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_SHININESS,
        rightSideShininessListener);

    ButtonGroup rightSideShininessButtonGroup = new ButtonGroup();
    rightSideShininessButtonGroup.add(this.rightSideMattRadioButton);
    rightSideShininessButtonGroup.add(this.rightSideShinyRadioButton);
    updateRightSideShininessRadioButtons(controller);

    this.rightSideBaseboardButton = new JButton(new ResourceAction.ButtonAction(
        new ResourceAction(preferences, WallPanel.class, "MODIFY_RIGHT_SIDE_BASEBOARD", true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            editBaseboard((JComponent)ev.getSource(),
                preferences.getLocalizedString(WallPanel.class, "rightSideBaseboardDialog.title"),
                controller.getRightSideBaseboardController());
          }
        }));

    // Top pattern and 3D color
    this.patternLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "patternLabel.text"));
    List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
    if (controller.getPattern() == null) {
      patterns = new ArrayList<TextureImage>(patterns);
      patterns.add(0, null);
    }
    this.patternComboBox = new JComboBox(new DefaultComboBoxModel(patterns.toArray()));
    final float resolutionScale = SwingTools.getResolutionScale();
    this.patternComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          TextureImage pattern = (TextureImage)value;
          final Component component = super.getListCellRendererComponent(
              list, pattern == null ? " " : "", index, isSelected, cellHasFocus);
          if (pattern != null) {
            final BufferedImage patternImage = SwingTools.getPatternImage(
                pattern, list.getBackground(), list.getForeground());
            setIcon(new Icon() {
                public int getIconWidth() {
                  return (int)(patternImage.getWidth() * 4 * resolutionScale + 1);
                }

                public int getIconHeight() {
                  return (int)(patternImage.getHeight() * resolutionScale + 2);
                }

                public void paintIcon(Component c, Graphics g, int x, int y) {
                  Graphics2D g2D = (Graphics2D)g;
                  g2D.scale(resolutionScale, resolutionScale);
                  for (int i = 0; i < 4; i++) {
                    g2D.drawImage(patternImage, x + i * patternImage.getWidth(), y + 1, list);
                  }
                  g2D.scale(1 / resolutionScale, 1 / resolutionScale);
                  g2D.setColor(list.getForeground());
                  g2D.drawRect(x, y, getIconWidth() - 2, getIconHeight() - 1);
                }
              });
          }
          return component;
        }
      });
    this.patternComboBox.setSelectedItem(controller.getPattern());
    this.patternComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setPattern((TextureImage)patternComboBox.getSelectedItem());
        }
      });
    controller.addPropertyChangeListener(WallController.Property.PATTERN,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            patternComboBox.setSelectedItem(controller.getPattern());
          }
        });

    this.topColorLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "topColorLabel.text"));
    this.topDefaultColorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "topDefaultColorRadioButton.text"));
    this.topDefaultColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (topDefaultColorRadioButton.isSelected()) {
            controller.setTopPaint(WallController.WallPaint.DEFAULT);
          }
        }
      });
    this.topColorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "topColorRadioButton.text"));
    this.topColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (topColorRadioButton.isSelected()) {
            controller.setTopPaint(WallController.WallPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.TOP_PAINT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateTopColorRadioButtons(controller);
          }
        });
    this.topColorButton = new ColorButton(preferences);
    this.topColorButton.setColorDialogTitle(preferences.getLocalizedString(
        WallPanel.class, "topColorDialog.title"));
    this.topColorButton.setColor(controller.getTopColor());
    this.topColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setTopColor(topColorButton.getColor());
            controller.setTopPaint(WallController.WallPaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(WallController.Property.TOP_COLOR,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            topColorButton.setColor(controller.getTopColor());
          }
        });

    ButtonGroup topColorGroup = new ButtonGroup();
    topColorGroup.add(this.topDefaultColorRadioButton);
    topColorGroup.add(this.topColorRadioButton);
    updateTopColorRadioButtons(controller);

    // Create height label and its spinner bound to RECTANGULAR_WALL_HEIGHT controller property
    this.rectangularWallRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "rectangularWallRadioButton.text"));
    this.rectangularWallRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (rectangularWallRadioButton.isSelected()) {
            controller.setShape(WallController.WallShape.RECTANGULAR_WALL);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.SHAPE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateWallShapeRadioButtons(controller);
          }
        });

    this.rectangularWallHeightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
            WallPanel.class, "rectangularWallHeightLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel rectangularWallHeightSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength);
    this.rectangularWallHeightSpinner = new NullableSpinner(rectangularWallHeightSpinnerModel);
    rectangularWallHeightSpinnerModel.setNullable(controller.getRectangularWallHeight() == null);
    rectangularWallHeightSpinnerModel.setLength(controller.getRectangularWallHeight());
    final PropertyChangeListener rectangularWallHeightChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          rectangularWallHeightSpinnerModel.setNullable(ev.getNewValue() == null);
          rectangularWallHeightSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT,
        rectangularWallHeightChangeListener);
    rectangularWallHeightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT,
              rectangularWallHeightChangeListener);
          controller.setRectangularWallHeight(rectangularWallHeightSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT,
              rectangularWallHeightChangeListener);
        }
      });

    this.slopingWallRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "slopingWallRadioButton.text"));
    this.slopingWallRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (slopingWallRadioButton.isSelected()) {
            controller.setShape(WallController.WallShape.SLOPING_WALL);
          }
        }
      });
    ButtonGroup wallHeightButtonGroup = new ButtonGroup();
    wallHeightButtonGroup.add(this.rectangularWallRadioButton);
    wallHeightButtonGroup.add(this.slopingWallRadioButton);
    updateWallShapeRadioButtons(controller);

    // Create height at start label and its spinner bound to SLOPING_WALL_HEIGHT_AT_START controller property
    this.slopingWallHeightAtStartLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "slopingWallHeightAtStartLabel.text"));
    final float minimumHeight = controller.getSlopingWallHeightAtStart() != null && controller.getSlopingWallHeightAtEnd() != null
        ? 0
        : minimumLength;
    final NullableSpinner.NullableSpinnerLengthModel slopingWallHeightAtStartSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumHeight, maximumLength);
    this.slopingWallHeightAtStartSpinner = new NullableSpinner(slopingWallHeightAtStartSpinnerModel);
    slopingWallHeightAtStartSpinnerModel.setNullable(controller.getSlopingWallHeightAtStart() == null);
    slopingWallHeightAtStartSpinnerModel.setLength(controller.getSlopingWallHeightAtStart());
    final PropertyChangeListener slopingWallHeightAtStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          slopingWallHeightAtStartSpinnerModel.setNullable(ev.getNewValue() == null);
          slopingWallHeightAtStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START,
        slopingWallHeightAtStartChangeListener);
    slopingWallHeightAtStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START,
              slopingWallHeightAtStartChangeListener);
          controller.setSlopingWallHeightAtStart(slopingWallHeightAtStartSpinnerModel.getLength());
          if (minimumHeight == 0
              && controller.getSlopingWallHeightAtStart() == 0
              && controller.getSlopingWallHeightAtEnd() == 0) {
            // Ensure wall height is never 0
            controller.setSlopingWallHeightAtEnd(minimumLength);
          }
          controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START,
              slopingWallHeightAtStartChangeListener);
        }
      });

    // Create height at end label and its spinner bound to SLOPING_WALL_HEIGHT_AT_END controller property
    this.slopingWallHeightAtEndLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "slopingWallHeightAtEndLabel.text"));
    final NullableSpinner.NullableSpinnerLengthModel slopingWallHeightAtEndSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumHeight, maximumLength);
    this.slopingWallHeightAtEndSpinner = new NullableSpinner(slopingWallHeightAtEndSpinnerModel);
    slopingWallHeightAtEndSpinnerModel.setNullable(controller.getSlopingWallHeightAtEnd() == null);
    slopingWallHeightAtEndSpinnerModel.setLength(controller.getSlopingWallHeightAtEnd());
    final PropertyChangeListener slopingWallHeightAtEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          slopingWallHeightAtEndSpinnerModel.setNullable(ev.getNewValue() == null);
          slopingWallHeightAtEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END,
        slopingWallHeightAtEndChangeListener);
    slopingWallHeightAtEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END,
              slopingWallHeightAtEndChangeListener);
          controller.setSlopingWallHeightAtEnd(slopingWallHeightAtEndSpinnerModel.getLength());
          if (minimumHeight == 0
              && controller.getSlopingWallHeightAtStart() == 0
              && controller.getSlopingWallHeightAtEnd() == 0) {
            // Ensure wall height is never 0
            controller.setSlopingWallHeightAtStart(minimumLength);
          }
          controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END,
              slopingWallHeightAtEndChangeListener);
        }
      });

    // Create thickness label and its spinner bound to THICKNESS controller property
    this.thicknessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "thicknessLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel thicknessSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength / 10);
    this.thicknessSpinner = new NullableSpinner(thicknessSpinnerModel);
    thicknessSpinnerModel.setNullable(controller.getThickness() == null);
    thicknessSpinnerModel.setLength(controller.getThickness());
    final PropertyChangeListener thicknessChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          thicknessSpinnerModel.setNullable(ev.getNewValue() == null);
          thicknessSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.THICKNESS,
        thicknessChangeListener);
    thicknessSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.THICKNESS,
              thicknessChangeListener);
          controller.setThickness(thicknessSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.THICKNESS,
              thicknessChangeListener);
        }
      });

    // Create arc extent label and its spinner bound to ARC_EXTENT_IN_DEGREES controller property
    this.arcExtentLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        WallPanel.class, "arcExtentLabel.text", unitName));
    final NullableSpinner.NullableSpinnerNumberModel arcExtentSpinnerModel =
        new NullableSpinner.NullableSpinnerNumberModel(new Float(0), new Float(-270), new Float(270), new Float(5));
    this.arcExtentSpinner = new NullableSpinner(arcExtentSpinnerModel);
    final PropertyChangeListener arcExtentChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          arcExtentSpinnerModel.setNullable(controller.getArcExtentInDegrees() == null);
          arcExtentSpinnerModel.setValue(controller.getArcExtentInDegrees());
        }
      };
    arcExtentChangeListener.propertyChange(null);
    // Use another listener that will update arc length in arc extent tooltip
    PropertyChangeListener arcLengthChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Float arcLength = controller.getArcLength();
          if (arcLength != null && (controller.getArcExtentInDegrees() != 0 || SwingTools.isToolTipShowing())) {
            String arcExtentSpinnerToolTip = preferences.getLocalizedString(WallPanel.class,
                "arcExtentSpinner.tooltip", preferences.getLengthUnit().getFormatWithUnit().format(arcLength));
            arcExtentSpinner.setToolTipText(arcExtentSpinnerToolTip);
            if (arcExtentSpinner.isShowing()
                && SwingTools.isToolTipShowing()) {
              // Trigger a mouse move to tool tip manager to ensure tool tip is updated
              Point point = MouseInfo.getPointerInfo().getLocation();
              SwingUtilities.convertPointFromScreen(point, arcExtentSpinner);
              ToolTipManager.sharedInstance().mouseMoved(
                  new MouseEvent(arcExtentSpinner, -1, System.currentTimeMillis(), 0,
                      Math.max(0, Math.min(arcExtentSpinner.getWidth(), point.x)),
                      Math.max(0, Math.min(arcExtentSpinner.getHeight(), point.y)), 1, false, 0));
            }
          } else {
            arcExtentSpinner.setToolTipText(null);
          }
        }
      };
    arcLengthChangeListener.propertyChange(null);
    controller.addPropertyChangeListener(WallController.Property.ARC_EXTENT_IN_DEGREES, arcLengthChangeListener);
    controller.addPropertyChangeListener(WallController.Property.ARC_EXTENT_IN_DEGREES, arcExtentChangeListener);
    arcExtentSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.ARC_EXTENT_IN_DEGREES,
              arcExtentChangeListener);
          Number value = (Number)arcExtentSpinnerModel.getValue();
          controller.setArcExtentInDegrees(value != null ? value.floatValue() : null);
          controller.addPropertyChangeListener(WallController.Property.ARC_EXTENT_IN_DEGREES,
              arcExtentChangeListener);
        }
      });

    // wallOrientationLabel shows an HTML explanation of wall orientation with an image URL in resource
    this.wallOrientationLabel = new JLabel(preferences.getLocalizedString(
            WallPanel.class, "wallOrientationLabel.text",
            new ResourceURLContent(WallPanel.class, "resources/wallOrientation.png").getURL()),
        JLabel.CENTER);
    // Use same font for label as tooltips
    this.wallOrientationLabel.setFont(UIManager.getFont("ToolTip.font"));

    this.dialogTitle = preferences.getLocalizedString(WallPanel.class, "wall.title");
  }

  /**
   * Updates left side color radio buttons.
   */
  private void updateLeftSideColorRadioButtons(WallController controller) {
    if (controller.getLeftSidePaint() == WallController.WallPaint.COLORED) {
      this.leftSideColorRadioButton.setSelected(true);
    } else if (controller.getLeftSidePaint() == WallController.WallPaint.TEXTURED) {
      this.leftSideTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.leftSideColorRadioButton, this.leftSideTextureRadioButton);
    }
  }

  /**
   * Updates left side shininess radio buttons.
   */
  private void updateLeftSideShininessRadioButtons(WallController controller) {
    if (controller.getLeftSideShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.leftSideMattRadioButton, this.leftSideShinyRadioButton);
    } else if (controller.getLeftSideShininess() == 0) {
      this.leftSideMattRadioButton.setSelected(true);
    } else { // null
      this.leftSideShinyRadioButton.setSelected(true);
    }
  }

  /**
   * Updates right side color radio buttons.
   */
  private void updateRightSideColorRadioButtons(WallController controller) {
    if (controller.getRightSidePaint() == WallController.WallPaint.COLORED) {
      this.rightSideColorRadioButton.setSelected(true);
    } else if (controller.getRightSidePaint() == WallController.WallPaint.TEXTURED) {
      this.rightSideTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.rightSideColorRadioButton, this.rightSideTextureRadioButton);
    }
  }

  /**
   * Updates right side shininess radio buttons.
   */
  private void updateRightSideShininessRadioButtons(WallController controller) {
    if (controller.getRightSideShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.rightSideMattRadioButton, this.rightSideShinyRadioButton);
    } else if (controller.getRightSideShininess() == 0) {
      this.rightSideMattRadioButton.setSelected(true);
    } else { // null
      this.rightSideShinyRadioButton.setSelected(true);
    }
  }

  /**
   * Updates top color radio buttons.
   */
  private void updateTopColorRadioButtons(WallController controller) {
    if (controller.getTopPaint() == WallController.WallPaint.COLORED) {
      this.topColorRadioButton.setSelected(true);
    } else if (controller.getTopPaint() == WallController.WallPaint.DEFAULT) {
      this.topDefaultColorRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.topColorRadioButton, this.topDefaultColorRadioButton);
    }
  }

  /**
   * Updates rectangular and sloping wall radio buttons.
   */
  private void updateWallShapeRadioButtons(WallController controller) {
    if (controller.getShape() == WallController.WallShape.SLOPING_WALL) {
      this.slopingWallRadioButton.setSelected(true);
    } else if (controller.getShape() == WallController.WallShape.RECTANGULAR_WALL) {
      this.rectangularWallRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.slopingWallRadioButton, this.rectangularWallRadioButton);
    }
  }

  /**
   * Edits the baseboard values in an option pane dialog.
   */
  private void editBaseboard(final JComponent parent, final String title,
                             BaseboardChoiceController baseboardChoiceController) {
    Boolean visible = baseboardChoiceController.getVisible();
    Integer color = baseboardChoiceController.getColor();
    HomeTexture texture = baseboardChoiceController.getTextureController().getTexture();
    BaseboardChoiceController.BaseboardPaint paint = baseboardChoiceController.getPaint();
    Float thickness = baseboardChoiceController.getThickness();
    Float height = baseboardChoiceController.getHeight();
    JComponent view = (JComponent)baseboardChoiceController.getView();
    // Add baseboard component to a panel with a flow layout to avoid it getting too large
    JPanel panel = new JPanel();
    panel.add(view);
    if (SwingTools.showConfirmDialog(parent, panel, title, (JComponent)view.getComponent(0)) != JOptionPane.OK_OPTION) {
      // Restore initial values
      baseboardChoiceController.setVisible(visible);
      baseboardChoiceController.setColor(color);
      baseboardChoiceController.getTextureController().setTexture(texture);
      baseboardChoiceController.setPaint(paint);
      baseboardChoiceController.setThickness(thickness);
      baseboardChoiceController.setHeight(height);
    }
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.xStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xStartLabel.setLabelFor(this.xStartSpinner);
      this.yStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yStartLabel.setLabelFor(this.yStartSpinner);
      this.xEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xEndLabel.setLabelFor(this.xEndSpinner);
      this.yEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yEndLabel.setLabelFor(this.yEndSpinner);
      this.distanceToEndPointLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "distanceToEndPointLabel.mnemonic")).getKeyCode());
      this.distanceToEndPointLabel.setLabelFor(this.distanceToEndPointSpinner);

      this.leftSideColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "leftSideColorRadioButton.mnemonic")).getKeyCode());
      this.leftSideTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "leftSideTextureRadioButton.mnemonic")).getKeyCode());
      this.leftSideMattRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "leftSideMattRadioButton.mnemonic")).getKeyCode());
      this.leftSideShinyRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "leftSideShinyRadioButton.mnemonic")).getKeyCode());
      this.rightSideColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rightSideColorRadioButton.mnemonic")).getKeyCode());
      this.rightSideTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rightSideTextureRadioButton.mnemonic")).getKeyCode());
      this.rightSideMattRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rightSideMattRadioButton.mnemonic")).getKeyCode());
      this.rightSideShinyRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rightSideShinyRadioButton.mnemonic")).getKeyCode());

      this.patternLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          WallPanel.class, "patternLabel.mnemonic")).getKeyCode());
      this.patternLabel.setLabelFor(this.patternComboBox);
      this.topDefaultColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
              WallPanel.class,"topDefaultColorRadioButton.mnemonic")).getKeyCode());
      this.topColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
              WallPanel.class,"topColorRadioButton.mnemonic")).getKeyCode());

      this.rectangularWallRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rectangularWallRadioButton.mnemonic")).getKeyCode());
      this.rectangularWallHeightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rectangularWallHeightLabel.mnemonic")).getKeyCode());
      this.rectangularWallHeightLabel.setLabelFor(this.rectangularWallHeightSpinner);
      this.slopingWallRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "slopingWallRadioButton.mnemonic")).getKeyCode());
      this.slopingWallHeightAtStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "slopingWallHeightAtStartLabel.mnemonic")).getKeyCode());
      this.slopingWallHeightAtStartLabel.setLabelFor(this.slopingWallHeightAtStartSpinner);
      this.slopingWallHeightAtEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "slopingWallHeightAtEndLabel.mnemonic")).getKeyCode());
      this.slopingWallHeightAtEndLabel.setLabelFor(this.slopingWallHeightAtEndSpinner);

      this.thicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "thicknessLabel.mnemonic")).getKeyCode());
      this.thicknessLabel.setLabelFor(this.thicknessSpinner);
      this.arcExtentLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "arcExtentLabel.mnemonic")).getKeyCode());
      this.arcExtentLabel.setLabelFor(this.arcExtentSpinner);
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents(UserPreferences preferences,
                                final WallController controller) {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    final JPanel startPointPanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "startPointPanel.title"),
        new JComponent [] {this.xStartLabel, this.xStartSpinner,
                           this.yStartLabel, this.yStartSpinner}, true);
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    Insets rowInsets;
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // User smaller insets for Mac OS X 10.5
      rowInsets = new Insets(0, 0, 0, 0);
    } else {
      rowInsets = new Insets(0, 0, standardGap, 0);
    }
    add(startPointPanel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Second row
    final JPanel endPointPanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "endPointPanel.title"),
        new JComponent [] {this.xEndLabel, this.xEndSpinner,
                           this.yEndLabel, this.yEndSpinner}, true);
    // Add distance label and spinner at the end of second row of endPointPanel
    endPointPanel.add(this.distanceToEndPointLabel, new GridBagConstraints(
        0, 1, 3, 1, 1, 0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(standardGap, 0, 0, standardGap), 0, 0));
    endPointPanel.add(this.distanceToEndPointSpinner, new GridBagConstraints(
        3, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), 0, 0));

    add(endPointPanel, new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Third row
    JPanel leftSidePanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "leftSidePanel.title"),
        new JComponent [] {this.leftSideColorRadioButton, this.leftSideColorButton,
                           this.leftSideTextureRadioButton, this.leftSideTextureComponent}, false);
    leftSidePanel.add(new JSeparator(), new GridBagConstraints(
        0, 2, 2, 1, 1, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(3, 0, 3, 0), 0, 0));
    leftSidePanel.add(this.leftSideMattRadioButton, new GridBagConstraints(
        0, 3, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    leftSidePanel.add(this.leftSideShinyRadioButton, new GridBagConstraints(
        1, 3, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    leftSidePanel.add(this.leftSideBaseboardButton, new GridBagConstraints(
        0, 4, 2, 1, 1, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(standardGap, 0, 0, 0), 0, 0));
    add(leftSidePanel, new GridBagConstraints(
        0, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));

    JPanel rightSidePanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "rightSidePanel.title"),
        new JComponent [] {this.rightSideColorRadioButton, this.rightSideColorButton,
                           this.rightSideTextureRadioButton, this.rightSideTextureComponent}, false);
    rightSidePanel.add(new JSeparator(), new GridBagConstraints(
        0, 2, 2, 1, 1, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(3, 0, 3, 0), 0, 0));
    rightSidePanel.add(this.rightSideMattRadioButton, new GridBagConstraints(
        0, 3, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    rightSidePanel.add(this.rightSideShinyRadioButton, new GridBagConstraints(
        1, 3, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    rightSidePanel.add(this.rightSideBaseboardButton, new GridBagConstraints(
        0, 4, 2, 1, 1, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(standardGap, 0, 0, 0), 0, 0));
    add(rightSidePanel, new GridBagConstraints(
        1, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));

    // Forth row
    JPanel topPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        WallPanel.class, "topPanel.title"));
    int leftInset = new JRadioButton().getPreferredSize().width;
    topPanel.add(this.patternLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, leftInset, 3, standardGap), 0, 0));
    topPanel.add(this.patternComboBox, new GridBagConstraints(
        1, 0, 3, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 3, 0), 0, 0));
    topPanel.add(this.topColorLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, leftInset, 0, standardGap), 0, 0));
    topPanel.add(this.topDefaultColorRadioButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    topPanel.add(this.topColorRadioButton, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    topPanel.add(this.topColorButton, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(topPanel, new GridBagConstraints(
        0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));

    JPanel heightPanel = SwingTools.createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "heightPanel.title"));
    // First row of height panel
    heightPanel.add(this.rectangularWallRadioButton, new GridBagConstraints(
        0, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));
    // Second row of height panel
    // Add a dummy label to align second and fourth row on radio buttons text
    int spinnerPadX = OperatingSystem.isMacOSX()  ? -20  : -10;
    heightPanel.add(new JLabel(), new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), new JRadioButton().getPreferredSize().width, 0));
    heightPanel.add(this.rectangularWallHeightLabel, new GridBagConstraints(
        1, 1, 1, 1, 1, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    heightPanel.add(this.rectangularWallHeightSpinner, new GridBagConstraints(
        2, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, standardGap), spinnerPadX, 0));
    // Third column of height panel
    heightPanel.add(this.slopingWallRadioButton, new GridBagConstraints(
        3, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 10, 2, 0), 0, 0));
    // Second row of height panel
    heightPanel.add(new JLabel(), new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), new JRadioButton().getPreferredSize().width, 0));
    heightPanel.add(this.slopingWallHeightAtStartLabel, new GridBagConstraints(
        4, 1, 1, 1, 1, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    heightPanel.add(this.slopingWallHeightAtStartSpinner, new GridBagConstraints(
        5, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), spinnerPadX, 0));
    // Third row of height panel
    heightPanel.add(this.slopingWallHeightAtEndLabel, new GridBagConstraints(
        4, 2, 1, 1, 1, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    heightPanel.add(this.slopingWallHeightAtEndSpinner, new GridBagConstraints(
        5, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), spinnerPadX, 0));
    add(heightPanel, new GridBagConstraints(
        0, 4, 2, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));

    // Sixth row
    JPanel ticknessAndArcExtentPanel = new JPanel(new GridBagLayout());
    ticknessAndArcExtentPanel.add(this.thicknessLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, standardGap), 0, 0));
    ticknessAndArcExtentPanel.add(this.thicknessSpinner, new GridBagConstraints(
        1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    ticknessAndArcExtentPanel.add(this.arcExtentLabel, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, standardGap), 0, 0));
    ticknessAndArcExtentPanel.add(this.arcExtentSpinner, new GridBagConstraints(
        3, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(ticknessAndArcExtentPanel, new GridBagConstraints(
        0, 5, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(standardGap, 8, 10, 8), 0, 0));

    // Last row
    add(this.wallOrientationLabel, new GridBagConstraints(
        0, 6, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    // Make startPointPanel and endPointPanel visible depending on editable points property
    controller.addPropertyChangeListener(WallController.Property.EDITABLE_POINTS,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            startPointPanel.setVisible(controller.isEditablePoints());
            endPointPanel.setVisible(controller.isEditablePoints());
            arcExtentLabel.setVisible(controller.isEditablePoints());
            arcExtentSpinner.setVisible(controller.isEditablePoints());
          }
        });
    startPointPanel.setVisible(controller.isEditablePoints());
    endPointPanel.setVisible(controller.isEditablePoints());
    this.arcExtentLabel.setVisible(controller.isEditablePoints());
    this.arcExtentSpinner.setVisible(controller.isEditablePoints());
  }

  private JPanel createTitledPanel(String title, JComponent [] components, boolean horizontal) {
    JPanel titledPanel = SwingTools.createTitledPanel(title);
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    if (horizontal) {
      int labelAlignment = OperatingSystem.isMacOSX()
          ? GridBagConstraints.LINE_END
          : GridBagConstraints.LINE_START;
      Insets labelInsets = new Insets(0, 0, 0, standardGap);
      Insets insets = new Insets(0, 0, 0, standardGap);
      for (int i = 0; i < components.length - 1; i += 2) {
        titledPanel.add(components [i], new GridBagConstraints(
            i, 0, 1, 1, 1, 0, labelAlignment,
            GridBagConstraints.NONE, labelInsets, 0, 0));
        titledPanel.add(components [i + 1], new GridBagConstraints(
            i + 1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.HORIZONTAL, insets, 0, 0));
      }

      titledPanel.add(components [components.length - 1], new GridBagConstraints(
          components.length - 1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    } else {
      for (int i = 0; i < components.length; i += 2) {
        int bottomInset = i < components.length - 2  ? Math.round(2 * SwingTools.getResolutionScale())  : 0;
        titledPanel.add(components [i], new GridBagConstraints(
            0, i / 2, components [i + 1] != null  ? 1  : 2, 1, 1, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE,
            new Insets(0, 0, bottomInset, standardGap), 0, 0));
        if (components [i + 1] != null) {
          titledPanel.add(components [i + 1], new GridBagConstraints(
              1, i / 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
              GridBagConstraints.HORIZONTAL, new Insets(0, 0, bottomInset, 0), 0, 0));
        }
      }
    }
    return titledPanel;
  }

  /**
   * Displays this panel in a modal dialog box.
   */
  public void displayView(View parentView) {
    Component homeRoot = SwingUtilities.getRoot((Component)parentView);
    if (homeRoot != null) {
      JOptionPane optionPane = new JOptionPane(this,
          JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
      JComponent parentComponent = SwingUtilities.getRootPane((JComponent)parentView);
      if (parentView != null) {
        optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
      }
      JDialog dialog = optionPane.createDialog(parentComponent, this.dialogTitle);
      Dimension screenSize = getToolkit().getScreenSize();
      Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
      // Check dialog isn't too high
      int screenHeight = screenSize.height - screenInsets.top - screenInsets.bottom;
      if (OperatingSystem.isLinux() && screenHeight == screenSize.height) {
        // Let's consider that under Linux at least an horizontal bar exists
        screenHeight -= 30;
      }
      if (dialog.getHeight() > screenHeight) {
        this.wallOrientationLabel.setVisible(false);
      }
      dialog.pack();
      if (dialog.getHeight() > screenHeight) {
        this.patternLabel.getParent().setVisible(false);
      }
      dialog.dispose();
    }

    JFormattedTextField thicknessTextField =
        ((JSpinner.DefaultEditor)thicknessSpinner.getEditor()).getTextField();
    if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, thicknessTextField) == JOptionPane.OK_OPTION) {
      this.controller.modifyWalls();
    }
  }
}
