/*
 * UserPreferencesController.java 28 oct 2008
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for user preferences view.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller.
   */
  public enum Property {LANGUAGE, UNIT, CURRENCY, VALUE_ADDED_TAX_ENABLED,
      MAGNETISM_ENABLED, RULERS_VISIBLE, GRID_VISIBLE, DEFAULT_FONT_NAME,
      FURNITURE_VIEWED_FROM_TOP, FURNITURE_MODEL_ICON_SIZE, ROOM_FLOOR_COLORED_OR_TEXTURED, WALL_PATTERN, NEW_WALL_PATTERN,
      NEW_WALL_THICKNESS, NEW_WALL_HEIGHT, NEW_FLOOR_THICKNESS, FURNITURE_CATALOG_VIEWED_IN_TREE,
      NAVIGATION_PANEL_VISIBLE, AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED, OBSERVER_CAMERA_SELECTED_AT_CHANGE,
      CHECK_UPDATES_ENABLED, AUTO_SAVE_DELAY_FOR_RECOVERY, AUTO_SAVE_FOR_RECOVERY_ENABLED}

  private final UserPreferences         preferences;
  private final ViewFactory             viewFactory;
  private final HomeController          homeController;
  private final PropertyChangeSupport   propertyChangeSupport;
  private DialogView                    userPreferencesView;

  private String                        language;
  private LengthUnit                    unit;
  private String                        currency;
  private boolean                       valueAddedTaxEnabled;
  private boolean                       furnitureCatalogViewedInTree;
  private boolean                       navigationPanelVisible;
  private boolean                       aerialViewCenteredOnSelectionEnabled;
  private boolean                       observerCameraSelectedAtChange;
  private boolean                       magnetismEnabled;
  private boolean                       rulersVisible;
  private boolean                       gridVisible;
  private String                        defaultFontName;
  private boolean                       furnitureViewedFromTop;
  private int                           furnitureModelIconSize;
  private boolean                       roomFloorColoredOrTextured;
  private TextureImage                  wallPattern;
  private TextureImage                  newWallPattern;
  private float                         newWallThickness;
  private float                         newWallHeight;
  private float                         newFloorThickness;
  private boolean                       checkUpdatesEnabled;
  private int                           autoSaveDelayForRecovery;
  private boolean                       autoSaveForRecoveryEnabled;

  /**
   * Creates the controller of user preferences view.
   */
  public UserPreferencesController(UserPreferences preferences,
                                   ViewFactory viewFactory,
                                   ContentManager contentManager) {
    this(preferences, viewFactory, contentManager, null);
  }

  /**
   * Creates the controller of user preferences view.
   */
  public UserPreferencesController(UserPreferences preferences,
                                   ViewFactory viewFactory,
                                   ContentManager contentManager,
                                   HomeController homeController) {
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.homeController = homeController;
    this.propertyChangeSupport = new PropertyChangeSupport(this);

    updateProperties();
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.userPreferencesView == null) {
      this.userPreferencesView = this.viewFactory.createUserPreferencesView(this.preferences, this);
    }
    return this.userPreferencesView;
  }

  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(View parentView) {
    getView().displayView(parentView);
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Updates preferences properties edited by this controller.
   */
  protected void updateProperties() {
    setLanguage(this.preferences.getLanguage());
    setUnit(this.preferences.getLengthUnit());
    setCurrency(this.preferences.getCurrency());
    setValueAddedTaxEnabled(this.preferences.isValueAddedTaxEnabled());
    setFurnitureCatalogViewedInTree(this.preferences.isFurnitureCatalogViewedInTree());
    setNavigationPanelVisible(this.preferences.isNavigationPanelVisible());
    setAerialViewCenteredOnSelectionEnabled(this.preferences.isAerialViewCenteredOnSelectionEnabled());
    setObserverCameraSelectedAtChange(this.preferences.isObserverCameraSelectedAtChange());
    setMagnetismEnabled(this.preferences.isMagnetismEnabled());
    setRulersVisible(this.preferences.isRulersVisible());
    setGridVisible(this.preferences.isGridVisible());
    setDefaultFontName(this.preferences.getDefaultFontName());
    setFurnitureViewedFromTop(this.preferences.isFurnitureViewedFromTop());
    setFurnitureModelIconSize(this.preferences.getFurnitureModelIconSize());
    setRoomFloorColoredOrTextured(this.preferences.isRoomFloorColoredOrTextured());
    setWallPattern(this.preferences.getWallPattern());
    setNewWallPattern(this.preferences.getNewWallPattern());
    float minimumLength = getUnit().getMinimumLength();
    float maximumLength = getUnit().getMaximumLength();
    setNewWallThickness(Math.min(Math.max(minimumLength, this.preferences.getNewWallThickness()), maximumLength / 10));
    setNewWallHeight(Math.min(Math.max(minimumLength, this.preferences.getNewWallHeight()), maximumLength));
    setNewFloorThickness(Math.min(Math.max(minimumLength, this.preferences.getNewFloorThickness()), maximumLength / 10));
    setCheckUpdatesEnabled(this.preferences.isCheckUpdatesEnabled());
    setAutoSaveDelayForRecovery(this.preferences.getAutoSaveDelayForRecovery());
    setAutoSaveForRecoveryEnabled(this.preferences.getAutoSaveDelayForRecovery() > 0);
  }

  /**
   * Returns <code>true</code> if the given <code>property</code> is editable.
   * Depending on whether a property is editable or not, the view associated to this controller
   * may render it differently.
   * The implementation of this method always returns <code>true</code> except for <code>LANGUAGE</code> if it's not editable.
   */
  public boolean isPropertyEditable(Property property) {
    switch (property) {
      case LANGUAGE :
        return this.preferences.isLanguageEditable();
      default :
        return true;
    }
  }

  /**
   * Sets the edited language.
   */
  public void setLanguage(String language) {
    if (language != this.language) {
      String oldLanguage = this.language;
      this.language = language;
      this.propertyChangeSupport.firePropertyChange(Property.LANGUAGE.name(), oldLanguage, language);
    }
  }

  /**
   * Returns the edited language.
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * Sets the edited unit.
   */
  public void setUnit(LengthUnit unit) {
    if (unit != this.unit) {
      LengthUnit oldUnit = this.unit;
      this.unit = unit;
      this.propertyChangeSupport.firePropertyChange(Property.UNIT.name(), oldUnit, unit);
    }
  }

  /**
   * Returns the edited unit.
   */
  public LengthUnit getUnit() {
    return this.unit;
  }

  /**
   * Sets the edited currency.
   * @since 6.0
   */
  public void setCurrency(String currency) {
    if (currency != this.currency) {
      String oldCurrency = this.currency;
      this.currency = currency;
      this.propertyChangeSupport.firePropertyChange(Property.CURRENCY.name(), oldCurrency, currency);
      if (currency == null) {
        setValueAddedTaxEnabled(false);
      }
    }
  }

  /**
   * Returns the edited currency.
   * @since 6.0
   */
  public String getCurrency() {
    return this.currency;
  }

  /**
   * Sets whether Value Added Tax should be taken in account in prices.
   * @since 6.0
   */
  public void setValueAddedTaxEnabled(boolean valueAddedTaxEnabled) {
    if (this.valueAddedTaxEnabled != valueAddedTaxEnabled) {
      this.valueAddedTaxEnabled = valueAddedTaxEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.VALUE_ADDED_TAX_ENABLED.name(),
          !valueAddedTaxEnabled, valueAddedTaxEnabled);
    }
  }

  /**
   * Returns <code>true</code> if Value Added Tax should be taken in account in prices.
   * @since 6.0
   */
  public boolean isValueAddedTaxEnabled() {
    return this.valueAddedTaxEnabled;
  }

  /**
   * Sets whether the furniture catalog should be viewed in a tree or a different way.
   */
  public void setFurnitureCatalogViewedInTree(boolean furnitureCatalogViewedInTree) {
    if (this.furnitureCatalogViewedInTree != furnitureCatalogViewedInTree) {
      this.furnitureCatalogViewedInTree = furnitureCatalogViewedInTree;
      this.propertyChangeSupport.firePropertyChange(Property.FURNITURE_CATALOG_VIEWED_IN_TREE.name(),
          !furnitureCatalogViewedInTree, furnitureCatalogViewedInTree);
    }
  }

  /**
   * Returns <code>true</code> if furniture catalog should be viewed in a tree.
   */
  public boolean isFurnitureCatalogViewedInTree() {
    return this.furnitureCatalogViewedInTree;
  }

  /**
   * Sets whether the navigation panel should be displayed or not.
   */
  public void setNavigationPanelVisible(boolean navigationPanelVisible) {
    if (this.navigationPanelVisible != navigationPanelVisible) {
      this.navigationPanelVisible = navigationPanelVisible;
      this.propertyChangeSupport.firePropertyChange(Property.NAVIGATION_PANEL_VISIBLE.name(),
          !navigationPanelVisible, navigationPanelVisible);
    }
  }

  /**
   * Returns <code>true</code> if the navigation panel should be displayed.
   */
  public boolean isNavigationPanelVisible() {
    return this.navigationPanelVisible;
  }

  /**
   * Sets whether aerial view should be centered on selection or not.
   * @since 4.0
   */
  public void setAerialViewCenteredOnSelectionEnabled(boolean aerialViewCenteredOnSelectionEnabled) {
    if (aerialViewCenteredOnSelectionEnabled != this.aerialViewCenteredOnSelectionEnabled) {
      this.aerialViewCenteredOnSelectionEnabled = aerialViewCenteredOnSelectionEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED.name(),
          !aerialViewCenteredOnSelectionEnabled, aerialViewCenteredOnSelectionEnabled);
    }
  }

  /**
   * Returns whether aerial view should be centered on selection or not.
   * @since 4.0
   */
  public boolean isAerialViewCenteredOnSelectionEnabled() {
    return this.aerialViewCenteredOnSelectionEnabled;
  }

  /**
   * Sets whether the observer camera should be selected at each change.
   * @since 5.5
   */
  public void setObserverCameraSelectedAtChange(boolean observerCameraSelectedAtChange) {
    if (observerCameraSelectedAtChange != this.observerCameraSelectedAtChange) {
      this.observerCameraSelectedAtChange = observerCameraSelectedAtChange;
      this.propertyChangeSupport.firePropertyChange(Property.OBSERVER_CAMERA_SELECTED_AT_CHANGE.name(),
          !observerCameraSelectedAtChange, observerCameraSelectedAtChange);
    }
  }

  /**
   * Returns whether the observer camera should be selected at each change.
   * @since 5.5
   */
  public boolean isObserverCameraSelectedAtChange() {
    return this.observerCameraSelectedAtChange;
  }

  /**
   * Sets whether magnetism is enabled or not.
   */
  public void setMagnetismEnabled(boolean magnetismEnabled) {
    if (magnetismEnabled != this.magnetismEnabled) {
      this.magnetismEnabled = magnetismEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.MAGNETISM_ENABLED.name(), !magnetismEnabled, magnetismEnabled);
    }
  }

  /**
   * Returns whether magnetism is enabled or not.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismEnabled;
  }

  /**
   * Sets whether rulers are visible or not.
   */
  public void setRulersVisible(boolean rulersVisible) {
    if (rulersVisible != this.rulersVisible) {
      this.rulersVisible = rulersVisible;
      this.propertyChangeSupport.firePropertyChange(Property.RULERS_VISIBLE.name(), !rulersVisible, rulersVisible);
    }
  }

  /**
   * Returns whether rulers are visible or not.
   */
  public boolean isRulersVisible() {
    return this.rulersVisible;
  }

  /**
   * Sets whether grid is visible or not.
   */
  public void setGridVisible(boolean gridVisible) {
    if (gridVisible != this.gridVisible) {
      this.gridVisible = gridVisible;
      this.propertyChangeSupport.firePropertyChange(Property.GRID_VISIBLE.name(), !gridVisible, gridVisible);
    }
  }

  /**
   * Returns whether grid is visible or not.
   */
  public boolean isGridVisible() {
    return this.gridVisible;
  }

  /**
   * Sets the name of the font that should be used by default.
   * @since 5.0
   */
  public void setDefaultFontName(String defaultFontName) {
    if (defaultFontName != this.defaultFontName
        && (defaultFontName == null || !defaultFontName.equals(this.defaultFontName))) {
      String oldName = this.defaultFontName;
      this.defaultFontName = defaultFontName;
      this.propertyChangeSupport.firePropertyChange(Property.DEFAULT_FONT_NAME.name(), oldName, defaultFontName);
    }
  }

  /**
   * Returns the name of the font that should be used by default or <code>null</code>
   * if the default font should be the default one in the application.
   * @since 5.0
   */
  public String getDefaultFontName() {
    return this.defaultFontName;
  }

  /**
   * Sets how furniture should be displayed in plan.
   */
  public void setFurnitureViewedFromTop(boolean furnitureViewedFromTop) {
    if (this.furnitureViewedFromTop != furnitureViewedFromTop) {
      this.furnitureViewedFromTop = furnitureViewedFromTop;
      this.propertyChangeSupport.firePropertyChange(Property.FURNITURE_VIEWED_FROM_TOP.name(),
          !furnitureViewedFromTop, furnitureViewedFromTop);
    }
  }

  /**
   * Returns how furniture should be displayed in plan.
   */
  public boolean isFurnitureViewedFromTop() {
    return this.furnitureViewedFromTop;
  }

  /**
   * Sets the size used to generate icons of furniture viewed from top.
   * @since 5.5
   */
  public void setFurnitureModelIconSize(int furnitureModelIconSize) {
    if (furnitureModelIconSize != this.furnitureModelIconSize) {
      int oldSize = this.furnitureModelIconSize;
      this.furnitureModelIconSize = furnitureModelIconSize;
      this.propertyChangeSupport.firePropertyChange(Property.FURNITURE_MODEL_ICON_SIZE.name(), oldSize, furnitureModelIconSize);
    }
  }

  /**
   * Returns the size used to generate icons of furniture viewed from top.
   * @since 5.5
   */
  public int getFurnitureModelIconSize() {
    return this.furnitureModelIconSize;
  }

  /**
   * Sets whether floor texture is visible in plan or not.
   */
  public void setRoomFloorColoredOrTextured(boolean floorTextureVisible) {
    if (this.roomFloorColoredOrTextured != floorTextureVisible) {
      this.roomFloorColoredOrTextured = floorTextureVisible;
      this.propertyChangeSupport.firePropertyChange(Property.ROOM_FLOOR_COLORED_OR_TEXTURED.name(),
          !floorTextureVisible, floorTextureVisible);
    }
  }

  /**
   * Returns <code>true</code> if floor texture is visible in plan.
   */
  public boolean isRoomFloorColoredOrTextured() {
    return this.roomFloorColoredOrTextured;
  }

  /**
   * Sets default walls top pattern in plan, and notifies
   * listeners of this change.
   */
  public void setWallPattern(TextureImage wallPattern) {
    if (this.wallPattern != wallPattern) {
      TextureImage oldWallPattern = this.wallPattern;
      this.wallPattern = wallPattern;
      this.propertyChangeSupport.firePropertyChange(Property.WALL_PATTERN.name(),
          oldWallPattern, wallPattern);
    }
  }

  /**
   * Returns the default walls top pattern in plan.
   */
  public TextureImage getWallPattern() {
    return this.wallPattern;
  }

  /**
   * Sets the edited new wall top pattern in plan, and notifies
   * listeners of this change.
   * @since 4.0
   */
  public void setNewWallPattern(TextureImage newWallPattern) {
    if (this.newWallPattern != newWallPattern) {
      TextureImage oldNewWallPattern = this.newWallPattern;
      this.newWallPattern = newWallPattern;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_PATTERN.name(),
          oldNewWallPattern, newWallPattern);
    }
  }

  /**
   * Returns the edited new wall top pattern in plan.
   * @since 4.0
   */
  public TextureImage getNewWallPattern() {
    return this.newWallPattern;
  }

  /**
   * Sets the edited new wall thickness.
   */
  public void setNewWallThickness(float newWallThickness) {
    if (newWallThickness != this.newWallThickness) {
      float oldNewWallThickness = this.newWallThickness;
      this.newWallThickness = newWallThickness;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_THICKNESS.name(), oldNewWallThickness, newWallThickness);
    }
  }

  /**
   * Returns the edited new wall thickness.
   */
  public float getNewWallThickness() {
    return this.newWallThickness;
  }

  /**
   * Sets the edited new wall height.
   */
  public void setNewWallHeight(float newWallHeight) {
    if (newWallHeight != this.newWallHeight) {
      float oldNewWallHeight = this.newWallHeight;
      this.newWallHeight = newWallHeight;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_HEIGHT.name(), oldNewWallHeight, newWallHeight);
    }
  }

  /**
   * Returns the edited new wall height.
   */
  public float getNewWallHeight() {
    return this.newWallHeight;
  }

  /**
   * Sets the edited new floor thickness.
   */
  public void setNewFloorThickness(float newFloorThickness) {
    if (newFloorThickness != this.newFloorThickness) {
      float oldNewFloorThickness = this.newFloorThickness;
      this.newFloorThickness = newFloorThickness;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_FLOOR_THICKNESS.name(), oldNewFloorThickness, newFloorThickness);
    }
  }

  /**
   * Returns the edited new floor thickness.
   */
  public float getNewFloorThickness() {
    return this.newFloorThickness;
  }

  /**
   * Sets whether updates should be checked or not.
   * @since 4.0
   */
  public void setCheckUpdatesEnabled(boolean updatesChecked) {
    if (updatesChecked != this.checkUpdatesEnabled) {
      this.checkUpdatesEnabled = updatesChecked;
      this.propertyChangeSupport.firePropertyChange(Property.CHECK_UPDATES_ENABLED.name(),
          !updatesChecked, updatesChecked);
    }
  }

  /**
   * Returns <code>true</code> if updates should be checked.
   * @since 4.0
   */
  public boolean isCheckUpdatesEnabled() {
    return this.checkUpdatesEnabled;
  }

  /**
   * Sets the edited auto recovery save delay.
   */
  public void setAutoSaveDelayForRecovery(int autoSaveDelayForRecovery) {
    if (autoSaveDelayForRecovery != this.autoSaveDelayForRecovery) {
      float oldAutoSaveDelayForRecovery = this.autoSaveDelayForRecovery;
      this.autoSaveDelayForRecovery = autoSaveDelayForRecovery;
      this.propertyChangeSupport.firePropertyChange(Property.AUTO_SAVE_DELAY_FOR_RECOVERY.name(),
          oldAutoSaveDelayForRecovery, autoSaveDelayForRecovery);
    }
  }

  /**
   * Returns the edited auto recovery save delay.
   */
  public int getAutoSaveDelayForRecovery() {
    return this.autoSaveDelayForRecovery;
  }

  /**
   * Sets whether auto recovery save is enabled or not.
   */
  public void setAutoSaveForRecoveryEnabled(boolean autoSaveForRecoveryEnabled) {
    if (autoSaveForRecoveryEnabled != this.autoSaveForRecoveryEnabled) {
      this.autoSaveForRecoveryEnabled = autoSaveForRecoveryEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.AUTO_SAVE_FOR_RECOVERY_ENABLED.name(),
          !autoSaveForRecoveryEnabled, autoSaveForRecoveryEnabled);
    }
  }

  /**
   * Returns <code>true</code> if auto recovery save is enabled.
   */
  public boolean isAutoSaveForRecoveryEnabled() {
    return this.autoSaveForRecoveryEnabled;
  }

  /**
   * Checks if some updates are available.
   * @since 4.0
   */
  public void checkUpdates() {
    if (this.homeController != null) {
      this.homeController.checkUpdates(false);
    }
  }

  /**
   * Returns <code>true</code> if language libraries can be imported.
   */
  public boolean mayImportLanguageLibrary() {
    return this.homeController != null;
  }

  /**
   * Imports a language library chosen by the user.
   */
  public void importLanguageLibrary() {
    if (this.homeController != null) {
      this.homeController.importLanguageLibrary();
    }
  }

  /**
   * Resets the displayed flags of action tips.
   */
  public void resetDisplayedActionTips() {
    this.preferences.resetIgnoredActionTips();
  }

  /**
   * Controls the modification of user preferences.
   */
  public void modifyUserPreferences() {
    this.preferences.setLanguage(getLanguage());
    this.preferences.setUnit(getUnit());
    this.preferences.setCurrency(getCurrency());
    this.preferences.setValueAddedTaxEnabled(isValueAddedTaxEnabled());
    this.preferences.setFurnitureCatalogViewedInTree(isFurnitureCatalogViewedInTree());
    this.preferences.setNavigationPanelVisible(isNavigationPanelVisible());
    this.preferences.setAerialViewCenteredOnSelectionEnabled(isAerialViewCenteredOnSelectionEnabled());
    this.preferences.setObserverCameraSelectedAtChange(isObserverCameraSelectedAtChange());
    this.preferences.setMagnetismEnabled(isMagnetismEnabled());
    this.preferences.setRulersVisible(isRulersVisible());
    this.preferences.setGridVisible(isGridVisible());
    this.preferences.setDefaultFontName(getDefaultFontName());
    this.preferences.setFurnitureViewedFromTop(isFurnitureViewedFromTop());
    this.preferences.setFurnitureModelIconSize(getFurnitureModelIconSize());
    this.preferences.setFloorColoredOrTextured(isRoomFloorColoredOrTextured());
    this.preferences.setWallPattern(getWallPattern());
    this.preferences.setNewWallPattern(getNewWallPattern());
    this.preferences.setNewWallThickness(getNewWallThickness());
    this.preferences.setNewWallHeight(getNewWallHeight());
    this.preferences.setNewFloorThickness(getNewFloorThickness());
    this.preferences.setCheckUpdatesEnabled(isCheckUpdatesEnabled());
    this.preferences.setAutoSaveDelayForRecovery(isAutoSaveForRecoveryEnabled()
        ? getAutoSaveDelayForRecovery() : 0);
  }
}
