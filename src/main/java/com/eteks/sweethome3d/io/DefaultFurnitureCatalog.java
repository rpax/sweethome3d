/*
 * DefaultFurnitureCatalog.java 7 avr. 2006
 * 
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import com.eteks.sweethome3d.model.CatalogDoorOrWindow;
import com.eteks.sweethome3d.model.CatalogLight;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.LightSource;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Furniture default catalog read from resources localized in <code>.properties</code> files.
 * @author Emmanuel Puybaret
 */
public class DefaultFurnitureCatalog extends FurnitureCatalog {
  /**
   * The keys of the properties values read in <code>.properties</code> files.
   */
  public enum PropertyKey {
    /**
     * The key for the ID of a piece of furniture (optional). 
     * Two pieces of furniture read in a furniture catalog can't have the same ID
     * and the second one will be ignored.   
     */
    ID("id"),
    /**
     * The key for the name of a piece of furniture (mandatory).
     */
    NAME("name"),
    /**
     * The key for the description of a piece of furniture (optional). 
     * This may give detailed information about a piece of furniture.
     */
    DESCRIPTION("description"),
    /**
     * The key for some additional information associated to a piece of furniture (optional).
     * This information may contain some HTML code or a link to an external web site.
     */
    INFORMATION("information"),
    /**
     * The key for the tags or keywords associated to a piece of furniture (optional). 
     * Tags are separated by commas with possible heading or trailing spaces. 
     */
    TAGS("tags"),
    /**
     * The key for the creation or publication date of a piece of furniture at 
     * <code>yyyy-MM-dd</code> format (optional).
     */
    CREATION_DATE("creationDate"),
    /**
     * The key for the grade of a piece of furniture (optional).
     */
    GRADE("grade"),
    /**
     * The key for the category's name of a piece of furniture (mandatory).
     * A new category with this name will be created if it doesn't exist.
     */
    CATEGORY("category"),
    /**
     * The key for the icon file of a piece of furniture (mandatory). 
     * This icon file can be either the path to an image relative to classpath
     * or an absolute URL. It should be encoded in application/x-www-form-urlencoded  
     * format if needed. 
     */
    ICON("icon"),
    /**
     * The key for the SHA-1 digest of the icon file of a piece of furniture (optional). 
     * This property is used to compare faster catalog resources with the ones of a read home,
     * and should be encoded in Base64.  
     */
    ICON_DIGEST("iconDigest"),
    /**
     * The key for the plan icon file of a piece of furniture (optional).
     * This icon file can be either the path to an image relative to classpath
     * or an absolute URL. It should be encoded in application/x-www-form-urlencoded  
     * format if needed.
     */
    PLAN_ICON("planIcon"),
    /**
     * The key for the SHA-1 digest of the plan icon file of a piece of furniture (optional). 
     * This property is used to compare faster catalog resources with the ones of a read home,
     * and should be encoded in Base64.  
     */
    PLAN_ICON_DIGEST("planIconDigest"),
    /**
     * The key for the 3D model file of a piece of furniture (mandatory).
     * The 3D model file can be either a path relative to classpath
     * or an absolute URL.  It should be encoded in application/x-www-form-urlencoded  
     * format if needed.
     */
    MODEL("model"),
    /**
     * The key for the size of the 3D model of a piece of furniture (optional).
     * If model content is a file this should contain the file size. 
     */
    MODEL_SIZE("modelSize"),
    /**
     * The key for the SHA-1 digest of the 3D model file of a piece of furniture (optional). 
     * This property is used to compare faster catalog resources with the ones of a read home,
     * and should be encoded in Base64.  
     */
    MODEL_DIGEST("modelDigest"),
    /**
     * The key for a piece of furniture with multiple parts (optional).
     * If the value of this key is <code>true</code>, all the files
     * stored in the same folder as the 3D model file (MTL, texture files...)
     * will be considered as being necessary to view correctly the 3D model. 
     */
    MULTI_PART_MODEL("multiPartModel"),
    /**
     * The key for the width in centimeters of a piece of furniture (mandatory).
     */
    WIDTH("width"),
    /**
     * The key for the depth in centimeters of a piece of furniture (mandatory).
     */
    DEPTH("depth"),
    /**
     * The key for the height in centimeters of a piece of furniture (mandatory).
     */
    HEIGHT("height"),
    /**
     * The key for the movability of a piece of furniture (mandatory).
     * If the value of this key is <code>true</code>, the piece of furniture
     * will be considered as a movable piece. 
     */
    MOVABLE("movable"),
    /**
     * The key for the door or window type of a piece of furniture (mandatory).
     * If the value of this key is <code>true</code>, the piece of furniture
     * will be considered as a door or a window. 
     */
    DOOR_OR_WINDOW("doorOrWindow"),
    /**
     * The key for the shape of a door or window used to cut out walls when they intersect it (optional).
     * This shape should be defined with the syntax of the d attribute of a 
     * <a href="http://www.w3.org/TR/SVG/paths.html">SVG path element</a>
     * and should fit in a square spreading from (0, 0) to (1, 1) which will be 
     * scaled afterwards to the real size of the piece. 
     * If not specified, then this shape will be automatically computed from the actual shape of the model.  
     */
    DOOR_OR_WINDOW_CUT_OUT_SHAPE("doorOrWindowCutOutShape"),
    /**
     * The key for the wall thickness in centimeters of a door or a window (optional).
     * By default, a door or a window has the same depth as the wall it belongs to.
     */
    DOOR_OR_WINDOW_WALL_THICKNESS("doorOrWindowWallThickness"),
    /**
     * The key for the distance in centimeters of a door or a window to its wall (optional).
     * By default, this distance is zero.
     */
    DOOR_OR_WINDOW_WALL_DISTANCE("doorOrWindowWallDistance"),
    /**
     * The key for the wall cut out rule of a door or a window (optional, <code>true</code> by default).
     * By default, a door or a window placed on a wall and parallel to it will cut out the both sides of that wall  
     * even if its depth is smaller than the wall thickness or if it intersects only one side of the wall.
     * If the value of this key is <code>false</code>, a door or a window will only dig the wall 
     * at its intersection, and will cut the both sides of a wall only if it intersects both of them.
     */
    DOOR_OR_WINDOW_WALL_CUT_OUT_ON_BOTH_SIDES("doorOrWindowWallCutOutOnBothSides"),
    /**
     * The key for the width/depth deformability of a door or a window (optional, <code>true</code> by default).
     * By default, the depth of a door or a window can be changed and adapted to 
     * the wall thickness where it's placed regardless of its width. To avoid this deformation
     * in the case of open doors, the value of this key can be set to <code>false</code>. 
     * Doors and windows with their width/depth deformability set to <code>false</code> 
     * and their {@link HomeDoorOrWindow#isBoundToWall() bouldToWall} flag set to <code>true</code>
     * will also make a hole in the wall when they are placed whatever their depth. 
     */
    DOOR_OR_WINDOW_WIDTH_DEPTH_DEFORMABLE("doorOrWindowWidthDepthDeformable"),
    /**
     * The key for the sash axis distance(s) of a door or a window along X axis (optional).
     * If a door or a window has more than one sash, the values of each sash should be 
     * separated by spaces.  
     */
    DOOR_OR_WINDOW_SASH_X_AXIS("doorOrWindowSashXAxis"),
    /**
     * The key for the sash axis distance(s) of a door or a window along Y axis 
     * (mandatory if sash axis distance along X axis is defined).
     */
    DOOR_OR_WINDOW_SASH_Y_AXIS("doorOrWindowSashYAxis"),
    /**
     * The key for the sash width(s) of a door or a window  
     * (mandatory if sash axis distance along X axis is defined).
     */
    DOOR_OR_WINDOW_SASH_WIDTH("doorOrWindowSashWidth"),
    /**
     * The key for the sash start angle(s) of a door or a window  
     * (mandatory if sash axis distance along X axis is defined).
     */
    DOOR_OR_WINDOW_SASH_START_ANGLE("doorOrWindowSashStartAngle"),
    /**
     * The key for the sash end angle(s) of a door or a window  
     * (mandatory if sash axis distance along X axis is defined).
     */
    DOOR_OR_WINDOW_SASH_END_ANGLE("doorOrWindowSashEndAngle"),
    /**
     * The key for the abscissa(s) of light sources in a light (optional).
     * If a light has more than one light source, the values of each light source should 
     * be separated by spaces.
     */
    LIGHT_SOURCE_X("lightSourceX"),
    /**
     * The key for the ordinate(s) of light sources in a light (mandatory if light source abscissa is defined).
     */
    LIGHT_SOURCE_Y("lightSourceY"),
    /**
     * The key for the elevation(s) of light sources in a light (mandatory if light source abscissa is defined).
     */
    LIGHT_SOURCE_Z("lightSourceZ"),
    /**
     * The key for the color(s) of light sources in a light (mandatory if light source abscissa is defined).
     */
    LIGHT_SOURCE_COLOR("lightSourceColor"),
    /**
     * The key for the diameter(s) of light sources in a light (optional).
     */
    LIGHT_SOURCE_DIAMETER("lightSourceDiameter"),
    /**
     * The key for the shape used to cut out upper levels when they intersect with a piece   
     * like a staircase (optional). This shape should be defined with the syntax of 
     * the d attribute of a <a href="http://www.w3.org/TR/SVG/paths.html">SVG path element</a>
     * and should fit in a square spreading from (0, 0) to (1, 1) which will be scaled afterwards 
     * to the real size of the piece. 
     */
    STAIRCASE_CUT_OUT_SHAPE("staircaseCutOutShape"),
    /**
     * The key for the elevation in centimeters of a piece of furniture (optional).
     */
    ELEVATION("elevation"),
    /**
     * The key for the preferred elevation (from the bottom of a piece) at which should be placed  
     * an object dropped on a piece (optional). A negative value means that the piece should be ignored
     * when an object is dropped on it. By default, this elevation is equal to its height. 
     */
    DROP_ON_TOP_ELEVATION("dropOnTopElevation"),
    /**
     * The key for the transformation matrix values applied to a piece of furniture (optional).
     * If the 3D model of a piece of furniture isn't correctly oriented, 
     * the value of this key should give the 9 values of the transformation matrix 
     * that will orient it correctly.  
     */
    MODEL_ROTATION("modelRotation"),
    /**
     * The key for the creator of a piece of furniture (optional).
     * By default, creator is eTeks.
     */
    CREATOR("creator"),
    /**
     * The key for the resizability of a piece of furniture (optional, <code>true</code> by default).
     * If the value of this key is <code>false</code>, the piece of furniture
     * will be considered as a piece with a fixed size. 
     */
    RESIZABLE("resizable"),
    /**
     * The key for the deformability of a piece of furniture (optional, <code>true</code> by default).
     * If the value of this key is <code>false</code>, the piece of furniture
     * will be considered as a piece that should always keep its proportions when resized. 
     */
    DEFORMABLE("deformable"),
    /**
     * The key for the texturable capability of a piece of furniture (optional, <code>true</code> by default).
     * If the value of this key is <code>false</code>, the piece of furniture
     * will be considered as a piece that will always keep the same color or texture. 
     */
    TEXTURABLE("texturable"),
    /**
     * The key for the ability of a piece of furniture to rotate around a horizontal axis (optional, <code>true</code> by default).
     * If the value of this key is <code>false</code>, the piece of furniture
     * will be considered as a piece that can't be horizontally rotated. 
     */
    HORIZONTALLY_ROTATABLE("horizontallyRotatable"),
    /**
     * The key for the price of a piece of furniture (optional).
     */
    PRICE("price"),
    /**
     * The key for the VAT percentage of a piece of furniture (optional).
     */
    VALUE_ADDED_TAX_PERCENTAGE("valueAddedTaxPercentage"),
    /**
     * The key for the currency ISO 4217 code of the price of a piece of furniture (optional).
     */
    CURRENCY("currency");
    
