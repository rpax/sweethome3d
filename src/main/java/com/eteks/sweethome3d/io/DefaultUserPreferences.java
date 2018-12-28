/*
 * DefaultUserPreferences.java 15 mai 2006
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

import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.HomeDescriptor;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.PatternsCatalog;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Default user preferences.
 * @author Emmanuel Puybaret
 */
public class DefaultUserPreferences extends UserPreferences {
  /**
   * Creates default user preferences read from resource files in the default language.
   */
  public DefaultUserPreferences() {
    this(true, null);
  }

  /**
   * Creates default user preferences read from resource files.
   * @param readCatalogs          if <code>false</code> furniture and texture catalog won't be read
   * @param localizedPreferences  preferences used to read localized resource files
   */
  DefaultUserPreferences(boolean readCatalogs,
                         UserPreferences localizedPreferences) {
    if (localizedPreferences == null) {
      localizedPreferences = this;
    } else {
      setLanguage(localizedPreferences.getLanguage());
    }
    // Read default furniture catalog
    setFurnitureCatalog(readCatalogs
        ? new DefaultFurnitureCatalog(localizedPreferences, (File)null)
        : new FurnitureCatalog());
    // Read default textures catalog
    setTexturesCatalog(readCatalogs
        ? new DefaultTexturesCatalog(localizedPreferences, (File)null)
        : new TexturesCatalog());
    // Build default patterns catalog
    List<TextureImage> patterns = new ArrayList<TextureImage>();
    patterns.add(new DefaultPatternTexture("foreground"));
    patterns.add(new DefaultPatternTexture("reversedHatchUp"));
    patterns.add(new DefaultPatternTexture("reversedHatchDown"));
    patterns.add(new DefaultPatternTexture("reversedCrossHatch"));
    patterns.add(new DefaultPatternTexture("background"));
    patterns.add(new DefaultPatternTexture("hatchUp"));
    patterns.add(new DefaultPatternTexture("hatchDown"));
    patterns.add(new DefaultPatternTexture("crossHatch"));
    PatternsCatalog patternsCatalog = new PatternsCatalog(patterns);
    setPatternsCatalog(patternsCatalog);
    // Read other preferences from resource bundle
    setFurnitureCatalogViewedInTree(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "furnitureCatalogViewedInTree")));
    setNavigationPanelVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "navigationPanelVisible")));
    setAerialViewCenteredOnSelectionEnabled(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "aerialViewCenteredOnSelectionEnabled", "false")));
    setObserverCameraSelectedAtChange(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "observerCameraSelectedAtChange", "true")));
    setUnit(LengthUnit.valueOf(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "unit").toUpperCase(Locale.ENGLISH)));
    setRulersVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "rulersVisible")));
    setGridVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "gridVisible")));
    // Allow furnitureViewedFromTop and roomFloorColoredOrTextured to be different according to the running OS
    String osName = System.getProperty("os.name");
    setFurnitureViewedFromTop(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "furnitureViewedFromTop." + osName,
        localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "furnitureViewedFromTop"))));
    setFurnitureModelIconSize(Integer.parseInt(getOptionalLocalizedString(localizedPreferences, "furnitureModelIconSize", "128")));
    setFloorColoredOrTextured(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "roomFloorColoredOrTextured." + osName,
        localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "roomFloorColoredOrTextured"))));
    setWallPattern(patternsCatalog.getPattern(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "wallPattern")));
    String newWallPattern = localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "wallPattern");
    if (newWallPattern != null) {
      setNewWallPattern(patternsCatalog.getPattern(newWallPattern));
    }
    setNewWallThickness(Float.parseFloat(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "newWallThickness")));
    setNewWallHeight(Float.parseFloat(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "newHomeWallHeight")));
    setNewWallBaseboardThickness(Float.parseFloat(getOptionalLocalizedString(localizedPreferences, "newWallBaseboardThickness", "1")));
    setNewWallBaseboardHeight(Float.parseFloat(getOptionalLocalizedString(localizedPreferences, "newWallBaseboardlHeight", "7")));
    setNewFloorThickness(Float.parseFloat(getOptionalLocalizedString(localizedPreferences, "newFloorThickness", "12")));
    setCheckUpdatesEnabled(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "checkUpdatesEnabled", "false")));
    setAutoSaveDelayForRecovery(Integer.parseInt(getOptionalLocalizedString(localizedPreferences, "autoSaveDelayForRecovery", "0")));
    setCurrency(getOptionalLocalizedString(localizedPreferences, "currency", null));
    setDefaultValueAddedTaxPercentage(new BigDecimal(getOptionalLocalizedString(localizedPreferences, "defaultValueAddedTaxPercentage", "20")));
    setValueAddedTaxEnabled(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "valueAddedTaxEnabled", "false")));
    for (String property : new String [] {"LevelName", "HomePieceOfFurnitureName", "RoomName", "LabelText"}) {
      String autoCompletionStringsList = getOptionalLocalizedString(localizedPreferences, "autoCompletionStrings#" + property, null);
      if (autoCompletionStringsList != null) {
        String [] autoCompletionStrings = autoCompletionStringsList.trim().split(",");
        if (autoCompletionStrings.length > 0) {
          for (int i = 0; i < autoCompletionStrings.length; i++) {
            autoCompletionStrings [i] = autoCompletionStrings [i].trim();
          }
          setAutoCompletionStrings(property, Arrays.asList(autoCompletionStrings));
        }
      }
    }
    List<HomeDescriptor> homeExamples = new ArrayList<HomeDescriptor>();
    int i = 0;
    while (true) {
      try {
        String homeExampleName = localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "homeExampleName#" + ++i);
        homeExamples.add(new HomeDescriptor(homeExampleName,
            getContent(localizedPreferences, "homeExampleContent#" + i, false),
            getContent(localizedPreferences, "homeExampleIcon#" + i, true)));
      } catch (IllegalArgumentException ex) {
        break;
      }
    }
    setHomeExamples(homeExamples);
  }

  /**
   * Returns the content of matching the value of the given content key.
   */
  private Content getContent(UserPreferences localizedPreferences,
                             String contentKey,
                             boolean optional) {
    String contentFile = optional
        ? getOptionalLocalizedString(localizedPreferences, contentKey, null)
        : localizedPreferences.getLocalizedString(DefaultUserPreferences.class, contentKey);
    if (optional && contentFile == null) {
      return null;
    }
    try {
      // Try first to interpret contentFile as an absolute URL
      return new URLContent(new URL(contentFile));
    } catch (MalformedURLException ex) {
      // Otherwise find if it's a resource
      return new ResourceURLContent(DefaultFurnitureCatalog.class, contentFile);
    }
  }

  private String getOptionalLocalizedString(UserPreferences localizedPreferences,
                                            String   resourceKey,
                                            String   defaultValue) {
    try {
      return localizedPreferences.getLocalizedString(DefaultUserPreferences.class, resourceKey);
    } catch (IllegalArgumentException ex) {
      return defaultValue;
    }
  }

  /**
   * Throws an exception because default user preferences can't be written
   * with this class.
   */
  @Override
  public void write() throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't be written");
  }

  /**
   * Throws an exception because default user preferences can't manage language libraries.
   */
  @Override
  public boolean languageLibraryExists(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage language libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional language libraries.
   */
  @Override
  public void addLanguageLibrary(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage language libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional furniture libraries.
   */
  @Override
  public boolean furnitureLibraryExists(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage furniture libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional furniture libraries.
   */
  @Override
  public void addFurnitureLibrary(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage furniture libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage textures libraries.
   */
  @Override
  public boolean texturesLibraryExists(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage textures libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional textures libraries.
   */
  @Override
  public void addTexturesLibrary(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage textures libraries");
  }

  /**
   * Throws an exception because default user preferences don't support libraries.
   * @since 4.0
   */
  @Override
  public List<Library> getLibraries() {
    throw new UnsupportedOperationException();
  }
}
