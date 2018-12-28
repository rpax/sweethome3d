/*
 * FileUserPreferences.java 18 sept 2006
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
package com.eteks.sweethome3d.io;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.eteks.sweethome3d.model.CatalogDoorOrWindow;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.PatternsCatalog;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * User preferences initialized from
 * {@link com.eteks.sweethome3d.io.DefaultUserPreferences default user preferences}
 * and stored in user preferences on local file system.
 * @author Emmanuel Puybaret
 */
public class FileUserPreferences extends UserPreferences {
  private static final String LANGUAGE                                  = "language";
  private static final String UNIT                                      = "unit";
  private static final String EXTENSIBLE_UNIT                           = "extensibleUnit";
  private static final String CURRENCY                                  = "currency";
  private static final String VALUE_ADDED_TAX_ENABLED                   = "valueAddedTaxEnabled";
  private static final String DEFAULT_VALUE_ADDED_TAX_PERCENTAGE        = "defaultValueAddedTaxPercentage";
  private static final String FURNITURE_CATALOG_VIEWED_IN_TREE          = "furnitureCatalogViewedInTree";
  private static final String NAVIGATION_PANEL_VISIBLE                  = "navigationPanelVisible";
  private static final String AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED = "aerialViewCenteredOnSelectionEnabled";
  private static final String OBSERVER_CAMERA_SELECTED_AT_CHANGE        = "observerCameraSelectedAtChange";
  private static final String MAGNETISM_ENABLED                         = "magnetismEnabled";
  private static final String RULERS_VISIBLE                            = "rulersVisible";
  private static final String GRID_VISIBLE                              = "gridVisible";
  private static final String DEFAULT_FONT_NAME                         = "defaultFontName";
  private static final String FURNITURE_VIEWED_FROM_TOP                 = "furnitureViewedFromTop";
  private static final String FURNITURE_MODEL_ICON_SIZE                 = "furnitureModelIconSize";
  private static final String ROOM_FLOOR_COLORED_OR_TEXTURED            = "roomFloorColoredOrTextured";
  private static final String WALL_PATTERN                              = "wallPattern";
  private static final String NEW_WALL_PATTERN                          = "newWallPattern";
  private static final String NEW_WALL_THICKNESS                        = "newWallThickness";
  private static final String NEW_WALL_HEIGHT                           = "newHomeWallHeight";
  private static final String NEW_WALL_BASEBOARD_THICKNESS              = "newWallBaseboardThickness";
  private static final String NEW_WALL_BASEBOARD_HEIGHT                 = "newWallBaseboardHeight";
  private static final String NEW_FLOOR_THICKNESS                       = "newFloorThickness";
  private static final String CHECK_UPDATES_ENABLED                     = "checkUpdatesEnabled";
  private static final String UPDATES_MINIMUM_DATE                      = "updatesMinimumDate";
  private static final String AUTO_SAVE_DELAY_FOR_RECOVERY              = "autoSaveDelayForRecovery";
  private static final String AUTO_COMPLETION_PROPERTY                  = "autoCompletionProperty#";
  private static final String AUTO_COMPLETION_STRINGS                   = "autoCompletionStrings#";
  private static final String RECENT_COLORS                             = "recentColors";
  private static final String RECENT_TEXTURE_NAME                       = "recentTextureName#";
  private static final String RECENT_TEXTURE_CREATOR                    = "recentTextureCreator#";
  private static final String RECENT_TEXTURE_IMAGE                      = "recentTextureImage#";
  private static final String RECENT_TEXTURE_WIDTH                      = "recentTextureWidth#";
  private static final String RECENT_TEXTURE_HEIGHT                     = "recentTextureHeight#";
  private static final String RECENT_HOMES                              = "recentHomes#";
  private static final String IGNORED_ACTION_TIP                        = "ignoredActionTip#";

  private static final String FURNITURE_NAME                            = "furnitureName#";
  private static final String FURNITURE_CREATOR                         = "furnitureCreator#";
  private static final String FURNITURE_CATEGORY                        = "furnitureCategory#";
  private static final String FURNITURE_ICON                            = "furnitureIcon#";
  private static final String FURNITURE_MODEL                           = "furnitureModel#";
  private static final String FURNITURE_WIDTH                           = "furnitureWidth#";
  private static final String FURNITURE_DEPTH                           = "furnitureDepth#";
  private static final String FURNITURE_HEIGHT                          = "furnitureHeight#";
  private static final String FURNITURE_MOVABLE                         = "furnitureMovable#";
  private static final String FURNITURE_DOOR_OR_WINDOW                  = "furnitureDoorOrWindow#";
  private static final String FURNITURE_ELEVATION                       = "furnitureElevation#";
  private static final String FURNITURE_COLOR                           = "furnitureColor#";
  private static final String FURNITURE_MODEL_SIZE                      = "furnitureModelSize#";
  private static final String FURNITURE_MODEL_ROTATION                  = "furnitureModelRotation#";
  private static final String FURNITURE_STAIRCASE_CUT_OUT_SHAPE         = "furnitureStaircaseCutOutShape#";
  private static final String FURNITURE_BACK_FACE_SHOWN                 = "furnitureBackFaceShown#";
  private static final String FURNITURE_ICON_YAW                        = "furnitureIconYaw#";
  private static final String FURNITURE_PROPORTIONAL                    = "furnitureProportional#";

  private static final String TEXTURE_NAME                              = "textureName#";
  private static final String TEXTURE_CREATOR                           = "textureCreator#";
  private static final String TEXTURE_CATEGORY                          = "textureCategory#";
  private static final String TEXTURE_IMAGE                             = "textureImage#";
  private static final String TEXTURE_WIDTH                             = "textureWidth#";
  private static final String TEXTURE_HEIGHT                            = "textureHeight#";

  private static final String FURNITURE_CONTENT_PREFIX                  = "Furniture-3-";
  private static final String TEXTURE_CONTENT_PREFIX                    = "Texture-3-";

  private static final String LANGUAGE_LIBRARIES_PLUGIN_SUB_FOLDER      = "languages";
  private static final String FURNITURE_LIBRARIES_PLUGIN_SUB_FOLDER     = "furniture";
  private static final String TEXTURES_LIBRARIES_PLUGIN_SUB_FOLDER      = "textures";

  private static final PreferencesURLContent MISSING_CONTENT;

  private final Map<String, Boolean> ignoredActionTips = new HashMap<String, Boolean>();
  private List<ClassLoader>          resourceClassLoaders;
  private final File                 preferencesFolder;
  private final File []              applicationFolders;
  private Preferences                preferences;
  private Executor                   catalogsLoader;
  private Executor                   updater;
  private List<Library>              libraries;

  private Map<Content, PreferencesURLContent> copiedContentsCache = new WeakHashMap<Content, PreferencesURLContent>();

  public static final String PLUGIN_LANGUAGE_LIBRARY_FAMILY = "PluginLanguageLibrary";

  static {
    PreferencesURLContent dummyURLContent = null;
    try {
      dummyURLContent = new PreferencesURLContent(new URL("file:/missingSweetHome3DContent"));
    } catch (MalformedURLException ex) {
    }
    MISSING_CONTENT = dummyURLContent;
  }

  /**
   * Creates user preferences read from user preferences in file system,
   * and from resource files.
   */
  public FileUserPreferences() {
    this(null, null);
  }

  /**
   * Creates user preferences stored in the folders given in parameter.
   * @param preferencesFolder the folder where preferences files are stored
   *    or <code>null</code> if this folder is the default one.
   * @param applicationFolders the folders where application private files are stored
   *    or <code>null</code> if it's the default one. As the first application folder
   *    is used as the folder where plug-ins files are imported by the user, it should
   *    have write access otherwise the user won't be able to import them.
   */
  public FileUserPreferences(File preferencesFolder,
                             File [] applicationFolders) {
    this(preferencesFolder, applicationFolders, null);
  }