    private String keyPrefix;

    private PropertyKey(String keyPrefix) {
      this.keyPrefix = keyPrefix;
    }
    
    /**
     * Returns the key for the piece property of the given index.
     */
    public String getKey(int pieceIndex) {
      return keyPrefix + "#" + pieceIndex;
    }

    /**
     * Returns the <code>PropertyKey</code> instance matching the given key prefix.
     */
    public static PropertyKey fromPrefix(String keyPrefix) {
      for (PropertyKey key : PropertyKey.values()) {
        if (key.keyPrefix.equals(keyPrefix)) {
          return key;
        }
      }
      throw new IllegalArgumentException("Unknow prefix " + keyPrefix);
    }
  }

  /**
   * The name of <code>.properties</code> family files in plugin furniture catalog files. 
   */
  public static final String PLUGIN_FURNITURE_CATALOG_FAMILY = "PluginFurnitureCatalog";
  
  private static final String CONTRIBUTED_FURNITURE_CATALOG_FAMILY = "ContributedFurnitureCatalog";
  private static final String ADDITIONAL_FURNITURE_CATALOG_FAMILY  = "AdditionalFurnitureCatalog";
  
  private static Map<ResourceBundle, Map<Integer, List<String>>> furnitureAdditionalKeys = new WeakHashMap<ResourceBundle, Map<Integer,List<String>>>();
  
