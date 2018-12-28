/*
 * ImportedFurnitureWizardStepsPanel.java 4 juil. 07
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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.swing.Action;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.j3d.OBJWriter;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.ImportedFurnitureWizardController;
import com.eteks.sweethome3d.viewcontroller.ImportedFurnitureWizardStepsView;

/**
 * Wizard panel for furniture import.
 * @author Emmanuel Puybaret
 */
public class ImportedFurnitureWizardStepsPanel extends JPanel
                                               implements ImportedFurnitureWizardStepsView {
  private final ImportedFurnitureWizardController controller;
  private CardLayout                        cardLayout;
  private JLabel                            modelChoiceOrChangeLabel;
  private JButton                           modelChoiceOrChangeButton;
  private JButton                           findModelsButton;
  private JLabel                            modelChoiceErrorLabel;
  private ModelPreviewComponent             modelPreviewComponent;
  private JLabel                            orientationLabel;
  private JButton                           defaultOrientationButton;
  private JButton                           turnLeftButton;
  private JButton                           turnRightButton;
  private JButton                           turnUpButton;
  private JButton                           turnDownButton;
  private int                               horizontalAngle;
  private int                               verticalAngle;
  private JToolTip                          orientationToolTip;
  private JWindow                           orientationToolTipWindow;
  private RotationPreviewComponent          rotationPreviewComponent;
  private JLabel                            backFaceShownLabel;
  private JCheckBox                         backFaceShownCheckBox;
  private JLabel                            attributesLabel;
  private JLabel                            nameLabel;
  private JTextField                        nameTextField;
  private JCheckBox                         addToCatalogCheckBox;
  private JLabel                            categoryLabel;
  private JComboBox                         categoryComboBox;
  private JLabel                            creatorLabel;
  private JTextField                        creatorTextField;
  private JLabel                            widthLabel;
  private JSpinner                          widthSpinner;
  private JLabel                            depthLabel;
  private JSpinner                          depthSpinner;
  private JLabel                            heightLabel;
  private JSpinner                          heightSpinner;
  private JCheckBox                         keepProportionsCheckBox;
  private JLabel                            elevationLabel;
  private JSpinner                          elevationSpinner;
  private AttributesPreviewComponent        attributesPreviewComponent;
  private JCheckBox                         movableCheckBox;
  private JCheckBox                         doorOrWindowCheckBox;
  private JCheckBox                         staircaseCheckBox;
  private JLabel                            colorLabel;
  private ColorButton                       colorButton;
  private JButton                           clearColorButton;
  private JLabel                            iconLabel;
  private IconPreviewComponent              iconPreviewComponent;
  private Cursor                            defaultCursor;
  private Executor                          modelLoader;

  /**
   * Creates a view for furniture import.
   */
  public ImportedFurnitureWizardStepsPanel(CatalogPieceOfFurniture piece,
                                           String modelName,
                                           boolean importHomePiece,
                                           UserPreferences preferences,
                                           final ImportedFurnitureWizardController controller) {
    this.controller = controller;
    // Create a model loader for each wizard in case model loading hangs
    this.modelLoader = Executors.newSingleThreadExecutor();
    createComponents(importHomePiece, preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
    updateController(piece, preferences);
    if (modelName != null) {
      updateController(modelName, preferences, controller.getContentManager(),
          importHomePiece
              ? null
              : preferences.getFurnitureCatalog().getCategories().get(0), true);
    }

    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.STEP,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            updateStep(controller);
          }
        });
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(final boolean importHomePiece,
                                final UserPreferences preferences,
                                final ImportedFurnitureWizardController controller) {
    // Get unit name matching current unit
    String unitName = preferences.getLengthUnit().getName();

    // Model panel components
    this.modelChoiceOrChangeLabel = new JLabel();
    this.modelChoiceOrChangeButton = new JButton();
    final FurnitureCategory defaultModelCategory =
        (importHomePiece || preferences.getFurnitureCatalog().getCategories().size() == 0)
            ? null
            : preferences.getFurnitureCatalog().getCategories().get(0);
    this.modelChoiceOrChangeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          String modelName = showModelChoiceDialog(preferences, controller.getContentManager());
          if (modelName != null) {
            updateController(modelName, preferences,
                controller.getContentManager(), defaultModelCategory, false);
          }
        }
      });
    try {
      this.findModelsButton = new JButton(SwingTools.getLocalizedLabelText(preferences,
          ImportedFurnitureWizardStepsPanel.class, "findModelsButton.text"));
      final String findModelsUrl = preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "findModelsButton.url");
      this.findModelsButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            boolean documentShown = false;
            try {
              // Display Find models page in browser
              documentShown = SwingTools.showDocumentInBrowser(new URL(findModelsUrl));
            } catch (MalformedURLException ex) {
              // Document isn't shown
            }
            if (!documentShown) {
              // If the document wasn't shown, display a message
              // with a copiable URL in a message box
              JTextArea findModelsMessageTextArea = new JTextArea(preferences.getLocalizedString(
                  ImportedFurnitureWizardStepsPanel.class, "findModelsMessage.text"));
              String findModelsTitle = preferences.getLocalizedString(
                  ImportedFurnitureWizardStepsPanel.class, "findModelsMessage.title");
              findModelsMessageTextArea.setEditable(false);
              findModelsMessageTextArea.setOpaque(false);
              JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedFurnitureWizardStepsPanel.this),
                  findModelsMessageTextArea, findModelsTitle,
                  JOptionPane.INFORMATION_MESSAGE);
            }
          }
        });
    } catch (IllegalArgumentException ex) {
      // Don't create findModelsButton if its text or url isn't defined
    }
    this.modelChoiceErrorLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "modelChoiceErrorLabel.text"));
    // Make modelChoiceErrorLabel visible only if an error occurred during model content loading
    this.modelChoiceErrorLabel.setVisible(false);
    this.modelPreviewComponent = new ModelPreviewComponent(true);
    // Add a transfer handler to model preview component to let user drag and drop a file in component
    this.modelPreviewComponent.setTransferHandler(new TransferHandler() {
        @Override
        public boolean canImport(JComponent comp, DataFlavor [] flavors) {
          return Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(JComponent comp, Transferable transferedFiles) {
          boolean success = false;
          try {
            List<File> files = (List<File>)transferedFiles.getTransferData(DataFlavor.javaFileListFlavor);
            for (File file : files) {
              final String modelName = file.getAbsolutePath();
              // Try to import the first file that would be accepted by content manager
              if (controller.getContentManager().isAcceptable(modelName, ContentManager.ContentType.MODEL)) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      updateController(modelName, preferences,
                          controller.getContentManager(), defaultModelCategory, false);
                    }
                  });
                success = true;
                break;
              }
            }
          } catch (UnsupportedFlavorException ex) {
            // No success
          } catch (IOException ex) {
            // No success
          }
          if (!success) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedFurnitureWizardStepsPanel.this),
                      preferences.getLocalizedString(ImportedFurnitureWizardStepsPanel.class, "modelChoiceErrorLabel.text"));
                }
              });
          }
          return success;
        }
      });
    this.modelPreviewComponent.setBorder(SwingTools.getDropableComponentBorder());

    // Orientation panel components
    this.orientationLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "orientationLabel.text"));
    this.defaultOrientationButton = new JButton(new ResourceAction(preferences,
            ImportedFurnitureWizardStepsPanel.class, "DEFAULT_ORIENTATION", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          updateModelRotation(new Transform3D());
          horizontalAngle = 0;
          verticalAngle = 0;
        }
      });
    final String angleTooltipFormat = preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "angleTooltipFeedback");
    this.orientationToolTip = new JToolTip();
    this.turnLeftButton = new AutoRepeatButton(new ResourceAction(preferences,
            ImportedFurnitureWizardStepsPanel.class, "TURN_LEFT", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D leftRotation = new Transform3D();
          int deltaAngle = (ev.getModifiers() & ActionEvent.SHIFT_MASK) == 0
              ? -90
              : -1;
          leftRotation.rotY(Math.toRadians(deltaAngle));
          leftRotation.mul(oldTransform);
          updateModelRotation(leftRotation);
          horizontalAngle = (horizontalAngle + deltaAngle) % 360;
          orientationToolTip.setTipText(String.format(angleTooltipFormat, horizontalAngle));
          verticalAngle = 0;
        }
      });
    this.turnRightButton = new AutoRepeatButton(new ResourceAction(preferences,
            ImportedFurnitureWizardStepsPanel.class, "TURN_RIGHT", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D rightRotation = new Transform3D();
          int deltaAngle = (ev.getModifiers() & ActionEvent.SHIFT_MASK) == 0
              ? 90
              : 1;
          rightRotation.rotY(Math.toRadians(deltaAngle));
          rightRotation.mul(oldTransform);
          updateModelRotation(rightRotation);
          horizontalAngle = (horizontalAngle + deltaAngle) % 360;
          orientationToolTip.setTipText(String.format(angleTooltipFormat, horizontalAngle));
          verticalAngle = 0;
        }
      });
    this.turnUpButton = new AutoRepeatButton(new ResourceAction(preferences,
            ImportedFurnitureWizardStepsPanel.class, "TURN_UP", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D upRotation = new Transform3D();
          int deltaAngle = (ev.getModifiers() & ActionEvent.SHIFT_MASK) == 0
              ? -90
              : -1;
          upRotation.rotX(Math.toRadians(deltaAngle));
          upRotation.mul(oldTransform);
          updateModelRotation(upRotation);
          verticalAngle = (verticalAngle + deltaAngle) % 360;
          orientationToolTip.setTipText(String.format(angleTooltipFormat, verticalAngle));
          horizontalAngle = 0;
        }
      });
    this.turnDownButton = new AutoRepeatButton(new ResourceAction(preferences,
            ImportedFurnitureWizardStepsPanel.class, "TURN_DOWN", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D downRotation = new Transform3D();
          int deltaAngle = (ev.getModifiers() & ActionEvent.SHIFT_MASK) == 0
              ? 90
              : 1;
          downRotation.rotX(Math.toRadians(deltaAngle));
          downRotation.mul(oldTransform);
          updateModelRotation(downRotation);
          verticalAngle = (verticalAngle + deltaAngle) % 360;
          orientationToolTip.setTipText(String.format(angleTooltipFormat, verticalAngle));
          horizontalAngle = 0;
        }
      });

    this.backFaceShownLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "backFaceShownLabel.text"));
    this.backFaceShownCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "backFaceShownCheckBox.text"));
    this.backFaceShownCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setBackFaceShown(backFaceShownCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHOWN,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If back face shown changes update back face shown check box
            backFaceShownCheckBox.setSelected(controller.isBackFaceShown());
          }
        });
    this.rotationPreviewComponent = new RotationPreviewComponent(preferences, controller);

    // Attributes panel components
    this.attributesLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "attributesLabel.text"));
    this.nameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "nameLabel.text"));
    this.nameTextField = new JTextField(10);
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
    }
    DocumentListener nameListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          nameTextField.getDocument().removeDocumentListener(this);
          controller.setName(nameTextField.getText().trim());
          nameTextField.getDocument().addDocumentListener(this);
        }

        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }

        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      };
    this.nameTextField.getDocument().addDocumentListener(nameListener);
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.NAME,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If name changes update name text field
            if (!nameTextField.getText().trim().equals(controller.getName())) {
              nameTextField.setText(controller.getName());
            }
          }
        });

    this.addToCatalogCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "addToCatalogCheckBox.text"));
    // Propose the add to catalog option only for home furniture import
    this.addToCatalogCheckBox.setVisible(importHomePiece);
    this.addToCatalogCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          if (addToCatalogCheckBox.isSelected()) {
            categoryComboBox.setEnabled(true);
            controller.setCategory((FurnitureCategory)categoryComboBox.getSelectedItem());
          } else {
            categoryComboBox.setEnabled(false);
            controller.setCategory(null);
          }
        }
      });
    this.categoryLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "categoryLabel.text"));
    this.categoryComboBox = new JComboBox(preferences.getFurnitureCatalog().getCategories().toArray());
    // The piece category isn't enabled by default for home furniture import
    this.categoryComboBox.setEnabled(!importHomePiece);
    this.categoryComboBox.setEditable(true);
    final ComboBoxEditor defaultEditor = this.categoryComboBox.getEditor();
    // Change editor to edit category name
    this.categoryComboBox.setEditor(new ComboBoxEditor() {
        public Object getItem() {
          String name = (String)defaultEditor.getItem();
          name = name.trim();
          // If category is empty, replace it by the last selected item
          if (name.length() == 0) {
            Object selectedItem = categoryComboBox.getSelectedItem();
            setItem(selectedItem);
            return selectedItem;
          } else {
            FurnitureCategory category = new FurnitureCategory(name);
            // Search an existing category
            List<FurnitureCategory> categories = preferences.getFurnitureCatalog().getCategories();
            int categoryIndex = Collections.binarySearch(categories, category);
            if (categoryIndex >= 0) {
              return categories.get(categoryIndex);
            }
            // If no existing category was found, return a new one
            return category;
          }
        }

        public void setItem(Object value) {
          if (value != null) {
            FurnitureCategory category = (FurnitureCategory)value;
            defaultEditor.setItem(category.getName());
          }
        }

        public void addActionListener(ActionListener l) {
          defaultEditor.addActionListener(l);
        }

        public Component getEditorComponent() {
          return defaultEditor.getEditorComponent();
        }

        public void removeActionListener(ActionListener l) {
          defaultEditor.removeActionListener(l);
        }

        public void selectAll() {
          defaultEditor.selectAll();
        }
      });
    this.categoryComboBox.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
          FurnitureCategory category = (FurnitureCategory)value;
          return super.getListCellRendererComponent(list, category.getName(), index, isSelected, cellHasFocus);
        }
      });
    this.categoryComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setCategory((FurnitureCategory)ev.getItem());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.CATEGORY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If category changes update category combo box
            FurnitureCategory category = controller.getCategory();
            if (category != null) {
              categoryComboBox.setSelectedItem(category);
            }
          }
        });
    if (this.categoryComboBox.getItemCount() > 0) {
      this.categoryComboBox.setSelectedIndex(0);
    }

    this.creatorLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "creatorLabel.text"));
    this.creatorTextField = new JTextField(10);
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      SwingTools.addAutoSelectionOnFocusGain(this.creatorTextField);
    }
    DocumentListener creatorListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          creatorTextField.getDocument().removeDocumentListener(this);
          controller.setCreator(creatorTextField.getText().trim());
          creatorTextField.getDocument().addDocumentListener(this);
        }

        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }

        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      };
    this.creatorTextField.getDocument().addDocumentListener(creatorListener);
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.CREATOR,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If creator changes update creator text field
            if (!creatorTextField.getText().trim().equals(controller.getCreator())) {
              creatorTextField.setText(controller.getCreator());
            }
          }
        });

    this.widthLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "widthLabel.text", unitName));
    final float minimumLength = preferences.getLengthUnit().getMinimumLength();
    final float maximumLength = preferences.getLengthUnit().getMaximumLength();
    final NullableSpinner.NullableSpinnerLengthModel widthSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, Math.min(controller.getWidth(), minimumLength), maximumLength);
    this.widthSpinner = new NullableSpinner(widthSpinnerModel);
    widthSpinnerModel.addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          widthSpinnerModel.removeChangeListener(this);
          // If width spinner value changes update controller
          controller.setWidth(widthSpinnerModel.getLength());
          widthSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.WIDTH,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If width changes update width spinner
            widthSpinnerModel.setLength(controller.getWidth());
            widthSpinnerModel.setMinimumLength(Math.min(controller.getWidth(), minimumLength));
          }
        });

    this.depthLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "depthLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel depthSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, Math.min(controller.getDepth(), minimumLength), maximumLength);
    this.depthSpinner = new NullableSpinner(depthSpinnerModel);
    depthSpinnerModel.addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          depthSpinnerModel.removeChangeListener(this);
          // If depth spinner value changes update controller
          controller.setDepth(depthSpinnerModel.getLength());
          depthSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.DEPTH,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If depth changes update depth spinner
            depthSpinnerModel.setLength(controller.getDepth());
            depthSpinnerModel.setMinimumLength(Math.min(controller.getDepth(), minimumLength));
          }
        });

    this.heightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
            ImportedFurnitureWizardStepsPanel.class, "heightLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, Math.min(controller.getHeight(), minimumLength), maximumLength);
    this.heightSpinner = new NullableSpinner(heightSpinnerModel);
    heightSpinnerModel.addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          heightSpinnerModel.removeChangeListener(this);
          // If width spinner value changes update controller
          controller.setHeight(heightSpinnerModel.getLength());
          heightSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.HEIGHT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If height changes update height spinner
            heightSpinnerModel.setLength(controller.getHeight());
            heightSpinnerModel.setMinimumLength(Math.min(controller.getHeight(), minimumLength));
          }
        });
    this.keepProportionsCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "keepProportionsCheckBox.text"));
    this.keepProportionsCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setProportional(keepProportionsCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.PROPORTIONAL,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If proportional property changes update keep proportions check box
            keepProportionsCheckBox.setSelected(controller.isProportional());
          }
        });

    this.elevationLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "elevationLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, preferences.getLengthUnit().getMaximumElevation());
    this.elevationSpinner = new NullableSpinner(elevationSpinnerModel);
    elevationSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          elevationSpinnerModel.removeChangeListener(this);
          controller.setElevation(elevationSpinnerModel.getLength());
          elevationSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.ELEVATION,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If elevation changes update elevation spinner
            elevationSpinnerModel.setLength(controller.getElevation());
          }
        });

    this.movableCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "movableCheckBox.text"));
    this.movableCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setMovable(movableCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.MOVABLE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If movable changes update movable check box
            movableCheckBox.setSelected(controller.isMovable());
          }
        });

    this.doorOrWindowCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "doorOrWindowCheckBox.text"));
    this.doorOrWindowCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setDoorOrWindow(doorOrWindowCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.DOOR_OR_WINDOW,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If door or window changes update door or window check box
            boolean doorOrWindow = controller.isDoorOrWindow();
            doorOrWindowCheckBox.setSelected(doorOrWindow);
            movableCheckBox.setEnabled(!doorOrWindow && controller.getStaircaseCutOutShape() == null);
          }
        });

    this.staircaseCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "staircaseCheckBox.text"));
    this.staircaseCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setStaircaseCutOutShape(staircaseCheckBox.isSelected()
              ? PieceOfFurniture.DEFAULT_CUT_OUT_SHAPE
              : null);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.STAIRCASE_CUT_OUT_SHAPE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If staircase cut out shape changes update its check box
            String staircaseCutOutShape = controller.getStaircaseCutOutShape();
            staircaseCheckBox.setSelected(staircaseCutOutShape != null);
            movableCheckBox.setEnabled(!controller.isDoorOrWindow() && staircaseCutOutShape == null);
          }
        });

    this.colorLabel = new JLabel(
        String.format(SwingTools.getLocalizedLabelText(preferences,
            ImportedFurnitureWizardStepsPanel.class, "colorLabel.text"), unitName));
    this.colorButton = new ColorButton(preferences);
    this.colorButton.setColorDialogTitle(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "colorDialog.title"));
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
          }
        });
    this.clearColorButton = new JButton(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "clearColorButton.text"));
    this.clearColorButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.setColor(null);
        }
      });
    this.clearColorButton.setEnabled(false);
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.COLOR,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If color changes update color buttons
            colorButton.setColor(controller.getColor());
            clearColorButton.setEnabled(controller.getColor() != null);
          }
        });

    this.attributesPreviewComponent = new AttributesPreviewComponent(controller);

    // Icon panel components
    this.iconLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "iconLabel.text"));
    this.iconPreviewComponent = new IconPreviewComponent(this.controller);
  }

  /**
   * A button that repeats its action when kept pressed.
   */
  private class AutoRepeatButton extends JButton {
    private boolean shiftPressed;

    public AutoRepeatButton(final Action action) {
      super(action);
      // Create a timer that will repeat action each 40 ms when SHIFT is pressed
      final Timer timer = new Timer(40, new ActionListener() {
          public void actionPerformed(final ActionEvent ev) {
            action.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, ev.getWhen(), ActionEvent.SHIFT_MASK));
            showOrientationToolTip();
          }
        });
      timer.setInitialDelay(250);

      // Update timer when button is armed
      addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  if (shiftPressed) {
                    if (getModel().isArmed()
                        && !timer.isRunning()) {
                      timer.restart();
                    } else if (!getModel().isArmed()
                               && timer.isRunning()) {
                      timer.stop();
                    }
                  }
                }
              });
          }
        });
      addMouseListener(new MouseAdapter() {
          @Override
          public void mousePressed(final MouseEvent ev) {
            shiftPressed = ev.isShiftDown();
          }

          @Override
          public void mouseClicked(final MouseEvent ev) {
            showOrientationToolTip();
          }

          @Override
          public void mouseReleased(MouseEvent ev) {
            new Timer(500, new ActionListener() {
                public void actionPerformed(final ActionEvent ev) {
                  deleteOrientationToolTip();
                  ((Timer)ev.getSource()).stop();
                }
              }).start();
          }
        });
    }
  }

  /**
   * Shows the orientation tool tip.
   */
  private void showOrientationToolTip() {
    if (this.orientationToolTipWindow == null) {
      // Show tool tip in a window (we don't use a Swing Popup because
      // we require the tool tip window to resize itself depending on the content)
      this.orientationToolTipWindow = new JWindow(SwingUtilities.getWindowAncestor(this));
      this.orientationToolTipWindow.setFocusableWindowState(false);
      this.orientationToolTipWindow.add(this.orientationToolTip);
    } else {
      this.orientationToolTip.revalidate();
    }
    Point point = MouseInfo.getPointerInfo().getLocation();
    // Add to point the half of cursor size
    Dimension cursorSize = getToolkit().getBestCursorSize(16, 16);
    if (cursorSize.width != 0) {
      point.x += cursorSize.width + 2;
      point.y += cursorSize.height + 2;
    } else {
      // If custom cursor isn't supported let's consider
      // default cursor size is 16 pixels wide
      point.x += 18;
      point.y += 18;
    }
    this.orientationToolTipWindow.setLocation(point);
    this.orientationToolTipWindow.pack();
    this.orientationToolTipWindow.setVisible(true);
    this.orientationToolTip.paintImmediately(this.orientationToolTip.getBounds());
  }

  /**
   * Deletes tool tip text window from screen.
   */
  private void deleteOrientationToolTip() {
    if (this.orientationToolTipWindow != null) {
      this.orientationToolTipWindow.setVisible(false);
    }
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      if (this.findModelsButton != null) {
        this.findModelsButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            ImportedFurnitureWizardStepsPanel.class, "findModelsButton.mnemonic")).getKeyCode());
      }
      this.backFaceShownCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "backFaceShownCheckBox.mnemonic")).getKeyCode());
      this.nameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "nameLabel.mnemonic")).getKeyCode());
      this.nameLabel.setLabelFor(this.nameTextField);
      this.addToCatalogCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "addToCatalogCheckBox.mnemonic")).getKeyCode());;
      this.categoryLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "categoryLabel.mnemonic")).getKeyCode());
      this.categoryLabel.setLabelFor(this.categoryComboBox);
      this.creatorLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "creatorLabel.mnemonic")).getKeyCode());
      this.creatorLabel.setLabelFor(this.creatorTextField);
      this.widthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "widthLabel.mnemonic")).getKeyCode());
      this.widthLabel.setLabelFor(this.widthSpinner);
      this.depthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "depthLabel.mnemonic")).getKeyCode());
      this.depthLabel.setLabelFor(this.depthSpinner);
      this.heightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "heightLabel.mnemonic")).getKeyCode());
      this.heightLabel.setLabelFor(this.heightSpinner);
      this.keepProportionsCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "keepProportionsCheckBox.mnemonic")).getKeyCode());
      this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "elevationLabel.mnemonic")).getKeyCode());
      this.elevationLabel.setLabelFor(this.elevationSpinner);
      this.movableCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "movableCheckBox.mnemonic")).getKeyCode());;
      this.doorOrWindowCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "doorOrWindowCheckBox.mnemonic")).getKeyCode());;
      this.staircaseCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "staircaseCheckBox.mnemonic")).getKeyCode());;
      this.colorLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "colorLabel.mnemonic")).getKeyCode());
      this.colorLabel.setLabelFor(this.colorButton);
      this.clearColorButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "clearColorButton.mnemonic")).getKeyCode());
    }
  }

  /**
   * Layouts components in 4 panels added to this panel as cards.
   */
  private void layoutComponents() {
    this.cardLayout = new CardLayout();
    setLayout(this.cardLayout);
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    JPanel modelPanel = new JPanel(new GridBagLayout());
    modelPanel.add(this.modelChoiceOrChangeLabel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(standardGap, 0, standardGap, 0), 0, 0));
    if (this.findModelsButton != null) {
      modelPanel.add(this.modelChoiceOrChangeButton, new GridBagConstraints(
          0, 1, 1, 1, 1, 0, GridBagConstraints.LINE_END,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
      modelPanel.add(this.findModelsButton, new GridBagConstraints(
          1, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    } else {
      modelPanel.add(this.modelChoiceOrChangeButton, new GridBagConstraints(
          0, 1, 2, 1, 1, 0, GridBagConstraints.CENTER,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
    modelPanel.add(this.modelChoiceErrorLabel, new GridBagConstraints(
        0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(standardGap, 0, 0, 0), 0, 0));
    modelPanel.add(this.modelPreviewComponent, new GridBagConstraints(
        0, 3, 2, 1, 0, 1, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(standardGap, 0, standardGap, 0), 0, 0));

    JPanel orientationPanel = new JPanel(new GridBagLayout());
    orientationPanel.add(this.orientationLabel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(standardGap, 0, standardGap, 0), 0, 0));
    orientationPanel.add(this.rotationPreviewComponent, new GridBagConstraints(
        0, 1, 1, 1, 1, 1, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 15), 0, 0));
    JPanel rotationButtonsPanel = new JPanel(new GridBagLayout()) {
        @Override
        public void applyComponentOrientation(ComponentOrientation orientation) {
          // Ignore panel orientation to ensure left button is always at left of panel
        }
      };
    if (!OperatingSystem.isMacOSX()) {
      // Make buttons square
      Dimension preferredSize = this.turnUpButton.getPreferredSize();
      preferredSize.width =
      preferredSize.height = preferredSize.height + 4;
      this.turnUpButton.setPreferredSize(preferredSize);
      this.turnLeftButton.setPreferredSize(preferredSize);
      this.turnRightButton.setPreferredSize(preferredSize);
      this.turnDownButton.setPreferredSize(preferredSize);
      this.defaultOrientationButton.setPreferredSize(preferredSize);
    }

    rotationButtonsPanel.add(this.turnUpButton, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.SOUTH,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    rotationButtonsPanel.add(this.turnLeftButton, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.EAST,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    rotationButtonsPanel.add(this.defaultOrientationButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    rotationButtonsPanel.add(this.turnRightButton, new GridBagConstraints(
        2, 1, 1, 1, 1, 0, GridBagConstraints.WEST,
        GridBagConstraints.NONE, new Insets(0, standardGap, standardGap, 0), 0, 0));
    rotationButtonsPanel.add(this.turnDownButton, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.NORTH,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    orientationPanel.add(rotationButtonsPanel, new GridBagConstraints(
        1, 1, 1, 1, 1, 1, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    orientationPanel.add(this.backFaceShownLabel, new GridBagConstraints(
        0, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(standardGap, 0, standardGap, 0), 0, 0));
    orientationPanel.add(this.backFaceShownCheckBox, new GridBagConstraints(
        0, 5, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));

    JPanel attributesPanel = new JPanel(new GridBagLayout());
    attributesPanel.add(this.attributesLabel, new GridBagConstraints(
        0, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(standardGap, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.attributesPreviewComponent, new GridBagConstraints(
        0, 1, 1, 14, 1, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.nameLabel, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.nameTextField, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.addToCatalogCheckBox, new GridBagConstraints(
        1, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.categoryLabel, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.categoryComboBox, new GridBagConstraints(
        2, 3, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.creatorLabel, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.creatorTextField, new GridBagConstraints(
        2, 4, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.widthLabel, new GridBagConstraints(
        1, 5, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.widthSpinner, new GridBagConstraints(
        2, 5, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.depthLabel, new GridBagConstraints(
        1, 6, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.depthSpinner, new GridBagConstraints(
        2, 6, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.heightLabel, new GridBagConstraints(
        1, 7, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.heightSpinner, new GridBagConstraints(
        2, 7, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.keepProportionsCheckBox, new GridBagConstraints(
        1, 8, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.elevationLabel, new GridBagConstraints(
        1, 9, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.elevationSpinner, new GridBagConstraints(
        2, 9, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.movableCheckBox, new GridBagConstraints(
        1, 10, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.doorOrWindowCheckBox, new GridBagConstraints(
        1, 11, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.staircaseCheckBox, new GridBagConstraints(
        1, 12, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.colorLabel, new GridBagConstraints(
        1, 13, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    attributesPanel.add(this.colorButton, new GridBagConstraints(
        2, 13, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    attributesPanel.add(this.clearColorButton, new GridBagConstraints(
        2, 14, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    // Add a dummy label to force components to be at top of panel
    attributesPanel.add(new JLabel(), new GridBagConstraints(
        1, 15, 1, 1, 1, 1, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JPanel iconPanel = new JPanel(new GridBagLayout());
    iconPanel.add(this.iconLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(standardGap, 0, standardGap, 0), 0, 0));
    iconPanel.add(this.iconPreviewComponent, new GridBagConstraints(
        0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(standardGap, 0, standardGap, 0), 0, 0));

    add(modelPanel, ImportedFurnitureWizardController.Step.MODEL.name());
    add(orientationPanel, ImportedFurnitureWizardController.Step.ROTATION.name());
    add(attributesPanel, ImportedFurnitureWizardController.Step.ATTRIBUTES.name());
    add(iconPanel, ImportedFurnitureWizardController.Step.ICON.name());
  }

  /**
   * Switches to the component card matching current step.
   */
  private void updateStep(ImportedFurnitureWizardController controller) {
    ImportedFurnitureWizardController.Step step = controller.getStep();
    this.cardLayout.show(this, step.name());
    switch (step) {
      case MODEL:
        this.modelChoiceOrChangeButton.requestFocusInWindow();
        break;
      case ATTRIBUTES:
        this.nameTextField.requestFocusInWindow();
        break;
    }
  }

  /**
   * Returns the transformation matching current model rotation.
   */
  private Transform3D getModelRotationTransform() {
    float [][] modelRotation = this.controller.getModelRotation();
    Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
        modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
        modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
    Transform3D transform = new Transform3D();
    transform.setRotation(modelRotationMatrix);
    return transform;
  }

  /**
   * Updates model rotation from the values of <code>transform</code>.
   */
  private void updateModelRotation(Transform3D transform) {
    Matrix3f modelRotationMatrix = new Matrix3f();
    transform.getRotationScale(modelRotationMatrix);
    this.controller.setModelRotation(new float [][] {{modelRotationMatrix.m00, modelRotationMatrix.m01, modelRotationMatrix.m02},
                                                     {modelRotationMatrix.m10, modelRotationMatrix.m11, modelRotationMatrix.m12},
                                                     {modelRotationMatrix.m20, modelRotationMatrix.m21, modelRotationMatrix.m22}});
  }

  /**
   * Updates controller initial values from <code>piece</code>.
   */
  private void updateController(final CatalogPieceOfFurniture piece,
                                final UserPreferences preferences) {
    updatePreviewComponentsModel(null);
    if (piece == null) {
      setModelChoiceTexts(preferences);
    } else {
      setModelChangeTexts(preferences);
      setReadingState();
      // Load piece model asynchronously
      ModelManager.getInstance().loadModel(piece.getModel(),
          new ModelManager.ModelObserver() {
            public void modelUpdated(BranchGroup modelRoot) {
              updatePreviewComponentsModel(piece.getModel());
              setDefaultState();
              controller.setModel(piece.getModel());
              controller.setModelSize(piece.getModelSize());
              controller.setModelRotation(piece.getModelRotation());
              controller.setBackFaceShown(piece.isBackFaceShown());
              controller.setName(piece.getName());
              controller.setCreator(piece.getCreator());
              controller.setCategory(piece.getCategory());
              controller.setWidth(piece.getWidth());
              controller.setDepth(piece.getDepth());
              controller.setHeight(piece.getHeight());
              controller.setMovable(piece.isMovable());
              controller.setDoorOrWindow(piece.isDoorOrWindow());
              controller.setStaircaseCutOutShape(piece.getStaircaseCutOutShape());
              controller.setElevation(piece.getElevation());
              controller.setColor(piece.getColor());
              controller.setIconYaw(piece.getIconYaw());
              controller.setProportional(piece.isProportional());
            }

            public void modelError(Exception ex) {
              controller.setModel(null);
              setModelChoiceTexts(preferences);
              modelChoiceErrorLabel.setVisible(true);
              if (isShowing()) {
                SwingUtilities.getWindowAncestor(modelChoiceErrorLabel).pack();
              }
              setDefaultState();
            }
          });
    }
  }

  /**
   * Reads model from <code>modelName</code> and updates controller values.
   */
  private void updateController(final String modelName,
                                final UserPreferences preferences,
                                final ContentManager contentManager,
                                final FurnitureCategory defaultCategory,
                                final boolean ignoreException) {
    // Cancel current model
    this.controller.setModel(null);
    updatePreviewComponentsModel(null);
    setReadingState();
    // Read model in modelLoader executor
    this.modelLoader.execute(new Runnable() {
        public void run() {
          Content modelContent = null;
          try {
            modelContent = contentManager.getContent(modelName);
          } catch (RecorderException ex) {
            setDefaultStateAndShowModelChoiceError(modelName, preferences, !ignoreException);
          }

          try {
            BranchGroup model = ModelManager.getInstance().loadModel(modelContent);
            final Vector3f  modelSize = ModelManager.getInstance().getSize(model);
            // Copy model to a temporary OBJ content with materials and textures
            final Content copiedContent = copyToTemporaryOBJContent(model, modelName);
            // Load copied content using cache to make it accessible by preview components without waiting in EDT
            ModelManager.getInstance().loadModel(copiedContent, true, new ModelManager.ModelObserver() {
                public void modelUpdated(BranchGroup modelRoot) {
                  EventQueue.invokeLater(new Runnable() {
                      public void run() {
                        setDefaultStateAndInitializeReadModel(copiedContent, modelName, defaultCategory,
                            modelSize, preferences, contentManager);
                      }
                    });
                }

                public void modelError(Exception ex) {
                  EventQueue.invokeLater(new Runnable() {
                      public void run() {
                        setDefaultStateAndShowModelChoiceError(modelName, preferences, !ignoreException);
                      }
                    });
                }
              });
            return;
          } catch (IllegalArgumentException ex) {
            // Thrown by getSize if model is empty
          } catch (IOException ex) {
            // Try with zipped content
          }

          try {
            // Copy model content to a temporary content
            modelContent = TemporaryURLContent.copyToTemporaryURLContent(modelContent);
          } catch (IOException ex) {
            setDefaultStateAndShowModelChoiceError(modelName, preferences, !ignoreException);
            return;
          }

          // If content couldn't be loaded, try to load model as a zipped file
          ZipInputStream zipIn = null;
          try {
            URLContent urlContent = (URLContent)modelContent;
            // Open zipped stream
            zipIn = new ZipInputStream(urlContent.openStream());
            // Parse entries to see if one is readable
            while (true) {
              ZipEntry entry;
              try {
                if ((entry = zipIn.getNextEntry()) == null) {
                  // No more entry
                  break;
                }
              } catch (IllegalArgumentException ex) {
                // Exception thrown if entry name can't be read
                break;
              }

              String entryName = entry.getName();
              // Ignore directory entries and entries starting by a dot
              if (!entryName.endsWith("/")) {
                int slashIndex = entryName.lastIndexOf('/');
                String entryFileName = entryName.substring(++slashIndex);
                if (!entryFileName.startsWith(".")) {
                  URL entryUrl = new URL("jar:" + urlContent.getURL() + "!/"
                      + URLEncoder.encode(entryName, "UTF-8").replace("+", "%20").replace("%2F", "/"));
                  final Content entryContent = new TemporaryURLContent(entryUrl);
                  final AtomicReference<Vector3f> modelSize = new AtomicReference<Vector3f>();
                  // Load content using cache to make it accessible by preview components without waiting in EDT
                  ModelManager.getInstance().loadModel(entryContent, true, new ModelManager.ModelObserver() {
                      public void modelUpdated(BranchGroup modelRoot) {
                        try {
                          modelSize.set(ModelManager.getInstance().getSize(modelRoot));
                        } catch (IllegalArgumentException ex) {
                          // Thrown by getSize if model is empty
                        }
                      }

                      public void modelError(Exception ex) {
                      }
                    });

                  if (modelSize.get() != null) {
                    // Check if all remaining entries in the ZIP file can be read, to be able to save edited home with them
                    do {
                      try {
                        entry = zipIn.getNextEntry();
                      } catch (IllegalArgumentException ex) {
                        // Exception thrown if entry name can't be read
                        break;
                      }
                    } while (entry != null);

                    if (entry == null) {
                      EventQueue.invokeAndWait(new Runnable() {
                          public void run() {
                            setDefaultStateAndInitializeReadModel(entryContent, modelName, defaultCategory,
                                modelSize.get(), preferences, contentManager);
                          }
                      });
                      return;
                    }
                  }
                }
              }
            }
          } catch (IOException ex) {
            setDefaultStateAndShowModelChoiceError(modelName, preferences, !ignoreException);
            return;
          } catch (InterruptedException ex) {
            setDefaultState();
            return;
          } catch (InvocationTargetException ex) {
            // Show next message
          } finally {
            try {
              if (zipIn != null) {
                zipIn.close();
              }
            } catch (IOException ex) {
              // Ignore close exception
            }
          }

          // Found no readable model
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                if (isShowing()) {
                  setDefaultState();
                  setModelChoiceTexts(preferences);
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedFurnitureWizardStepsPanel.this),
                      preferences.getLocalizedString(ImportedFurnitureWizardStepsPanel.class, "modelChoiceFormatError"));
                }
              }
            });
        }
      });
  }

  /**
   * Restores default state and initializes read model.
   */
  private void setDefaultStateAndInitializeReadModel(final Content modelContent,
                                                     final String modelName,
                                                     final FurnitureCategory defaultCategory,
                                                     final Vector3f modelSize,
                                                     final UserPreferences preferences,
                                                     final ContentManager contentManager) {
    setDefaultState();
    updatePreviewComponentsModel(modelContent);
    controller.setModel(modelContent);
    controller.setModelSize(modelContent instanceof URLContent  ? ((URLContent)modelContent).getSize()  : null);
    setModelChangeTexts(preferences);
    modelChoiceErrorLabel.setVisible(false);
    controller.setModelRotation(new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}});
    controller.setBackFaceShown(false);
    controller.setName(contentManager.getPresentationName(
        modelName, ContentManager.ContentType.MODEL));
    controller.setCreator(null);
    controller.setCategory(defaultCategory);
    // Initialize size with default values
    controller.setWidth(modelSize.x);
    controller.setDepth(modelSize.z);
    controller.setHeight(modelSize.y);
    controller.setMovable(true);
    controller.setDoorOrWindow(false);
    controller.setStaircaseCutOutShape(null);
    controller.setColor(null);
    controller.setIconYaw((float)Math.PI / 8);
    controller.setProportional(true);
  }

  /**
   * Restores default state and shows an error message about the chosen model.
   */
  private void setDefaultStateAndShowModelChoiceError(final String modelName,
                                                      final UserPreferences preferences,
                                                      boolean showError) {
    setDefaultState();
    if (showError) {
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedFurnitureWizardStepsPanel.this),
                preferences.getLocalizedString(
                    ImportedFurnitureWizardStepsPanel.class, "modelChoiceError", modelName));
          }
        });
    }
  }

  /**
   * Returns a copy of a given <code>model</code> as a zip content at OBJ format.
   * Caution : this method must be thread safe because it can be called from model loader executor.
   */
  private Content copyToTemporaryOBJContent(BranchGroup model, String modelName) throws IOException {
    try {
      setReadingState();
      String objFile = new File(modelName).getName();
      if (!objFile.toLowerCase().endsWith(".obj")) {
        objFile += ".obj";
      }
      // Ensure the file contains only letters, figures, underscores, dots, hyphens or spaces
      if (objFile.matches(".*[^a-zA-Z0-9_\\.\\-\\ ].*")) {
        objFile = "model.obj";
      }
      File tempZipFile = OperatingSystem.createTemporaryFile("import", ".zip");
      OBJWriter.writeNodeInZIPFile(model, tempZipFile, 0, objFile, "3D model import " + modelName);
      return new TemporaryURLContent(new URL("jar:" + tempZipFile.toURI().toURL() + "!/"
          + URLEncoder.encode(objFile, "UTF-8").replace("+", "%20")));
    } finally {
      setDefaultState();
    }
  }

  /**
   * Sets the cursor to wait cursor and disables model choice button.
   */
  private void setReadingState() {
    this.modelChoiceOrChangeButton.setEnabled(false);
    Component rootPane = SwingUtilities.getRoot(ImportedFurnitureWizardStepsPanel.this);
    if (rootPane != null) {
      if (this.defaultCursor == null) {
        this.defaultCursor = rootPane.getCursor();
      }
      rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    } else {
      addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent event) {
          removeAncestorListener(this);
          if (!modelChoiceOrChangeButton.isEnabled()) {
            setReadingState();
          }
        }

        public void ancestorRemoved(AncestorEvent event) {
        }

        public void ancestorMoved(AncestorEvent event) {
        }
      });
    }
  }

  /**
   * Sets the default cursor and enables model choice button.
   */
  private void setDefaultState() {
    if (EventQueue.isDispatchThread()) {
      this.modelChoiceOrChangeButton.setEnabled(true);
      Component rootPane = SwingUtilities.getRoot(ImportedFurnitureWizardStepsPanel.this);
      if (rootPane != null) {
        rootPane.setCursor(this.defaultCursor);
      }
    } else {
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            setDefaultState();
          }
        });
    }
  }

  /**
   * Updates the model displayed by preview components.
   */
  private void updatePreviewComponentsModel(final Content model) {
    modelPreviewComponent.setModel(model);
    rotationPreviewComponent.setModel(model);
    attributesPreviewComponent.setModel(model);
    iconPreviewComponent.setModel(model);
  }

  /**
   * Sets the texts of label and button of model panel with change texts.
   */
  private void setModelChangeTexts(UserPreferences preferences) {
    this.modelChoiceOrChangeLabel.setText(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "modelChangeLabel.text"));
    this.modelChoiceOrChangeButton.setText(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "modelChangeButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.modelChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              ImportedFurnitureWizardStepsPanel.class, "modelChangeButton.mnemonic")).getKeyCode());
    }
  }

  /**
   * Sets the texts of label and button of model panel with choice texts.
   */
  private void setModelChoiceTexts(UserPreferences preferences) {
    this.modelChoiceOrChangeLabel.setText(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "modelChoiceLabel.text"));
    this.modelChoiceOrChangeButton.setText(SwingTools.getLocalizedLabelText(preferences,
        ImportedFurnitureWizardStepsPanel.class, "modelChoiceButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.modelChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              ImportedFurnitureWizardStepsPanel.class, "modelChoiceButton.mnemonic")).getKeyCode());
    }
  }

  /**
   * Returns the model name chosen for a file chooser dialog.
   */
  private String showModelChoiceDialog(UserPreferences preferences,
                                       ContentManager contentManager) {
    return contentManager.showOpenDialog(this,
        preferences.getLocalizedString(
            ImportedFurnitureWizardStepsPanel.class, "modelChoiceDialog.title"),
        ContentManager.ContentType.MODEL);
  }

  /**
   * Returns the icon content of the chosen piece.
   * Icon is created once on demand of view's controller, because it demands either
   * icon panel being displayed, or an offscreen 3D buffer that costs too much to create at each yaw change.
   */
  public Content getIcon() {
    try {
      return this.iconPreviewComponent.getIcon(400);
    } catch (IOException ex) {
      try {
        return new URLContent(new URL("file:/dummySweetHome3DContent"));
      } catch (MalformedURLException ex1) {
        return null;
      }
    }
  }

  /**
   * Preview component for model changes.
   */
  private static abstract class AbstractModelPreviewComponent extends ModelPreviewComponent {
    public AbstractModelPreviewComponent(boolean pitchAndScaleChangeSupported) {
      super(pitchAndScaleChangeSupported);
    }

    /**
     * Adds listeners to <code>controller</code> to update the rotation and the size of the piece model
     * displayed by this component.
     */
    protected void addSizeListeners(final ImportedFurnitureWizardController controller) {
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHOWN,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setBackFaceShown(controller.isBackFaceShown());
            }
          });
      PropertyChangeListener sizeChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            setModelRotationAndSize(controller.getModelRotation(),
                controller.getWidth(), controller.getDepth(), controller.getHeight());
          }
        };
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.MODEL_ROTATION,
          sizeChangeListener);
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.WIDTH,
          sizeChangeListener);
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.DEPTH,
          sizeChangeListener);
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.HEIGHT,
          sizeChangeListener);
    }

    /**
     * Adds listener to <code>controller</code> to update the color of the piece
     * displayed by this component.
     */
    protected void addColorListener(final ImportedFurnitureWizardController controller) {
      PropertyChangeListener colorChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setModelColor(controller.getColor());
          }
        };
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.COLOR,
          colorChangeListener);
    }

    /**
     * Adds listener to <code>controller</code> to update the yaw of the piece icon
     * displayed by this component.
     */
    protected void addIconYawListener(final ImportedFurnitureWizardController controller) {
      PropertyChangeListener iconYawChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setViewYaw(controller.getIconYaw());
          }
        };
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.ICON_YAW,
          iconYawChangeListener);
    }
  }


  /**
   * Preview component for model orientation.
   */
  private static class RotationPreviewComponent extends JPanel {
    private static final int COMPONENT_PREFERRED_WIDTH = Math.round(200 * SwingTools.getResolutionScale());

    private ModelPreviewComponent perspectiveViewComponent3D;
    private JLabel                frontViewLabel;
    private ModelPreviewComponent frontViewComponent3D;
    private JLabel                sideViewLabel;
    private ModelPreviewComponent sideViewComponent3D;
    private JLabel                topViewLabel;
    private ModelPreviewComponent topViewComponent3D;
    private JLabel                perspectiveViewLabel;
    private BranchGroup           modelNode;

    public RotationPreviewComponent(UserPreferences preferences,
                                    final ImportedFurnitureWizardController controller) {
      createComponents(preferences, controller);
      layoutComponents();
    }

    public void setModel(Content model) {
      this.perspectiveViewComponent3D.setModel(model);
      this.frontViewComponent3D.setModel(model);
      this.sideViewComponent3D.setModel(model);
      this.topViewComponent3D.setModel(model);
    }

    /**
     * Creates components displayed by this panel.
     */
    private void createComponents(UserPreferences preferences,
                                  ImportedFurnitureWizardController controller) {
      Color backgroundColor = new Color(0xE5E5E5);
      this.perspectiveViewComponent3D = new ModelPreviewComponent(true);
      this.perspectiveViewComponent3D.setBackground(backgroundColor);
      addRotationListener(this.perspectiveViewComponent3D, controller, true);

      this.frontViewComponent3D = new ModelPreviewComponent(false, false, false);
      this.frontViewComponent3D.setViewYaw(0);
      this.frontViewComponent3D.setViewPitch(0);
      this.frontViewComponent3D.setParallelProjection(true);
      this.frontViewComponent3D.setBackground(backgroundColor);
      addRotationListener(this.frontViewComponent3D, controller, false);

      this.sideViewComponent3D = new ModelPreviewComponent(false, false, false);
      this.sideViewComponent3D.setViewYaw(Locale.getDefault().equals(Locale.US)
          ? -(float)Math.PI / 2
          : (float)Math.PI / 2);
      this.sideViewComponent3D.setViewPitch(0);
      this.sideViewComponent3D.setParallelProjection(true);
      this.sideViewComponent3D.setBackground(backgroundColor);
      addRotationListener(this.sideViewComponent3D, controller, false);

      this.topViewComponent3D = new ModelPreviewComponent(false, false, false);
      this.topViewComponent3D.setViewYaw(0);
      this.topViewComponent3D.setViewPitch(-(float)Math.PI / 2);
      this.topViewComponent3D.setParallelProjection(true);
      this.topViewComponent3D.setBackground(backgroundColor);
      addRotationListener(this.topViewComponent3D, controller, false);

      this.frontViewLabel = new JLabel(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "frontViewLabel.text"));
      this.sideViewLabel = new JLabel(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "sideViewLabel.text"));
      this.topViewLabel = new JLabel(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "topViewLabel.text"));
      this.perspectiveViewLabel = new JLabel(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "perspectiveViewLabel.text"));
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(COMPONENT_PREFERRED_WIDTH,
          COMPONENT_PREFERRED_WIDTH + 4 + this.frontViewLabel.getPreferredSize().height * 2);
    }

    /**
     * Adds listeners to <code>controller</code> to update the rotation of the piece model
     * displayed by the 3D components.
     */
    protected void addRotationListener(final ModelPreviewComponent viewComponent3D,
                                       final ImportedFurnitureWizardController controller,
                                       final boolean mainComponent) {
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHOWN,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              viewComponent3D.setBackFaceShown(controller.isBackFaceShown());
            }
          });
      if (mainComponent) {
        controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.MODEL,
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                modelNode = null;
              }
            });
      }
      PropertyChangeListener rotationChangeListener = new PropertyChangeListener () {
          public void propertyChange(final PropertyChangeEvent ev) {
            viewComponent3D.setModelRotation(controller.getModelRotation());

            if (mainComponent
                && ev.getOldValue() != null
                && viewComponent3D.getModel() != null) {
              // Update size when a new rotation is provided
              if (modelNode == null) {
                ModelManager.getInstance().loadModel(viewComponent3D.getModel(), new ModelManager.ModelObserver() {
                    public void modelUpdated(BranchGroup modelRoot) {
                      modelNode = modelRoot;
                      updateSize(controller, (float [][])ev.getOldValue(), (float [][])ev.getNewValue());
                    }

                    public void modelError(Exception ex) {
                    }
                  });
              } else {
                updateSize(controller, (float [][])ev.getOldValue(), (float [][])ev.getNewValue());
              }
            }
          }

          private void updateSize(final ImportedFurnitureWizardController controller,
                                  float [][] oldModelRotation,
                                  float [][] newModelRotation) {
            try {
              Transform3D normalization = ModelManager.getInstance().getNormalizedTransform(modelNode, oldModelRotation, 1f);
              Transform3D scaleTransform = new Transform3D();
              scaleTransform.setScale(new Vector3d(controller.getWidth(), controller.getHeight(), controller.getDepth()));
              scaleTransform.mul(normalization);

              // Compute rotation before old model rotation
              Matrix3f oldModelRotationMatrix = new Matrix3f(oldModelRotation [0][0], oldModelRotation [0][1], oldModelRotation [0][2],
                  oldModelRotation [1][0], oldModelRotation [1][1], oldModelRotation [1][2],
                  oldModelRotation [2][0], oldModelRotation [2][1], oldModelRotation [2][2]);
              oldModelRotationMatrix.invert();
              Transform3D backRotationTransform = new Transform3D();
              backRotationTransform.setRotation(oldModelRotationMatrix);
              backRotationTransform.mul(scaleTransform);

              // Compute size after new model rotation
              Matrix3f newModelRotationMatrix = new Matrix3f(newModelRotation [0][0], newModelRotation [0][1], newModelRotation [0][2],
                  newModelRotation [1][0], newModelRotation [1][1], newModelRotation [1][2],
                  newModelRotation [2][0], newModelRotation [2][1], newModelRotation [2][2]);
              Transform3D transform = new Transform3D();
              transform.setRotation(newModelRotationMatrix);
              transform.mul(backRotationTransform);

              Vector3f newSize = ModelManager.getInstance().getSize(modelNode, transform);
              controller.setWidth(newSize.x);
              controller.setHeight(newSize.y);
              controller.setDepth(newSize.z);
            } catch (IllegalArgumentException ex) {
              // Model is empty
            }
          }
        };
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.MODEL_ROTATION,
          rotationChangeListener);
    }

    /**
     * Layouts components.
     */
    private void layoutComponents() {
      setLayout(new GridBagLayout());

      // Place the 4 3D components differently depending on US or other country
      if (Locale.getDefault().equals(Locale.US)) {
        // Default projection view at top left
        add(this.perspectiveViewLabel, new GridBagConstraints(
            0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
        add(this.perspectiveViewComponent3D, new GridBagConstraints(
            0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
        // Top view at top right
        add(this.topViewLabel, new GridBagConstraints(
            1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
        add(this.topViewComponent3D, new GridBagConstraints(
            1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
        // Left view at bottom left
        add(this.sideViewLabel, new GridBagConstraints(
            0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
        add(this.sideViewComponent3D, new GridBagConstraints(
            0, 3, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
        // Front view at bottom right
        add(this.frontViewLabel, new GridBagConstraints(
            1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
        add(this.frontViewComponent3D, new GridBagConstraints(
            1, 3, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      } else {
        // Right view at top left
        add(this.sideViewLabel, new GridBagConstraints(
            0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
        add(this.sideViewComponent3D, new GridBagConstraints(
            0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
        // Front view at top right
        add(this.frontViewLabel, new GridBagConstraints(
            1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
        add(this.frontViewComponent3D, new GridBagConstraints(
            1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
        // Default projection view at bottom left
        add(this.perspectiveViewLabel, new GridBagConstraints(
            0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
        add(this.perspectiveViewComponent3D, new GridBagConstraints(
            0, 3, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
        // Top view at bottom right
        add(this.topViewLabel, new GridBagConstraints(
            1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
        add(this.topViewComponent3D, new GridBagConstraints(
            1, 3, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      }
    }
  }


  /**
   * Preview component for model attributes.
   */
  private static class AttributesPreviewComponent extends AbstractModelPreviewComponent {
    public AttributesPreviewComponent(ImportedFurnitureWizardController controller) {
      super(true);
      addSizeListeners(controller);
      addColorListener(controller);
    }
  }


  /**
   * Preview component for model icon.
   */
  private static class IconPreviewComponent extends AbstractModelPreviewComponent {
    private static final int ICON_SIZE = Math.round(128 * SwingTools.getResolutionScale());

    private ImportedFurnitureWizardController controller;

    public IconPreviewComponent(ImportedFurnitureWizardController controller) {
      super(false);
      this.controller = controller;
      addSizeListeners(controller);
      addColorListener(controller);
      addIconYawListener(controller);

      Color backgroundColor = UIManager.getColor("window");
      if (backgroundColor == null) {
        backgroundColor = Color.LIGHT_GRAY;
      }
      setBackground(backgroundColor);
    }

    @Override
    public Dimension getPreferredSize() {
      Insets insets = getInsets();
      return new Dimension(ICON_SIZE + insets.left + insets.right, ICON_SIZE  + insets.top + insets.bottom);
    }

    /**
     * Sets the <code>yaw</code> angle used by view platform transform.
     */
    protected void setViewYaw(float viewYaw) {
      super.setViewYaw(viewYaw);
      this.controller.setIconYaw(viewYaw);
    }
  }
}
