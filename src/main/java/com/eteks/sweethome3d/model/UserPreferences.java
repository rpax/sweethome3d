/*
 * UserPreferences.java 15 mai 2006
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
package com.eteks.sweethome3d.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyPermission;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * User preferences.
 * @author Emmanuel Puybaret
 */
public abstract class UserPreferences {
  /**
   * The properties of user preferences that may change. <code>PropertyChangeListener</code>s added
   * to user preferences will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {LANGUAGE, SUPPORTED_LANGUAGES, UNIT, CURRENCY, VALUE_ADDED_TAX_ENABLED, DEFAULT_VALUE_ADDED_TAX_PERCENTAGE,
                        MAGNETISM_ENABLED, RULERS_VISIBLE, GRID_VISIBLE, DEFAULT_FONT_NAME,
                        FURNITURE_VIEWED_FROM_TOP, FURNITURE_MODEL_ICON_SIZE, ROOM_FLOOR_COLORED_OR_TEXTURED, WALL_PATTERN, NEW_WALL_PATTERN,
                        NEW_WALL_THICKNESS, NEW_WALL_HEIGHT, NEW_WALL_SIDEBOARD_THICKNESS, NEW_WALL_SIDEBOARD_HEIGHT, NEW_FLOOR_THICKNESS,
                        RECENT_HOMES, IGNORED_ACTION_TIP, FURNITURE_CATALOG_VIEWED_IN_TREE, NAVIGATION_PANEL_VISIBLE,
                        AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED, OBSERVER_CAMERA_SELECTED_AT_CHANGE, CHECK_UPDATES_ENABLED,
                        UPDATES_MINIMUM_DATE, AUTO_SAVE_DELAY_FOR_RECOVERY, AUTO_COMPLETION_STRINGS, RECENT_COLORS, RECENT_TEXTURES, HOME_EXAMPLES}

  public static final String FURNITURE_LIBRARY_TYPE = "Furniture library";
  public static final String TEXTURES_LIBRARY_TYPE  = "Textures library";
  public static final String LANGUAGE_LIBRARY_TYPE  = "Language library";

  private static final String [] DEFAULT_SUPPORTED_LANGUAGES;
  private static final List<ClassLoader> DEFAULT_CLASS_LOADER =
      Arrays.asList(new ClassLoader [] {UserPreferences.class.getClassLoader()});

  private static final TextStyle DEFAULT_TEXT_STYLE = new TextStyle(18f);
  private static final TextStyle DEFAULT_ROOM_TEXT_STYLE = new TextStyle(24f);

  static {
    Properties supportedLanguagesProperties = new Properties();
    String [] defaultSupportedLanguages;
    try {
      // As of version 4.1 where Trusted-Library manifest attribute was added to applet jars,
      // UserPreferences.properties was renamed as SupportedLanguages.properties
      // because strangely UserPreferences.properties resource couldn't be found in applet environment
      InputStream in = UserPreferences.class.getResourceAsStream("SupportedLanguages.properties");
      supportedLanguagesProperties.load(in);
      in.close();
      // Get property value of supportedLanguages
      defaultSupportedLanguages = supportedLanguagesProperties.getProperty("supportedLanguages", "en").split("\\s");
    } catch (IOException ex) {
      defaultSupportedLanguages = new String [] {"en"};
    }
    DEFAULT_SUPPORTED_LANGUAGES = defaultSupportedLanguages;
  }

  private final PropertyChangeSupport          propertyChangeSupport;
  private final Map<Class<?>, ResourceBundle>  classResourceBundles;
  private final Map<String, ResourceBundle>    resourceBundles;

  private FurnitureCatalog furnitureCatalog;
  private TexturesCatalog  texturesCatalog;
  private PatternsCatalog  patternsCatalog;
  private final String     defaultCountry;
  private String []        supportedLanguages;
  private String           language;
  private String           currency;
  private boolean          valueAddedTaxEnabled;
  private BigDecimal       defaultValueAddedTaxPercentage;
  private LengthUnit       unit;
  private boolean          furnitureCatalogViewedInTree = true;
  private boolean          aerialViewCenteredOnSelectionEnabled;
  private boolean          observerCameraSelectedAtChange = true;
  private boolean          navigationPanelVisible = true;
  private boolean          magnetismEnabled    = true;
  private boolean          rulersVisible       = true;
  private boolean          gridVisible         = true;
  private String           defaultFontName;
  private boolean          drawingModeEnabled;
  private boolean          furnitureViewedFromTop;
  private int              furnitureModelIconSize = 128;
  private boolean          roomFloorColoredOrTextured;
  private TextureImage     wallPattern;
  private TextureImage     newWallPattern;
  private float            newWallThickness;
  private float            newWallHeight;
  private float            newWallBaseboardThickness;
  private float            newWallBaseboardHeight;
  private float            newFloorThickness;
  private List<String>     recentHomes;
  private boolean          checkUpdatesEnabled;
  private Long             updatesMinimumDate;
  private int              autoSaveDelayForRecovery;
  private Map<String, List<String>>  autoCompletionStrings;
  private List<Integer>        recentColors;
  private List<TextureImage>   recentTextures;
  private List<HomeDescriptor> homeExamples;

  /**
   * Creates user preferences.<br>
   * Caution: during creation, the default locale will be updated if it doesn't belong to the supported ones.
   */
  public UserPreferences() {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.classResourceBundles = new HashMap<Class<?>, ResourceBundle>();
    this.resourceBundles = new HashMap<String, ResourceBundle>();
    this.autoCompletionStrings = new LinkedHashMap<String, List<String>>();
    this.recentHomes = Collections.emptyList();
    this.recentColors = Collections.emptyList();
    this.recentTextures = Collections.emptyList();
    this.homeExamples = Collections.emptyList();

    try {
      this.drawingModeEnabled = Boolean.getBoolean("com.eteks.sweethome3d.j3d.drawingModeEnabled");
    } catch (SecurityException ex) {
    }

    this.supportedLanguages = DEFAULT_SUPPORTED_LANGUAGES;
    this.defaultCountry = Locale.getDefault().getCountry();
    String defaultLanguage = Locale.getDefault().getLanguage();
    // Find closest language among supported languages in Sweet Home 3D
    // For example, use simplified Chinese even for Chinese users (zh_?) not from China (zh_CN)
    // unless their exact locale is supported as in Taiwan (zh_TW)
    for (String supportedLanguage : this.supportedLanguages) {
      if (supportedLanguage.equals(defaultLanguage + "_" + this.defaultCountry)) {
        this.language = supportedLanguage;
        break; // Found the exact supported language
      } else if (this.language == null
                 && supportedLanguage.startsWith(defaultLanguage)) {
        this.language = supportedLanguage; // Found a supported language
      }
    }
    // If no language was found, let's use English by default
    if (this.language == null) {
      this.language = Locale.ENGLISH.getLanguage();
    }
    updateDefaultLocale();
  }