  private List<Library> libraries = new ArrayList<Library>();
  
  /**
   * Creates a default furniture catalog read from resources in the package of this class.
   */
  public DefaultFurnitureCatalog() {
    this((File)null);
  }
  
  /**
   * Creates a default furniture catalog read from resources and   
   * furniture plugin folder if <code>furniturePluginFolder</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(File furniturePluginFolder) {
    this(null, furniturePluginFolder);
  }
  
  /**
   * Creates a default furniture catalog read from resources and   
   * furniture plugin folder if <code>furniturePluginFolder</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(final UserPreferences preferences, 
                                 File furniturePluginFolder) {
    this(preferences, furniturePluginFolder == null ? null : new File [] {furniturePluginFolder});
  }
  
  /**
   * Creates a default furniture catalog read from resources and   
   * furniture plugin folders if <code>furniturePluginFolders</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(final UserPreferences preferences, 
                                 File [] furniturePluginFolders) {
    Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter = 
        new HashMap<FurnitureCategory, Map<CatalogPieceOfFurniture,Integer>>();
    List<String> identifiedFurniture = new ArrayList<String>();
    
    readDefaultFurnitureCatalogs(preferences, furnitureHomonymsCounter, identifiedFurniture);
    
    if (furniturePluginFolders != null) {
      for (File furniturePluginFolder : furniturePluginFolders) {
        // Try to load sh3f files from furniture plugin folder
        File [] pluginFurnitureCatalogFiles = furniturePluginFolder.listFiles(new FileFilter () {
          public boolean accept(File pathname) {
            return pathname.isFile();
          }
        });
        
        if (pluginFurnitureCatalogFiles != null) {
          // Treat furniture catalog files in reverse order of their version
          Arrays.sort(pluginFurnitureCatalogFiles, Collections.reverseOrder(OperatingSystem.getFileVersionComparator()));
          for (File pluginFurnitureCatalogFile : pluginFurnitureCatalogFiles) {
            // Try to load the properties file describing furniture catalog from current file  
            readPluginFurnitureCatalog(pluginFurnitureCatalogFile, identifiedFurniture);
          }
        }
      }
    }
  }

  /**
   * Creates a default furniture catalog read only from resources in the given URLs.
   */
  public DefaultFurnitureCatalog(URL [] pluginFurnitureCatalogUrls) {
    this(pluginFurnitureCatalogUrls, null);
  }
  
  /**
   * Creates a default furniture catalog read only from resources in the given URLs 
   * or in the classpath if the security manager doesn't allow to create class loaders.
   * Model and icon URLs will built from <code>furnitureResourcesUrlBase</code> if it isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(URL [] pluginFurnitureCatalogUrls,
                                 URL    furnitureResourcesUrlBase) {
    List<String> identifiedFurniture = new ArrayList<String>();
    try {
      SecurityManager securityManager = System.getSecurityManager();
      if (securityManager != null) {
        securityManager.checkCreateClassLoader();
      }

      for (URL pluginFurnitureCatalogUrl : pluginFurnitureCatalogUrls) {
        try {        
          ResourceBundle resource = ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault(), 
              new URLContentClassLoader(pluginFurnitureCatalogUrl));
          this.libraries.add(0, new DefaultLibrary(pluginFurnitureCatalogUrl.toExternalForm(), 
              UserPreferences.FURNITURE_LIBRARY_TYPE, resource));
          readFurniture(resource, pluginFurnitureCatalogUrl, furnitureResourcesUrlBase, identifiedFurniture);
        } catch (MissingResourceException ex) {
          // Ignore malformed furniture catalog
        } catch (IllegalArgumentException ex) {
          // Ignore malformed furniture catalog
        }
      }
    } catch (AccessControlException ex) {
      // Use only furniture accessible through classpath
      ResourceBundle resource = ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault());
      readFurniture(resource, null, furnitureResourcesUrlBase, identifiedFurniture);
    }
  }
  
  /**
   * Returns the furniture libraries at initialization.
   * @since 4.0 
   */
  public List<Library> getLibraries() {
    return Collections.unmodifiableList(this.libraries);
  }

