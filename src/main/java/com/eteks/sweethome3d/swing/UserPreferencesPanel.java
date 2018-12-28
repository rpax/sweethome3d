/*
 * UserPreferencesPanel.java 18 sept. 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.security.AccessControlException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanel extends JPanel implements DialogView {
  private final UserPreferencesController controller;
  private JLabel           languageLabel;
  private JComboBox        languageComboBox;
  private JButton          languageLibraryImportButton;
  private JLabel           unitLabel;
  private JComboBox        unitComboBox;
  private JLabel           currencyLabel;
  private JComboBox        currencyComboBox;
  private JCheckBox        valueAddedTaxCheckBox;
  private JLabel           furnitureCatalogViewLabel;
  private JRadioButton     treeRadioButton;
  private JRadioButton     listRadioButton;
  private JLabel           navigationPanelLabel;
  private JCheckBox        navigationPanelCheckBox;
  private JLabel           aerialViewCenteredOnSelectionLabel;
  private JCheckBox        aerialViewCenteredOnSelectionCheckBox;
  private JLabel           observerCameraSelectedAtChangeLabel;
  private JCheckBox        observerCameraSelectedAtChangeCheckBox;
  private JLabel           magnetismLabel;
  private JCheckBox        magnetismCheckBox;
  private JLabel           rulersLabel;
  private JCheckBox        rulersCheckBox;
  private JLabel           gridLabel;
  private JCheckBox        gridCheckBox;
  private JLabel           defaultFontNameLabel;
  private FontNameComboBox defaultFontNameComboBox;
  private JLabel           furnitureIconLabel;
  private JRadioButton     catalogIconRadioButton;
  private JRadioButton     topViewRadioButton;
  private JLabel           iconSizeLabel;
  private JComboBox        iconSizeComboBox;
  private JLabel           roomRenderingLabel;
  private JRadioButton     monochromeRadioButton;
  private JRadioButton     floorColorOrTextureRadioButton;
  private JLabel           wallPatternLabel;
  private JComboBox        wallPatternComboBox;
  private JLabel           newWallPatternLabel;
  private JComboBox        newWallPatternComboBox;
  private JLabel           newWallThicknessLabel;
  private JSpinner         newWallThicknessSpinner;
  private JLabel           newWallHeightLabel;
  private JSpinner         newWallHeightSpinner;
  private JLabel           newFloorThicknessLabel;
  private JSpinner         newFloorThicknessSpinner;
  private JCheckBox        checkUpdatesCheckBox;
  private JButton          checkUpdatesNowButton;
  private JCheckBox        autoSaveDelayForRecoveryCheckBox;
  private JSpinner         autoSaveDelayForRecoverySpinner;
  private JLabel           autoSaveDelayForRecoveryUnitLabel;
  private JButton          resetDisplayedActionTipsButton;
  private String           dialogTitle;

  /**
   * Creates a preferences panel that layouts the editable properties
   * of its <code>controller</code>.
   */
  public UserPreferencesPanel(UserPreferences preferences,
                              UserPreferencesController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final UserPreferencesController controller) {
    if (controller.isPropertyEditable(UserPreferencesController.Property.LANGUAGE)) {
      // Create language label and combo box bound to controller LANGUAGE property
      this.languageLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "languageLabel.text"));
      this.languageComboBox = new JComboBox(new DefaultComboBoxModel(preferences.getSupportedLanguages()));
      this.languageComboBox.setRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList list,
              Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String language = (String)value;
            Locale locale;
            int underscoreIndex = language.indexOf("_");
            if (underscoreIndex != -1) {
              locale = new Locale(language.substring(0, underscoreIndex),
                  language.substring(underscoreIndex + 1));
            } else {
              locale = new Locale(language);
            }
            String displayedValue = locale.getDisplayLanguage(locale);
            displayedValue = Character.toUpperCase(displayedValue.charAt(0)) + displayedValue.substring(1);
            if (underscoreIndex != -1) {
              displayedValue += " - " + locale.getDisplayCountry(locale);
            }
            return super.getListCellRendererComponent(list, displayedValue, index, isSelected, cellHasFocus);
          }
        });
      this.languageComboBox.setMaximumRowCount(Integer.MAX_VALUE);
      this.languageComboBox.setSelectedItem(controller.getLanguage());
      this.languageComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setLanguage((String)languageComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.LANGUAGE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              languageComboBox.setSelectedItem(controller.getLanguage());
            }
          });
      preferences.addPropertyChangeListener(UserPreferences.Property.SUPPORTED_LANGUAGES,
          new SupportedLanguagesChangeListener(this));
    }

    if (controller.mayImportLanguageLibrary()) {
      this.languageLibraryImportButton = new JButton(new ResourceAction(
          preferences, UserPreferencesPanel.class, "IMPORT_LANGUAGE_LIBRARY", true) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              controller.importLanguageLibrary();
            }
          });
      this.languageLibraryImportButton.setToolTipText(preferences.getLocalizedString(
          UserPreferencesPanel.class, "IMPORT_LANGUAGE_LIBRARY.tooltip"));
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.UNIT)) {
      // Create unit label and combo box bound to controller UNIT property
      this.unitLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "unitLabel.text"));
      this.unitComboBox = new JComboBox(LengthUnit.values());
      final Map<LengthUnit, String> comboBoxTexts = new HashMap<LengthUnit, String>();
      comboBoxTexts.put(LengthUnit.MILLIMETER, preferences.getLocalizedString(
          UserPreferencesPanel.class, "unitComboBox.millimeter.text"));
      comboBoxTexts.put(LengthUnit.CENTIMETER, preferences.getLocalizedString(
          UserPreferencesPanel.class, "unitComboBox.centimeter.text"));
      comboBoxTexts.put(LengthUnit.METER, preferences.getLocalizedString(
          UserPreferencesPanel.class, "unitComboBox.meter.text"));
      comboBoxTexts.put(LengthUnit.INCH, preferences.getLocalizedString(
          UserPreferencesPanel.class, "unitComboBox.inch.text"));
      comboBoxTexts.put(LengthUnit.INCH_DECIMALS, preferences.getLocalizedString(
          UserPreferencesPanel.class, "unitComboBox.inchDecimals.text"));
      this.unitComboBox.setRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                        boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, comboBoxTexts.get(value), index, isSelected, cellHasFocus);
          }
        });
      this.unitComboBox.setSelectedItem(controller.getUnit());
      this.unitComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setUnit((LengthUnit)unitComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              unitComboBox.setSelectedItem(controller.getUnit());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.CURRENCY)) {
      // Create currency label and combo box bound to controller CURRENCY property
      this.currencyLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "currencyLabel.text"));

      final Map<String, String> currencyMap = new HashMap<String, String>();
      for (Locale locale : Locale.getAvailableLocales()) {
        try {
          Currency currency = Currency.getInstance(locale);
          if (currencyMap.get(currency.getCurrencyCode()) == null
              || currencyMap.get(currency.getCurrencyCode()).length() > currency.getSymbol(locale).length()) {
            currencyMap.put(currency.getCurrencyCode(), currency.getSymbol(locale));
          }
        } catch (IllegalArgumentException ex) {
          // Currency in Locale not found
        }
      }
      Vector<String> currencies = new Vector<String>(currencyMap.keySet());
      Collections.sort(currencies);
      Locale defaultLocale = Locale.getDefault();
      Currency defaultLocaleCurrency = defaultLocale.getCountry() != null && defaultLocale.getCountry().length() == 2
          ? Currency.getInstance(defaultLocale)
          : null;
      if (defaultLocaleCurrency != null) {
        currencies.add(0, defaultLocaleCurrency.getCurrencyCode()); // Add also the currency of the current locale
      }
      currencies.add(0, null);
      this.currencyComboBox = new JComboBox(currencies);
      final String noCurrencyText = preferences.getLocalizedString(UserPreferencesPanel.class, "currencyComboBox.noCurrency.text");
      this.currencyComboBox.setRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                        boolean cellHasFocus) {
            if (value == null) {
              value = noCurrencyText;
            } else {
              value = value + " " + currencyMap.get(value);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          }
        });
      this.currencyComboBox.setSelectedItem(controller.getCurrency());
      this.currencyComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setCurrency((String)currencyComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.CURRENCY,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              currencyComboBox.setSelectedItem(controller.getCurrency());
              if (valueAddedTaxCheckBox != null) {
                valueAddedTaxCheckBox.setEnabled(controller.getCurrency() != null);
              }
            }
          });

      if (controller.isPropertyEditable(UserPreferencesController.Property.VALUE_ADDED_TAX_ENABLED)) {
        this.valueAddedTaxCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
            UserPreferencesPanel.class, "valueAddedTaxCheckBox.text"));
        this.valueAddedTaxCheckBox.setEnabled(controller.getCurrency() != null);
        this.valueAddedTaxCheckBox.setSelected(controller.isValueAddedTaxEnabled());
        this.valueAddedTaxCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
              controller.setValueAddedTaxEnabled(valueAddedTaxCheckBox.isSelected());
            }
          });
        controller.addPropertyChangeListener(UserPreferencesController.Property.VALUE_ADDED_TAX_ENABLED,
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                  valueAddedTaxCheckBox.setEnabled(controller.getCurrency() != null);
                  valueAddedTaxCheckBox.setSelected(controller.isValueAddedTaxEnabled());
                }
              });
      }
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.FURNITURE_CATALOG_VIEWED_IN_TREE)) {
      // Create furniture catalog label and radio buttons bound to controller FURNITURE_CATALOG_VIEWED_IN_TREE property
      this.furnitureCatalogViewLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "furnitureCatalogViewLabel.text"));
      this.treeRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "treeRadioButton.text"),
          controller.isFurnitureCatalogViewedInTree());
      this.listRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "listRadioButton.text"),
          !controller.isFurnitureCatalogViewedInTree());
      ButtonGroup furnitureCatalogViewButtonGroup = new ButtonGroup();
      furnitureCatalogViewButtonGroup.add(this.treeRadioButton);
      furnitureCatalogViewButtonGroup.add(this.listRadioButton);

      ItemListener furnitureCatalogViewChangeListener = new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setFurnitureCatalogViewedInTree(treeRadioButton.isSelected());
          }
        };
      this.treeRadioButton.addItemListener(furnitureCatalogViewChangeListener);
      this.listRadioButton.addItemListener(furnitureCatalogViewChangeListener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_CATALOG_VIEWED_IN_TREE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              treeRadioButton.setSelected(controller.isFurnitureCatalogViewedInTree());
            }
          });
    }

    boolean no3D;
    try {
      no3D = Boolean.getBoolean("com.eteks.sweethome3d.no3D");
    } catch (AccessControlException ex) {
      // If com.eteks.sweethome3d.no3D property can't be read,
      // security manager won't allow to access to Java 3D DLLs required by 3D view too
      no3D = true;
    }
    if (controller.isPropertyEditable(UserPreferencesController.Property.NAVIGATION_PANEL_VISIBLE)
        && !no3D) {
      // Create navigation panel label and check box bound to controller NAVIGATION_PANEL_VISIBLE property
      this.navigationPanelLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "navigationPanelLabel.text"));
      this.navigationPanelCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "navigationPanelCheckBox.text"));
      if (!OperatingSystem.isMacOSX()
          || OperatingSystem.isMacOSXLeopardOrSuperior()) {
        this.navigationPanelCheckBox.setSelected(controller.isNavigationPanelVisible());
        this.navigationPanelCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
              controller.setNavigationPanelVisible(navigationPanelCheckBox.isSelected());
            }
          });
        controller.addPropertyChangeListener(UserPreferencesController.Property.NAVIGATION_PANEL_VISIBLE,
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                navigationPanelCheckBox.setSelected(controller.isNavigationPanelVisible());
              }
            });
      } else {
        // No support for navigation panel under Mac OS X Tiger (too unstable)
        this.navigationPanelCheckBox.setEnabled(false);
      }
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED)
        && !no3D) {
      // Create aerialViewCenteredOnSelection label and check box bound to controller AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED property
      this.aerialViewCenteredOnSelectionLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "aerialViewCenteredOnSelectionLabel.text"));
      this.aerialViewCenteredOnSelectionCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "aerialViewCenteredOnSelectionCheckBox.text"), controller.isAerialViewCenteredOnSelectionEnabled());
      this.aerialViewCenteredOnSelectionCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setAerialViewCenteredOnSelectionEnabled(aerialViewCenteredOnSelectionCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              aerialViewCenteredOnSelectionCheckBox.setSelected(controller.isAerialViewCenteredOnSelectionEnabled());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.OBSERVER_CAMERA_SELECTED_AT_CHANGE)
        && !no3D) {
      // Create observerCameraSelectedAtChangeLabel label and check box bound to controller OBSERVER_CAMERA_SELECTED_AT_CHANGE property
      this.observerCameraSelectedAtChangeLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "observerCameraSelectedAtChangeLabel.text"));
      this.observerCameraSelectedAtChangeCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "observerCameraSelectedAtChangeCheckBox.text"), controller.isObserverCameraSelectedAtChange());
      this.observerCameraSelectedAtChangeCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setObserverCameraSelectedAtChange(observerCameraSelectedAtChangeCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.OBSERVER_CAMERA_SELECTED_AT_CHANGE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              observerCameraSelectedAtChangeCheckBox.setSelected(controller.isObserverCameraSelectedAtChange());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.MAGNETISM_ENABLED)) {
      // Create magnetism label and check box bound to controller MAGNETISM_ENABLED property
      this.magnetismLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "magnetismLabel.text"));
      this.magnetismCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "magnetismCheckBox.text"), controller.isMagnetismEnabled());
      this.magnetismCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setMagnetismEnabled(magnetismCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.MAGNETISM_ENABLED,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              magnetismCheckBox.setSelected(controller.isMagnetismEnabled());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.RULERS_VISIBLE)) {
      // Create rulers label and check box bound to controller RULERS_VISIBLE property
      this.rulersLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "rulersLabel.text"));
      this.rulersCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "rulersCheckBox.text"), controller.isRulersVisible());
      this.rulersCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setRulersVisible(rulersCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.RULERS_VISIBLE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              rulersCheckBox.setSelected(controller.isRulersVisible());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.GRID_VISIBLE)) {
      // Create grid label and check box bound to controller GRID_VISIBLE property
      this.gridLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "gridLabel.text"));
      this.gridCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "gridCheckBox.text"), controller.isGridVisible());
      this.gridCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setGridVisible(gridCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.GRID_VISIBLE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              gridCheckBox.setSelected(controller.isGridVisible());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.DEFAULT_FONT_NAME)) {
      // Create font name label and combo box bound to controller DEFAULT_FONT_NAME property
      this.defaultFontNameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "defaultFontNameLabel.text"));
      this.defaultFontNameComboBox = new FontNameComboBox(preferences);
      this.defaultFontNameComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            String selectedItem = (String)defaultFontNameComboBox.getSelectedItem();
            controller.setDefaultFontName(selectedItem == FontNameComboBox.DEFAULT_SYSTEM_FONT_NAME
                ? null : selectedItem);
          }
        });
      PropertyChangeListener fontNameChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            String defaultFontName = controller.getDefaultFontName();
            defaultFontNameComboBox.setSelectedItem(defaultFontName == null
                ? FontNameComboBox.DEFAULT_SYSTEM_FONT_NAME : defaultFontName);
          }
        };
      controller.addPropertyChangeListener(UserPreferencesController.Property.DEFAULT_FONT_NAME, fontNameChangeListener);
      fontNameChangeListener.propertyChange(null);
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.FURNITURE_VIEWED_FROM_TOP)) {
      // Create furniture appearance label and radio buttons bound to controller FURNITURE_VIEWED_FROM_TOP property
      this.furnitureIconLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "furnitureIconLabel.text"));
      this.catalogIconRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "catalogIconRadioButton.text"),
          !controller.isFurnitureViewedFromTop());
      this.topViewRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "topViewRadioButton.text"),
          controller.isFurnitureViewedFromTop());
      if (controller.isPropertyEditable(UserPreferencesController.Property.FURNITURE_MODEL_ICON_SIZE)
          && !no3D) {
        this.iconSizeLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
            UserPreferencesPanel.class, "iconSizeLabel.text"));
        Set<Integer> iconSizes = new TreeSet<Integer>(Arrays.asList(128, 256, 512 ,1024));
        iconSizes.add(controller.getFurnitureModelIconSize());
        this.iconSizeComboBox = new JComboBox(iconSizes.toArray());
        this.iconSizeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
              return super.getListCellRendererComponent(list, value + "\u00d7" + value, index, isSelected, cellHasFocus);
            }
          });
        this.iconSizeComboBox.setSelectedItem(controller.getFurnitureModelIconSize());
        this.iconSizeComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
              controller.setFurnitureModelIconSize((Integer)iconSizeComboBox.getSelectedItem());
            }
          });
        controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_MODEL_ICON_SIZE,
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                iconSizeComboBox.setSelectedItem(controller.getFurnitureModelIconSize());
              }
            });
      }

      if (no3D) {
        this.catalogIconRadioButton.setEnabled(false);
        this.topViewRadioButton.setEnabled(false);
      } else {
        if (Component3DManager.getInstance().isOffScreenImageSupported()) {
          ButtonGroup furnitureAppearanceButtonGroup = new ButtonGroup();
          furnitureAppearanceButtonGroup.add(this.catalogIconRadioButton);
          furnitureAppearanceButtonGroup.add(this.topViewRadioButton);

          ItemListener furnitureAppearanceChangeListener = new ItemListener() {
              public void itemStateChanged(ItemEvent ev) {
                controller.setFurnitureViewedFromTop(topViewRadioButton.isSelected());
              }
            };
          this.catalogIconRadioButton.addItemListener(furnitureAppearanceChangeListener);
          this.topViewRadioButton.addItemListener(furnitureAppearanceChangeListener);
          controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_VIEWED_FROM_TOP,
              new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                  topViewRadioButton.setSelected(controller.isFurnitureViewedFromTop());
                  if (iconSizeComboBox != null) {
                    iconSizeComboBox.setEnabled(controller.isFurnitureViewedFromTop());
                  }
                }
              });
          if (this.iconSizeComboBox != null) {
            this.iconSizeComboBox.setEnabled(controller.isFurnitureViewedFromTop());
          }
        } else {
          this.catalogIconRadioButton.setEnabled(false);
          this.topViewRadioButton.setEnabled(false);
        }
      }
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.ROOM_FLOOR_COLORED_OR_TEXTURED)) {
      // Create room rendering label and radio buttons bound to controller ROOM_FLOOR_COLORED_OR_TEXTURED property
      this.roomRenderingLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "roomRenderingLabel.text"));
      this.monochromeRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "monochromeRadioButton.text"),
          !controller.isRoomFloorColoredOrTextured());
      this.floorColorOrTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "floorColorOrTextureRadioButton.text"),
          controller.isRoomFloorColoredOrTextured());
      ButtonGroup roomRenderingButtonGroup = new ButtonGroup();
      roomRenderingButtonGroup.add(this.monochromeRadioButton);
      roomRenderingButtonGroup.add(this.floorColorOrTextureRadioButton);
      ItemListener roomRenderingChangeListener = new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setRoomFloorColoredOrTextured(floorColorOrTextureRadioButton.isSelected());
          }
        };
      this.monochromeRadioButton.addItemListener(roomRenderingChangeListener);
      this.floorColorOrTextureRadioButton.addItemListener(roomRenderingChangeListener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.ROOM_FLOOR_COLORED_OR_TEXTURED,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              floorColorOrTextureRadioButton.setSelected(controller.isRoomFloorColoredOrTextured());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_WALL_PATTERN)) {
      // Create new wall pattern label and combo box bound to controller NEW_WALL_PATTERN property
      this.newWallPatternLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "newWallPatternLabel.text"));
      List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
      this.newWallPatternComboBox = new JComboBox(new DefaultComboBoxModel(patterns.toArray()));
      this.newWallPatternComboBox.setRenderer(getPatternRenderer());
      TextureImage newWallPattern = controller.getNewWallPattern();
      this.newWallPatternComboBox.setSelectedItem(newWallPattern != null
          ? newWallPattern
          : controller.getWallPattern());
      this.newWallPatternComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setNewWallPattern((TextureImage)newWallPatternComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_PATTERN,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newWallPatternComboBox.setSelectedItem(controller.getNewWallPattern());
            }
          });
    } else if (controller.isPropertyEditable(UserPreferencesController.Property.WALL_PATTERN)) {
      // Create wall pattern label and combo box bound to controller WALL_PATTERN property
      this.wallPatternLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "wallPatternLabel.text"));
      List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
      this.wallPatternComboBox = new JComboBox(new DefaultComboBoxModel(patterns.toArray()));
      this.wallPatternComboBox.setRenderer(getPatternRenderer());
      this.wallPatternComboBox.setSelectedItem(controller.getWallPattern());
      this.wallPatternComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setWallPattern((TextureImage)wallPatternComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.WALL_PATTERN,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              wallPatternComboBox.setSelectedItem(controller.getWallPattern());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_WALL_THICKNESS)) {
      // Create wall thickness label and spinner bound to controller NEW_WALL_THICKNESS property
      this.newWallThicknessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "newWallThicknessLabel.text"));
      final SpinnerLengthModel newWallThicknessSpinnerModel = new SpinnerLengthModel(0.5f, 0.125f, controller);
      this.newWallThicknessSpinner = new AutoCommitLengthSpinner(newWallThicknessSpinnerModel, controller);
      newWallThicknessSpinnerModel.setValue(controller.getNewWallThickness());
      newWallThicknessSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.setNewWallThickness(((Number)newWallThicknessSpinnerModel.getValue()).floatValue());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_THICKNESS,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newWallThicknessSpinnerModel.setValue(controller.getNewWallThickness());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_WALL_HEIGHT)) {
      // Create wall height label and spinner bound to controller NEW_WALL_HEIGHT property
      this.newWallHeightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "newWallHeightLabel.text"));
      final SpinnerLengthModel newWallHeightSpinnerModel = new SpinnerLengthModel(10f, 2f, controller);
      this.newWallHeightSpinner = new AutoCommitLengthSpinner(newWallHeightSpinnerModel, controller);
      newWallHeightSpinnerModel.setValue(controller.getNewWallHeight());
      newWallHeightSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.setNewWallHeight(((Number)newWallHeightSpinnerModel.getValue()).floatValue());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_HEIGHT,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newWallHeightSpinnerModel.setValue(controller.getNewWallHeight());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_FLOOR_THICKNESS)) {
      // Create wall thickness label and spinner bound to controller NEW_FLOOR_THICKNESS property
      this.newFloorThicknessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "newFloorThicknessLabel.text"));
      final SpinnerLengthModel newFloorThicknessSpinnerModel = new SpinnerLengthModel(0.5f, 0.125f, controller);
      this.newFloorThicknessSpinner = new AutoCommitLengthSpinner(newFloorThicknessSpinnerModel, controller);
      newFloorThicknessSpinnerModel.setValue(controller.getNewFloorThickness());
      newFloorThicknessSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.setNewFloorThickness(((Number)newFloorThicknessSpinnerModel.getValue()).floatValue());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_FLOOR_THICKNESS,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newFloorThicknessSpinnerModel.setValue(controller.getNewFloorThickness());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.CHECK_UPDATES_ENABLED)) {
      // Create check box bound to controller CHECK_UPDATES_ENABLED property
      this.checkUpdatesCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "checkUpdatesCheckBox.text"), controller.isCheckUpdatesEnabled());
      this.checkUpdatesCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setCheckUpdatesEnabled(checkUpdatesCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.CHECK_UPDATES_ENABLED,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              checkUpdatesCheckBox.setSelected(controller.isCheckUpdatesEnabled());
            }
          });

      this.checkUpdatesNowButton = new JButton(new ResourceAction.ButtonAction(
          new ResourceAction(preferences, UserPreferencesPanel.class, "CHECK_UPDATES_NOW", true) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              controller.checkUpdates();
            }
          }));
    }


    if (controller.isPropertyEditable(UserPreferencesController.Property.AUTO_SAVE_DELAY_FOR_RECOVERY)) {
      this.autoSaveDelayForRecoveryCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "autoSaveDelayForRecoveryCheckBox.text"));
      final SpinnerNumberModel autoSaveDelayForRecoverySpinnerModel = new SpinnerNumberModel(10, 1, 60, 5) {
          @Override
          public Object getNextValue() {
            if (((Number)getValue()).intValue() == ((Number)getMinimum()).intValue()) {
              return getStepSize();
            } else {
              return super.getNextValue();
            }
          }

          @Override
          public Object getPreviousValue() {
            if (((Number)getValue()).intValue() - ((Number)getStepSize()).intValue() < ((Number)getMinimum()).intValue()) {
              return super.getMinimum();
            } else {
              return super.getPreviousValue();
            }
          }
        };
      this.autoSaveDelayForRecoverySpinner = new AutoCommitSpinner(autoSaveDelayForRecoverySpinnerModel);
      this.autoSaveDelayForRecoveryUnitLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
          UserPreferencesPanel.class, "autoSaveDelayForRecoveryUnitLabel.text"));
      updateAutoSaveDelayForRecoveryComponents(controller);
      this.autoSaveDelayForRecoveryCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.setAutoSaveForRecoveryEnabled(autoSaveDelayForRecoveryCheckBox.isSelected());
          }
        });
      autoSaveDelayForRecoverySpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.setAutoSaveDelayForRecovery(((Number)autoSaveDelayForRecoverySpinnerModel.getValue()).intValue() * 60000);
          }
        });
      PropertyChangeListener listener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateAutoSaveDelayForRecoveryComponents(controller);
          }
        };
      controller.addPropertyChangeListener(UserPreferencesController.Property.AUTO_SAVE_DELAY_FOR_RECOVERY, listener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.AUTO_SAVE_FOR_RECOVERY_ENABLED, listener);
    }

    this.resetDisplayedActionTipsButton = new JButton(new ResourceAction.ButtonAction(
        new ResourceAction(preferences, UserPreferencesPanel.class, "RESET_DISPLAYED_ACTION_TIPS", true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            controller.resetDisplayedActionTips();
          }
        }));

    this.dialogTitle = preferences.getLocalizedString(UserPreferencesPanel.class, "preferences.title");
  }

  /**
   * Returns a renderer for patterns combo box.
   */
  private DefaultListCellRenderer getPatternRenderer() {
    final float resolutionScale = SwingTools.getResolutionScale();
    return new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          TextureImage wallPattern = (TextureImage)value;
          final Component component = super.getListCellRendererComponent(
              list, "", index, isSelected, cellHasFocus);
          final BufferedImage patternImage = SwingTools.getPatternImage(
              wallPattern, list.getBackground(), list.getForeground());
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
          return component;
        }
      };
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.
   */
  private static class SupportedLanguagesChangeListener implements PropertyChangeListener {
    private WeakReference<UserPreferencesPanel> userPreferencesPanel;

    public SupportedLanguagesChangeListener(UserPreferencesPanel userPreferencesPanel) {
      this.userPreferencesPanel = new WeakReference<UserPreferencesPanel>(userPreferencesPanel);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If panel was garbage collected, remove this listener from preferences
      UserPreferencesPanel userPreferencesPanel = this.userPreferencesPanel.get();
      if (userPreferencesPanel == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.SUPPORTED_LANGUAGES, this);
      } else {
        JComboBox languageComboBox = userPreferencesPanel.languageComboBox;
        List<String> oldSupportedLanguages = Arrays.asList((String [])ev.getOldValue());
        String [] supportedLanguages = (String [])ev.getNewValue();
        languageComboBox.setModel(new DefaultComboBoxModel(supportedLanguages));
        // Select the first language added to supported languages
        for (String language : supportedLanguages) {
          if (!oldSupportedLanguages.contains(language)) {
            languageComboBox.setSelectedItem(language);
            return;
          }
        }
        languageComboBox.setSelectedItem(userPreferencesPanel.controller.getLanguage());
      }
    }
  }

  private void updateAutoSaveDelayForRecoveryComponents(UserPreferencesController controller) {
    int autoSaveDelayForRecoveryInMinutes = controller.getAutoSaveDelayForRecovery() / 60000;
    boolean autoSaveForRecoveryEnabled = controller.isAutoSaveForRecoveryEnabled();
    this.autoSaveDelayForRecoverySpinner.setEnabled(autoSaveForRecoveryEnabled);
    this.autoSaveDelayForRecoveryCheckBox.setSelected(autoSaveForRecoveryEnabled);
    if (autoSaveForRecoveryEnabled) {
      this.autoSaveDelayForRecoverySpinner.setValue(autoSaveDelayForRecoveryInMinutes);
    }
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      if (this.languageLabel != null) {
        this.languageLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "languageLabel.mnemonic")).getKeyCode());
        this.languageLabel.setLabelFor(this.languageComboBox);
      }
      if (this.unitLabel != null) {
        this.unitLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "unitLabel.mnemonic")).getKeyCode());
        this.unitLabel.setLabelFor(this.unitComboBox);
      }
      if (this.currencyLabel != null) {
        this.currencyLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "currencyLabel.mnemonic")).getKeyCode());
        this.currencyLabel.setLabelFor(this.currencyComboBox);
      }
      if (this.valueAddedTaxCheckBox != null) {
        this.valueAddedTaxCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "valueAddedTaxCheckBox.mnemonic")).getKeyCode());
      }
      if (this.furnitureCatalogViewLabel != null) {
        this.treeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "treeRadioButton.mnemonic")).getKeyCode());
        this.listRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "listRadioButton.mnemonic")).getKeyCode());
      }
      if (this.navigationPanelLabel != null) {
        this.navigationPanelCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "navigationPanelCheckBox.mnemonic")).getKeyCode());
      }
      if (this.magnetismLabel != null) {
        this.magnetismCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "magnetismCheckBox.mnemonic")).getKeyCode());
      }
      if (this.aerialViewCenteredOnSelectionLabel != null) {
        this.aerialViewCenteredOnSelectionCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "aerialViewCenteredOnSelectionCheckBox.mnemonic")).getKeyCode());
      }
      if (this.observerCameraSelectedAtChangeLabel != null) {
        String mnemonic = preferences.getLocalizedString(UserPreferencesPanel.class, "observerCameraSelectedAtChangeCheckBox.mnemonic");
        if (mnemonic.length() > 0) {
          this.observerCameraSelectedAtChangeCheckBox.setMnemonic(KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        }
      }
      if (this.rulersLabel != null) {
        this.rulersCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "rulersCheckBox.mnemonic")).getKeyCode());
      }
      if (this.gridLabel != null) {
        this.gridCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "gridCheckBox.mnemonic")).getKeyCode());
      }
      if (this.defaultFontNameLabel != null) {
        this.defaultFontNameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "defaultFontNameLabel.mnemonic")).getKeyCode());
        this.defaultFontNameLabel.setLabelFor(this.defaultFontNameComboBox);
      }
      if (this.furnitureIconLabel != null) {
        this.catalogIconRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "catalogIconRadioButton.mnemonic")).getKeyCode());
        this.topViewRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "topViewRadioButton.mnemonic")).getKeyCode());
      }
      if (this.iconSizeLabel != null) {
        String mnemonic = preferences.getLocalizedString(UserPreferencesPanel.class, "iconSizeLabel.mnemonic");
        if (mnemonic.length() > 0) {
          this.iconSizeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        }
      }
      if (this.roomRenderingLabel != null) {
        this.monochromeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "monochromeRadioButton.mnemonic")).getKeyCode());
        this.floorColorOrTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "floorColorOrTextureRadioButton.mnemonic")).getKeyCode());
      }
      if (this.newWallPatternLabel != null) {
        this.newWallPatternLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "newWallPatternLabel.mnemonic")).getKeyCode());
        this.newWallPatternLabel.setLabelFor(this.newWallPatternComboBox);
      } else if (this.wallPatternLabel != null) {
        this.wallPatternLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "wallPatternLabel.mnemonic")).getKeyCode());
        this.wallPatternLabel.setLabelFor(this.wallPatternComboBox);
      }
      if (this.newWallThicknessLabel != null) {
        this.newWallThicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "newWallThicknessLabel.mnemonic")).getKeyCode());
        this.newWallThicknessLabel.setLabelFor(this.newWallThicknessSpinner);
      }
      if (this.newWallHeightLabel != null) {
        this.newWallHeightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "newWallHeightLabel.mnemonic")).getKeyCode());
        this.newWallHeightLabel.setLabelFor(this.newWallHeightSpinner);
      }
      if (this.newFloorThicknessLabel != null) {
        this.newFloorThicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "newFloorThicknessLabel.mnemonic")).getKeyCode());
        this.newFloorThicknessLabel.setLabelFor(this.newFloorThicknessSpinner);
      }
      if (this.checkUpdatesCheckBox != null) {
        this.checkUpdatesCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "checkUpdatesCheckBox.mnemonic")).getKeyCode());
      }
      if (this.autoSaveDelayForRecoveryCheckBox != null) {
        this.autoSaveDelayForRecoveryCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "autoSaveDelayForRecoveryCheckBox.mnemonic")).getKeyCode());
      }
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents() {
    boolean macOSX = OperatingSystem.isMacOSX();
    int labelAlignment = macOSX
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    boolean smallScreen = getToolkit().getScreenSize().height <= (macOSX ? 768 : 650);
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    if (smallScreen) {
      standardGap = (macOSX ? 4 : 3);
    }
    int checkBoxVecticalGap = (int)(3 * SwingTools.getResolutionScale());
    Insets labelInsets = new Insets(0, 0, standardGap, standardGap);
    Insets checkBoxLabelInsets = new Insets(0, 0, smallScreen
        ? (macOSX ? 2 : 0)
        : (macOSX ? 5 : checkBoxVecticalGap), standardGap);
    Insets labelInsetsWithSpace = new Insets(0, 0, smallScreen ? 2 : 10, standardGap);
    Insets rightComponentInsets = new Insets(0, 0, standardGap, 0);
    Insets checkBoxInsets = new Insets(0, 0, smallScreen
        ? (macOSX ? 2 : 0)
        : (macOSX ? 5 : checkBoxVecticalGap), 0);
    Insets rightComponentInsetsWithSpace = new Insets(0, 0, smallScreen ? 0 : 10, 0);
    if (this.languageLabel != null) {
      // First row
      add(this.languageLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.languageComboBox, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(macOSX ? 1 : 0, 0, standardGap, 0), 0, 0));
      if (this.languageLibraryImportButton != null) {
        add(this.languageLibraryImportButton, new GridBagConstraints(
            2, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(0, standardGap, standardGap, 0), 0, 0));
      }
    }
    if (this.unitLabel != null) {
      // Second row
      add(this.unitLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.unitComboBox, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, macOSX ? -20 : 0, 0));
      // Keep third row empty (used to contain unit radio buttons)
    }
    if (this.currencyLabel != null) {
      // Fourth row
      add(this.currencyLabel, new GridBagConstraints(
          0, 3, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.currencyComboBox, new GridBagConstraints(
          1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
      if (this.valueAddedTaxCheckBox != null) {
        add(this.valueAddedTaxCheckBox, new GridBagConstraints(
            2, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(0, standardGap, standardGap, 0), 0, 0));
      }
    }
    if (this.furnitureCatalogViewLabel != null) {
      // Fifth row
      add(this.furnitureCatalogViewLabel, new GridBagConstraints(
          0, 4, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));

      JPanel furnitureCatalogViewPanel = new JPanel(new GridBagLayout());
      furnitureCatalogViewPanel.add(this.treeRadioButton, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
      furnitureCatalogViewPanel.add(this.listRadioButton, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      add(furnitureCatalogViewPanel, new GridBagConstraints(
          1, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, checkBoxInsets, 0, 0));
    }
    if (this.navigationPanelLabel != null) {
      // Sixth row
      add(this.navigationPanelLabel, new GridBagConstraints(
          0, 5, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));
      add(this.navigationPanelCheckBox, new GridBagConstraints(
          1, 5, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, checkBoxInsets, 0, 0));
    }
    if (this.aerialViewCenteredOnSelectionLabel != null) {
      // Seventh row
      add(this.aerialViewCenteredOnSelectionLabel, new GridBagConstraints(
          0, 6, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));
      add(this.aerialViewCenteredOnSelectionCheckBox, new GridBagConstraints(
          1, 6, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, checkBoxInsets, 0, 0));
    }
    if (this.observerCameraSelectedAtChangeLabel != null) {
      // Eighth row
      add(this.observerCameraSelectedAtChangeLabel, new GridBagConstraints(
          0, 7, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsetsWithSpace, 0, 0));
      add(this.observerCameraSelectedAtChangeCheckBox, new GridBagConstraints(
          1, 7, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, rightComponentInsetsWithSpace, 0, 0));
    }
    if (this.magnetismLabel != null) {
      // Ninth row
      add(this.magnetismLabel, new GridBagConstraints(
          0, 8, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));
      add(this.magnetismCheckBox, new GridBagConstraints(
          1, 8, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, checkBoxInsets, 0, 0));
    }
    if (this.rulersLabel != null) {
      // Tenth row
      add(this.rulersLabel, new GridBagConstraints(
          0, 9, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));
      add(this.rulersCheckBox, new GridBagConstraints(
          1, 9, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, checkBoxInsets, 0, 0));
    }
    if (this.gridLabel != null) {
      // Eleventh row
      add(this.gridLabel, new GridBagConstraints(
          0, 10, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));
      add(this.gridCheckBox, new GridBagConstraints(
          1, 10, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, checkBoxInsets, 0, 0));
    }
    if (this.defaultFontNameLabel != null) {
      // Twelfth row
      add(this.defaultFontNameLabel, new GridBagConstraints(
          0, 11, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      Dimension preferredSize = this.defaultFontNameComboBox.getPreferredSize();
      if (this.unitComboBox != null
          && this.floorColorOrTextureRadioButton != null) {
        preferredSize.width = Math.min(preferredSize.width,
            this.unitComboBox.getPreferredSize().width + 5 + this.floorColorOrTextureRadioButton.getPreferredSize().width);
      } else {
        preferredSize.width = Math.min(preferredSize.width, 250);
      }
      this.defaultFontNameComboBox.setPreferredSize(preferredSize);
      add(this.defaultFontNameComboBox, new GridBagConstraints(
          1, 11, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.furnitureIconLabel != null) {
      // Thirteenth and fourteenth row
      add(this.furnitureIconLabel, new GridBagConstraints(
          0, 12, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));
      add(this.catalogIconRadioButton, new GridBagConstraints(
          1, 12, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, checkBoxInsets, 0, 0));

      JPanel topViewPanel = new JPanel(new GridBagLayout());
      topViewPanel.add(this.topViewRadioButton, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 15), 0, 0));
      if (this.iconSizeLabel != null) {
        topViewPanel.add(this.iconSizeLabel, new GridBagConstraints(
            1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
        topViewPanel.add(this.iconSizeComboBox, new GridBagConstraints(
            2, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      }
      add(topViewPanel, new GridBagConstraints(
          1, 13, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, rightComponentInsetsWithSpace, 0, 0));
    }
    if (this.roomRenderingLabel != null) {
      // Fifteenth row
      add(this.roomRenderingLabel, new GridBagConstraints(
          0, 14, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));

      JPanel roomRenderingPanel = new JPanel(new GridBagLayout());
      roomRenderingPanel.add(this.monochromeRadioButton, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
      roomRenderingPanel.add(this.floorColorOrTextureRadioButton, new GridBagConstraints(
          1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      add(roomRenderingPanel, new GridBagConstraints(
          1, 14, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, checkBoxInsets, 0, 0));
    }
    if (this.newWallPatternLabel != null) {
      // Sixteenth row
      add(this.newWallPatternLabel, new GridBagConstraints(
          0, 15, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.newWallPatternComboBox, new GridBagConstraints(
          1, 15, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    } else if (this.wallPatternLabel != null) {
      add(this.wallPatternLabel, new GridBagConstraints(
          0, 15, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.wallPatternComboBox, new GridBagConstraints(
          1, 15, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.newWallThicknessLabel != null) {
      // Seventeenth row
      add(this.newWallThicknessLabel, new GridBagConstraints(
          0, 16, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.newWallThicknessSpinner, new GridBagConstraints(
          1, 16, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.newWallHeightLabel != null) {
      // Eighteenth row
      add(this.newWallHeightLabel, new GridBagConstraints(
          0, 17, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.newWallHeightSpinner, new GridBagConstraints(
          1, 17, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.newFloorThicknessLabel != null) {
      // Nineteenth row
      add(this.newFloorThicknessLabel, new GridBagConstraints(
          0, 18, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.newFloorThicknessSpinner, new GridBagConstraints(
          1, 18, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.checkUpdatesCheckBox != null
        || this.autoSaveDelayForRecoveryCheckBox != null) {
      // Twentieth row
      JPanel updatesAndAutoSaveDelayForRecoveryPanel = new JPanel(new GridBagLayout());
      if (this.checkUpdatesCheckBox != null) {
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.checkUpdatesCheckBox,
            new GridBagConstraints(
                0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, checkBoxLabelInsets, 0, 0));
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.checkUpdatesNowButton,
            new GridBagConstraints(
                1, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, checkBoxInsets, 0, 0));
      }
      if (this.autoSaveDelayForRecoveryCheckBox != null) {
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.autoSaveDelayForRecoveryCheckBox,
            new GridBagConstraints(
                0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.autoSaveDelayForRecoverySpinner,
            new GridBagConstraints(
                1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.autoSaveDelayForRecoveryUnitLabel,
            new GridBagConstraints(
                2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      }
      add(updatesAndAutoSaveDelayForRecoveryPanel, new GridBagConstraints(
          0, 19, 3, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }

    // Last row
    if (this.resetDisplayedActionTipsButton.getText() != null
        && this.resetDisplayedActionTipsButton.getText().length() > 0) {
      // Display reset button only if its text isn't empty
      add(this.resetDisplayedActionTipsButton, new GridBagConstraints(
          0, 20, 3, 1, 0, 0, GridBagConstraints.CENTER,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
  }

  /**
   * Displays this panel in a dialog box.
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.languageComboBox) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyUserPreferences();
    }
  }

  private static class SpinnerLengthModel extends SpinnerNumberModel {
    public SpinnerLengthModel(final float centimeterStepSize,
                              final float inchStepSize,
                              final UserPreferencesController controller) {
      // Invoke constructor that take objects in parameter to avoid any ambiguity
      super(new Float(1f), new Float(0f), new Float(100000f), new Float(centimeterStepSize));
      // Add a listener to update step when unit changes
      controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT,
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            updateStepsAndLength(centimeterStepSize, inchStepSize, controller);
          }
        });
      updateStepsAndLength(centimeterStepSize, inchStepSize, controller);
    }

    private void updateStepsAndLength(float centimeterStepSize,
                                      float inchStepSize,
                                      UserPreferencesController controller) {
      if (controller.getUnit() == LengthUnit.INCH
          || controller.getUnit() == LengthUnit.INCH_DECIMALS) {
        setStepSize(LengthUnit.inchToCentimeter(inchStepSize));
      } else {
        setStepSize(centimeterStepSize);
      }
      fireStateChanged();
    }
  }

  private static class AutoCommitLengthSpinner extends AutoCommitSpinner {
    public AutoCommitLengthSpinner(SpinnerModel model,
                                   final UserPreferencesController controller) {
      super(model, controller.getUnit().getFormat());
      // Add a listener to update format when unit changes
      controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT,
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setFormat((DecimalFormat)controller.getUnit().getFormat());
          }
        });
    }
  }
}