  /**
   * Creates user preferences stored in the folders given in parameter.
   * @param preferencesFolder the folder where preferences files are stored
   *    or <code>null</code> if this folder is the default one.
   * @param applicationFolders  the folders where application private files are stored
   *    or <code>null</code> if it's the default one. As the first application folder
   *    is used as the folder where plug-ins files are imported by the user, it should
   *    have write access otherwise the user won't be able to import them.
   * @param updater  an executor that will be used to update user preferences for lengthy
   *    operations. If <code>null</code>, then these operations and
   *    updates will be executed in the current thread.
   */
  public FileUserPreferences(File preferencesFolder,
                             File [] applicationFolders,
                             Executor updater) {
    this.libraries = new ArrayList<Library>();
    this.preferencesFolder = preferencesFolder;
    this.applicationFolders = applicationFolders;
    Executor defaultExecutor = new Executor() {
        public void execute(Runnable command) {
          command.run();
        }
      };
    if (updater == null) {
      this.catalogsLoader =
      this.updater = defaultExecutor;
    } else {
      this.catalogsLoader = Executors.newSingleThreadExecutor();
      this.updater = updater;
    }

    updateSupportedLanguages();

    final Preferences preferences;
    // From version 3.0 use portable preferences
    PortablePreferences portablePreferences = new PortablePreferences();
    // If portable preferences storage doesn't exist and default preferences folder is used
    if (!portablePreferences.exist()
        && preferencesFolder == null) {
      // Retrieve preferences from pre version 3.0
      preferences = getPreferences();
    } else {
      preferences = portablePreferences;
    }

    String language = preferences.get(LANGUAGE, getLanguage());
    // Check language is still supported
    if (!Arrays.asList(getSupportedLanguages()).contains(language)) {
      language = Locale.ENGLISH.getLanguage();
    }
    setLanguage(language);

    setFurnitureCatalog(new FurnitureCatalog());
    // Fill default furniture catalog
    updateFurnitureDefaultCatalog(defaultExecutor, defaultExecutor);
    // Read additional furniture
    readModifiableFurnitureCatalog(preferences);

    setTexturesCatalog(new TexturesCatalog());
    // Fill default textures catalog
    updateTexturesDefaultCatalog(defaultExecutor, defaultExecutor);
    // Read additional textures
    readModifiableTexturesCatalog(preferences);

    DefaultUserPreferences defaultPreferences = new DefaultUserPreferences(false, this);

    // Fill default patterns catalog
    PatternsCatalog patternsCatalog = defaultPreferences.getPatternsCatalog();
    setPatternsCatalog(patternsCatalog);

    // Read other preferences
    LengthUnit defaultLengthUnit = defaultPreferences.getLengthUnit();
    try {
      // EXTENSIBLE_UNIT was added in version 4.0 to store new additional length unit
      // to avoid breaking program if an older version of FileUserPreferences reads new preferences
      String extensibleUnit = preferences.get(EXTENSIBLE_UNIT, null);
      if (extensibleUnit != null) {
        setUnit(LengthUnit.valueOf(extensibleUnit));
      } else {
        setUnit(LengthUnit.valueOf(preferences.get(UNIT, defaultLengthUnit.name())));
      }
    } catch (IllegalArgumentException ex) {
      setUnit(defaultLengthUnit);
    }
    setCurrency(preferences.get(CURRENCY, defaultPreferences.getCurrency()));
    setValueAddedTaxEnabled(preferences.getBoolean(VALUE_ADDED_TAX_ENABLED, defaultPreferences.isValueAddedTaxEnabled()));
    String percentage = preferences.get(DEFAULT_VALUE_ADDED_TAX_PERCENTAGE, null);
    BigDecimal valueAddedTaxPercentage = defaultPreferences.getDefaultValueAddedTaxPercentage();
    if (percentage != null) {
      try {
        valueAddedTaxPercentage = new BigDecimal(percentage);
      } catch (NumberFormatException ex) {
      }
    }
    setDefaultValueAddedTaxPercentage(valueAddedTaxPercentage);
    setFurnitureCatalogViewedInTree(preferences.getBoolean(FURNITURE_CATALOG_VIEWED_IN_TREE,
        defaultPreferences.isFurnitureCatalogViewedInTree()));
    setNavigationPanelVisible(preferences.getBoolean(NAVIGATION_PANEL_VISIBLE,
        defaultPreferences.isNavigationPanelVisible()));
    setAerialViewCenteredOnSelectionEnabled(preferences.getBoolean(AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED,
        defaultPreferences.isAerialViewCenteredOnSelectionEnabled()));
    setObserverCameraSelectedAtChange(preferences.getBoolean(OBSERVER_CAMERA_SELECTED_AT_CHANGE,
        defaultPreferences.isObserverCameraSelectedAtChange()));
    setMagnetismEnabled(preferences.getBoolean(MAGNETISM_ENABLED, true));
    setRulersVisible(preferences.getBoolean(RULERS_VISIBLE, defaultPreferences.isRulersVisible()));
    setGridVisible(preferences.getBoolean(GRID_VISIBLE, defaultPreferences.isGridVisible()));
    setDefaultFontName(preferences.get(DEFAULT_FONT_NAME,  defaultPreferences.getDefaultFontName()));
    setFurnitureViewedFromTop(preferences.getBoolean(FURNITURE_VIEWED_FROM_TOP,
        defaultPreferences.isFurnitureViewedFromTop()));
    setFurnitureModelIconSize(preferences.getInt(FURNITURE_MODEL_ICON_SIZE, defaultPreferences.getFurnitureModelIconSize()));
    setFloorColoredOrTextured(preferences.getBoolean(ROOM_FLOOR_COLORED_OR_TEXTURED,
        defaultPreferences.isRoomFloorColoredOrTextured()));
    try {
      setWallPattern(patternsCatalog.getPattern(preferences.get(WALL_PATTERN,
          defaultPreferences.getWallPattern().getName())));
    } catch (IllegalArgumentException ex) {
      // Ensure wall pattern always exists even if new patterns are added in future versions
      setWallPattern(defaultPreferences.getWallPattern());
    }
    try {
      if (defaultPreferences.getNewWallPattern() != null) {
        setNewWallPattern(patternsCatalog.getPattern(preferences.get(NEW_WALL_PATTERN,
            defaultPreferences.getNewWallPattern().getName())));
      }
    } catch (IllegalArgumentException ex) {
      // Keep new wall pattern unchanged
    }
    setNewWallThickness(preferences.getFloat(NEW_WALL_THICKNESS,
        defaultPreferences.getNewWallThickness()));
    setNewWallHeight(preferences.getFloat(NEW_WALL_HEIGHT,
        defaultPreferences.getNewWallHeight()));
    setNewWallBaseboardThickness(preferences.getFloat(NEW_WALL_BASEBOARD_THICKNESS,
        defaultPreferences.getNewWallBaseboardThickness()));
    setNewWallBaseboardHeight(preferences.getFloat(NEW_WALL_BASEBOARD_HEIGHT,
        defaultPreferences.getNewWallBaseboardHeight()));
    setNewFloorThickness(preferences.getFloat(NEW_FLOOR_THICKNESS,
        defaultPreferences.getNewFloorThickness()));
    setCheckUpdatesEnabled(preferences.getBoolean(CHECK_UPDATES_ENABLED,
        defaultPreferences.isCheckUpdatesEnabled()));
    if (preferences.get(UPDATES_MINIMUM_DATE, null) != null) {
      setUpdatesMinimumDate(preferences.getLong(UPDATES_MINIMUM_DATE, 0));
    }
    setAutoSaveDelayForRecovery(preferences.getInt(AUTO_SAVE_DELAY_FOR_RECOVERY,
        defaultPreferences.getAutoSaveDelayForRecovery()));
    // Read recent colors list
    String [] recentColors = preferences.get(RECENT_COLORS, "").split(",");
    List<Integer> recentColorsList = new ArrayList<Integer>(recentColors.length);
    for (String color : recentColors) {
      if (color.length() > 0) {
        recentColorsList.add(Integer.decode(color) | 0xFF000000);
      }
    }
    setRecentColors(recentColorsList);
    readRecentTextures(preferences);
    // Read recent homes list
    List<String> recentHomes = new ArrayList<String>();
    for (int i = 1; i <= getRecentHomesMaxCount(); i++) {
      String recentHome = preferences.get(RECENT_HOMES + i, null);
      if (recentHome != null) {
        recentHomes.add(recentHome);
      }
    }
    setRecentHomes(recentHomes);
    // Read ignored action tips
    for (int i = 1; ; i++) {
      String ignoredActionTip = preferences.get(IGNORED_ACTION_TIP + i, "");
      if (ignoredActionTip.length() == 0) {
        break;
      } else {
        this.ignoredActionTips.put(ignoredActionTip, true);
      }
    }
    // Get default auto completion strings
    for (String property : defaultPreferences.getAutoCompletedProperties()) {
      setAutoCompletionStrings(property, defaultPreferences.getAutoCompletionStrings(property));
    }
    // Read auto completion strings list
    for (int i = 1; ; i++) {
      String autoCompletionProperty = preferences.get(AUTO_COMPLETION_PROPERTY + i, null);
      String autoCompletionStrings = preferences.get(AUTO_COMPLETION_STRINGS + i, null);
      if (autoCompletionProperty != null && autoCompletionStrings != null) {
        setAutoCompletionStrings(autoCompletionProperty, Arrays.asList(autoCompletionStrings.split(",")));
      } else {
        break;
      }
    }

    setHomeExamples(defaultPreferences.getHomeExamples());

    addPropertyChangeListener(Property.LANGUAGE, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          // Update catalogs with new default locale
          updateFurnitureDefaultCatalog(catalogsLoader, FileUserPreferences.this.updater);
          updateTexturesDefaultCatalog(catalogsLoader, FileUserPreferences.this.updater);
          updateAutoCompletionStrings();
          setHomeExamples(new DefaultUserPreferences(false, FileUserPreferences.this).getHomeExamples());
        }
      });

    if (preferences != portablePreferences) {
      // Switch to portable preferences now that all preferences are read
      this.preferences = portablePreferences;
    } else {
      this.preferences = preferences;
    }
  }

  /**
   * Updates the default supported languages with languages available in plugin folder.
   */
  private void updateSupportedLanguages() {
    removeLibraries(LANGUAGE_LIBRARY_TYPE);
    List<ClassLoader> resourceClassLoaders = new ArrayList<ClassLoader>();
    String [] defaultSupportedLanguages = getDefaultSupportedLanguages();
    Set<String> supportedLanguages = new TreeSet<String>(Arrays.asList(defaultSupportedLanguages));

    File [] languageLibrariesPluginFolders = getLanguageLibrariesPluginFolders();
    if (languageLibrariesPluginFolders != null) {
      for (File languageLibrariesPluginFolder : languageLibrariesPluginFolders) {
        // Try to load sh3l files from language plugin folder
        File [] pluginLanguageLibraryFiles = languageLibrariesPluginFolder.listFiles(new FileFilter () {
          public boolean accept(File pathname) {
            return pathname.isFile();
          }
        });

        if (pluginLanguageLibraryFiles != null) {
          // Treat language files in reverse order so file named with a date or a version
          // will be taken into account from most recent to least recent
          Arrays.sort(pluginLanguageLibraryFiles, Collections.reverseOrder(OperatingSystem.getFileVersionComparator()));
          for (File pluginLanguageLibraryFile : pluginLanguageLibraryFiles) {
            try {
              Set<String> languages = getLanguages(pluginLanguageLibraryFile);
              if (!languages.isEmpty()) {
                supportedLanguages.addAll(languages);
                URL pluginFurnitureCatalogUrl = pluginLanguageLibraryFile.toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL [] {pluginFurnitureCatalogUrl});
                resourceClassLoaders.add(classLoader);

                DefaultLibrary languageLibrary;
                try {
                  languageLibrary = new DefaultLibrary(pluginLanguageLibraryFile.getCanonicalPath(), LANGUAGE_LIBRARY_TYPE,
                      ResourceBundle.getBundle(PLUGIN_LANGUAGE_LIBRARY_FAMILY, Locale.getDefault(), classLoader));
                } catch (MissingResourceException ex) {
                  languageLibrary = new DefaultLibrary(pluginLanguageLibraryFile.getCanonicalPath(), LANGUAGE_LIBRARY_TYPE,
                      null, getLanguageLibraryDefaultName(languages), null, getDefaultVersion(pluginLanguageLibraryFile), null, null);
                }
                libraries.add(0, languageLibrary);
              }
            } catch (IOException ex) {
              // Ignore malformed files
            }
          }
        }
      }
    }

    // Give less priority to default class loader
    resourceClassLoaders.addAll(super.getResourceClassLoaders());
    this.resourceClassLoaders = Collections.unmodifiableList(resourceClassLoaders);
    if (defaultSupportedLanguages.length < supportedLanguages.size()) {
      setSupportedLanguages(supportedLanguages.toArray(new String [supportedLanguages.size()]));
    }
  }

  /**
   * Returns the languages included in the given language library file.
   */
  private Set<String> getLanguages(File languageLibraryFile) throws IOException {
    Set<String> languages = new LinkedHashSet<String>();
    ZipInputStream zipIn = null;
    try {
      // Search if zip file contains some *_xx.properties or *_xx_xx.properties files
      zipIn = new ZipInputStream(new FileInputStream(languageLibraryFile));
      for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
        String zipEntryName = entry.getName();
        int underscoreIndex = zipEntryName.indexOf('_');
        if (underscoreIndex != -1) {
          int extensionIndex = zipEntryName.lastIndexOf(".properties");
          if (extensionIndex != -1 && underscoreIndex < extensionIndex - 2) {
            String language = zipEntryName.substring(underscoreIndex + 1, extensionIndex);
            int countrySeparator = language.indexOf('_');
            if (countrySeparator == 2
                && language.length() == 5) {
              languages.add(language);
            } else if (language.length() == 2) {
              languages.add(language);
            }
          }
        }
      }
      return languages;
    } finally {
      if (zipIn != null) {
        zipIn.close();
      }
    }
  }

  /**
   * Returns a text in English describing the given languages.
   */
  private String getLanguageLibraryDefaultName(Set<String> languages) {
    String description = "";
    for (String language : languages) {
      if (description.length() > 0) {
        description += ", ";
      }
      int underscoreIndex = language.indexOf('_');
      Locale locale = underscoreIndex < 0
          ? new Locale(language)
          : new Locale(language.substring(0, underscoreIndex), language.substring(underscoreIndex + 1));
      description += locale.getDisplayLanguage(Locale.ENGLISH);
      if (underscoreIndex >= 0) {
        description += " (" + locale.getDisplayCountry(Locale.ENGLISH) + ")";
      }
    }
    if (languages.size() > 1) {
      description += " languages support";
    } else {
      description += " language support";
    }
    return description;
  }

  /**
   * Returns a version number from the given file name or <code>null</code>.
   */
  private String getDefaultVersion(File pluginLanguageLibraryFile) {
    String fileName = pluginLanguageLibraryFile.getName();
    // Search version number between last hyphen and last point
    int hyphenIndex = fileName.lastIndexOf('-');
    if (hyphenIndex > 0) {
      int pointIndex = fileName.lastIndexOf('.');
      if (pointIndex < 0) {
        pointIndex = fileName.length();
      }
      String version = fileName.substring(hyphenIndex + 1, pointIndex);
      if (version.matches("[\\d\\.]+")) {
        return version;
      }
    }
    return null;
  }

  /**
   * Returns the default class loader of user preferences and the class loaders that
   * give access to resources in language libraries plugin folder.
   */
  @Override
  public List<ClassLoader> getResourceClassLoaders() {
    return this.resourceClassLoaders;
  }

  /**
   * Reloads furniture default catalogs.
   */
  private void updateFurnitureDefaultCatalog(Executor furnitureCatalogLoader,
                                             final Executor updater) {
    final FurnitureCatalog furnitureCatalog = getFurnitureCatalog();
    furnitureCatalogLoader.execute(new Runnable() {
        public void run() {
          updater.execute(new Runnable() {
              public void run() {
                // Delete default furniture of current furniture catalog
                for (FurnitureCategory category : furnitureCatalog.getCategories()) {
                  for (CatalogPieceOfFurniture piece : category.getFurniture()) {
                    if (!piece.isModifiable()) {
                      furnitureCatalog.delete(piece);
                    }
                  }
                }
              }
            });

          // Read default furniture catalog
          final FurnitureCatalog resourceFurnitureCatalog =
              readFurnitureCatalogFromResource(getFurnitureLibrariesPluginFolders());
          for (final FurnitureCategory category : resourceFurnitureCatalog.getCategories()) {
            for (final CatalogPieceOfFurniture piece : category.getFurniture()) {
              updater.execute(new Runnable() {
                  public void run() {
                    furnitureCatalog.add(category, piece);
                  }
                });
            }
          }
          if (resourceFurnitureCatalog instanceof DefaultFurnitureCatalog) {
            updater.execute(new Runnable() {
                public void run() {
                  removeLibraries(FURNITURE_LIBRARY_TYPE);
                  libraries.addAll(((DefaultFurnitureCatalog)resourceFurnitureCatalog).getLibraries());
                }
              });
          }
        }
      });
  }

  /**
   * Returns the furniture catalog contained in resources of the application and in the given plug-in folders.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   */
  protected FurnitureCatalog readFurnitureCatalogFromResource(File [] furniturePluginFolders) {
    return new DefaultFurnitureCatalog(this, furniturePluginFolders);
  }

  /**
   * Removes from the list of libraries the ones of the given type.
   */
  private void removeLibraries(String libraryType) {
    for (Iterator<Library> it = this.libraries.iterator(); it.hasNext(); ) {
      Library library = it.next();
      if (library.getType() == libraryType) {
        it.remove();
      }
    }
  }

  /**
   * Reloads textures default catalog.
   */
  private void updateTexturesDefaultCatalog(Executor texturesCatalogLoader,
                                            final Executor updater) {
    final TexturesCatalog texturesCatalog = getTexturesCatalog();
    texturesCatalogLoader.execute(new Runnable() {
        public void run() {
          updater.execute(new Runnable() {
              public void run() {
                // Delete default textures of current textures catalog
                for (TexturesCategory category : texturesCatalog.getCategories()) {
                  for (CatalogTexture texture : category.getTextures()) {
                    if (!texture.isModifiable()) {
                      texturesCatalog.delete(texture);
                    }
                  }
                }
              }
            });

          // Read default textures catalog
          final TexturesCatalog resourceTexturesCatalog =
              readTexturesCatalogFromResource(getTexturesLibrariesPluginFolders());
          for (final TexturesCategory category : resourceTexturesCatalog.getCategories()) {
            for (final CatalogTexture texture : category.getTextures()) {
              updater.execute(new Runnable() {
                  public void run() {
                    texturesCatalog.add(category, texture);
                  }
                });
            }
          }
          if (resourceTexturesCatalog instanceof DefaultTexturesCatalog) {
            updater.execute(new Runnable() {
                public void run() {
                  removeLibraries(TEXTURES_LIBRARY_TYPE);
                  libraries.addAll(((DefaultTexturesCatalog)resourceTexturesCatalog).getLibraries());
                }
              });
          }
        }
      });
  }

  /**
   * Returns the textures catalog contained in resources of the application and in the given plug-in folders.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   */
  protected TexturesCatalog readTexturesCatalogFromResource(File [] texturesPluginFolders) {
    return new DefaultTexturesCatalog(this, texturesPluginFolders);
  }

  /**
   * Adds to auto completion strings the default strings of the new chosen language.
   */
  private void updateAutoCompletionStrings() {
    DefaultUserPreferences defaultPreferences = new DefaultUserPreferences(false, this);
    for (String property : defaultPreferences.getAutoCompletedProperties()) {
      for (String autoCompletionString : defaultPreferences.getAutoCompletionStrings(property)) {
        addAutoCompletionString(property, autoCompletionString);
      }
    }
  }

  /**
   * Read recent textures from preferences.
   */
  private void readRecentTextures(Preferences preferences) {
    File preferencesFolder;
    try {
      preferencesFolder = getPreferencesFolder();
    } catch (IOException ex) {
      return;
    }
    List<TextureImage> recentTextures = new ArrayList<TextureImage>();
    for (int index = 1; true; index++) {
      String textureName = preferences.get(RECENT_TEXTURE_NAME + index, null);
      if (textureName == null) {
        break;
      } else {
        Content image = getContent(preferences, RECENT_TEXTURE_IMAGE + index, preferencesFolder);
        if (image != MISSING_CONTENT) {
          float width = preferences.getFloat(RECENT_TEXTURE_WIDTH + index, -1);
          float height = preferences.getFloat(RECENT_TEXTURE_HEIGHT + index, -1);
          String creator = preferences.get(RECENT_TEXTURE_CREATOR + index, null);
          recentTextures.add(new CatalogTexture(null, textureName, image, width, height, creator));
        }
      }
    }
    setRecentTextures(recentTextures);
  }

  /**
   * Read modifiable furniture catalog from preferences.
   */
  private void readModifiableFurnitureCatalog(Preferences preferences) {
    File preferencesFolder;
    try {
      preferencesFolder = getPreferencesFolder();
    } catch (IOException ex) {
      ex.printStackTrace();
      return;
    }
    CatalogPieceOfFurniture piece;
    for (int i = 1; (piece = readModifiablePieceOfFurniture(preferences, i, preferencesFolder)) != null; i++) {
      if (piece.getIcon() != MISSING_CONTENT
          && piece.getModel() != MISSING_CONTENT) {
        FurnitureCategory pieceCategory = readModifiableFurnitureCategory(preferences, i);
        getFurnitureCatalog().add(pieceCategory, piece);
      }
    }
  }

  /**
   * Returns the modifiable piece of furniture read from <code>preferences</code> at the given <code>index</code>.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   * @param preferences        the preferences from which piece of furniture data can be read
   * @param index              the index of the read piece
   * @param preferencesFolder  the folder where piece resources can be stored
   * @return the read piece of furniture or <code>null</code> if the piece at the given index doesn't exist.
   */
  protected CatalogPieceOfFurniture readModifiablePieceOfFurniture(Preferences preferences,
                                                                   int index,
                                                                   File preferencesFolder) {
    String name = preferences.get(FURNITURE_NAME + index, null);
    if (name == null) {
      // Return null if key furnitureName# doesn't exist
      return null;
    }
    URLContent icon  = getContent(preferences, FURNITURE_ICON + index, preferencesFolder);
    URLContent model = getContent(preferences, FURNITURE_MODEL + index, preferencesFolder);
    float width = preferences.getFloat(FURNITURE_WIDTH + index, 0.1f);
    float depth = preferences.getFloat(FURNITURE_DEPTH + index, 0.1f);
    float height = preferences.getFloat(FURNITURE_HEIGHT + index, 0.1f);
    float elevation = preferences.getFloat(FURNITURE_ELEVATION + index, 0);
    boolean movable = preferences.getBoolean(FURNITURE_MOVABLE + index, false);
    boolean doorOrWindow = preferences.getBoolean(FURNITURE_DOOR_OR_WINDOW + index, false);
    String staircaseCutOutShape = preferences.get(FURNITURE_STAIRCASE_CUT_OUT_SHAPE + index, null);
    String colorString = preferences.get(FURNITURE_COLOR + index, null);
    Integer color = colorString != null
        ? Integer.valueOf(colorString) : null;
    float [][] modelRotation = getModelRotation(preferences, FURNITURE_MODEL_ROTATION + index);
    boolean backFaceShown = preferences.getBoolean(FURNITURE_BACK_FACE_SHOWN + index, false);
    String modelSizeString = preferences.get(FURNITURE_MODEL_SIZE + index, null);
    Long modelSize = modelSizeString != null
        ? Long.valueOf(modelSizeString) : model.getSize();
    String creator = preferences.get(FURNITURE_CREATOR + index, null);
    float iconYaw = preferences.getFloat(FURNITURE_ICON_YAW + index, 0);
    boolean proportional = preferences.getBoolean(FURNITURE_PROPORTIONAL + index, true);

    if (doorOrWindow) {
      return new CatalogDoorOrWindow(name, icon, model,
          width, depth, height, elevation, movable, 1, 0, new Sash [0],
          color, modelRotation, backFaceShown, modelSize, creator, iconYaw, proportional);
    } else {
      return new CatalogPieceOfFurniture(name, icon, model,
          width, depth, height, elevation, movable,
          staircaseCutOutShape, color, modelRotation, backFaceShown, modelSize, creator, iconYaw, proportional);
    }
  }

  /**
   * Returns the furniture category of a piece at the given <code>index</code>
   * read from <code>preferences</code>.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   * @param preferences        the preferences from which piece of furniture data can be read
   * @param index              the index of the read piece
   */
  protected FurnitureCategory readModifiableFurnitureCategory(Preferences preferences, int index) {
    String category = preferences.get(FURNITURE_CATEGORY + index, "");
    return new FurnitureCategory(category);
  }

  /**
   * Returns model rotation parsed from key value.
   */
  private float [][] getModelRotation(Preferences preferences, String key) {
    String modelRotationString = preferences.get(key, null);
    if (modelRotationString == null) {
      return new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    } else {
      String [] values = modelRotationString.split(" ", 9);
      if (values.length != 9) {
        return new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
      } else {
        try {
          return new float [][] {{Float.parseFloat(values [0]),
                                  Float.parseFloat(values [1]),
                                  Float.parseFloat(values [2])},
                                 {Float.parseFloat(values [3]),
                                  Float.parseFloat(values [4]),
                                  Float.parseFloat(values [5])},
                                 {Float.parseFloat(values [6]),
                                  Float.parseFloat(values [7]),
                                  Float.parseFloat(values [8])}};
        } catch (NumberFormatException ex) {
          return new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        }
      }
    }
  }

  /**
   * Returns a content instance from the resource file value of key.
   */
  private PreferencesURLContent getContent(Preferences preferences, String key,
                                           File preferencesFolder) {
    String content = preferences.get(key, null);
    if (content != null) {
      try {
        String preferencesFolderUrl = preferencesFolder.toURI().toURL().toString();
        URL url;
        if (content.startsWith(preferencesFolderUrl)
            || content.startsWith("jar:" + preferencesFolderUrl)) {
          url = new URL(content);
        } else {
          url = new URL(content.replace("file:", preferencesFolderUrl));
        }
        PreferencesURLContent urlContent =  new PreferencesURLContent(url);
        // Check if a local file exists
        if (urlContent.isJAREntry()) {
          URL jarEntryURL = urlContent.getJAREntryURL();
          if ("file".equals(jarEntryURL.getProtocol())
              && !new File(jarEntryURL.toURI()).exists()) {
            return MISSING_CONTENT;
          }
        } else if ("file".equals(url.getProtocol())
                   && !new File(url.toURI()).exists()) {
          return MISSING_CONTENT;
        }
        return urlContent;
      } catch (IOException ex) {
        // Return MISSING_CONTENT for incorrect URL and content
      } catch (URISyntaxException ex) {
        // Return MISSING_CONTENT for incorrect content
      }
    }
    return MISSING_CONTENT;
  }

  /**
   * Read modifiable textures catalog from preferences.
   */
  private void readModifiableTexturesCatalog(Preferences preferences) {
    File preferencesFolder;
    try {
      preferencesFolder = getPreferencesFolder();
    } catch (IOException ex) {
      ex.printStackTrace();
      return;
    }
    CatalogTexture texture;
    for (int i = 1; (texture = readModifiableTexture(preferences, i, preferencesFolder)) != null; i++) {
      if (texture.getImage() != MISSING_CONTENT) {
        TexturesCategory textureCategory = readModifiableTextureCategory(preferences, i);
        getTexturesCatalog().add(textureCategory, texture);
      }
    }
  }

  /**
   * Returns the modifiable texture read from <code>preferences</code> at the given <code>index</code>.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   * @param preferences        the preferences from which texture data can be read
   * @param index              the index of the read texture
   * @param preferencesFolder  the folder where textures resources can be stored
   * @return the read texture or <code>null</code> if the texture at the given index doesn't exist.
   */
  protected CatalogTexture readModifiableTexture(Preferences preferences,
                                                 int index, File preferencesFolder) {
    String name = preferences.get(TEXTURE_NAME + index, null);
    if (name == null) {
      // Return null if key textureName# doesn't exist
      return null;
    }
    Content image = getContent(preferences, TEXTURE_IMAGE + index, preferencesFolder);
    float width = preferences.getFloat(TEXTURE_WIDTH + index, 0.1f);
    float height = preferences.getFloat(TEXTURE_HEIGHT + index, 0.1f);
    String creator = preferences.get(TEXTURE_CREATOR + index, null);
    return new CatalogTexture(null, name, image, width, height, creator, true);
  }

  /**
   * Returns the category of a texture at the given <code>index</code>
   * read from <code>preferences</code>.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   * @param preferences        the preferences from which texture data can be read
   * @param index              the index of the read piece
   */
  protected TexturesCategory readModifiableTextureCategory(Preferences preferences, int index) {
    String category = preferences.get(TEXTURE_CATEGORY + index, "");
    return new TexturesCategory(category);
  }

  /**
   * Writes user preferences in current user preferences in system.
   */
  @Override
  public void write() throws RecorderException {
    Preferences preferences = getPreferences();
    writeModifiableFurnitureCatalog(preferences);
    writeRecentAndModifiableTexturesCatalog(preferences);

    // Write other preferences
    preferences.put(LANGUAGE, getLanguage());
    preferences.put(EXTENSIBLE_UNIT, getLengthUnit().name());
    String currency = getCurrency();
    if (currency == null) {
      preferences.remove(CURRENCY);
    } else {
      preferences.put(CURRENCY, currency);
    }
    preferences.putBoolean(VALUE_ADDED_TAX_ENABLED, isValueAddedTaxEnabled());
    BigDecimal valueAddedTaxPercentage = getDefaultValueAddedTaxPercentage();
    if (valueAddedTaxPercentage == null) {
      preferences.remove(DEFAULT_VALUE_ADDED_TAX_PERCENTAGE);
    } else {
      preferences.put(DEFAULT_VALUE_ADDED_TAX_PERCENTAGE, valueAddedTaxPercentage.toPlainString());
    }
    preferences.putBoolean(FURNITURE_CATALOG_VIEWED_IN_TREE, isFurnitureCatalogViewedInTree());
    preferences.putBoolean(NAVIGATION_PANEL_VISIBLE, isNavigationPanelVisible());
    preferences.putBoolean(MAGNETISM_ENABLED, isMagnetismEnabled());
    preferences.putBoolean(AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED, isAerialViewCenteredOnSelectionEnabled());
    preferences.putBoolean(OBSERVER_CAMERA_SELECTED_AT_CHANGE, isObserverCameraSelectedAtChange());
    preferences.putBoolean(RULERS_VISIBLE, isRulersVisible());
    preferences.putBoolean(GRID_VISIBLE, isGridVisible());
    String defaultFontName = getDefaultFontName();
    if (defaultFontName == null) {
      preferences.remove(DEFAULT_FONT_NAME);
    } else {
      preferences.put(DEFAULT_FONT_NAME, defaultFontName);
    }
    preferences.putBoolean(FURNITURE_VIEWED_FROM_TOP, isFurnitureViewedFromTop());
    preferences.putInt(FURNITURE_MODEL_ICON_SIZE, getFurnitureModelIconSize());
    preferences.putBoolean(ROOM_FLOOR_COLORED_OR_TEXTURED, isRoomFloorColoredOrTextured());
    preferences.put(WALL_PATTERN, getWallPattern().getName());
    TextureImage newWallPattern = getNewWallPattern();
    if (newWallPattern != null) {
      preferences.put(NEW_WALL_PATTERN, newWallPattern.getName());
    }
    preferences.putFloat(NEW_WALL_THICKNESS, getNewWallThickness());
    preferences.putFloat(NEW_WALL_HEIGHT, getNewWallHeight());
    preferences.putFloat(NEW_WALL_BASEBOARD_THICKNESS, getNewWallBaseboardThickness());
    preferences.putFloat(NEW_WALL_BASEBOARD_HEIGHT, getNewWallBaseboardHeight());
    preferences.putFloat(NEW_FLOOR_THICKNESS, getNewFloorThickness());
    preferences.putBoolean(CHECK_UPDATES_ENABLED, isCheckUpdatesEnabled());
    Long updatesMinimumDate = getUpdatesMinimumDate();
    if (updatesMinimumDate != null) {
      preferences.putLong(UPDATES_MINIMUM_DATE, updatesMinimumDate);
    }
    preferences.putInt(AUTO_SAVE_DELAY_FOR_RECOVERY, getAutoSaveDelayForRecovery());
    // Write recent homes list
    int i = 1;
    for (Iterator<String> it = getRecentHomes().iterator(); it.hasNext() && i <= getRecentHomesMaxCount(); i ++) {
      preferences.put(RECENT_HOMES + i, it.next());
    }
    // Remove obsolete keys
    for ( ; i <= getRecentHomesMaxCount(); i++) {
      preferences.remove(RECENT_HOMES + i);
    }
    // Write recent colors
    StringBuilder recentColors = new StringBuilder();
    Iterator<Integer> itColor = getRecentColors().iterator();
    for (int j = 0; j < 100 && itColor.hasNext(); j++) {
      if (j > 0) {
        recentColors.append(",");
      }
      recentColors.append(String.format("#%6X", itColor.next() & 0xFFFFFF).replace(' ', '0'));
    }
    preferences.put(RECENT_COLORS, recentColors.toString());
    // Write ignored action tips
    i = 1;
    for (Iterator<Map.Entry<String, Boolean>> it = this.ignoredActionTips.entrySet().iterator();
         it.hasNext(); ) {
      Entry<String, Boolean> ignoredActionTipEntry = it.next();
      if (ignoredActionTipEntry.getValue()) {
        preferences.put(IGNORED_ACTION_TIP + i++, ignoredActionTipEntry.getKey());
      }
    }
    // Remove obsolete keys
    for ( ; i <= this.ignoredActionTips.size(); i++) {
      preferences.remove(IGNORED_ACTION_TIP + i);
    }
    // Write auto completion strings lists
    i = 1;
    for (String property : getAutoCompletedProperties()) {
      StringBuilder autoCompletionStrings = new StringBuilder();
      Iterator<String> it = getAutoCompletionStrings(property).iterator();
      for (int j = 0; j < 1000 && it.hasNext(); j++) {
        String autoCompletionString = it.next();
        // As strings are comma separated, accept only the ones without a comma
        if (autoCompletionString.indexOf(',') < 0
            && autoCompletionStrings.length() + autoCompletionString.length() + 1 <= Preferences.MAX_VALUE_LENGTH) {
          if (autoCompletionStrings.length() > 0) {
            autoCompletionStrings.append(",");
          }
          autoCompletionStrings.append(autoCompletionString);
        }
      }
      preferences.put(AUTO_COMPLETION_PROPERTY + i, property);
      preferences.put(AUTO_COMPLETION_STRINGS + i++, autoCompletionStrings.toString());
    }
    for ( ; preferences.get(AUTO_COMPLETION_PROPERTY + i, null) != null; i++) {
      preferences.remove(AUTO_COMPLETION_PROPERTY + i);
      preferences.remove(AUTO_COMPLETION_STRINGS + i);
    }

    try {
      // Write preferences
      preferences.flush();
    } catch (BackingStoreException ex) {
      throw new RecorderException("Couldn't write preferences", ex);
    }
  }

  /**
   * Writes modifiable furniture in <code>preferences</code>.
   */
  private void writeModifiableFurnitureCatalog(Preferences preferences) throws RecorderException {
    final Set<URL> furnitureContentURLs = new HashSet<URL>();
    int i = 1;
    for (FurnitureCategory category : getFurnitureCatalog().getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        if (piece.isModifiable()) {
          preferences.put(FURNITURE_NAME + i, piece.getName());
          preferences.put(FURNITURE_CATEGORY + i, category.getName());
          putContent(preferences, FURNITURE_ICON + i, piece.getIcon(),
              FURNITURE_CONTENT_PREFIX, furnitureContentURLs);
          putContent(preferences, FURNITURE_MODEL + i, piece.getModel(),
              FURNITURE_CONTENT_PREFIX, furnitureContentURLs);
          preferences.putFloat(FURNITURE_WIDTH + i, piece.getWidth());
          preferences.putFloat(FURNITURE_DEPTH + i, piece.getDepth());
          preferences.putFloat(FURNITURE_HEIGHT + i, piece.getHeight());
          preferences.putFloat(FURNITURE_ELEVATION + i, piece.getElevation());
          preferences.putBoolean(FURNITURE_MOVABLE + i, piece.isMovable());
          preferences.putBoolean(FURNITURE_DOOR_OR_WINDOW + i, piece.isDoorOrWindow());
          if (piece.getStaircaseCutOutShape() != null) {
            preferences.put(FURNITURE_STAIRCASE_CUT_OUT_SHAPE + i, piece.getStaircaseCutOutShape());
          } else {
            preferences.remove(FURNITURE_STAIRCASE_CUT_OUT_SHAPE + i);
          }
          if (piece.getColor() != null) {
            preferences.put(FURNITURE_COLOR + i, String.valueOf(piece.getColor()));
          } else {
            preferences.remove(FURNITURE_COLOR + i);
          }
          float [][] modelRotation = piece.getModelRotation();
          preferences.put(FURNITURE_MODEL_ROTATION + i,
              floatToString(modelRotation[0][0]) + " " + floatToString(modelRotation[0][1]) + " " + floatToString(modelRotation[0][2]) + " "
              + floatToString(modelRotation[1][0]) + " " + floatToString(modelRotation[1][1]) + " " + floatToString(modelRotation[1][2]) + " "
              + floatToString(modelRotation[2][0]) + " " + floatToString(modelRotation[2][1]) + " " + floatToString(modelRotation[2][2]));
          preferences.putBoolean(FURNITURE_BACK_FACE_SHOWN + i, piece.isBackFaceShown());
          if (piece.getModelSize() != null) {
            preferences.putLong(FURNITURE_MODEL_SIZE + i, piece.getModelSize());
          } else {
            preferences.remove(FURNITURE_MODEL_SIZE + i);
          }
          if (piece.getCreator() != null) {
            preferences.put(FURNITURE_CREATOR + i, piece.getCreator());
          } else {
            preferences.remove(FURNITURE_CREATOR + i);
          }
          preferences.putFloat(FURNITURE_ICON_YAW + i, piece.getIconYaw());
          preferences.putBoolean(FURNITURE_PROPORTIONAL + i, piece.isProportional());
          i++;
        }
      }
    }
    // Remove obsolete keys
    for ( ; preferences.get(FURNITURE_NAME + i, null) != null; i++) {
      preferences.remove(FURNITURE_NAME + i);
      preferences.remove(FURNITURE_CATEGORY + i);
      preferences.remove(FURNITURE_ICON + i);
      preferences.remove(FURNITURE_MODEL + i);
      preferences.remove(FURNITURE_WIDTH + i);
      preferences.remove(FURNITURE_DEPTH + i);
      preferences.remove(FURNITURE_HEIGHT + i);
      preferences.remove(FURNITURE_ELEVATION + i);
      preferences.remove(FURNITURE_MOVABLE + i);
      preferences.remove(FURNITURE_DOOR_OR_WINDOW + i);
      preferences.remove(FURNITURE_STAIRCASE_CUT_OUT_SHAPE + i);
      preferences.remove(FURNITURE_COLOR + i);
      preferences.remove(FURNITURE_MODEL_ROTATION + i);
      preferences.remove(FURNITURE_BACK_FACE_SHOWN + i);
      preferences.remove(FURNITURE_MODEL_SIZE + i);
      preferences.remove(FURNITURE_CREATOR + i);
      preferences.remove(FURNITURE_ICON_YAW + i);
      preferences.remove(FURNITURE_PROPORTIONAL + i);
    }
    deleteObsoleteContent(furnitureContentURLs, FURNITURE_CONTENT_PREFIX);
  }

  /**
   * Returns the string value of the given float, except for -1.0, 1.0 or 0.0 where -1, 1 and 0 is returned.
   */
  private String floatToString(float f) {
    if (Math.abs(f) < 1E-6) {
      return "0";
    } else if (Math.abs(f - 1f) < 1E-6) {
      return "1";
    } else if (Math.abs(f + 1f) < 1E-6) {
      return "-1";
    } else {
      return String.valueOf(f);
    }
  }

  /**
   * Writes recent textures and modifiable textures catalog in <code>preferences</code>.
   */
  private void writeRecentAndModifiableTexturesCatalog(Preferences preferences) throws RecorderException {
    final Set<URL> texturesContentURLs = new HashSet<URL>();
    // Save recent textures
    int i = 1;
    for (TextureImage texture : getRecentTextures()) {
      preferences.put(RECENT_TEXTURE_NAME + i, texture.getName());
      putContent(preferences, RECENT_TEXTURE_IMAGE + i, texture.getImage(),
          TEXTURE_CONTENT_PREFIX, texturesContentURLs);
      if (texture.getWidth() != -1) {
        preferences.putFloat(RECENT_TEXTURE_WIDTH + i, texture.getWidth());
      } else {
        preferences.remove(RECENT_TEXTURE_WIDTH + i);
      }
      if (texture.getHeight() != -1) {
        preferences.putFloat(RECENT_TEXTURE_HEIGHT + i, texture.getHeight());
      } else {
        preferences.remove(RECENT_TEXTURE_HEIGHT + i);
      }
      if (texture.getCreator() != null) {
        preferences.put(RECENT_TEXTURE_CREATOR + i, texture.getCreator());
      } else {
        preferences.remove(RECENT_TEXTURE_CREATOR + i);
      }
      i++;
    }
    // Remove obsolete keys
    for ( ; preferences.get(RECENT_TEXTURE_NAME + i, null) != null; i++) {
      preferences.remove(RECENT_TEXTURE_NAME + i);
      preferences.remove(RECENT_TEXTURE_IMAGE + i);
      preferences.remove(RECENT_TEXTURE_WIDTH + i);
      preferences.remove(RECENT_TEXTURE_HEIGHT + i);
      preferences.remove(RECENT_TEXTURE_CREATOR + i);
    }

    // Save modifiable textures
    i = 1;
    for (TexturesCategory category : getTexturesCatalog().getCategories()) {
      for (CatalogTexture texture : category.getTextures()) {
        if (texture.isModifiable()) {
          preferences.put(TEXTURE_NAME + i, texture.getName());
          preferences.put(TEXTURE_CATEGORY + i, category.getName());
          putContent(preferences, TEXTURE_IMAGE + i, texture.getImage(),
              TEXTURE_CONTENT_PREFIX, texturesContentURLs);
          preferences.putFloat(TEXTURE_WIDTH + i, texture.getWidth());
          preferences.putFloat(TEXTURE_HEIGHT + i, texture.getHeight());
          if (texture.getCreator() != null) {
            preferences.put(TEXTURE_CREATOR + i, texture.getCreator());
          } else {
            preferences.remove(TEXTURE_CREATOR + i);
          }
          i++;
        }
      }
    }
    // Remove obsolete keys
    for ( ; preferences.get(TEXTURE_NAME + i, null) != null; i++) {
      preferences.remove(TEXTURE_NAME + i);
      preferences.remove(TEXTURE_CATEGORY + i);
      preferences.remove(TEXTURE_IMAGE + i);
      preferences.remove(TEXTURE_WIDTH + i);
      preferences.remove(TEXTURE_HEIGHT + i);
      preferences.remove(TEXTURE_CREATOR + i);
    }

    deleteObsoleteContent(texturesContentURLs, TEXTURE_CONTENT_PREFIX);
  }

  /**
   * Writes <code>key</code> <code>content</code> in <code>preferences</code>.
   */
  private void putContent(Preferences preferences, String key,
                          Content content, String contentPrefix,
                          Set<URL> savedContentURLs) throws RecorderException {
    if (content instanceof PreferencesURLContent) {
      PreferencesURLContent preferencesContent = (PreferencesURLContent)content;
      try {
        preferences.put(key, preferencesContent.getURL().toString()
            .replace(getPreferencesFolder().toURI().toURL().toString(), "file:"));
      } catch (IOException ex) {
        throw new RecorderException("Can't save content", ex);
      }
      // Add to furnitureContentURLs the URL to the application file
      if (preferencesContent.isJAREntry()) {
        savedContentURLs.add(preferencesContent.getJAREntryURL());
      } else {
        savedContentURLs.add(preferencesContent.getURL());
      }
    } else {
      PreferencesURLContent preferencesContent = this.copiedContentsCache.get(content);
      if (preferencesContent == null) {
        if (content instanceof TemporaryURLContent
            && ((TemporaryURLContent)content).isJAREntry()) {
          URLContent urlContent = (URLContent)content;
          try {
            // If content is a JAR entry copy the content of its URL and rebuild a new URL content from
            // this copy and the entry name
            PreferencesURLContent copiedContent = copyToPreferencesURLContent(new URLContent(urlContent.getJAREntryURL()), contentPrefix);
            preferencesContent = new PreferencesURLContent(new URL("jar:" + copiedContent.getURL() + "!/" + urlContent.getJAREntryName()));
          } catch (MalformedURLException ex) {
            // Shouldn't happen
            throw new RecorderException("Can't build URL", ex);
          }
        } else {
          preferencesContent = copyToPreferencesURLContent(content, contentPrefix);
        }
        // Store the copied content in cache to avoid copying it again the next time preferences are written
        this.copiedContentsCache.put(content, preferencesContent);
      }

      putContent(preferences, key, preferencesContent, contentPrefix, savedContentURLs);
    }
  }

  /**
   * Returns a content object that references a copy of <code>content</code> in
   * user preferences folder.
   */
  private PreferencesURLContent copyToPreferencesURLContent(Content content,
                                                            String contentPrefix) throws RecorderException {
    InputStream tempIn = null;
    OutputStream tempOut = null;
    try {
      File preferencesFile = createPreferencesFile(contentPrefix);
      tempIn = content.openStream();
      tempOut = new FileOutputStream(preferencesFile);
      byte [] buffer = new byte [8192];
      int size;
      while ((size = tempIn.read(buffer)) != -1) {
        tempOut.write(buffer, 0, size);
      }
      return new PreferencesURLContent(preferencesFile.toURI().toURL());
    } catch (IOException ex) {
      throw new RecorderException("Can't save content", ex);
    } finally {
      try {
        if (tempIn != null) {
          tempIn.close();
        }
        if (tempOut != null) {
          tempOut.close();
        }
      } catch (IOException ex) {
        throw new RecorderException("Can't close files", ex);
      }
    }
  }

  /**
   * Returns the folders where language libraries files are placed
   * or <code>null</code> if that folder can't be retrieved.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   */
  protected File [] getLanguageLibrariesPluginFolders() {
    try {
      return getApplicationSubfolders(LANGUAGE_LIBRARIES_PLUGIN_SUB_FOLDER);
    } catch (IOException ex) {
      return null;
    }
  }

  /**
   * Returns the folders where furniture catalog files are placed
   * or <code>null</code> if that folder can't be retrieved.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   */
  protected File [] getFurnitureLibrariesPluginFolders() {
    try {
      return getApplicationSubfolders(FURNITURE_LIBRARIES_PLUGIN_SUB_FOLDER);
    } catch (IOException ex) {
      return null;
    }
  }

  /**
   * Returns the folders where texture catalog files are placed
   * or <code>null</code> if that folder can't be retrieved.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   */
  protected File [] getTexturesLibrariesPluginFolders() {
    try {
      return getApplicationSubfolders(TEXTURES_LIBRARIES_PLUGIN_SUB_FOLDER);
    } catch (IOException ex) {
      return null;
    }
  }

  /**
   * Returns the first Sweet Home 3D application folder.
   */
  public File getApplicationFolder() throws IOException {
    File [] applicationFolders = getApplicationFolders();
    if (applicationFolders.length == 0) {
      throw new IOException("No application folder defined");
    } else {
      return applicationFolders [0];
    }
  }

  /**
   * Returns Sweet Home 3D application folders.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   */
  public File [] getApplicationFolders() throws IOException {
    if (this.applicationFolders != null) {
      return this.applicationFolders;
    } else {
      return new File [] {OperatingSystem.getDefaultApplicationFolder()};
    }
  }

  /**
   * Returns subfolders of Sweet Home 3D application folders of a given name.
   * Caution : This method can be called from constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   */
  public File [] getApplicationSubfolders(String subfolder) throws IOException {
    File [] applicationFolders = getApplicationFolders();
    File [] applicationSubfolders = new File [applicationFolders.length];
    for (int i = 0; i < applicationFolders.length; i++) {
      applicationSubfolders [i] = new File(applicationFolders [i], subfolder);
    }
    return applicationSubfolders;
  }

  /**
   * Returns a new file in user preferences folder.
   */
  private File createPreferencesFile(String filePrefix) throws IOException {
    checkPreferencesFolder();
    // Return a new file in preferences folder
    return File.createTempFile(filePrefix, ".pref", getPreferencesFolder());
  }

  /**
   * Creates preferences folder and its sub folders if it doesn't exist.
   */
  private void checkPreferencesFolder() throws IOException {
    File preferencesFolder = getPreferencesFolder();
    // Create preferences folder if it doesn't exist
    if (!preferencesFolder.exists()
        && !preferencesFolder.mkdirs()) {
      throw new IOException("Couldn't create " + preferencesFolder);
    }
    checkPreferencesSubFolder(getLanguageLibrariesPluginFolders());
    checkPreferencesSubFolder(getFurnitureLibrariesPluginFolders());
    checkPreferencesSubFolder(getTexturesLibrariesPluginFolders());
  }

  /**
   * Creates the first folder in the given folders.
   */
  private void checkPreferencesSubFolder(File [] librariesPluginFolders) {
    if (librariesPluginFolders != null
        && librariesPluginFolders.length > 0
        && !librariesPluginFolders [0].exists()) {
      librariesPluginFolders [0].mkdirs();
    }
  }

  /**
   * Deletes from application folder the content files starting by <code>contentPrefix</code>
   * that don't belong to <code>contentURLs</code>.
   */
  private void deleteObsoleteContent(final Set<URL> contentURLs,
                                     final String contentPrefix) throws RecorderException {
    // Search obsolete contents
    File applicationFolder;
    try {
      applicationFolder = getPreferencesFolder();
    } catch (IOException ex) {
      throw new RecorderException("Can't access to application folder");
    }
    File [] obsoleteContentFiles = applicationFolder.listFiles(
        new FileFilter() {
          public boolean accept(File applicationFile) {
            try {
              URL toURL = applicationFile.toURI().toURL();
              return applicationFile.getName().startsWith(contentPrefix)
                 && !contentURLs.contains(toURL);
            } catch (MalformedURLException ex) {
              return false;
            }
          }
        });
    if (obsoleteContentFiles != null) {
      // Delete obsolete contents at program exit to ensure removed contents
      // can still be saved in homes that reference them
      for (File file : obsoleteContentFiles) {
        file.deleteOnExit();
      }
    }
  }

  /**
   * Returns the folder where files depending on preferences are stored.
   */
  private File getPreferencesFolder() throws IOException {
    if (this.preferencesFolder != null) {
      return this.preferencesFolder;
    } else {
      return OperatingSystem.getDefaultApplicationFolder();
    }
  }

  /**
   * Returns default Java preferences for current system user.
   * Caution : This method is called once in constructor so overriding implementations
   * shouldn't be based on the state of their fields.
   */
  protected Preferences getPreferences() {
    if (this.preferences != null) {
      return this.preferences;
    } else {
      return Preferences.userNodeForPackage(FileUserPreferences.class);
    }
  }

  /**
   * Sets which action tip should be ignored.
   */
  @Override
  public void setActionTipIgnored(String actionKey) {
    this.ignoredActionTips.put(actionKey, true);
    super.setActionTipIgnored(actionKey);
  }

  /**
   * Returns whether an action tip should be ignored or not.
   */
  @Override
  public boolean isActionTipIgnored(String actionKey) {
    Boolean ignoredActionTip = this.ignoredActionTips.get(actionKey);
    return ignoredActionTip != null && ignoredActionTip.booleanValue();
  }

  /**
   * Resets the display flag of action tips.
   */
  @Override
  public void resetIgnoredActionTips() {
    for (Iterator<Map.Entry<String, Boolean>> it = this.ignoredActionTips.entrySet().iterator();
         it.hasNext(); ) {
      Entry<String, Boolean> ignoredActionTipEntry = it.next();
      ignoredActionTipEntry.setValue(false);
    }
    super.resetIgnoredActionTips();
  }

  /**
   * Returns <code>true</code> if the language library at the given location exists
   * in the first language libraries folder.
   * @param location the file path of the resource to check
   */
  public boolean languageLibraryExists(String location) throws RecorderException {
    File [] languageLibrariesPluginFolders = getLanguageLibrariesPluginFolders();
    if (languageLibrariesPluginFolders == null
        || languageLibrariesPluginFolders.length == 0) {
      throw new RecorderException("Can't access to language libraries plugin folder");
    } else {
      String libraryFileName = new File(location).getName();
      return new File(languageLibrariesPluginFolders [0], libraryFileName).exists();
    }
  }

  /**
   * Adds <code>languageLibraryPath</code> to the first language libraries folder
   * to make the language library it contains available to supported languages.
   */
  public void addLanguageLibrary(String languageLibraryPath) throws RecorderException {
    try {
      File [] languageLibrariesPluginFolders = getLanguageLibrariesPluginFolders();
      if (languageLibrariesPluginFolders == null
          || languageLibrariesPluginFolders.length == 0) {
        throw new RecorderException("Can't access to language libraries plugin folder");
      }
      copyToLibraryFolder(new File(languageLibraryPath), languageLibrariesPluginFolders [0]);
      updateSupportedLanguages();
    } catch (IOException ex) {
      throw new RecorderException(
          "Can't write " + languageLibraryPath +  " in language libraries plugin folder", ex);
    }
  }

  /**
   * Returns <code>true</code> if the furniture library at the given <code>location</code> exists
   * in the first furniture libraries folder.
   * @param location the file path of the resource to check
   */
  @Override
  public boolean furnitureLibraryExists(String location) throws RecorderException {
    File [] furnitureLibrariesPluginFolders = getFurnitureLibrariesPluginFolders();
    if (furnitureLibrariesPluginFolders == null
        || furnitureLibrariesPluginFolders.length == 0) {
      throw new RecorderException("Can't access to furniture libraries plugin folder");
    } else {
      String libraryFileName = new File(location).getName();
      return new File(furnitureLibrariesPluginFolders [0], libraryFileName).exists();
    }
  }

  /**
   * Adds the file <code>furnitureLibraryPath</code> to the first furniture libraries folder
   * to make the furniture library available to catalog.
   */
  @Override
  public void addFurnitureLibrary(String furnitureLibraryPath) throws RecorderException {
    try {
      File [] furnitureLibrariesPluginFolders = getFurnitureLibrariesPluginFolders();
      if (furnitureLibrariesPluginFolders == null
          || furnitureLibrariesPluginFolders.length == 0) {
        throw new RecorderException("Can't access to furniture libraries plugin folder");
      }
      copyToLibraryFolder(new File(furnitureLibraryPath), furnitureLibrariesPluginFolders [0]);
      updateFurnitureDefaultCatalog(this.catalogsLoader, this.updater);
    } catch (IOException ex) {
      throw new RecorderException(
          "Can't write " + furnitureLibraryPath +  " in furniture libraries plugin folder", ex);
    }
  }

  /**
   * Returns <code>true</code> if the textures library at the given <code>location</code> exists
   * in the first textures libraries folder.
   * @param location the file path of the resource to check
   */
  @Override
  public boolean texturesLibraryExists(String location) throws RecorderException {
    File [] texturesLibrariesPluginFolders = getTexturesLibrariesPluginFolders();
    if (texturesLibrariesPluginFolders == null
        || texturesLibrariesPluginFolders.length == 0) {
      throw new RecorderException("Can't access to textures libraries plugin folder");
    } else {
      String libraryLocation = new File(location).getName();
      return new File(texturesLibrariesPluginFolders [0], libraryLocation).exists();
    }
  }

  /**
   * Adds the file <code>texturesLibraryPath</code> to the first textures libraries folder
   * to make the textures library available to catalog.
   */
  @Override
  public void addTexturesLibrary(String texturesLibraryPath) throws RecorderException {
    try {
      File [] texturesLibrariesPluginFolders = getTexturesLibrariesPluginFolders();
      if (texturesLibrariesPluginFolders == null
          || texturesLibrariesPluginFolders.length == 0) {
        throw new RecorderException("Can't access to textures libraries plugin folder");
      }
      copyToLibraryFolder(new File(texturesLibraryPath), texturesLibrariesPluginFolders [0]);
      updateTexturesDefaultCatalog(this.catalogsLoader, this.updater);
    } catch (IOException ex) {
      throw new RecorderException(
          "Can't write " + texturesLibraryPath +  " in textures libraries plugin folder", ex);
    }
  }

  /**
   * Copies a library file to a folder.
   */
  private void copyToLibraryFolder(File libraryFile, File folder) throws IOException {
    String libraryFileName = libraryFile.getName();
    File destinationFile = new File(folder, libraryFileName);
    if (destinationFile.exists()) {
      // Delete file to reinitialize handlers
      destinationFile.delete();
    }
    InputStream tempIn = null;
    OutputStream tempOut = null;
    try {
      tempIn = new BufferedInputStream(new FileInputStream(libraryFile));
      // Create folder if it doesn't exist
      folder.mkdirs();
      tempOut = new FileOutputStream(destinationFile);
      byte [] buffer = new byte [8192];
      int size;
      while ((size = tempIn.read(buffer)) != -1) {
        tempOut.write(buffer, 0, size);
      }
    } finally {
      if (tempIn != null) {
        tempIn.close();
      }
      if (tempOut != null) {
        tempOut.close();
      }
    }
  }

  /**
   * Returns the libraries available in user preferences.
   * @since 4.0
   */
  @Override
  public List<Library> getLibraries() {
    return Collections.unmodifiableList(new ArrayList<Library>(this.libraries));
  }

  /**
   * Deletes the given <code>libraries</code> and updates user preferences.
   * @since 4.0
   */
  public void deleteLibraries(List<Library> libraries) throws RecorderException {
    boolean updateFurnitureCatalog = false;
    boolean updateTexturesCatalog  = false;
    boolean updateSupportedLanguages = false;
    for (Library library : libraries) {
      if (!new File(library.getLocation()).delete()) {
        throw new RecorderException("Couldn't delete file " + library.getLocation());
      } else {
        if (FURNITURE_LIBRARY_TYPE.equals(library.getType())) {
          updateFurnitureCatalog = true;
        } else if (TEXTURES_LIBRARY_TYPE.equals(library.getType())) {
          updateTexturesCatalog = true;
        }  else if (LANGUAGE_LIBRARY_TYPE.equals(library.getType())) {
          updateSupportedLanguages = true;
        }
      }
    }

    if (updateFurnitureCatalog) {
      updateFurnitureDefaultCatalog(this.catalogsLoader, this.updater);
    }
    if (updateTexturesCatalog) {
      updateTexturesDefaultCatalog(this.catalogsLoader, this.updater);
    }
    if (updateSupportedLanguages) {
      updateSupportedLanguages();
    }
  }

  /**
   * Returns <code>true</code> if the given file <code>library</code> can be deleted.
   * @since 4.0
   */
  public boolean isLibraryDeletable(Library library) {
    return new File(library.getLocation()).canWrite();
  }


  /**
   * A content stored in preferences.
   */
  private static class PreferencesURLContent extends URLContent {
    public PreferencesURLContent(URL url) {
      super(url);
    }
  }


  /**
   * Preferences based on the <code>preferences.xml</code> file
   * stored in a preferences folder.
   */
  private class PortablePreferences extends AbstractPreferences {
    private static final String PREFERENCES_FILE = "preferences.xml";

    private Properties  preferencesProperties;
    private boolean     exist;

    private PortablePreferences() {
      super(null, "");
      this.preferencesProperties = new Properties();
      this.exist = readPreferences();
    }

    public boolean exist() {
      return this.exist;
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
      this.preferencesProperties.clear();
      this.exist = readPreferences();
    }

    @Override
    protected void removeSpi(String key) {
      this.preferencesProperties.remove(key);
    }

    @Override
    protected void putSpi(String key, String value) {
      this.preferencesProperties.put(key, value);
    }

    @Override
    protected String [] keysSpi() throws BackingStoreException {
      return this.preferencesProperties.keySet().toArray(new String [0]);
    }

    @Override
    protected String getSpi(String key) {
      return (String)this.preferencesProperties.get(key);
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
      try {
        writePreferences();
      } catch (IOException ex) {
        throw new BackingStoreException(ex);
      }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
      throw new UnsupportedOperationException();
    }

    @Override
    protected String [] childrenNamesSpi() throws BackingStoreException {
      throw new UnsupportedOperationException();
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
      throw new UnsupportedOperationException();
    }

    /**
     * Reads user preferences.
     */
    private boolean readPreferences() {
      InputStream in = null;
      try {
        in = new FileInputStream(new File(getPreferencesFolder(), PREFERENCES_FILE));
        this.preferencesProperties.loadFromXML(in);
        return true;
      } catch (IOException ex) {
        // Preferences don't exist
        return false;
      } finally {
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException ex) {
          // Let default preferences unchanged
        }
      }
    }

    /**
     * Writes user preferences.
     */
    private void writePreferences() throws IOException {
      OutputStream out = null;
      try {
        checkPreferencesFolder();
        out = new FileOutputStream(new File(getPreferencesFolder(), PREFERENCES_FILE));
        this.preferencesProperties.storeToXML(out, "Portable user preferences 3.0");
      } finally {
        if (out != null) {
          out.close();
          this.exist = true;
        }
      }
    }
  }
}