  private static final Map<File,URL> pluginFurnitureCatalogUrlUpdates = new HashMap<File, URL>(); 
  
  /**
   * Reads plug-in furniture catalog from the <code>pluginFurnitureCatalogFile</code> file. 
   */
  private void readPluginFurnitureCatalog(File pluginFurnitureCatalogFile,
                                          List<String> identifiedFurniture) {
    try {
      final URL pluginFurnitureCatalogUrl;
      long urlModificationDate = pluginFurnitureCatalogFile.lastModified();
      URL urlUpdate = pluginFurnitureCatalogUrlUpdates.get(pluginFurnitureCatalogFile);
      if (pluginFurnitureCatalogFile.canWrite()
          && (urlUpdate == null 
              || urlUpdate.openConnection().getLastModified() < urlModificationDate)) {
        // Copy updated resource URL content to a temporary file to ensure furniture added to home can safely 
        // reference any file of the catalog file even if its content is changed afterwards
        TemporaryURLContent contentCopy = TemporaryURLContent.copyToTemporaryURLContent(new URLContent(pluginFurnitureCatalogFile.toURI().toURL()));
        URL temporaryFurnitureCatalogUrl = contentCopy.getURL();
        pluginFurnitureCatalogUrlUpdates.put(pluginFurnitureCatalogFile, temporaryFurnitureCatalogUrl);
        pluginFurnitureCatalogUrl = temporaryFurnitureCatalogUrl;
      } else if (urlUpdate != null) {
        pluginFurnitureCatalogUrl = urlUpdate;
      } else {
        pluginFurnitureCatalogUrl = pluginFurnitureCatalogFile.toURI().toURL();
      }
      
      final ClassLoader urlLoader = new URLContentClassLoader(pluginFurnitureCatalogUrl);
      ResourceBundle resourceBundle = ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault(), urlLoader);
      this.libraries.add(0, new DefaultLibrary(pluginFurnitureCatalogFile.getCanonicalPath(), 
          UserPreferences.FURNITURE_LIBRARY_TYPE, resourceBundle));
      readFurniture(resourceBundle, pluginFurnitureCatalogUrl, null, identifiedFurniture);
    } catch (MissingResourceException ex) {
      // Ignore malformed furniture catalog
    } catch (IllegalArgumentException ex) {
      // Ignore malformed furniture catalog
    } catch (IOException ex) {
      // Ignore unaccessible catalog
    }
  }
  
  /**
   * Reads the default furniture described in properties files accessible through classpath.
   */
  private void readDefaultFurnitureCatalogs(UserPreferences preferences,
                                            Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter,
                                            List<String> identifiedFurniture) {
    // Try to load com.eteks.sweethome3d.io.DefaultFurnitureCatalog property file from classpath 
    String defaultFurnitureCatalogFamily = DefaultFurnitureCatalog.class.getName();
    readFurnitureCatalog(defaultFurnitureCatalogFamily, 
        preferences, furnitureHomonymsCounter, identifiedFurniture);
    
    // Try to load com.eteks.sweethome3d.io.ContributedFurnitureCatalog property file from classpath 
    String classPackage = defaultFurnitureCatalogFamily.substring(0, defaultFurnitureCatalogFamily.lastIndexOf("."));
    readFurnitureCatalog(classPackage + "." + CONTRIBUTED_FURNITURE_CATALOG_FAMILY, 
        preferences, furnitureHomonymsCounter, identifiedFurniture);
    
    // Try to load com.eteks.sweethome3d.io.AdditionalFurnitureCatalog property file from classpath
    readFurnitureCatalog(classPackage + "." + ADDITIONAL_FURNITURE_CATALOG_FAMILY, 
        preferences, furnitureHomonymsCounter, identifiedFurniture);
  }
  
  /**
   * Reads furniture of a given catalog family from resources.
   */
  private void readFurnitureCatalog(final String furnitureCatalogFamily,
                                    final UserPreferences preferences,
                                    Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter,
                                    List<String> identifiedFurniture) {
    ResourceBundle resource;
    if (preferences != null) {
      // Adapt getLocalizedString to ResourceBundle
      resource = new ResourceBundle() {
          @Override
          protected Object handleGetObject(String key) {
            try {
              return preferences.getLocalizedString(furnitureCatalogFamily, key);
            } catch (IllegalArgumentException ex) {
              throw new MissingResourceException("Unknown key " + key, 
                  furnitureCatalogFamily + "_" + Locale.getDefault(), key);
            }
          }
          
          @Override
          public Enumeration<String> getKeys() {
            final Iterator<String> keys = preferences.getLocalizedStringKeys(furnitureCatalogFamily);
            return new Enumeration<String>() {
                public boolean hasMoreElements() {
                  return keys.hasNext();
                }
  
                public String nextElement() {
                  return keys.next();
                }
              };
          }
        };
    } else {
      try {
        resource = ResourceBundle.getBundle(furnitureCatalogFamily);
      } catch (MissingResourceException ex) {
        return;
      }
    }
    readFurniture(resource, null, null, identifiedFurniture);
  }
  
  /**
   * Reads each piece of furniture described in <code>resource</code> bundle.
   * Resources described in piece properties will be loaded from <code>furnitureCatalogUrl</code> 
   * if it isn't <code>null</code> or relative to <code>furnitureResourcesUrlBase</code>. 
   */
  private void readFurniture(ResourceBundle resource, 
                             URL furnitureCatalogUrl,
                             URL furnitureResourcesUrlBase,
                             List<String> identifiedFurniture) {
    int index = 0;
    while (true) {
      // Ignore furniture with a key ignored# set at true
      String ignored;
      try {
        ignored = resource.getString("ignored#" + (++index));
      } catch (MissingResourceException ex) {
        // Not ignored
        ignored = null;
      }
      
      if (ignored == null || !Boolean.parseBoolean(ignored)) {
        CatalogPieceOfFurniture piece = ignored == null
            ? readPieceOfFurniture(resource, index, furnitureCatalogUrl, furnitureResourcesUrlBase)
            : null;
        if (piece == null) {
          // Read furniture until no data is found at current index
          break;
        } else {
          if (piece.getId() != null) {
            // Take into account only furniture that have an ID
            if (identifiedFurniture.contains(piece.getId())) {
              continue;
            } else {
              // Add id to identifiedFurniture to be sure that two pieces with a same ID
              // won't be added twice to furniture catalog (in case they are cited twice
              // in different furniture properties files)
              identifiedFurniture.add(piece.getId());
            }
          } 
          FurnitureCategory pieceCategory = readFurnitureCategory(resource, index);
          add(pieceCategory, piece);
        }
      } 
    } 
  }

  /**
   * Returns the properties of the piece at the given <code>index</code> 
   * different from default properties.
   */
  protected Map<String, String> getAdditionalProperties(ResourceBundle resource, 
                                                        int index) {
    // Get all property keys of furniture different from default properties
    Map<Integer, List<String>> catalogAdditionalKeys = furnitureAdditionalKeys.get(resource);
    if (catalogAdditionalKeys == null) {
      catalogAdditionalKeys = new HashMap<Integer, List<String>>();
      furnitureAdditionalKeys.put(resource, catalogAdditionalKeys);
      for (Enumeration<String> keys = resource.getKeys(); keys.hasMoreElements(); ) {
        String key = keys.nextElement();
        int sharpIndex = key.lastIndexOf('#');
        if (sharpIndex != -1
            && sharpIndex + 1 < key.length()) {
          try {
            int pieceIndex = Integer.valueOf(key.substring(sharpIndex + 1));
            String propertyKey = key.substring(0, sharpIndex);
            if (!isDefaultProperty(propertyKey)) {
              List<String> otherKeys = catalogAdditionalKeys.get(pieceIndex);
              if (otherKeys == null) {
                otherKeys = new ArrayList<String>();
                catalogAdditionalKeys.put(pieceIndex, otherKeys);
              }
              otherKeys.add(propertyKey);
            }
          } catch (NumberFormatException ex) {
            // Not a key that matches a piece of furniture
          }
        }
      }
    }
    
    List<String> additionalKeys = catalogAdditionalKeys.get(index);
    if (additionalKeys != null) {
      Map<String, String> additionalProperties;
      int propertiesCount = additionalKeys.size();
      if (propertiesCount == 1) {
        String key = additionalKeys.get(0);
        additionalProperties = Collections.singletonMap(key, resource.getString(key + "#" + index)); 
      } else {
        additionalProperties = new HashMap<String, String>(propertiesCount);
        for (int i = 0; i < propertiesCount; i++) {
          String key = additionalKeys.get(i);
          additionalProperties.put(key, resource.getString(key + "#" + index));
        }
      }
      return Collections.unmodifiableMap(additionalProperties);
    } else {
      return Collections.emptyMap();
    }
  }
  
  /**
   * Returns <code>true</code> if the given parameter is the prefix of a default property 
   * used as an attribute of a piece of furniture.
   */
  protected boolean isDefaultProperty(String keyPrefix) {
    try {
      PropertyKey.fromPrefix(keyPrefix);
      return true;
    } catch (IllegalArgumentException ex) {
      return "ignored".equals(keyPrefix);
    }
  }

  /**
   * Returns the piece of furniture at the given <code>index</code> of a 
   * localized <code>resource</code> bundle. 
   * @param resource             a resource bundle 
   * @param index                the index of the read piece
   * @param furnitureCatalogUrl  the URL from which piece resources will be loaded 
   *            or <code>null</code> if it's read from current classpath.
   * @param furnitureResourcesUrlBase the URL used as a base to build the URL to piece resources  
   *            or <code>null</code> if it's read from current classpath or <code>furnitureCatalogUrl</code>
   * @return the read piece of furniture or <code>null</code> if the piece at the given index doesn't exist.
   * @throws MissingResourceException if mandatory keys are not defined.
   */
  protected CatalogPieceOfFurniture readPieceOfFurniture(ResourceBundle resource, 
                                                         int index, 
                                                         URL furnitureCatalogUrl,
                                                         URL furnitureResourcesUrlBase) {
    String name = null;
    try {
      name = resource.getString(PropertyKey.NAME.getKey(index));
    } catch (MissingResourceException ex) {
      // Return null if key name# doesn't exist
      return null;
    }
    String id = getOptionalString(resource, PropertyKey.ID.getKey(index), null);
    String description = getOptionalString(resource, PropertyKey.DESCRIPTION.getKey(index), null);
    String information = getOptionalString(resource, PropertyKey.INFORMATION.getKey(index), null);
    String tagsString = getOptionalString(resource, PropertyKey.TAGS.getKey(index), null);
    String [] tags;
    if (tagsString != null) {
      tags = tagsString.split("\\s*,\\s*");
    } else {
      tags = new String [0];
    }
    String creationDateString = getOptionalString(resource, PropertyKey.CREATION_DATE.getKey(index), null);
    Long creationDate = null;
    if (creationDateString != null) {
      try {
        creationDate = new SimpleDateFormat("yyyy-MM-dd").parse(creationDateString).getTime();
      } catch (ParseException ex) {
        throw new IllegalArgumentException("Can't parse date "+ creationDateString, ex);
      }
    }
    String gradeString = getOptionalString(resource, PropertyKey.GRADE.getKey(index), null);
    Float grade = null;
    if (gradeString != null) {
      grade = Float.valueOf(gradeString);
    }
    Content icon  = getContent(resource, PropertyKey.ICON.getKey(index), PropertyKey.ICON_DIGEST.getKey(index), 
        furnitureCatalogUrl, furnitureResourcesUrlBase, false, false);
    Content planIcon = getContent(resource, PropertyKey.PLAN_ICON.getKey(index), PropertyKey.PLAN_ICON_DIGEST.getKey(index), 
        furnitureCatalogUrl, furnitureResourcesUrlBase, false, true);
    boolean multiPartModel = getOptionalBoolean(resource, PropertyKey.MULTI_PART_MODEL.getKey(index), false);
    Content model = getContent(resource, PropertyKey.MODEL.getKey(index), PropertyKey.MODEL_DIGEST.getKey(index), 
        furnitureCatalogUrl, furnitureResourcesUrlBase, multiPartModel, false);
    float width = Float.parseFloat(resource.getString(PropertyKey.WIDTH.getKey(index)));
    float depth = Float.parseFloat(resource.getString(PropertyKey.DEPTH.getKey(index)));
    float height = Float.parseFloat(resource.getString(PropertyKey.HEIGHT.getKey(index)));
    float elevation = getOptionalFloat(resource, PropertyKey.ELEVATION.getKey(index), 0);
    float dropOnTopElevation = getOptionalFloat(resource, PropertyKey.DROP_ON_TOP_ELEVATION.getKey(index), height) / height;
    boolean movable = Boolean.parseBoolean(resource.getString(PropertyKey.MOVABLE.getKey(index)));
    boolean doorOrWindow = Boolean.parseBoolean(resource.getString(PropertyKey.DOOR_OR_WINDOW.getKey(index)));
    String staircaseCutOutShape = getOptionalString(resource, PropertyKey.STAIRCASE_CUT_OUT_SHAPE.getKey(index), null);     
    float [][] modelRotation = getModelRotation(resource, PropertyKey.MODEL_ROTATION.getKey(index));
    // By default creator is eTeks
    String modelSizeString = getOptionalString(resource, PropertyKey.MODEL_SIZE.getKey(index), null);
    Long modelSize = null;
    if (modelSizeString != null) {
      modelSize = Long.parseLong(modelSizeString);
    } else {
      // Request model size (this should be avoided when content is stored on a server)
      modelSize = ContentDigestManager.getInstance().getContentSize(model);
    }
    String creator = getOptionalString(resource, PropertyKey.CREATOR.getKey(index), null);
    boolean resizable = getOptionalBoolean(resource, PropertyKey.RESIZABLE.getKey(index), true);
    boolean deformable = getOptionalBoolean(resource, PropertyKey.DEFORMABLE.getKey(index), true);
    boolean texturable = getOptionalBoolean(resource, PropertyKey.TEXTURABLE.getKey(index), true);
    boolean horizontallyRotatable = getOptionalBoolean(resource, PropertyKey.HORIZONTALLY_ROTATABLE.getKey(index), true);
    BigDecimal price = null;
    try {
      price = new BigDecimal(resource.getString(PropertyKey.PRICE.getKey(index)));
    } catch (MissingResourceException ex) {
      // By default price is null
    }
    BigDecimal valueAddedTaxPercentage = null;
    try {
      valueAddedTaxPercentage = new BigDecimal(resource.getString(PropertyKey.VALUE_ADDED_TAX_PERCENTAGE.getKey(index)));
    } catch (MissingResourceException ex) {
      // By default price is null
    }
    String currency = getOptionalString(resource, PropertyKey.CURRENCY.getKey(index), null);

    Map<String, String> additionalProperties = getAdditionalProperties(resource, index);
    
    if (doorOrWindow) {
      String doorOrWindowCutOutShape = getOptionalString(resource, PropertyKey.DOOR_OR_WINDOW_CUT_OUT_SHAPE.getKey(index), null);     
      float wallThicknessPercentage = getOptionalFloat(
          resource, PropertyKey.DOOR_OR_WINDOW_WALL_THICKNESS.getKey(index), depth) / depth;
      float wallDistancePercentage = getOptionalFloat(
          resource, PropertyKey.DOOR_OR_WINDOW_WALL_DISTANCE.getKey(index), 0) / depth;
      boolean wallCutOutOnBothSides = getOptionalBoolean(
          resource, PropertyKey.DOOR_OR_WINDOW_WALL_CUT_OUT_ON_BOTH_SIDES.getKey(index), true);
      boolean widthDepthDeformable = getOptionalBoolean(
          resource, PropertyKey.DOOR_OR_WINDOW_WIDTH_DEPTH_DEFORMABLE.getKey(index), true);
      Sash [] sashes = getDoorOrWindowSashes(resource, index, width, depth);
      return new CatalogDoorOrWindow(id, name, description, information, tags, creationDate, grade, 
          icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, 
          doorOrWindowCutOutShape, wallThicknessPercentage, wallDistancePercentage, wallCutOutOnBothSides, widthDepthDeformable, sashes,
          modelRotation, false, modelSize, creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, currency, additionalProperties);
    } else {
      LightSource [] lightSources = getLightSources(resource, index, width, depth, height);
      if (lightSources != null) {
        return new CatalogLight(id, name, description, information, tags, creationDate, grade, 
            icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, 
            lightSources, staircaseCutOutShape, modelRotation, false, modelSize, creator, 
            resizable, deformable, texturable, horizontallyRotatable, price, valueAddedTaxPercentage, currency, additionalProperties);
      } else {
        return new CatalogPieceOfFurniture(id, name, description, information, tags, creationDate, grade, 
            icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, 
            staircaseCutOutShape, modelRotation, false, modelSize, creator, 
            resizable, deformable, texturable, horizontallyRotatable, price, valueAddedTaxPercentage, currency, additionalProperties);
      }
    }
  }
  
  /**
   * Returns the furniture category of a piece at the given <code>index</code> of a 
   * localized <code>resource</code> bundle. 
   * @throws MissingResourceException if mandatory keys are not defined.
   */
  protected FurnitureCategory readFurnitureCategory(ResourceBundle resource, int index) {
    String category = resource.getString(PropertyKey.CATEGORY.getKey(index));
    return new FurnitureCategory(category);
  }
    
  /**
   * Returns a valid content instance from the resource file or URL value of key.
   * @param resource a resource bundle
   * @param contentKey        the key of a resource content file
   * @param contentDigestKey  the key of the digest of a resource content file
   * @param furnitureUrl the URL of the file containing the target resource if it's not <code>null</code> 
   * @param resourceUrlBase the URL used as a base to build the URL to content file  
   *            or <code>null</code> if it's read from current classpath or <code>furnitureCatalogUrl</code>.
   * @param multiPartModel if <code>true</code> the resource is a multi part resource stored 
   *                 in a folder with other required resources
   * @throws IllegalArgumentException if the file value doesn't match a valid resource or URL.
   */
  private Content getContent(ResourceBundle resource, 
                             String contentKey, 
                             String contentDigestKey,
                             URL furnitureUrl,
                             URL resourceUrlBase, 
                             boolean multiPartModel,
                             boolean optional) {
    String contentFile = optional
        ? getOptionalString(resource, contentKey, null)
        : resource.getString(contentKey);
    if (optional && contentFile == null) {
      return null;
    }
    URLContent content;
    try {
      // Try first to interpret contentFile as an absolute URL 
      // or an URL relative to resourceUrlBase if it's not null
      URL url;
      if (resourceUrlBase == null) {
        url = new URL(contentFile);
      } else {
        url = contentFile.startsWith("?") 
            ? new URL(resourceUrlBase + contentFile)
            : new URL(resourceUrlBase, contentFile);
        if (contentFile.indexOf('!') >= 0 && !contentFile.startsWith("jar:")) {
          url = new URL("jar:" + url);
        }
      }
      content = new URLContent(url);
    } catch (MalformedURLException ex) {
      if (furnitureUrl == null) {
        // Otherwise find if it's a resource
        content = new ResourceURLContent(DefaultFurnitureCatalog.class, contentFile, multiPartModel);
      } else {
        try {
          content = new ResourceURLContent(new URL("jar:" + furnitureUrl + "!" + contentFile), multiPartModel);
        } catch (MalformedURLException ex2) {
          throw new IllegalArgumentException("Invalid URL", ex2);
        }
      }
    }
    
    // Store content digest if it exists
    // Except in special cases like URL content in applets where it might avoid to download content  
    // to compute its digest, it's not recommended to store digests in sh3f and imported files. 
    // Missing digests will be computed on demand, ensuring it will be updated in case content is damaged
    String contentDigest = getOptionalString(resource, contentDigestKey, null);
    if (contentDigest != null && contentDigest.length() > 0) {
      try {        
        ContentDigestManager.getInstance().setContentDigest(content, Base64.decode(contentDigest));
      } catch (IOException ex) {
        // Ignore wrong digest
      }
    }
    return content;
  }
  
  /**
   * Returns model rotation parsed from key value.
   */
  private float [][] getModelRotation(ResourceBundle resource, String key) {
    try {
      String modelRotationString = resource.getString(key);
      String [] values = modelRotationString.split(" ", 9);
      
      if (values.length == 9) {
        return new float [][] {{Float.parseFloat(values [0]), 
                                Float.parseFloat(values [1]), 
                                Float.parseFloat(values [2])}, 
                               {Float.parseFloat(values [3]), 
                                Float.parseFloat(values [4]), 
                                Float.parseFloat(values [5])}, 
                               {Float.parseFloat(values [6]), 
                                Float.parseFloat(values [7]), 
                                Float.parseFloat(values [8])}};
      } else {
        return null;
      }
    } catch (MissingResourceException ex) {
      return null;
    } catch (NumberFormatException ex) {
      return null;
    }
  }
  
  /**
   * Returns optional door or windows sashes.
   */
  private Sash [] getDoorOrWindowSashes(ResourceBundle resource, int index, 
                                        float doorOrWindowWidth, 
                                        float doorOrWindowDepth) throws MissingResourceException {
    Sash [] sashes;
    String sashXAxisString = getOptionalString(resource, PropertyKey.DOOR_OR_WINDOW_SASH_X_AXIS.getKey(index), null);
    if (sashXAxisString != null) {
      String [] sashXAxisValues = sashXAxisString.split(" ");
      // If doorOrWindowHingesX#i key exists the 3 other keys with the same count of numbers must exist too
      String [] sashYAxisValues = resource.getString(PropertyKey.DOOR_OR_WINDOW_SASH_Y_AXIS.getKey(index)).split(" ");
      if (sashYAxisValues.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + PropertyKey.DOOR_OR_WINDOW_SASH_Y_AXIS.getKey(index) + " key");
      }
      String [] sashWidths = resource.getString(PropertyKey.DOOR_OR_WINDOW_SASH_WIDTH.getKey(index)).split(" ");
      if (sashWidths.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + PropertyKey.DOOR_OR_WINDOW_SASH_WIDTH.getKey(index) + " key");
      }
      String [] sashStartAngles = resource.getString(PropertyKey.DOOR_OR_WINDOW_SASH_START_ANGLE.getKey(index)).split(" ");
      if (sashStartAngles.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + PropertyKey.DOOR_OR_WINDOW_SASH_START_ANGLE.getKey(index) + " key");
      }
      String [] sashEndAngles = resource.getString(PropertyKey.DOOR_OR_WINDOW_SASH_END_ANGLE.getKey(index)).split(" ");
      if (sashEndAngles.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + PropertyKey.DOOR_OR_WINDOW_SASH_END_ANGLE.getKey(index) + " key");
      }
      
      sashes = new Sash [sashXAxisValues.length];
      for (int i = 0; i < sashes.length; i++) {
        // Create the matching sash, converting cm to percentage of width or depth, and degrees to radians
        sashes [i] = new Sash(Float.parseFloat(sashXAxisValues [i]) / doorOrWindowWidth, 
            Float.parseFloat(sashYAxisValues [i]) / doorOrWindowDepth, 
            Float.parseFloat(sashWidths [i]) / doorOrWindowWidth, 
            (float)Math.toRadians(Float.parseFloat(sashStartAngles [i])), 
            (float)Math.toRadians(Float.parseFloat(sashEndAngles [i])));
      }
    } else {
      sashes = new Sash [0];
    }
    
    return sashes;
  }

  /**
   * Returns optional light sources.
   */
  private LightSource [] getLightSources(ResourceBundle resource, int index, 
                                         float lightWidth, 
                                         float lightDepth,
                                         float lightHeight) throws MissingResourceException {
    LightSource [] lightSources = null;
    String lightSourceXString = getOptionalString(resource, PropertyKey.LIGHT_SOURCE_X.getKey(index), null);
    if (lightSourceXString != null) {
      String [] lightSourceX = lightSourceXString.split(" ");
      // If doorOrWindowHingesX#i key exists the 3 other keys with the same count of numbers must exist too
      String [] lightSourceY = resource.getString(PropertyKey.LIGHT_SOURCE_Y.getKey(index)).split(" ");
      if (lightSourceY.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + PropertyKey.LIGHT_SOURCE_Y.getKey(index) + " key");
      }
      String [] lightSourceZ = resource.getString(PropertyKey.LIGHT_SOURCE_Z.getKey(index)).split(" ");
      if (lightSourceZ.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + PropertyKey.LIGHT_SOURCE_Z.getKey(index) + " key");
      }
      String [] lightSourceColors = resource.getString(PropertyKey.LIGHT_SOURCE_COLOR.getKey(index)).split(" ");
      if (lightSourceColors.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + PropertyKey.LIGHT_SOURCE_COLOR.getKey(index) + " key");
      }
      String lightSourceDiametersString = getOptionalString(resource, PropertyKey.LIGHT_SOURCE_DIAMETER.getKey(index), null);
      String [] lightSourceDiameters;
      if (lightSourceDiametersString != null) {
        lightSourceDiameters = lightSourceDiametersString.split(" ");
        if (lightSourceDiameters.length != lightSourceX.length) {
          throw new IllegalArgumentException(
              "Expected " + lightSourceX.length + " values in " + PropertyKey.LIGHT_SOURCE_DIAMETER.getKey(index) + " key");
        }
      } else {
        lightSourceDiameters = null;
      }
      
      lightSources = new LightSource [lightSourceX.length];
      for (int i = 0; i < lightSources.length; i++) {
        int color = lightSourceColors [i].startsWith("#")
            ? Integer.parseInt(lightSourceColors [i].substring(1), 16)
            : Integer.parseInt(lightSourceColors [i]);
        // Create the matching light source, converting cm to percentage of width, depth and height
        lightSources [i] = new LightSource(Float.parseFloat(lightSourceX [i]) / lightWidth, 
            Float.parseFloat(lightSourceY [i]) / lightDepth, 
            Float.parseFloat(lightSourceZ [i]) / lightHeight, 
            color,
            lightSourceDiameters != null
                ? Float.parseFloat(lightSourceDiameters [i]) / lightWidth
                : null);
      }
    }     
    return lightSources;
  }

  /**
   * Returns the value of <code>propertyKey</code> in <code>resource</code>, 
   * or <code>defaultValue</code> if the property doesn't exist.
   */
  private String getOptionalString(ResourceBundle resource, 
                                   String propertyKey,
                                   String defaultValue) {
    try {
      return resource.getString(propertyKey);
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }

  /**
   * Returns the value of <code>propertyKey</code> in <code>resource</code>, 
   * or <code>defaultValue</code> if the property doesn't exist.
   */
  private float getOptionalFloat(ResourceBundle resource, 
                                 String propertyKey,
                                 float defaultValue) {
    try {
      return Float.parseFloat(resource.getString(propertyKey));
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }

  /**
   * Returns the boolean value of <code>propertyKey</code> in <code>resource</code>, 
   * or <code>defaultValue</code> if the property doesn't exist.
   */
  private boolean getOptionalBoolean(ResourceBundle resource, 
                                     String propertyKey,
                                     boolean defaultValue) {
    try {
      return Boolean.parseBoolean(resource.getString(propertyKey));
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }
}