  /**
   * Updates default locale from preferences language.
   */
  private void updateDefaultLocale() {
    try {
      int underscoreIndex = this.language.indexOf("_");
      if (underscoreIndex != -1) {
        Locale.setDefault(new Locale(this.language.substring(0, underscoreIndex),
            this.language.substring(underscoreIndex + 1)));
      } else {
        Locale.setDefault(new Locale(this.language, this.defaultCountry));
      }
    } catch (AccessControlException ex) {
      // Let's keep default language even if it's not supported
      this.language = Locale.getDefault().getLanguage();
    }
  }

  /**
   * Writes user preferences.
   * @throws RecorderException if user preferences couldn'y be saved.
   */
  public abstract void write() throws RecorderException;

  /**
   * Adds the <code>listener</code> in parameter to these preferences.
   * <br>Caution: a user preferences instance generally exists during all the application ;
   * therefore you should take care of not bounding permanently listeners to this
   * object (for example, do not create anonymous listeners on user preferences
   * in classes depending on an edited home).
   */
  public void addPropertyChangeListener(Property property,
                                        PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the <code>listener</code> in parameter from these preferences.
   */
  public void removePropertyChangeListener(Property property,
                                           PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Returns the furniture catalog.
   */
  public FurnitureCatalog getFurnitureCatalog() {
    return this.furnitureCatalog;
  }

  /**
   * Sets furniture catalog.
   */
  protected void setFurnitureCatalog(FurnitureCatalog catalog) {
    this.furnitureCatalog = catalog;
  }

  /**
   * Returns the textures catalog.
   */
  public TexturesCatalog getTexturesCatalog() {
    return this.texturesCatalog;
  }

  /**
   * Sets textures catalog.
   */
  protected void setTexturesCatalog(TexturesCatalog catalog) {
    this.texturesCatalog = catalog;
  }

  /**
   * Returns the patterns catalog available to fill plan areas.
   */
  public PatternsCatalog getPatternsCatalog() {
    return this.patternsCatalog;
  }

  /**
   * Sets the patterns available to fill plan areas.
   */
  protected void setPatternsCatalog(PatternsCatalog catalog) {
    this.patternsCatalog = catalog;
  }

  /**
   * Returns the length unit currently in use.
   */
  public LengthUnit getLengthUnit() {
    return this.unit;
  }

  /**
   * Changes the unit currently in use, and notifies listeners of this change.
   * @param unit one of the values of Unit.
   */
  public void setUnit(LengthUnit unit) {
    if (this.unit != unit) {
      LengthUnit oldUnit = this.unit;
      this.unit = unit;
      this.propertyChangeSupport.firePropertyChange(Property.UNIT.name(), oldUnit, unit);
    }
  }

  /**
   * Returns the preferred language to display information, noted with an ISO 639 code
   * that may be followed by an underscore and an ISO 3166 code.
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * If {@linkplain #isLanguageEditable() language can be changed}, sets the preferred language to display information,
   * changes current default locale accordingly and notifies listeners of this change.
   * @param language an ISO 639 code that may be followed by an underscore and an ISO 3166 code
   *            (for example fr, de, it, en_US, zh_CN).
   */
  public void setLanguage(String language) {
    if (!language.equals(this.language)
        && isLanguageEditable()) {
      String oldLanguage = this.language;
      this.language = language;
      updateDefaultLocale();
      this.classResourceBundles.clear();
      this.resourceBundles.clear();
      this.propertyChangeSupport.firePropertyChange(Property.LANGUAGE.name(),
          oldLanguage, language);
    }
  }

  /**
   * Returns <code>true</code> if the language in preferences can be set.
   * @return <code>true</code> except if <code>user.language</code> System property isn't writable.
   * @since 3.4
   */
  public boolean isLanguageEditable() {
    try {
      SecurityManager securityManager = System.getSecurityManager();
      if (securityManager != null) {
        securityManager.checkPermission(new PropertyPermission("user.language", "write"));
      }
      return true;
    } catch (AccessControlException ex) {
      return false;
    }
  }

  /**
   * Returns the array of default available languages in Sweet Home 3D.
   */
  public String [] getDefaultSupportedLanguages() {
    return DEFAULT_SUPPORTED_LANGUAGES.clone();
  }

  /**
   * Returns the array of available languages in Sweet Home 3D including languages in libraries.
   * @since 3.4
   */
  public String [] getSupportedLanguages() {
    return this.supportedLanguages.clone();
  }

  /**
   * Sets the available languages in Sweet Home 3D.
   * @since 3.4
   */
  protected void setSupportedLanguages(String [] supportedLanguages) {
    if (!Arrays.deepEquals(this.supportedLanguages, supportedLanguages)) {
      String [] oldSupportedLanguages = this.supportedLanguages;
      this.supportedLanguages = supportedLanguages.clone();
      this.propertyChangeSupport.firePropertyChange(Property.SUPPORTED_LANGUAGES.name(),
          oldSupportedLanguages, supportedLanguages);
    }
  }

  /**
   * Returns the string matching <code>resourceKey</code> in current language in the
   * context of <code>resourceClass</code>.
   * If <code>resourceParameters</code> isn't empty the string is considered
   * as a format string, and the returned string will be formatted with these parameters.
   * This implementation searches first the key in a properties file named as
   * <code>resourceClass</code>, then if this file doesn't exist, it searches
   * the key prefixed by <code>resourceClass</code> name and a dot in a package.properties file
   * in the folder matching the package of <code>resourceClass</code>.
   * @throws IllegalArgumentException if no string for the given key can be found
   */
  public String getLocalizedString(Class<?> resourceClass,
                                   String   resourceKey,
                                   Object ... resourceParameters) {
    ResourceBundle classResourceBundle = this.classResourceBundles.get(resourceClass);
    if (classResourceBundle == null) {
      try {
        classResourceBundle = getResourceBundle(resourceClass.getName());
        this.classResourceBundles.put(resourceClass, classResourceBundle);
      } catch (IOException ex) {
        try {
          String className = resourceClass.getName();
          int lastIndex = className.lastIndexOf(".");
          String resourceFamily;
          if (lastIndex != -1) {
            resourceFamily = className.substring(0, lastIndex) + ".package";
          } else {
            resourceFamily = "package";
          }
          classResourceBundle = new PrefixedResourceBundle(getResourceBundle(resourceFamily),
              resourceClass.getSimpleName() + ".");
          this.classResourceBundles.put(resourceClass, classResourceBundle);
        } catch (IOException ex2) {
          throw new IllegalArgumentException(
              "Can't find resource bundle for " + resourceClass, ex);
        }
      }
    }

    return getLocalizedString(classResourceBundle, resourceKey, resourceParameters);
  }

  /**
   * Returns the string matching <code>resourceKey</code> in current language
   * for the given resource family.
   * <code>resourceFamily</code> should match the absolute path of a .properties resource family,
   * shouldn't start by a slash and may contain dots '.' or slash '/' as folder separators.
   * If <code>resourceParameters</code> isn't empty the string is considered
   * as a format string, and the returned string will be formatted with these parameters.
   * This implementation searches the key in a properties file named as
   * <code>resourceFamily</code>.
   * @throws IllegalArgumentException if no string for the given key can be found
   * @since 2.3
   */
  public String getLocalizedString(String resourceFamily,
                                   String resourceKey,
                                   Object ... resourceParameters) {
    try {
      ResourceBundle resourceBundle = getResourceBundle(resourceFamily);
      return getLocalizedString(resourceBundle, resourceKey, resourceParameters);
    } catch (IOException ex) {
      throw new IllegalArgumentException(
          "Can't find resource bundle for " + resourceFamily, ex);
    }
  }

  /**
   * Returns a new resource bundle for the given <code>familyName</code>
   * that matches current default locale. The search will be done
   * only among .properties files.
   * @throws IOException if no .properties file was found
   */
  private ResourceBundle getResourceBundle(String resourceFamily) throws IOException {
    resourceFamily = resourceFamily.replace('.', '/');
    ResourceBundle resourceBundle = this.resourceBundles.get(resourceFamily);
    if (resourceBundle != null) {
      return resourceBundle;
    }
    Locale defaultLocale = Locale.getDefault();
    String language = defaultLocale.getLanguage();
    String country = defaultLocale.getCountry();
    String [] suffixes = {".properties",
                          "_" + language + ".properties",
                          "_" + language + "_" + country + ".properties"};
    for (String suffix : suffixes) {
      for (ClassLoader classLoader : getResourceClassLoaders()) {
        InputStream in = classLoader.getResourceAsStream(resourceFamily + suffix);
        if (in != null) {
          final ResourceBundle parentResourceBundle = resourceBundle;
          try {
            resourceBundle = new PropertyResourceBundle(in) {
              {
                setParent(parentResourceBundle);
              }
            };
            break;
          } catch (IllegalArgumentException ex) {
            // May happen if the file contains some wrongly encoded characters
            ex.printStackTrace();
          } finally {
            in.close();
          }
        }
      }
    }
    if (resourceBundle == null) {
      throw new IOException("No available resource bundle for " + resourceFamily);
    }
    this.resourceBundles.put(resourceFamily, resourceBundle);
    return resourceBundle;
  }

  /**
   * Returns the string matching <code>resourceKey</code> for the given resource bundle.
   */
  private String getLocalizedString(ResourceBundle resourceBundle,
                                    String         resourceKey,
                                    Object...      resourceParameters) {
    try {
      String localizedString = resourceBundle.getString(resourceKey);
      if (resourceParameters.length > 0) {
        localizedString = String.format(localizedString, resourceParameters);
      }
      return localizedString;
    } catch (MissingResourceException ex) {
      throw new IllegalArgumentException("Unknown key " + resourceKey);
    }
  }

  /**
   * Returns the keys of the localized property strings of the given resource family.
   * <code>resourceFamily</code> should match the absolute path of a .properties resource family,
   * shouldn't start by a slash and may contain dots '.' or slash '/' as folder separators.
   * @since 5.7
   */
  public Iterator<String> getLocalizedStringKeys(String resourceFamily) {
    try {
      final Enumeration<String> keys = getResourceBundle(resourceFamily).getKeys();
      return new Iterator<String>() {
          public boolean hasNext() {
            return keys.hasMoreElements();
          }

          public String next() {
            return keys.nextElement();
          }

          public void remove() {
            throw new UnsupportedOperationException("Enumeration not modifiable");
          }
        };
    } catch (IOException ex) {
      return Collections.<String>emptyList().iterator();
    }
  }

  /**
   * Returns the class loaders through which localized strings returned by
   * {@link #getLocalizedString(Class, String, Object...) getLocalizedString} might be loaded.
   * @since 2.3
   */
  public List<ClassLoader> getResourceClassLoaders() {
    return DEFAULT_CLASS_LOADER;
  }

  /**
   * Returns the currency in use, noted with ISO 4217 code, or <code>null</code>
   * if prices aren't used in application.
   */
  public String getCurrency() {
    return this.currency;
  }

  /**
   * Sets the currency in use.
   */
  public void setCurrency(String currency) {
    if (currency != this.currency
        && (currency == null || !currency.equals(this.currency))) {
      String oldCurrency = this.currency;
      this.currency = currency;
      this.propertyChangeSupport.firePropertyChange(Property.CURRENCY.name(), oldCurrency, currency);

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
   * Sets whether Value Added Tax should be taken in account in prices.
   * @param valueAddedTaxEnabled if <code>true</code> VAT will be added to prices.
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
   * Returns the Value Added Tax percentage applied to prices by default, or <code>null</code>
   * if VAT isn't taken into account in the application.
   * @since 6.0
   */
  public BigDecimal getDefaultValueAddedTaxPercentage() {
    return this.defaultValueAddedTaxPercentage;
  }

  /**
   * Sets the Value Added Tax percentage applied to prices by default.
   * @param valueAddedTaxPercentage the default VAT percentage
   * @since 6.0
   */
  public void setDefaultValueAddedTaxPercentage(BigDecimal valueAddedTaxPercentage) {
    if (valueAddedTaxPercentage != this.defaultValueAddedTaxPercentage
        && (valueAddedTaxPercentage == null || !valueAddedTaxPercentage.equals(this.defaultValueAddedTaxPercentage))) {
      BigDecimal oldValueAddedTaxPercentage = this.defaultValueAddedTaxPercentage;
      this.defaultValueAddedTaxPercentage = valueAddedTaxPercentage;
      this.propertyChangeSupport.firePropertyChange(Property.DEFAULT_VALUE_ADDED_TAX_PERCENTAGE.name(), oldValueAddedTaxPercentage, valueAddedTaxPercentage);

    }
  }

  /**
   * Returns <code>true</code> if the furniture catalog should be viewed in a tree.
   * @since 2.3
   */
  public boolean isFurnitureCatalogViewedInTree() {
    return this.furnitureCatalogViewedInTree;
  }

  /**
   * Sets whether the furniture catalog should be viewed in a tree or a different way.
   * @since 2.3
   */
  public void setFurnitureCatalogViewedInTree(boolean furnitureCatalogViewedInTree) {
    if (this.furnitureCatalogViewedInTree != furnitureCatalogViewedInTree) {
      this.furnitureCatalogViewedInTree = furnitureCatalogViewedInTree;
      this.propertyChangeSupport.firePropertyChange(Property.FURNITURE_CATALOG_VIEWED_IN_TREE.name(),
          !furnitureCatalogViewedInTree, furnitureCatalogViewedInTree);
    }
  }

  /**
   * Returns <code>true</code> if the navigation panel should be displayed.
   * @since 2.3
   */
  public boolean isNavigationPanelVisible() {
    return this.navigationPanelVisible;
  }

  /**
   * Sets whether the navigation panel should be displayed or not.
   * @since 2.3
   */
  public void setNavigationPanelVisible(boolean navigationPanelVisible) {
    if (this.navigationPanelVisible != navigationPanelVisible) {
      this.navigationPanelVisible = navigationPanelVisible;
      this.propertyChangeSupport.firePropertyChange(Property.NAVIGATION_PANEL_VISIBLE.name(),
          !navigationPanelVisible, navigationPanelVisible);
    }
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
   * Returns <code>true</code> if magnetism is enabled.
   * @return <code>true</code> by default.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismEnabled;
  }

  /**
   * Sets whether magnetism is enabled or not, and notifies
   * listeners of this change.
   * @param magnetismEnabled <code>true</code> if magnetism is enabled,
   *          <code>false</code> otherwise.
   */
  public void setMagnetismEnabled(boolean magnetismEnabled) {
    if (this.magnetismEnabled != magnetismEnabled) {
      this.magnetismEnabled = magnetismEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.MAGNETISM_ENABLED.name(),
          !magnetismEnabled, magnetismEnabled);
    }
  }

  /**
   * Returns <code>true</code> if rulers are visible.
   * @return <code>true</code> by default.
   */
  public boolean isRulersVisible() {
    return this.rulersVisible;
  }

  /**
   * Sets whether rulers are visible or not, and notifies
   * listeners of this change.
   * @param rulersVisible <code>true</code> if rulers are visible,
   *          <code>false</code> otherwise.
   */
  public void setRulersVisible(boolean rulersVisible) {
    if (this.rulersVisible != rulersVisible) {
      this.rulersVisible = rulersVisible;
      this.propertyChangeSupport.firePropertyChange(Property.RULERS_VISIBLE.name(),
          !rulersVisible, rulersVisible);
    }
  }

  /**
   * Returns <code>true</code> if plan grid visible.
   * @return <code>true</code> by default.
   */
  public boolean isGridVisible() {
    return this.gridVisible;
  }

  /**
   * Sets whether plan grid is visible or not, and notifies
   * listeners of this change.
   * @param gridVisible <code>true</code> if grid is visible,
   *          <code>false</code> otherwise.
   */
  public void setGridVisible(boolean gridVisible) {
    if (this.gridVisible != gridVisible) {
      this.gridVisible = gridVisible;
      this.propertyChangeSupport.firePropertyChange(Property.GRID_VISIBLE.name(),
          !gridVisible, gridVisible);
    }
  }

  /**
   * Returns <code>true</code> is {@link HomeEnvironment#getDrawingMode() drawing mode}
   * should be taken into account.
   * @since 6.0
   */
  public boolean isDrawingModeEnabled() {
    return this.drawingModeEnabled;
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
   * Returns <code>true</code> if furniture should be viewed from its top in plan.
   * @since 2.0
   */
  public boolean isFurnitureViewedFromTop() {
    return this.furnitureViewedFromTop;
  }

  /**
   * Sets how furniture icon should be displayed in plan, and notifies
   * listeners of this change.
   * @param furnitureViewedFromTop if <code>true</code> the furniture
   *    should be viewed from its top.
   * @since 2.0
   */
  public void setFurnitureViewedFromTop(boolean furnitureViewedFromTop) {
    if (this.furnitureViewedFromTop != furnitureViewedFromTop) {
      this.furnitureViewedFromTop = furnitureViewedFromTop;
      this.propertyChangeSupport.firePropertyChange(Property.FURNITURE_VIEWED_FROM_TOP.name(),
          !furnitureViewedFromTop, furnitureViewedFromTop);
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
   * Sets the name of the font that should be used by default.
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
   * Returns <code>true</code> if room floors should be rendered with color or texture
   * in plan.
   * @return <code>false</code> by default.
   * @since 2.0
   */
  public boolean isRoomFloorColoredOrTextured() {
    return this.roomFloorColoredOrTextured;
  }

  /**
   * Sets whether room floors should be rendered with color or texture,
   * and notifies listeners of this change.
   * @param roomFloorColoredOrTextured <code>true</code> if floor color
   *          or texture is used, <code>false</code> otherwise.
   * @since 2.0
   */
  public void setFloorColoredOrTextured(boolean roomFloorColoredOrTextured) {
    if (this.roomFloorColoredOrTextured != roomFloorColoredOrTextured) {
      this.roomFloorColoredOrTextured = roomFloorColoredOrTextured;
      this.propertyChangeSupport.firePropertyChange(Property.ROOM_FLOOR_COLORED_OR_TEXTURED.name(),
          !roomFloorColoredOrTextured, roomFloorColoredOrTextured);
    }
  }

  /**
   * Returns the wall pattern in plan used by default.
   * @since 2.0
   */
  public TextureImage getWallPattern() {
    return this.wallPattern;
  }

  /**
   * Sets how walls should be displayed in plan by default, and notifies
   * listeners of this change.
   * @since 2.0
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
   * Returns the pattern used for new walls in plan or <code>null</code> if it's not set.
   * @since 4.0
   */
  public TextureImage getNewWallPattern() {
    return this.newWallPattern;
  }

  /**
   * Sets how new walls should be displayed in plan, and notifies
   * listeners of this change.
   * @since 4.0
   */
  public void setNewWallPattern(TextureImage newWallPattern) {
    if (this.newWallPattern != newWallPattern) {
      TextureImage oldWallPattern = this.newWallPattern;
      this.newWallPattern = newWallPattern;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_PATTERN.name(),
          oldWallPattern, newWallPattern);
    }
  }

  /**
   * Returns default thickness of new walls in home.
   */
  public float getNewWallThickness() {
    return this.newWallThickness;
  }

  /**
   * Sets default thickness of new walls in home, and notifies
   * listeners of this change.
   */
  public void setNewWallThickness(float newWallThickness) {
    if (this.newWallThickness != newWallThickness) {
      float oldDefaultThickness = this.newWallThickness;
      this.newWallThickness = newWallThickness;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_THICKNESS.name(),
          oldDefaultThickness, newWallThickness);
    }
  }

  /**
   * Returns default wall height of new home walls.
   */
  public float getNewWallHeight() {
    return this.newWallHeight;
  }

  /**
   * Sets default wall height of new walls, and notifies
   * listeners of this change.
   */
  public void setNewWallHeight(float newWallHeight) {
    if (this.newWallHeight != newWallHeight) {
      float oldWallHeight = this.newWallHeight;
      this.newWallHeight = newWallHeight;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_HEIGHT.name(),
          oldWallHeight, newWallHeight);
    }
  }

  /**
   * Returns default baseboard thickness of new walls in home.
   * @since 5.0
   */
  public float getNewWallBaseboardThickness() {
    return this.newWallBaseboardThickness;
  }

  /**
   * Sets default baseboard thickness of new walls in home, and notifies
   * listeners of this change.
   * @since 5.0
   */
  public void setNewWallBaseboardThickness(float newWallBaseboardThickness) {
    if (this.newWallBaseboardThickness != newWallBaseboardThickness) {
      float oldThickness = this.newWallBaseboardThickness;
      this.newWallBaseboardThickness = newWallBaseboardThickness;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_SIDEBOARD_THICKNESS.name(),
          oldThickness, newWallBaseboardThickness);
    }
  }

  /**
   * Returns default baseboard height of new home walls.
   * @since 5.0
   */
  public float getNewWallBaseboardHeight() {
    return this.newWallBaseboardHeight;
  }

  /**
   * Sets default baseboard height of new walls, and notifies
   * listeners of this change.
   * @since 5.0
   */
  public void setNewWallBaseboardHeight(float newWallBaseboardHeight) {
    if (this.newWallBaseboardHeight != newWallBaseboardHeight) {
      float oldHeight = this.newWallBaseboardHeight;
      this.newWallBaseboardHeight = newWallBaseboardHeight;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_SIDEBOARD_HEIGHT.name(),
          oldHeight, newWallBaseboardHeight);
    }
  }

  /**
   * Returns default thickness of the floor of new levels in home.
   * @since 3.4
   */
  public float getNewFloorThickness() {
    return this.newFloorThickness;
  }

  /**
   * Sets default thickness of the floor of new levels in home, and notifies
   * listeners of this change.
   * @since 3.4
   */
  public void setNewFloorThickness(float newFloorThickness) {
    if (this.newFloorThickness != newFloorThickness) {
      float oldFloorThickness = this.newFloorThickness;
      this.newFloorThickness = newFloorThickness;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_FLOOR_THICKNESS.name(),
          oldFloorThickness, newFloorThickness);
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
   * Returns the minimum date of updates that may interest user.
   * @return the date expressed in millis second since the epoch or <code>null</code> if not defined.
   * @since 4.0
   */
  public Long getUpdatesMinimumDate() {
    return this.updatesMinimumDate;
  }

  /**
   * Sets the minimum date of updates that may interest user, and notifies
   * listeners of this change.
   * @since 4.0
   */
  public void setUpdatesMinimumDate(Long updatesMinimumDate) {
    if (this.updatesMinimumDate != updatesMinimumDate
        && (updatesMinimumDate == null || !updatesMinimumDate.equals(this.updatesMinimumDate))) {
      Long oldUpdatesMinimumDate = this.updatesMinimumDate;
      this.updatesMinimumDate = updatesMinimumDate;
      this.propertyChangeSupport.firePropertyChange(Property.UPDATES_MINIMUM_DATE.name(),
          oldUpdatesMinimumDate, updatesMinimumDate);
    }
  }

  /**
   * Returns the delay between two automatic save operations of homes for recovery purpose.
   * @return a delay in milliseconds or 0 to disable auto save.
   * @since 3.0
   */
  public int getAutoSaveDelayForRecovery() {
    return this.autoSaveDelayForRecovery;
  }

  /**
   * Sets the delay between two automatic save operations of homes for recovery purpose.
   * @since 3.0
   */
  public void setAutoSaveDelayForRecovery(int autoSaveDelayForRecovery) {
    if (this.autoSaveDelayForRecovery != autoSaveDelayForRecovery) {
      float oldAutoSaveDelayForRecovery = this.autoSaveDelayForRecovery;
      this.autoSaveDelayForRecovery = autoSaveDelayForRecovery;
      this.propertyChangeSupport.firePropertyChange(Property.AUTO_SAVE_DELAY_FOR_RECOVERY.name(),
          oldAutoSaveDelayForRecovery, autoSaveDelayForRecovery);
    }
  }

  /**
   * Returns an unmodifiable list of the recent homes.
   */
  public List<String> getRecentHomes() {
    return Collections.unmodifiableList(this.recentHomes);
  }

  /**
   * Sets the recent homes list and notifies listeners of this change.
   */
  public void setRecentHomes(List<String> recentHomes) {
    if (!recentHomes.equals(this.recentHomes)) {
      List<String> oldRecentHomes = this.recentHomes;
      this.recentHomes = new ArrayList<String>(recentHomes);
      this.propertyChangeSupport.firePropertyChange(Property.RECENT_HOMES.name(),
          oldRecentHomes, getRecentHomes());
    }
  }

  /**
   * Returns the maximum count of homes that should be proposed to the user.
   */
  public int getRecentHomesMaxCount() {
    return 10;
  }

  /**
   * Returns the maximum count of stored cameras in homes that should be proposed to the user.
   * @since 4.5
   */
  public int getStoredCamerasMaxCount() {
    return 50;
  }

  /**
   * Sets which action tip should be ignored.
   * <br>This method should be overridden to store the ignore information.
   * By default it just notifies listeners of this change.
   */
  public void setActionTipIgnored(String actionKey) {
    this.propertyChangeSupport.firePropertyChange(Property.IGNORED_ACTION_TIP.name(), null, actionKey);
  }

  /**
   * Returns whether an action tip should be ignored or not.
   * <br>This method should be overridden to return the display information
   * stored in {@link #setActionTipIgnored(String) setActionTipIgnored}.
   * By default it returns <code>true</code>.
   */
  public boolean isActionTipIgnored(String actionKey) {
    return true;
  }

  /**
   * Resets the ignore flag of action tips.
   * <br>This method should be overridden to clear all the display flags.
   * By default it just notifies listeners of this change.
   */
  public void resetIgnoredActionTips() {
    this.propertyChangeSupport.firePropertyChange(Property.IGNORED_ACTION_TIP.name(), null, null);
  }

  /**
   * Returns the default text style of a class of selectable item.
   */
  public TextStyle getDefaultTextStyle(Class<? extends Selectable> selectableClass) {
    if (Room.class.isAssignableFrom(selectableClass)) {
      return DEFAULT_ROOM_TEXT_STYLE;
    } else {
      return DEFAULT_TEXT_STYLE;
    }
  }

  /**
   * Returns the strings that may be used for the auto completion of the given <code>property</code>.
   * @since 3.4
   */
  public List<String> getAutoCompletionStrings(String property) {
    List<String> propertyAutoCompletionStrings = this.autoCompletionStrings.get(property);
    if (propertyAutoCompletionStrings != null) {
      return Collections.unmodifiableList(propertyAutoCompletionStrings);
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Adds the given string to the list of the strings used in auto completion of a <code>property</code>
   * and notifies listeners of this change.
   * @since 3.4
   */
  public void addAutoCompletionString(String property, String autoCompletionString) {
    if (autoCompletionString != null
        && autoCompletionString.length() > 0) {
      List<String> propertyAutoCompletionStrings = this.autoCompletionStrings.get(property);
      if (propertyAutoCompletionStrings == null) {
        propertyAutoCompletionStrings = new ArrayList<String>();
      } else if (!propertyAutoCompletionStrings.contains(autoCompletionString)) {
        propertyAutoCompletionStrings = new ArrayList<String>(propertyAutoCompletionStrings);
      } else {
        return;
      }
      propertyAutoCompletionStrings.add(0, autoCompletionString);
      setAutoCompletionStrings(property, propertyAutoCompletionStrings);
    }
  }

  /**
   * Sets the auto completion strings list of the given <code>property</code> and notifies listeners of this change.
   * @since 3.4
   */
  public void setAutoCompletionStrings(String property, List<String> autoCompletionStrings) {
    List<String> propertyAutoCompletionStrings = this.autoCompletionStrings.get(property);
    if (!autoCompletionStrings.equals(propertyAutoCompletionStrings)) {
      this.autoCompletionStrings.put(property, new ArrayList<String>(autoCompletionStrings));
      this.propertyChangeSupport.firePropertyChange(Property.AUTO_COMPLETION_STRINGS.name(),
          null, property);
    }
  }

  /**
   * Returns the list of properties with auto completion strings.
   * @since 3.4
   */
  public List<String> getAutoCompletedProperties() {
    if (this.autoCompletionStrings != null) {
      return Arrays.asList(this.autoCompletionStrings.keySet().toArray(new String [this.autoCompletionStrings.size()]));
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Returns an unmodifiable list of the recent colors.
   * @since 4.0
   */
  public List<Integer> getRecentColors() {
    return Collections.unmodifiableList(this.recentColors);
  }

  /**
   * Sets the recent colors list and notifies listeners of this change.
   * @since 4.0
   */
  public void setRecentColors(List<Integer> recentColors) {
    if (!recentColors.equals(this.recentColors)) {
      List<Integer> oldRecentColors = this.recentColors;
      this.recentColors = new ArrayList<Integer>(recentColors);
      this.propertyChangeSupport.firePropertyChange(Property.RECENT_COLORS.name(),
          oldRecentColors, getRecentColors());
    }
  }

  /**
   * Returns an unmodifiable list of the recent textures.
   * @since 4.4
   */
  public List<TextureImage> getRecentTextures() {
    return Collections.unmodifiableList(this.recentTextures);
  }

  /**
   * Sets the recent colors list and notifies listeners of this change.
   * @since 4.4
   */
  public void setRecentTextures(List<TextureImage> recentTextures) {
    if (!recentTextures.equals(this.recentTextures)) {
      List<TextureImage> oldRecentTextures = this.recentTextures;
      this.recentTextures = new ArrayList<TextureImage>(recentTextures);
      this.propertyChangeSupport.firePropertyChange(Property.RECENT_TEXTURES.name(),
          oldRecentTextures, getRecentTextures());
    }
  }

  /**
   * Sets the home examples available for the user.
   * @since 5.5
   */
  protected void setHomeExamples(List<HomeDescriptor> homeExamples) {
    if (!homeExamples.equals(this.homeExamples)) {
      List<HomeDescriptor> oldExamples = this.homeExamples;
      this.homeExamples = new ArrayList<HomeDescriptor>(homeExamples);
      this.propertyChangeSupport.firePropertyChange(Property.HOME_EXAMPLES.name(),
          oldExamples, getHomeExamples());
    }
  }

  /**
   * Returns the home examples available for the user.
   * @since 5.5
   */
  public List<HomeDescriptor> getHomeExamples() {
    return Collections.unmodifiableList(this.homeExamples);
  }

  /**
   * Adds the language library to make the languages it contains available to supported languages.
   * @param languageLibraryLocation  the location where the library can be found.
   * @since 2.3
   */
  public abstract void addLanguageLibrary(String languageLibraryLocation) throws RecorderException;

  /**
   * Returns <code>true</code> if the language library at the given location exists.
   * @param languageLibraryLocation the name of the resource to check
   * @since 2.3
   */
  public abstract boolean languageLibraryExists(String languageLibraryLocation) throws RecorderException;

  /**
   * Adds <code>furnitureLibraryName</code> to furniture catalog
   * to make the furniture it contains available.
   * @param furnitureLibraryLocation  the location where the library can be found.
   */
  public abstract void addFurnitureLibrary(String furnitureLibraryLocation) throws RecorderException;

  /**
   * Returns <code>true</code> if the furniture library at the given location exists.
   * @param furnitureLibraryLocation the name of the resource to check
   */
  public abstract boolean furnitureLibraryExists(String furnitureLibraryLocation) throws RecorderException;

  /**
   * Adds the textures library at the given location to textures catalog
   * to make the textures it contains available.
   * @param texturesLibraryLocation  the location where the library can be found.
   * @since 2.3
   */
  public abstract void addTexturesLibrary(String texturesLibraryLocation) throws RecorderException;

  /**
   * Returns <code>true</code> if the textures library at the given location exists.
   * @param texturesLibraryLocation the name of the resource to check
   * @since 2.3
   */
  public abstract boolean texturesLibraryExists(String texturesLibraryLocation) throws RecorderException;

  /**
   * Returns the libraries available in user preferences.
   * @since 4.0
   */
  public abstract List<Library> getLibraries();

  /**
   * A resource bundle with a prefix added to resource key.
   */
  private static class PrefixedResourceBundle extends ResourceBundle {
    private ResourceBundle resourceBundle;
    private String         keyPrefix;

    public PrefixedResourceBundle(ResourceBundle resourceBundle,
                                  String keyPrefix) {
      this.resourceBundle = resourceBundle;
      this.keyPrefix = keyPrefix;
    }

    @Override
    public Locale getLocale() {
      return this.resourceBundle.getLocale();
    }

    @Override
    protected Object handleGetObject(String key) {
      key = this.keyPrefix + key;
      return this.resourceBundle.getObject(key);
    }

    @Override
    public Enumeration<String> getKeys() {
      return this.resourceBundle.getKeys();
    }
  }
}
